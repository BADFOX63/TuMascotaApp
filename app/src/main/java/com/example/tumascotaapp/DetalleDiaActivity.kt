package com.example.tumascotaapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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

class DetalleDiaActivity : ComponentActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fecha = intent.getStringExtra("fecha") ?: return

        setContent {
            DetalleDiaScreen(fecha = fecha)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DetalleDiaScreen(fecha: String) {
        val uid = auth.currentUser?.uid ?: ""
        val fechaRef = firestore.collection("calendarios").document(uid).collection("fechas").document(fecha)

        var estado by remember { mutableStateOf("activo") }
        var horasDesactivadas by remember { mutableStateOf<List<String>>(emptyList()) }
        var mensaje by remember { mutableStateOf("Cargando...") }

        // Cargar datos
        LaunchedEffect(fecha) {
            fechaRef.get().addOnSuccessListener { doc ->
                estado = doc.getString("estado") ?: "activo"
                horasDesactivadas = doc.get("horasDesactivadas") as? List<String> ?: emptyList()
                mensaje = ""
            }.addOnFailureListener {
                mensaje = "Error cargando información"
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Detalle de Día") })
            },
            content = { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text("📅 Fecha: $fecha", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Estado actual: ${estado.uppercase()}")
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = {
                        val nuevoEstado = if (estado == "activo") "inactivo" else "activo"
                        fechaRef.update("estado", nuevoEstado)
                            .addOnSuccessListener {
                                estado = nuevoEstado
                                Toast.makeText(this@DetalleDiaActivity, "Estado actualizado", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@DetalleDiaActivity, "Error al actualizar estado", Toast.LENGTH_SHORT).show()
                            }
                    }) {
                        Text(if (estado == "activo") "Inactivar Día" else "Activar Día")
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("Horas desactivadas:")
                    if (horasDesactivadas.isEmpty()) {
                        Text("Ninguna hora desactivada")
                    } else {
                        LazyColumn {
                            items(horasDesactivadas) { hora ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    Text(hora, modifier = Modifier.weight(1f))
                                    Button(onClick = {
                                        val nuevasHoras = horasDesactivadas.toMutableList().apply { remove(hora) }
                                        fechaRef.update("horasDesactivadas", nuevasHoras)
                                            .addOnSuccessListener {
                                                horasDesactivadas = nuevasHoras
                                                Toast.makeText(this@DetalleDiaActivity, "Hora reactivada", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(this@DetalleDiaActivity, "Error al reactivar hora", Toast.LENGTH_SHORT).show()
                                            }
                                    }) {
                                        Text("Activar")
                                    }
                                }
                            }
                        }
                    }

                    if (mensaje.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(mensaje, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        )
    }
}
