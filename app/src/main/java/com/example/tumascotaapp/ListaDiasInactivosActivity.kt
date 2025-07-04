package com.example.tumascotaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.platform.LocalContext


data class DiaInactivo(
    val fecha: String,
    val estado: String,
    val horasDesactivadas: List<String>
)

class ListaDiasInactivosActivity : ComponentActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ListaDiasInactivosScreen()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ListaDiasInactivosScreen() {
        val uid = auth.currentUser?.uid ?: ""
        val calendarioRef = firestore.collection("calendarios").document(uid).collection("fechas")

        var listaDias by remember { mutableStateOf<List<DiaInactivo>>(emptyList()) }
        var mensaje by remember { mutableStateOf("Cargando...") }

        val context = LocalContext.current

        fun cargarDias() {
            calendarioRef.get()
                .addOnSuccessListener { snapshot ->
                    val diasFiltrados = snapshot.documents.mapNotNull { doc ->
                        val estado = doc.getString("estado") ?: "activo"
                        val horas = doc.get("horasDesactivadas") as? List<String> ?: emptyList()

                        if (estado == "inactivo" || horas.isNotEmpty()) {
                            DiaInactivo(
                                fecha = doc.id,
                                estado = estado,
                                horasDesactivadas = horas
                            )
                        } else {
                            null
                        }
                    }

                    if (diasFiltrados.isEmpty()) {
                        mensaje = "No hay días inactivos ni horas desactivadas."
                        listaDias = emptyList()
                    } else {
                        listaDias = diasFiltrados
                        mensaje = ""
                    }
                }
                .addOnFailureListener {
                    mensaje = "Error cargando días inactivos."
                }
        }

        // 🚀 Llama a la función cada vez que entra en foco
        LaunchedEffect(Unit) {
            cargarDias()
        }

        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Días Inactivos y Horas") })
            },
            content = { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp)
                ) {
                    if (listaDias.isEmpty()) {
                        Text(mensaje)
                    } else {
                        LazyColumn {
                            items(listaDias) { dia ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .clickable {
                                            val intent = android.content.Intent(
                                                context,
                                                DetalleDiaActivity::class.java
                                            )
                                            intent.putExtra("fecha", dia.fecha)
                                            context.startActivity(intent)
                                        },
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("📅 Fecha: ${dia.fecha}")
                                        Text("Estado: ${if (dia.estado == "activo") "Activo 🟢" else "Inactivo 🔴"}")
                                        Text("Horas desactivadas: ${if (dia.horasDesactivadas.isEmpty()) "Ninguna" else dia.horasDesactivadas.joinToString(", ")}")
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
