package com.example.tumascotaapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.tumascotaapp.model.Servicio

class AgregarServicioActivity : ComponentActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AgregarServicioScreen()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AgregarServicioScreen() {
        val context = LocalContext.current
        val uid = auth.currentUser?.uid ?: return

        var nombre by remember { mutableStateOf("") }
        var descripcion by remember { mutableStateOf("") }
        var precioTexto by remember { mutableStateOf("") }

        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Agregar Servicio") })
            },
            content = { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Top
                ) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre del Servicio") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Descripción del Servicio") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = precioTexto,
                        onValueChange = { precioTexto = it },
                        label = { Text("Precio Base (COP)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val precio = precioTexto.toDoubleOrNull()

                            if (nombre.isNotBlank() && descripcion.isNotBlank() && precio != null) {
                                val servicio = hashMapOf(
                                    "nombre_servicio" to nombre,
                                    "descripcion" to descripcion,
                                    "costo" to precio
                                )

                                firestore.collection("veterinarios").document(uid)
                                    .collection("servicios")
                                    .add(servicio)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Servicio agregado correctamente", Toast.LENGTH_SHORT).show()
                                        finish()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Error al guardar el servicio", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                Toast.makeText(context, "Completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Guardar Servicio")
                    }
                }
            }
        )
    }
}

class AgregarListaServiciosActivity : ComponentActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private var listaServicios = mutableStateListOf<Servicio>()

    private lateinit var editarServicioLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        editarServicioLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            cargarServicios()
        }

        setContent {
            ListaServiciosScreen()
        }

        cargarServicios()
    }

    override fun onResume() {
        super.onResume()
        cargarServicios()
    }

    private fun cargarServicios() {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("veterinarios").document(uid)
            .collection("servicios").get()
            .addOnSuccessListener { result ->
                val servicios = result.documents.map { doc ->
                    Servicio(
                        id = doc.id,
                        nombre = doc.getString("nombre_servicio") ?: "",
                        descripcion = doc.getString("descripcion") ?: "",
                        costo = (doc.get("costo") as? Number)?.toDouble() ?: 0.0
                    )
                }
                listaServicios.clear()
                listaServicios.addAll(servicios)
            }
    }

    private fun eliminarServicio(servicioId: String) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("veterinarios").document(uid)
            .collection("servicios").document(servicioId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Servicio eliminado", Toast.LENGTH_SHORT).show()
                listaServicios.removeAll { it.id == servicioId }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show()
            }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ListaServiciosScreen() {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Mis Servicios") })
            },
            content = { padding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    items(listaServicios) { servicio ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("🔧 Servicio: ${servicio.nombre}")
                                Text("📝 Descripción: ${servicio.descripcion}")
                                Text("💰 Precio: ${String.format("%,.0f", servicio.costo)} COP")

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Button(
                                        onClick = {
                                            val intent = Intent(this@AgregarListaServiciosActivity, EditarServicioActivity::class.java)
                                            intent.putExtra("idServicio", servicio.id)
                                            editarServicioLauncher.launch(intent)
                                        },
                                        colors = ButtonDefaults.buttonColors()
                                    ) {
                                        Text("Editar")
                                    }

                                    Button(
                                        onClick = { eliminarServicio(servicio.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text("Eliminar")
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
