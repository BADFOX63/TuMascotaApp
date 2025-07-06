package com.example.tumascotaapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.tumascotaapp.model.Servicio
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ListaServiciosActivity : ComponentActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private lateinit var editarServicioLauncher: androidx.activity.result.ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        editarServicioLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Si el servicio fue editado o eliminado, recargar
                cargarServicios()
            }
        }

        setContent {
            ListaServiciosScreen()
        }
    }

    private var listaServiciosState = mutableStateListOf<Servicio>()
    private var mensajeState = mutableStateOf("Cargando...")

    private fun cargarServicios() {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("veterinarios").document(uid)
            .collection("servicios")
            .get()
            .addOnSuccessListener { snapshot ->
                val servicios = snapshot.documents.mapNotNull { doc ->
                    val nombre = doc.getString("nombre_servicio") ?: return@mapNotNull null
                    val descripcion = doc.getString("descripcion") ?: ""
                    val costo = doc.getDouble("costo") ?: 0.0
                    Servicio(doc.id, nombre, descripcion, costo)
                }
                listaServiciosState.clear()
                listaServiciosState.addAll(servicios)
                mensajeState.value = if (servicios.isEmpty()) "No hay servicios registrados." else ""
            }
            .addOnFailureListener {
                mensajeState.value = "Error al cargar servicios."
            }
    }

    override fun onResume() {
        super.onResume()
        cargarServicios()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ListaServiciosScreen() {
        val context = LocalContext.current

        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Mis Servicios") })
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        val intent = Intent(context, AgregarServicioActivity::class.java)
                        context.startActivity(intent)
                    }
                ) {
                    Text("+")
                }
            },
            content = { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    if (listaServiciosState.isEmpty()) {
                        Text(mensajeState.value)
                    } else {
                        LazyColumn {
                            items(listaServiciosState) { servicio ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .clickable {
                                            val intent = Intent(context, EditarServicioActivity::class.java).apply {
                                                putExtra("idServicio", servicio.id)
                                                putExtra("nombre", servicio.nombre)
                                                putExtra("descripcion", servicio.descripcion)
                                                putExtra("costo", servicio.costo)
                                            }
                                            editarServicioLauncher.launch(intent)
                                        },
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("🔹 ${servicio.nombre}", style = MaterialTheme.typography.titleMedium)
                                        Text("💬 ${servicio.descripcion}")
                                        Text("💰 ${String.format("%,.0f", servicio.costo)} COP")
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
