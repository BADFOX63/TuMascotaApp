package com.example.tumascotaapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.tumascotaapp.model.Servicio
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class AgendaCitaActivity : ComponentActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val dateFormatter = DateTimeFormatter.ISO_DATE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AgendaCitaScreen()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AgendaCitaScreen() {
        val context = LocalContext.current
        val uid = auth.currentUser?.uid

        val listaMascotas = remember { mutableStateListOf<String>() }
        var mascotaSeleccionada by remember { mutableStateOf("") }

        var fechaSeleccionada by remember { mutableStateOf<LocalDate?>(null) }
        var estadoFecha by remember { mutableStateOf<String?>(null) }
        var mensajeEstado by remember { mutableStateOf("") }
        var horasNoDisponibles by remember { mutableStateOf<List<String>>(emptyList()) }

        val listaTodasLasHoras = listOf("08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00")

        var horaSeleccionada by remember { mutableStateOf("") }
        var horaDropdownExpanded by remember { mutableStateOf(false) }

        var motivo by remember { mutableStateOf("") }

        val today = remember { LocalDate.now() }
        val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }

        val veterinarioId = "IFoa3Jij2fYkuYVyFiGn18QBYRZ2"

        val listaServicios = remember { mutableStateListOf<Servicio>() }
        var servicioSeleccionado by remember { mutableStateOf<Servicio?>(null) }
        var servicioDropdownExpanded by remember { mutableStateOf(false) }

        fun cargarDisponibilidad(fecha: LocalDate) {
            val fechaKey = fecha.format(dateFormatter)
            mensajeEstado = "Verificando disponibilidad..."
            estadoFecha = null
            horasNoDisponibles = emptyList()
            horaSeleccionada = ""

            firestore.collection("calendarios").document(veterinarioId)
                .collection("fechas").document(fechaKey)
                .get()
                .addOnSuccessListener { doc ->
                    val estado = doc.getString("estado") ?: "activo"
                    val horasDesactivadas = doc.get("horasDesactivadas") as? List<String> ?: emptyList()

                    estadoFecha = estado
                    horasNoDisponibles = horasDesactivadas

                    mensajeEstado = when {
                        estado == "inactivo" -> "⚠️ Fecha no disponible"
                        horasDesactivadas.size == listaTodasLasHoras.size -> "⚠️ Todas las horas ocupadas"
                        else -> "✅ Fecha disponible"
                    }
                }
                .addOnFailureListener {
                    mensajeEstado = "⚠️ Error al verificar disponibilidad"
                }
        }

        LaunchedEffect(uid) {
            uid?.let {
                firestore.collection("usuarios").document(it).collection("mascotas")
                    .get()
                    .addOnSuccessListener { result ->
                        listaMascotas.clear()
                        listaMascotas.addAll(result.documents.mapNotNull { it.getString("nombre") })
                        if (listaMascotas.isNotEmpty()) mascotaSeleccionada = listaMascotas[0]
                    }
            }

            firestore.collection("veterinarios").document(veterinarioId)
                .collection("servicios").get()
                .addOnSuccessListener { result ->
                    val servicios = result.documents.mapNotNull { doc ->
                        val nombre = doc.getString("nombre_servicio") ?: return@mapNotNull null
                        val descripcion = doc.getString("descripcion") ?: ""
                        val costo = doc.getDouble("costo") ?: 0.0
                        Servicio(doc.id, nombre, descripcion, costo)
                    }
                    listaServicios.clear()
                    listaServicios.addAll(servicios)
                    if (servicios.isNotEmpty()) servicioSeleccionado = servicios[0]
                }
        }

        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Agendar Cita") })
            },
            content = { padding ->
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    var mascotaDropdownExpanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = mascotaDropdownExpanded,
                        onExpandedChange = { mascotaDropdownExpanded = !mascotaDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = mascotaSeleccionada,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Selecciona tu Mascota") },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = mascotaDropdownExpanded,
                            onDismissRequest = { mascotaDropdownExpanded = false }
                        ) {
                            listaMascotas.forEach { mascota ->
                                DropdownMenuItem(
                                    text = { Text(mascota) },
                                    onClick = {
                                        mascotaSeleccionada = mascota
                                        mascotaDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    ExposedDropdownMenuBox(
                        expanded = servicioDropdownExpanded,
                        onExpandedChange = { servicioDropdownExpanded = !servicioDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = servicioSeleccionado?.nombre ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Selecciona un Servicio") },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = servicioDropdownExpanded,
                            onDismissRequest = { servicioDropdownExpanded = false }
                        ) {
                            listaServicios.forEach { servicio ->
                                DropdownMenuItem(
                                    text = { Text("${servicio.nombre} - $${String.format("%,.0f", servicio.costo)}") },
                                    onClick = {
                                        servicioSeleccionado = servicio
                                        servicioDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (servicioSeleccionado != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "💡 Precio base: $${String.format("%,.0f", servicioSeleccionado!!.costo)} COP (puede variar según características de la mascota)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = motivo,
                        onValueChange = { motivo = it },
                        label = { Text("Motivo de la Cita") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = {
                        val calendar = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val fecha = LocalDate.of(year, month + 1, dayOfMonth)
                                if (fecha.isBefore(today)) {
                                    Toast.makeText(context, "No puedes agendar en días pasados", Toast.LENGTH_SHORT).show()
                                } else {
                                    fechaSeleccionada = fecha
                                    cargarDisponibilidad(fecha)
                                }
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }) {
                        Text("Seleccionar Fecha")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Fecha: ${fechaSeleccionada?.format(dateFormatter) ?: "Ninguna"}")
                    Text(mensajeEstado)

                    Spacer(modifier = Modifier.height(16.dp))

                    if (fechaSeleccionada != null && estadoFecha == "activo") {
                        val horasFiltradas = listaTodasLasHoras.filterNot { horasNoDisponibles.contains(it) }.filter { hora ->
                            if (fechaSeleccionada == today) {
                                val horaInt = hora.substring(0, 2).toInt()
                                horaInt > currentHour
                            } else true
                        }

                        if (horasFiltradas.isNotEmpty()) {
                            ExposedDropdownMenuBox(
                                expanded = horaDropdownExpanded,
                                onExpandedChange = { horaDropdownExpanded = !horaDropdownExpanded }
                            ) {
                                OutlinedTextField(
                                    value = horaSeleccionada,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Selecciona la Hora") },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )

                                ExposedDropdownMenu(
                                    expanded = horaDropdownExpanded,
                                    onDismissRequest = { horaDropdownExpanded = false }
                                ) {
                                    horasFiltradas.forEach { hora ->
                                        DropdownMenuItem(
                                            text = { Text(hora) },
                                            onClick = {
                                                horaSeleccionada = hora
                                                horaDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        } else {
                            Text("No hay horas disponibles", color = MaterialTheme.colorScheme.error)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (mascotaSeleccionada.isNotEmpty() && fechaSeleccionada != null && horaSeleccionada.isNotEmpty() && motivo.isNotEmpty() && servicioSeleccionado != null) {
                                if (estadoFecha == "activo") {
                                    guardarCita(uid, mascotaSeleccionada, fechaSeleccionada!!, horaSeleccionada, motivo, servicioSeleccionado!!) {
                                        cargarDisponibilidad(fechaSeleccionada!!)
                                    }
                                } else {
                                    Toast.makeText(context, "La fecha seleccionada no está disponible", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = (estadoFecha == "activo" && horaSeleccionada.isNotEmpty() && motivo.isNotEmpty() && servicioSeleccionado != null)
                    ) {
                        Text("Agendar Cita")
                    }
                }
            }
        )
    }

    private fun guardarCita(uid: String?, mascota: String, fecha: LocalDate, hora: String, motivo: String, servicio: Servicio, onSuccess: () -> Unit) {
        if (uid == null) return

        val fechaKey = fecha.format(dateFormatter)
        val veterinarioId = "IFoa3Jij2fYkuYVyFiGn18QBYRZ2"

        firestore.collection("usuarios").document(uid).get()
            .addOnSuccessListener { userSnapshot ->
                val nombreUsuario = userSnapshot.getString("nombre") ?: "Sin nombre"
                val telefonoUsuario = userSnapshot.getString("telefono") ?: "Sin teléfono"
                val emailUsuario = userSnapshot.getString("email") ?: auth.currentUser?.email ?: "Sin correo"


                val cita = hashMapOf(
                    "mascota" to mascota,
                    "fecha" to fechaKey,
                    "hora" to hora,
                    "motivo" to motivo,
                    "servicio" to servicio.nombre,
                    "precioBase" to servicio.costo,
                    "duenioNombre" to nombreUsuario,
                    "duenioTelefono" to telefonoUsuario,
                    "usuarioEmail" to emailUsuario,
                    "usuarioId" to uid
                )

                firestore.collection("usuarios").document(uid)
                    .collection("citas").add(cita)

                firestore.collection("veterinarios").document(veterinarioId)
                    .collection("citas").add(cita)
                    .addOnSuccessListener {
                        val fechaRef = firestore.collection("calendarios")
                            .document(veterinarioId)
                            .collection("fechas")
                            .document(fechaKey)

                        firestore.runTransaction { transaction ->
                            val snapshot = transaction.get(fechaRef)
                            val horasActuales = snapshot.get("horasDesactivadas") as? List<String> ?: emptyList()
                            val nuevasHoras = horasActuales.toMutableSet().apply { add(hora) }.toList()
                            transaction.set(fechaRef, mapOf("estado" to "activo", "horasDesactivadas" to nuevasHoras))
                        }.addOnSuccessListener {
                            Toast.makeText(this, "Cita guardada y hora bloqueada", Toast.LENGTH_SHORT).show()
                            onSuccess()
                        }.addOnFailureListener {
                            Toast.makeText(this, "Error al bloquear hora", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al guardar cita", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error obteniendo datos de usuario", Toast.LENGTH_SHORT).show()
            }
    }
}