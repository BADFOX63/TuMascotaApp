package com.example.tumascotaapp

import android.content.Intent
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

data class Mascota(
    val id: String = "",
    val nombre: String = "",
    val tipo: String = "",
    val raza: String = ""
)

class PerfilActivity : ComponentActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PerfilScreen()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun PerfilScreen() {
        val currentUser = auth.currentUser
        val uid = currentUser?.uid ?: return

        var nombre by remember { mutableStateOf("") }
        var apellidos by remember { mutableStateOf("") }
        var telefono by remember { mutableStateOf("") }

        val mascotas = remember { mutableStateListOf<Mascota>() }

        LaunchedEffect(uid) {
            db.collection("usuarios").document(uid).get()
                .addOnSuccessListener { document ->
                    nombre = document.getString("nombre") ?: ""
                    apellidos = document.getString("apellidos") ?: ""
                    telefono = document.getString("telefono") ?: ""
                }

            db.collection("usuarios").document(uid).collection("mascotas")
                .get()
                .addOnSuccessListener { result ->
                    mascotas.clear()
                    mascotas.addAll(result.documents.mapNotNull { doc ->
                        val nombreMascota = doc.getString("nombre") ?: return@mapNotNull null
                        val tipoMascota = doc.getString("tipo") ?: ""
                        val razaMascota = doc.getString("raza") ?: ""
                        Mascota(id = doc.id, nombre = nombreMascota, tipo = tipoMascota, raza = razaMascota)
                    })
                }
        }

        Scaffold(
            topBar = { TopAppBar(title = { Text("Mi Perfil") }) },
            content = { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("👤 Datos Personales", style = MaterialTheme.typography.titleLarge)

                    OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = apellidos, onValueChange = { apellidos = it }, label = { Text("Apellidos") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = telefono, onValueChange = { telefono = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val userMap = mapOf(
                                "nombre" to nombre,
                                "apellidos" to apellidos,
                                "telefono" to telefono
                            )
                            db.collection("usuarios").document(uid).update(userMap)
                                .addOnSuccessListener {
                                    Toast.makeText(this@PerfilActivity, "Datos actualizados", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this@PerfilActivity, "Error al actualizar", Toast.LENGTH_SHORT).show()
                                }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Guardar Cambios")
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("🐾 Mis Mascotas", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (mascotas.isEmpty()) {
                        Text("No tienes mascotas registradas.")
                    } else {
                        LazyColumn {
                            items(mascotas) { mascota ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("🐾 ${mascota.nombre}", style = MaterialTheme.typography.titleMedium)
                                        Text("🐶 Tipo: ${mascota.tipo}")
                                        Text("🔖 Raza: ${mascota.raza}")

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Button(onClick = {
                                                val intent = Intent(this@PerfilActivity, ListaHistorialesMascotaActivity::class.java)
                                                intent.putExtra("mascotaNombre", mascota.nombre)
                                                startActivity(intent)
                                            }) {
                                                Text("Ver Historial")
                                            }

                                            Button(
                                                onClick = {
                                                    eliminarMascota(uid, mascota.id) {
                                                        mascotas.remove(mascota)
                                                        Toast.makeText(this@PerfilActivity, "Mascota eliminada", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
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
                }
            }
        )
    }

    private fun eliminarMascota(uid: String, mascotaId: String, onSuccess: () -> Unit) {
        db.collection("usuarios").document(uid)
            .collection("mascotas").document(mascotaId)
            .delete()
            .addOnSuccessListener { onSuccess() }
    }
}
