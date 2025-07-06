package com.example.tumascotaapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditarServicioActivity : ComponentActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val idServicio = intent.getStringExtra("idServicio") ?: return
        val nombreInicial = intent.getStringExtra("nombre") ?: ""
        val descripcionInicial = intent.getStringExtra("descripcion") ?: ""
        val costoInicial = intent.getDoubleExtra("costo", 0.0)

        setContent {
            EditarServicioScreen(idServicio, nombreInicial, descripcionInicial, costoInicial)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun EditarServicioScreen(idServicio: String, nombreInicial: String, descripcionInicial: String, costoInicial: Double) {
        val uid = auth.currentUser?.uid ?: return

        var nombre by remember { mutableStateOf(nombreInicial) }
        var descripcion by remember { mutableStateOf(descripcionInicial) }
        var costo by remember { mutableStateOf(costoInicial.toInt().toString()) }  // Mostrar sin decimales

        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Editar Servicio") })
            },
            content = { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre del Servicio") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = costo,
                        onValueChange = { costo = it },
                        label = { Text("Costo (COP)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            val costoDouble = costo.replace(",", "").toDoubleOrNull()  // Limpiar comas
                            if (nombre.isNotBlank() && descripcion.isNotBlank() && costoDouble != null) {
                                val servicioMap = mapOf(
                                    "nombre_servicio" to nombre,
                                    "descripcion" to descripcion,
                                    "costo" to costoDouble
                                )

                                firestore.collection("veterinarios").document(uid)
                                    .collection("servicios").document(idServicio)
                                    .set(servicioMap)
                                    .addOnSuccessListener {
                                        Toast.makeText(this@EditarServicioActivity, "Servicio actualizado", Toast.LENGTH_SHORT).show()
                                        finish()  
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this@EditarServicioActivity, "Error actualizando servicio", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                Toast.makeText(this@EditarServicioActivity, "Completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Guardar Cambios")
                    }

                    Button(
                        onClick = {
                            firestore.collection("veterinarios").document(uid)
                                .collection("servicios").document(idServicio)
                                .delete()
                                .addOnSuccessListener {
                                    Toast.makeText(this@EditarServicioActivity, "Servicio eliminado", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this@EditarServicioActivity, "Error eliminando servicio", Toast.LENGTH_SHORT).show()
                                }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Eliminar Servicio")
                    }
                }
            }
        )
    }
}
