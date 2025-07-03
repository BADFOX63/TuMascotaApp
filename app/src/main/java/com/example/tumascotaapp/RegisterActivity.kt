package com.example.tumascotaapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setContent {
            RegisterScreen { nombre, apellidos, telefono, email, password, nombreMascota, tipoMascota, razaMascota ->
                registerUser(nombre, apellidos, telefono, email, password, nombreMascota, tipoMascota, razaMascota)
            }
        }
    }

    private fun registerUser(
        nombre: String,
        apellidos: String,
        telefono: String,
        email: String,
        password: String,
        nombreMascota: String,
        tipoMascota: String,
        razaMascota: String
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    val userMap = hashMapOf(
                        "nombre" to nombre,
                        "apellidos" to apellidos,
                        "telefono" to telefono,
                        "email" to email,
                        "nombreMascota" to nombreMascota,
                        "tipoMascota" to tipoMascota,
                        "razaMascota" to razaMascota
                    )

                    userId?.let {
                        firestore.collection("usuarios").document(it)
                            .set(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, HomeActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    @Composable
    fun RegisterScreen(onRegister: (String, String, String, String, String, String, String, String) -> Unit) {
        val context = LocalContext.current

        var nombre by remember { mutableStateOf("") }
        var apellidos by remember { mutableStateOf("") }
        var telefono by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        var nombreMascota by remember { mutableStateOf("") }
        var tipoMascota by remember { mutableStateOf("") }
        var otroTipoMascota by remember { mutableStateOf("") }
        var razaMascota by remember { mutableStateOf("") }

        val tiposMascota = listOf("Perro", "Gato", "Ave", "Otro")
        var expanded by remember { mutableStateOf(false) }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Registro TuMascota", style = MaterialTheme.typography.headlineMedium)

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = apellidos, onValueChange = { apellidos = it }, label = { Text("Apellidos") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = telefono, onValueChange = { telefono = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(20.dp))
                OutlinedTextField(value = nombreMascota, onValueChange = { nombreMascota = it }, label = { Text("Nombre Mascota") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(10.dp))

                @OptIn(ExperimentalMaterial3Api::class) //habilitar las opciones experimentales
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = tipoMascota,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de Mascota") },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        tiposMascota.forEach { tipo ->
                            DropdownMenuItem(text = { Text(tipo) }, onClick = {
                                tipoMascota = tipo
                                expanded = false
                            })
                        }
                    }
                }

                if (tipoMascota == "Otro") {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(value = otroTipoMascota, onValueChange = { otroTipoMascota = it }, label = { Text("¿Qué tipo?") }, modifier = Modifier.fillMaxWidth())
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = razaMascota, onValueChange = { razaMascota = it }, label = { Text("Raza de la Mascota") }, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val tipoFinal = if (tipoMascota == "Otro") otroTipoMascota else tipoMascota
                        onRegister(nombre, apellidos, telefono, email, password, nombreMascota, tipoFinal, razaMascota)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Registrarse")
                }

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(onClick = {
                    context.startActivity(Intent(context, LoginActivity::class.java))
                }) {
                    Text("¿Ya tienes cuenta? Inicia sesión")
                }
            }
        }
    }
}
