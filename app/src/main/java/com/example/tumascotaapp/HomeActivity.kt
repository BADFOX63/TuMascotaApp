package com.example.tumascotaapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : ComponentActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HomeScreen()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun HomeScreen() {
        val context = LocalContext.current
        val currentUser = auth.currentUser
        var nombre by remember { mutableStateOf("Usuario") }

        // 🔑 Traer el nombre desde Firestore
        LaunchedEffect(currentUser?.uid) {
            currentUser?.uid?.let { uid ->
                db.collection("usuarios").document(uid).get()
                    .addOnSuccessListener { document ->
                        val nombreFirestore = document.getString("nombre")
                        if (!nombreFirestore.isNullOrEmpty()) {
                            nombre = nombreFirestore
                        }
                    }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("TuMascota - Veterinaria") }
                )
            },
            content = { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "¡Hola, $nombre!",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Bienvenido a TuMascota 🐾")
                    Text("Aquí podrás agendar citas, consultar horarios y más.")

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            context.startActivity(Intent(context, AgendaCitaActivity::class.java))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Agendar Cita Veterinaria")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            context.startActivity(Intent(context, AgregarMascotaActivity::class.java))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Agregar Mascota")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            context.startActivity(Intent(context, PerfilActivity::class.java))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Mi Perfil")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            auth.signOut()
                            startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
                            finish()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cerrar Sesión")
                    }
                }
            }
        )
    }
}
