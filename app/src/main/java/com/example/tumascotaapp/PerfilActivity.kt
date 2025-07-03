package com.example.tumascotaapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
        val uid = currentUser?.uid

        // 👤 Datos de la persona
        var nombre by remember { mutableStateOf("") }
        var apellidos by remember { mutableStateOf("") }
        var telefono by remember { mutableStateOf("") }

        // 🐾 Datos de la mascota
        var nombreMascota by remember { mutableStateOf("") }
        var tipoMascota by remember { mutableStateOf("") }
        var razaMascota by remember { mutableStateOf("") }

        // 🔄 Traer datos actuales
        LaunchedEffect(uid) {
            uid?.let {
                db.collection("usuarios").document(it).get()
                    .addOnSuccessListener { document ->
                        nombre = document.getString("nombre") ?: ""
                        apellidos = document.getString("apellidos") ?: ""
                        telefono = document.getString("telefono") ?: ""
                        nombreMascota = document.getString("nombreMascota") ?: ""
                        tipoMascota = document.getString("tipoMascota") ?: ""
                        razaMascota = document.getString("razaMascota") ?: ""
                    }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Mi Perfil") })
            },
            content = { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 🧑 Datos personales
                    OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = apellidos, onValueChange = { apellidos = it }, label = { Text("Apellidos") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = telefono, onValueChange = { telefono = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(16.dp))

                    // 🐶 Datos mascota
                    OutlinedTextField(value = nombreMascota, onValueChange = { nombreMascota = it }, label = { Text("Nombre Mascota") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = tipoMascota, onValueChange = { tipoMascota = it }, label = { Text("Tipo Mascota") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = razaMascota, onValueChange = { razaMascota = it }, label = { Text("Raza Mascota") }, modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            uid?.let {
                                val userMap = hashMapOf(
                                    "nombre" to nombre,
                                    "apellidos" to apellidos,
                                    "telefono" to telefono,
                                    "nombreMascota" to nombreMascota,
                                    "tipoMascota" to tipoMascota,
                                    "razaMascota" to razaMascota
                                )
                                db.collection("usuarios").document(it).update(userMap as Map<String, Any>)
                                    .addOnSuccessListener {
                                        Toast.makeText(this@PerfilActivity, "Datos actualizados", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this@PerfilActivity, "Error al actualizar", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Guardar Cambios")
                    }
                }
            }
        )
    }
}
