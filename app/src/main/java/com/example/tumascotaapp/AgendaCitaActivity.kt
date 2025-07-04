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

        val listaTodasLasHoras = listOf(
            "08:00", "09:00", "10:00", "11:00", "12:00",
            "13:00", "14:00", "15:00", "16:00", "17:00", "18:00"
        )

        var horaSeleccionada by remember { mutableStateOf("") }
        var horaDropdownExpanded by remember { mutableStateOf(false) }

        val veterinarioId = "IFoa3Jij2fYkuYVyFiGn18QBYRZ2"

        fun cargarDisponibilidad(fecha: LocalDate) {
            val fechaKey = fecha.format(dateFormatter)
            mensajeEstado = "Verificando disponibilidad..."
            estadoFecha = null
            horasNoDisponibles = emptyList()
            horaSeleccionada = ""

            firestore.collection("calendarios")
                .document(veterinarioId)
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
                        if (listaMascotas.isNotEmpty()) {
                            mascotaSeleccionada = listaMascotas[0]
                        }
                    }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Agendar Cita") })
            },
            content = { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
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

                    Button(onClick = {
                        val calendar = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val fecha = LocalDate.of(year, month + 1, dayOfMonth)
                                fechaSeleccionada = fecha
                                cargarDisponibilidad(fecha)
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
                        val horasFiltradas = listaTodasLasHoras.filterNot { horasNoDisponibles.contains(it) }

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
                            if (mascotaSeleccionada.isNotEmpty() && fechaSeleccionada != null && horaSeleccionada.isNotEmpty()) {
                                if (estadoFecha == "activo") {
                                    guardarCita(uid, mascotaSeleccionada, fechaSeleccionada!!, horaSeleccionada) {
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
                        enabled = (estadoFecha == "activo" && horaSeleccionada.isNotEmpty())
                    ) {
                        Text("Agendar Cita")
                    }
                }
            }
        )
    }

    private fun guardarCita(uid: String?, mascota: String, fecha: LocalDate, hora: String, onSuccess: () -> Unit) {
        if (uid == null) return

        val cita = hashMapOf(
            "mascota" to mascota,
            "fecha" to fecha.format(dateFormatter),
            "hora" to hora
        )

        val fechaKey = fecha.format(dateFormatter)
        val veterinarioId = "IFoa3Jij2fYkuYVyFiGn18QBYRZ2"

        firestore.collection("usuarios").document(uid)
            .collection("citas").add(cita)
            .addOnSuccessListener {
                val fechaRef = firestore.collection("calendarios")
                    .document(veterinarioId)
                    .collection("fechas")
                    .document(fechaKey)

                firestore.runTransaction { transaction ->
                    val snapshot = transaction.get(fechaRef)
                    if (!snapshot.exists()) {
                        val nuevaFecha = hashMapOf(
                            "estado" to "activo",
                            "horasDesactivadas" to listOf(hora)
                        )
                        transaction.set(fechaRef, nuevaFecha)
                    } else {
                        val horasActuales = snapshot.get("horasDesactivadas") as? List<String> ?: emptyList()
                        val nuevasHoras = horasActuales.toMutableSet().apply { add(hora) }.toList()
                        transaction.update(fechaRef, "horasDesactivadas", nuevasHoras)
                    }
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
}
