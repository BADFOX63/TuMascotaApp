package com.example.tumascotaapp

import android.os.Bundle
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

data class HistorialMascota(
    val id: String = "",
    val servicio: String = "",
    val diagnostico: String = "",
    val tratamiento: String = "",
    val observaciones: String = "",
    val precioFinal: Double = 0.0,
    val fechaCita: String = "",
    val horaCita: String = ""
)

class ListaHistorialesMascotaActivity : ComponentActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mascotaNombre = intent.getStringExtra("mascotaNombre") ?: ""

        setContent {
            HistorialesMascotaScreen(mascotaNombre)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun HistorialesMascotaScreen(mascotaNombre: String) {
        val uid = auth.currentUser?.uid ?: return

        val historiales = remember { mutableStateListOf<HistorialMascota>() }

        LaunchedEffect(uid, mascotaNombre) {
            db.collection("usuarios").document(uid)
                .collection("mascotas").document(mascotaNombre)
                .collection("historiales")
                .get()
                .addOnSuccessListener { result ->
                    val lista = result.documents.map { doc ->
                        HistorialMascota(
                            id = doc.id,
                            servicio = doc.getString("servicio") ?: "",
                            diagnostico = doc.getString("diagnostico") ?: "",
                            tratamiento = doc.getString("tratamiento") ?: "",
                            observaciones = doc.getString("observaciones") ?: "",
                            precioFinal = doc.getDouble("precioFinal") ?: 0.0,
                            fechaCita = doc.getString("fechaCita") ?: "",
                            horaCita = doc.getString("horaCita") ?: ""
                        )
                    }
                    historiales.clear()
                    historiales.addAll(lista)
                }
        }

        Scaffold(
            topBar = { TopAppBar(title = { Text("Historial: $mascotaNombre") }) },
            content = { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    if (historiales.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Esta mascota no tiene historial médico.")
                        }
                    } else {
                        LazyColumn {
                            items(historiales) { historial ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("🔧 Servicio: ${historial.servicio}", style = MaterialTheme.typography.titleMedium)
                                        Text("📅 Fecha: ${historial.fechaCita} ⏰ ${historial.horaCita}")
                                        Text("🩺 Diagnóstico: ${historial.diagnostico}")
                                        Text("💊 Tratamiento: ${historial.tratamiento}")
                                        if (historial.observaciones.isNotBlank()) {
                                            Text("📝 Observaciones: ${historial.observaciones}")
                                        }
                                        Text("💰 Precio Final: $${String.format("%,.0f", historial.precioFinal)} COP")
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
