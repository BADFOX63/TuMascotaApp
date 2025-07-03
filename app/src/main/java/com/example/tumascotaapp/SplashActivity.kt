package com.example.tumascotaapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            // Usuario ya está logeado
            startActivity(Intent(this, HomeActivity::class.java))
        } else {
            // No hay sesión activa
            startActivity(Intent(this, LoginActivity::class.java))
        }

        finish() // Cerramos Splash para que no quede en el historial
    }
}
