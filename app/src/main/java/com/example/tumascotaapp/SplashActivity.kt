package com.example.tumascotaapp

import com.example.tumascotaapp.R
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SplashActivity : ComponentActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)  // Pantalla simple de carga

        val currentUser = auth.currentUser

        if (currentUser != null) {
            val uid = currentUser.uid

            firestore.collection("usuarios").document(uid).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result != null && task.result.exists()) {
                        val rol = task.result.getString("rol") ?: "usuario"

                        if (rol == "veterinario") {
                            startActivity(Intent(this, VeterinarioActivity::class.java))
                        } else {
                            startActivity(Intent(this, HomeActivity::class.java))
                        }
                    } else {
                        Toast.makeText(this, "No se pudo obtener el rol.", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                    }
                    finish()
                }

        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
