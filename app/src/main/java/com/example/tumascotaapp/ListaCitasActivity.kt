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
    val duenioNombre: String = "",
    val duenioTelefono: String = ""
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

        // Cargar todas las citas una sola vez
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
                                motivo = doc.getString("motivo") ?:"",
                                duenioNombre = doc.getString("duenioNombre") ?: "",
                                duenioTelefono = doc.getString("duenioTelefono") ?: ""
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
            topBar = {
                TopAppBar(title = { Text("Citas Agendadas") })
            },
            content = { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                ) {

                    // Botón para elegir la fecha
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
                        Text("Ver citas de: ${fechaFormateada}")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val citasFiltradas = listaCitas.filter { it.fecha == fechaFormateada }

                    if (citasFiltradas.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No hay citas para ${fechaFormateada}.")
                        }
                    } else {
                        LazyColumn {
                            items(citasFiltradas) { cita ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .clickable {
                                            val intent = Intent(context, DetalleCitaActivity::class.java)
                                            intent.putExtra("citaId", cita.id)
                                            intent.putExtra("mascota", cita.mascota)
                                            intent.putExtra("fecha", cita.fecha)
                                            intent.putExtra("hora", cita.hora)
                                            intent.putExtra("motivo", cita.motivo)
                                            intent.putExtra("duenioNombre", cita.duenioNombre)
                                            intent.putExtra("duenioTelefono", cita.duenioTelefono)
                                            startActivity(intent)
                                        },
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("🐾 Mascota: ${cita.mascota}", style = MaterialTheme.typography.titleMedium)
                                        Text("📅 Fecha: ${cita.fecha}")
                                        Text("⏰ Hora: ${cita.hora}")
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
