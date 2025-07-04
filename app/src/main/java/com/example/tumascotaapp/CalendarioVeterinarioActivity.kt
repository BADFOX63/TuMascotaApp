package com.example.tumascotaapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
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

class CalendarioVeterinarioActivity : ComponentActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalendarioVeterinarioScreen()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CalendarioVeterinarioScreen() {
        val context = LocalContext.current
        var fechaSeleccionada by remember { mutableStateOf(LocalDate.now()) }
        var diaInactivo by remember { mutableStateOf(false) }

        // Horas de 8am a 6pm (18h)
        val horasDelDia = listOf(
            "08:00", "09:00", "10:00", "11:00", "12:00",
            "13:00", "14:00", "15:00", "16:00", "17:00", "18:00"
        )

        val horasDesactivadas = remember { mutableStateListOf<String>() }

        val dateFormatter = DateTimeFormatter.ISO_DATE

        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Configurar Horario") })
            },
            content = { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
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
                        Text("Seleccionar Fecha")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Fecha: ${fechaSeleccionada.format(dateFormatter)}")

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Día Inactivo: ")
                        Switch(checked = diaInactivo, onCheckedChange = { diaInactivo = it })
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!diaInactivo) {
                        Text("Desactiva las horas que no trabajarás:")

                        Spacer(modifier = Modifier.height(8.dp))

                        horasDelDia.forEach { hora ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .toggleable(
                                        value = horasDesactivadas.contains(hora),
                                        onValueChange = { selected ->
                                            if (selected) {
                                                horasDesactivadas.add(hora)
                                            } else {
                                                horasDesactivadas.remove(hora)
                                            }
                                        }
                                    )
                            ) {
                                Checkbox(
                                    checked = horasDesactivadas.contains(hora),
                                    onCheckedChange = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("$hora hrs")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            guardarConfiguracion(fechaSeleccionada, diaInactivo, horasDesactivadas)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Guardar Configuración")
                    }
                }
            }
        )
    }

    private fun guardarConfiguracion(
        fecha: LocalDate,
        diaInactivo: Boolean,
        horasDesactivadas: List<String>
    ) {
        val uid = auth.currentUser?.uid ?: return
        val fechaKey = fecha.format(DateTimeFormatter.ISO_DATE)

        val datos = hashMapOf<String, Any>(
            "estado" to if (diaInactivo) "inactivo" else "activo",
            "horasDesactivadas" to if (!diaInactivo) horasDesactivadas else emptyList<String>()
        )

        firestore.collection("calendarios").document(uid)
            .collection("fechas").document(fechaKey)
            .set(datos)
            .addOnSuccessListener {
                Toast.makeText(this, "Configuración guardada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
            }
    }
}
