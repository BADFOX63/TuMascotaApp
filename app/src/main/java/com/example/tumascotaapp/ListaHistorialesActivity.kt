package com.example.tumascotaapp

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

data class HistorialClinico(
    val id: String = "",
    val mascotaNombre: String = "",
    val duenioNombre: String = "",
    val usuarioEmail: String = "",
    val duenioTelefono: String = "",
    val servicio: String = "",
    val diagnostico: String = "",
    val tratamiento: String = "",
    val precioFinal: Double = 0.0,
    val usuarioId: String = "",
    val timestamp: Long = 0L,
    val fechaCita: String = "",
    val horaCita: String = ""
)

class ListaHistorialesActivity : ComponentActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ListaHistorialesScreen()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ListaHistorialesScreen() {
        val veterinarioId = auth.currentUser?.uid ?: return

        val historiales = remember { mutableStateListOf<HistorialClinico>() }
        var filtro by remember { mutableStateOf("") }

        var historialSeleccionado by remember { mutableStateOf<HistorialClinico?>(null) }
        var showEliminarConfirmacion by remember { mutableStateOf(false) }

        fun cargarHistoriales() {
            firestore.collection("veterinarios").document(veterinarioId)
                .collection("historiales")
                .get()
                .addOnSuccessListener { result ->
                    val lista = result.documents.mapNotNull { doc ->
                        HistorialClinico(
                            id = doc.id,
                            mascotaNombre = doc.getString("mascotaNombre") ?: "",
                            duenioNombre = doc.getString("duenioNombre") ?: "",
                            usuarioEmail = doc.getString("usuarioEmail") ?: "",
                            duenioTelefono = doc.getString("duenioTelefono") ?: "",
                            servicio = doc.getString("servicio") ?: "",
                            diagnostico = doc.getString("diagnostico") ?: "",
                            tratamiento = doc.getString("tratamiento") ?: "",
                            precioFinal = doc.getDouble("precioFinal") ?: 0.0,
                            usuarioId = doc.getString("usuarioId") ?: "",
                            timestamp = doc.getLong("timestamp") ?: 0L,
                            fechaCita = doc.getString("fechaCita") ?: "",
                            horaCita = doc.getString("horaCita") ?: ""
                        )
                    }
                    historiales.clear()
                    historiales.addAll(lista)
                }
        }

        LaunchedEffect(Unit) {
            cargarHistoriales()
        }

        val historialesFiltrados = historiales.filter {
            it.mascotaNombre.contains(filtro, ignoreCase = true) ||
                    it.duenioNombre.contains(filtro, ignoreCase = true) ||
                    it.usuarioEmail.contains(filtro, ignoreCase = true)
        }

        Scaffold(
            topBar = { TopAppBar(title = { Text("Historiales Clínicos") }) },
            content = { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = filtro,
                        onValueChange = { filtro = it },
                        label = { Text("Buscar por mascota, dueño o correo") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (historialesFiltrados.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No se encontraron historiales.")
                        }
                    } else {
                        LazyColumn {
                            items(historialesFiltrados) { historial ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .clickable { historialSeleccionado = historial },
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("🐾 Mascota: ${historial.mascotaNombre}", style = MaterialTheme.typography.titleMedium)
                                        Text("👤 Dueño: ${historial.duenioNombre}")
                                        Text("📧 Correo: ${historial.usuarioEmail}")
                                        Text("📞 Teléfono: ${historial.duenioTelefono}")
                                        Text("📅 Fecha: ${historial.fechaCita} ⏰ ${historial.horaCita}")
                                        Text("💰 Precio Final: $${String.format("%,.0f", historial.precioFinal)} COP")
                                    }
                                }
                            }
                        }
                    }

                    historialSeleccionado?.let { historial ->
                        AlertDialog(
                            onDismissRequest = { historialSeleccionado = null },
                            title = { Text("Detalle del Historial") },
                            text = {
                                Column {
                                    Text("🐾 Mascota: ${historial.mascotaNombre}")
                                    Text("👤 Dueño: ${historial.duenioNombre}")
                                    Text("📧 Correo: ${historial.usuarioEmail}")
                                    Text("📞 Teléfono: ${historial.duenioTelefono}")
                                    Text("🔧 Servicio: ${historial.servicio}")
                                    Text("📅 Fecha: ${historial.fechaCita} ⏰ ${historial.horaCita}")
                                    Text("🩺 Diagnóstico: ${historial.diagnostico}")
                                    Text("💊 Tratamiento: ${historial.tratamiento}")
                                    Text("💰 Precio Final: $${String.format("%,.0f", historial.precioFinal)} COP")
                                }
                            },
                            confirmButton = {
                                Row {
                                    TextButton(onClick = { historialSeleccionado = null }) {
                                        Text("Cerrar")
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    TextButton(onClick = {
                                        showEliminarConfirmacion = true
                                    }) {
                                        Text("Eliminar", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        )
                    }

                    if (showEliminarConfirmacion && historialSeleccionado != null) {
                        AlertDialog(
                            onDismissRequest = { showEliminarConfirmacion = false },
                            title = { Text("Eliminar Historial") },
                            text = { Text("¿Deseas eliminar este historial clínico? Esta acción no se puede deshacer.") },
                            confirmButton = {
                                TextButton(onClick = {
                                    eliminarHistorial(historialSeleccionado!!) {
                                        historiales.remove(historialSeleccionado)
                                        historialSeleccionado = null
                                        showEliminarConfirmacion = false
                                        Toast.makeText(this@ListaHistorialesActivity, "Historial eliminado", Toast.LENGTH_SHORT).show()
                                    }
                                }) {
                                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showEliminarConfirmacion = false }) {
                                    Text("Cancelar")
                                }
                            }
                        )
                    }
                }
            }
        )
    }

    private fun eliminarHistorial(historial: HistorialClinico, onDeleted: () -> Unit) {
        val veterinarioId = auth.currentUser?.uid ?: return

        firestore.collection("veterinarios").document(veterinarioId)
            .collection("historiales").document(historial.id)
            .delete()

        firestore.collection("usuarios").document(historial.usuarioId)
            .collection("mascotas").document(historial.mascotaNombre)
            .collection("historiales")
            .whereEqualTo("timestamp", historial.timestamp)
            .get()
            .addOnSuccessListener { snapshots ->
                snapshots.documents.forEach { it.reference.delete() }
                onDeleted()
            }
    }
}
