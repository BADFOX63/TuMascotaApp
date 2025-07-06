package com.example.tumascotaapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

class VeterinarioActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VeterinarioMenuScreen()
        }
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun VeterinarioMenuScreen() {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Panel Veterinario") })
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
                    Button(
                        onClick = {
                            startActivity(Intent(this@VeterinarioActivity, ListaCitasActivity::class.java))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text("Ver Citas Agendadas")
                    }

                    Button(
                        onClick = {
                            startActivity(Intent(this@VeterinarioActivity, CalendarioVeterinarioActivity::class.java))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text("Configurar Horarios")
                    }

                    Button(
                        onClick = {
                            startActivity(Intent(this@VeterinarioActivity, ListaDiasInactivosActivity::class.java))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text("Configurar Dias Inactivos")
                    }

                    Button(
                        onClick = {
                            startActivity(Intent(this@VeterinarioActivity, ListaServiciosActivity::class.java))
                        },
                        modifier = Modifier.
                        fillMaxWidth()
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text("Servicios")
                    }

                    Button(
                        onClick = {
                            val intent = Intent(this@VeterinarioActivity, ListaHistorialesActivity::class.java)
                            startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text("Historiales Clínicos")
                    }

                    Button(
                        onClick = {
                            FirebaseAuth.getInstance().signOut()
                            startActivity(Intent(this@VeterinarioActivity, LoginActivity::class.java))
                            finish()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text("Cerrar Sesión")
                    }
                }
            }
        )
    }
}
