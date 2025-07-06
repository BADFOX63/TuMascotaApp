package com.example.tumascotaapp

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


data class Cita(
    val id: String = "",
    val mascota: String = "",
    val fecha: String = "",
    val hora: String = "",
    val motivo: String = "",
    val servicio: String = "",
    val precioBase: Double = 0.0,
    val duenioNombre: String = "",
    val duenioTelefono: String = "",
    val usuarioId: String = "",
    val usuarioEmail: String = ""
)

class ListaCitasActivity : ComponentActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ListaCitasScreen()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ListaCitasScreen() {
        val context = this
        val veterinarioId = auth.currentUser?.uid

        val listaCitas = remember { mutableStateListOf<Cita>() }
        var fechaSeleccionada by remember { mutableStateOf(LocalDate.now()) }
        val dateFormatter = DateTimeFormatter.ISO_DATE
        val fechaFormateada = fechaSeleccionada.format(dateFormatter)

        //Cargar citas
        LaunchedEffect(veterinarioId) {
            veterinarioId?.let {
                firestore.collection("veterinarios").document(it).collection("citas")
                    .get()
                    .addOnSuccessListener { result ->
                        val citas = result.documents.map { doc ->
                            Cita(
                                id = doc.id,
                                mascota = doc.getString("mascota") ?: "",
                                fecha = doc.getString("fecha") ?: "",
                                hora = doc.getString("hora") ?: "",
                                motivo = doc.getString("motivo") ?: "",
                                servicio = doc.getString("servicio") ?: "No especificado",
                                precioBase = doc.getDouble("precioBase") ?: 0.0,
                                duenioNombre = doc.getString("duenioNombre") ?: "",
                                duenioTelefono = doc.getString("duenioTelefono") ?: "",
                                usuarioId = doc.getString("usuarioId") ?: "",
                                usuarioEmail = doc.getString("usuarioEmail") ?: ""
                            )
                        }
                        listaCitas.clear()
                        listaCitas.addAll(citas)
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error cargando citas", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        Scaffold(
            topBar = { TopAppBar(title = { Text("Citas Agendadas") }) },
            content = { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    Button(onClick = {
                        val calendar = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                fechaSeleccionada = LocalDate.of(year, month + 1, dayOfMonth)
                            },
                            fechaSeleccionada.year,
                            fechaSeleccionada.monthValue - 1,
                            fechaSeleccionada.dayOfMonth
                        ).show()
                    }) {
                        Text("Ver citas de: $fechaFormateada")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val citasFiltradas = listaCitas.filter { it.fecha == fechaFormateada }

                    if (citasFiltradas.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No hay citas para $fechaFormateada.")
                        }
                    } else {
                        LazyColumn {
                            items(citasFiltradas) { cita ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .clickable {
                                            val intent = Intent(context, DetalleCitaActivity::class.java).apply {
                                                putExtra("citaId", cita.id)
                                                putExtra("mascota", cita.mascota)
                                                putExtra("fecha", cita.fecha)
                                                putExtra("hora", cita.hora)
                                                putExtra("motivo", cita.motivo)
                                                putExtra("servicio", cita.servicio)
                                                putExtra("precioBase", cita.precioBase)
                                                putExtra("duenioNombre", cita.duenioNombre)
                                                putExtra("duenioTelefono", cita.duenioTelefono)
                                                putExtra("usuarioId", cita.usuarioId)
                                                putExtra("usuarioEmail", cita.usuarioEmail)
                                            }
                                            context.startActivity(intent)
                                        },
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("🐾 Mascota: ${cita.mascota}", style = MaterialTheme.typography.titleMedium)
                                        Text("📅 Fecha: ${cita.fecha}")
                                        Text("⏰ Hora: ${cita.hora}")
                                        Text("🔧 Servicio: ${cita.servicio}")
                                        Text("💰 Precio: $${String.format("%,.0f", cita.precioBase)} COP")
                                        Text("👤 Dueño: ${cita.duenioNombre}")
                                        Text("📞 Teléfono: ${cita.duenioTelefono}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}
