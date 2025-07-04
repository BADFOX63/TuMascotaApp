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

class AgregarMascotaActivity : ComponentActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AgregarMascotaScreen()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AgregarMascotaScreen() {
        var nombre by remember { mutableStateOf("") }
        var tipo by remember { mutableStateOf("") }
        var otroTipo by remember { mutableStateOf("") }
        var raza by remember { mutableStateOf("") }

        val tiposMascota = listOf("Perro", "Gato", "Ave", "Otro")
        var expanded by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Agregar Mascota", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre de la Mascota") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dropdown de tipo de mascota
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = tipo,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de Mascota") },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    tiposMascota.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                tipo = item
                                expanded = false
                            }
                        )
                    }
                }
            }

            if (tipo == "Otro") {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = otroTipo,
                    onValueChange = { otroTipo = it },
                    label = { Text("¿Qué tipo?") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = raza,
                onValueChange = { raza = it },
                label = { Text("Raza de la Mascota") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        val tipoFinal = if (tipo == "Otro") otroTipo else tipo
                        val mascota = hashMapOf(
                            "nombre" to nombre,
                            "tipo" to tipoFinal,
                            "raza" to raza
                        )

                        firestore.collection("usuarios").document(uid)
                            .collection("mascotas").add(mascota)
                            .addOnSuccessListener {
                                Toast.makeText(this@AgregarMascotaActivity, "Mascota agregada", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@AgregarMascotaActivity, "Error al guardar", Toast.LENGTH_SHORT).show()
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Mascota")
            }
        }
    }
}
