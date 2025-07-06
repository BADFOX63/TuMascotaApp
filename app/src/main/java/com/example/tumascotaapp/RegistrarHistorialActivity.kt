package com.example.tumascotaapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegistrarHistorialActivity : ComponentActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val citaId = intent.getStringExtra("citaId") ?: ""
        val usuarioId = intent.getStringExtra("usuarioId") ?: ""
        val usuarioEmail = intent.getStringExtra("usuarioEmail") ?: ""
        val duenioNombre = intent.getStringExtra("duenioNombre") ?: ""
        val duenioTelefono = intent.getStringExtra("duenioTelefono") ?: ""
        val mascotaNombre = intent.getStringExtra("mascota") ?: ""
        val servicio = intent.getStringExtra("servicio") ?: ""
        val precioBase = intent.getDoubleExtra("precio_inicial", 0.0)

        val fecha = intent.getStringExtra("fecha") ?: "Sin fecha"
        val hora = intent.getStringExtra("hora") ?: "Sin hora"

        setContent {
            RegistrarHistorialScreen(
                citaId = citaId,
                usuarioId = usuarioId,
                usuarioEmail = usuarioEmail,
                duenioNombre = duenioNombre,
                duenioTelefono = duenioTelefono,
                mascotaNombre = mascotaNombre,
                servicio = servicio,
                precioBase = precioBase,
                fecha = fecha,
                hora = hora
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun RegistrarHistorialScreen(
        citaId: String,
        usuarioId: String,
        usuarioEmail: String,
        duenioNombre: String,
        duenioTelefono: String,
        mascotaNombre: String,
        servicio: String,
        precioBase: Double,
        fecha: String,
        hora: String
    ) {
        val context = LocalContext.current
        val veterinarioId = auth.currentUser?.uid ?: return

        var diagnostico by remember { mutableStateOf("") }
        var tratamiento by remember { mutableStateOf("") }
        var observaciones by remember { mutableStateOf("") }
        var precioFinal by remember { mutableStateOf("") }

        Scaffold(
            topBar = { TopAppBar(title = { Text("Registrar Historial Clínico") }) },
            content = { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    Text("🐾 Mascota: $mascotaNombre", style = MaterialTheme.typography.titleMedium)
                    Text("👤 Dueño: $duenioNombre")
                    Text("📧 Correo: $usuarioEmail")
                    Text("📞 Teléfono: $duenioTelefono")
                    Text("🔧 Servicio: $servicio")
                    Text("💰 Precio Base: ${String.format("%,.0f", precioBase)} COP")
                    Text("📅 Fecha: $fecha")
                    Text("⏰ Hora: $hora")

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = diagnostico,
                        onValueChange = { diagnostico = it },
                        label = { Text("Diagnóstico") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = tratamiento,
                        onValueChange = { tratamiento = it },
                        label = { Text("Tratamiento") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = observaciones,
                        onValueChange = { observaciones = it },
                        label = { Text("Observaciones") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = precioFinal,
                        onValueChange = { precioFinal = it },
                        label = { Text("Precio Final (COP)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (diagnostico.isNotEmpty() && tratamiento.isNotEmpty() && precioFinal.isNotEmpty()) {
                                guardarHistorial(
                                    veterinarioId,
                                    usuarioId,
                                    usuarioEmail,
                                    duenioNombre,
                                    duenioTelefono,
                                    citaId,
                                    mascotaNombre,
                                    servicio,
                                    precioBase,
                                    fecha,
                                    hora,
                                    diagnostico,
                                    tratamiento,
                                    observaciones,
                                    precioFinal.toDoubleOrNull() ?: 0.0,
                                    context
                                )
                            } else {
                                Toast.makeText(context, "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Guardar Historial")
                    }
                }
            }
        )
    }

    private fun guardarHistorial(
        veterinarioId: String,
        usuarioId: String,
        usuarioEmail: String,
        duenioNombre: String,
        duenioTelefono: String,
        citaId: String,
        mascotaNombre: String,
        servicio: String,
        precioBase: Double,
        fecha: String,          
        hora: String,
        diagnostico: String,
        tratamiento: String,
        observaciones: String,
        precioFinal: Double,
        context: android.content.Context
    ) {
        val historial = hashMapOf(
            "citaId" to citaId,
            "mascotaNombre" to mascotaNombre,
            "servicio" to servicio,
            "precioBase" to precioBase,
            "fechaCita" to fecha,
            "horaCita" to hora,
            "diagnostico" to diagnostico,
            "tratamiento" to tratamiento,
            "observaciones" to observaciones,
            "precioFinal" to precioFinal,
            "usuarioId" to usuarioId,
            "usuarioEmail" to usuarioEmail,
            "duenioNombre" to duenioNombre,
            "duenioTelefono" to duenioTelefono,
            "veterinarioId" to veterinarioId,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("usuarios").document(usuarioId)
            .collection("mascotas").document(mascotaNombre)
            .collection("historiales").add(historial)

        firestore.collection("veterinarios").document(veterinarioId)
            .collection("historiales").add(historial)
            .addOnSuccessListener {
                Toast.makeText(context, "Historial guardado correctamente", Toast.LENGTH_SHORT).show()
                (context as? RegistrarHistorialActivity)?.finish()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al guardar historial", Toast.LENGTH_SHORT).show()
            }
    }

}
