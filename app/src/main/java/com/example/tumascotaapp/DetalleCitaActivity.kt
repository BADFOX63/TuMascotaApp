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
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.format.DateTimeFormatter

class DetalleCitaActivity : ComponentActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val dateFormatter = DateTimeFormatter.ISO_DATE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val citaId = intent.getStringExtra("citaId") ?: ""
        val mascota = intent.getStringExtra("mascota") ?: ""
        val fecha = intent.getStringExtra("fecha") ?: ""
        val hora = intent.getStringExtra("hora") ?: ""
        val duenioNombre = intent.getStringExtra("duenioNombre") ?: ""
        val duenioTelefono = intent.getStringExtra("duenioTelefono") ?: ""
        val motivo = intent.getStringExtra("motivo") ?: "Sin motivo"

        setContent {
            DetalleCitaScreen(
                citaId = citaId,
                mascota = mascota,
                fecha = fecha,
                hora = hora,
                duenioNombre = duenioNombre,
                duenioTelefono = duenioTelefono,
                motivo = motivo
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DetalleCitaScreen(
        citaId: String,
        mascota: String,
        fecha: String,
        hora: String,
        duenioNombre: String,
        duenioTelefono: String,
        motivo: String
    ) {
        val context = LocalContext.current
        val usuarioId = auth.currentUser?.uid
        val veterinarioId = usuarioId

        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Detalle de Cita") })
            },
            content = { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text("🐾 Mascota: $mascota", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("📅 Fecha: $fecha")
                    Text("⏰ Hora: $hora")
                    Text("📝 Motivo: $motivo")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("👤 Dueño: $duenioNombre")
                    Text("📞 Teléfono: $duenioTelefono")

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (usuarioId != null && veterinarioId != null) {
                                cancelarCita(
                                    usuarioId,
                                    veterinarioId,
                                    citaId,
                                    fecha,
                                    hora,
                                    context
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Cancelar Cita")
                    }
                }
            }
        )
    }

    private fun cancelarCita(
        usuarioId: String,
        veterinarioId: String,
        citaId: String,
        fecha: String,
        hora: String,
        context: android.content.Context
    ) {
        val fechaKey = fecha
        val fechaRef = firestore.collection("calendarios")
            .document(veterinarioId)
            .collection("fechas")
            .document(fechaKey)

        val userDelete = firestore.collection("usuarios").document(usuarioId)
            .collection("citas").document(citaId).delete()

        val vetDelete = firestore.collection("veterinarios").document(veterinarioId)
            .collection("citas").document(citaId).delete()

        val calendarUpdate = firestore.runTransaction { transaction ->
            val snapshot = transaction.get(fechaRef)
            val horasActuales = snapshot.get("horasDesactivadas") as? List<String> ?: emptyList()
            val nuevasHoras = horasActuales.toMutableList().apply { remove(hora) }
            transaction.update(fechaRef, "horasDesactivadas", nuevasHoras)
        }

        userDelete
            .continueWithTask { vetDelete }
            .continueWithTask { calendarUpdate }
            .addOnSuccessListener {
                Toast.makeText(context, "Cita cancelada y hora liberada", Toast.LENGTH_SHORT).show()
                val intent = Intent(context, ListaCitasActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                if (context is DetalleCitaActivity) context.finish()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error cancelando cita", Toast.LENGTH_SHORT).show()
            }
    }
}
