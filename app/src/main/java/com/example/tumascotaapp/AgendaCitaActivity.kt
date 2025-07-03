package com.example.tumascotaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

class AgendaCitaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AgendaCitaScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaCitaScreen() {
    var nombreMascota by remember { mutableStateOf("") }
    var tipoMascota by remember { mutableStateOf("") }
    var otroTipo by remember { mutableStateOf(TextFieldValue("")) }
    var raza by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }
    var motivo by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Agendar Cita Veterinaria") }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = nombreMascota,
                    onValueChange = { nombreMascota = it },
                    label = { Text("Nombre de la Mascota") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                TipoMascotaDropdown(
                    selectedTipo = tipoMascota,
                    onTipoSelected = { tipoMascota = it }
                )

                if (tipoMascota == "Otro") {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = otroTipo,
                        onValueChange = { otroTipo = it },
                        label = { Text("Escribe el tipo de mascota") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = raza,
                    onValueChange = { raza = it },
                    label = { Text("Raza (si aplica)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = fecha,
                    onValueChange = { fecha = it },
                    label = { Text("Fecha (ej: 2024-07-03)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = hora,
                    onValueChange = { hora = it },
                    label = { Text("Hora (ej: 15:30)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = motivo,
                    onValueChange = { motivo = it },
                    label = { Text("Motivo de la cita") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        // Aquí guardar en Firebase
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Agendar Cita")
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipoMascotaDropdown(
    selectedTipo: String,
    onTipoSelected: (String) -> Unit
) {
    val tiposMascota = listOf("Perro", "Gato", "Ave", "Otro")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedTipo,
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
            tiposMascota.forEach { tipo ->
                DropdownMenuItem(
                    text = { Text(tipo) },
                    onClick = {
                        onTipoSelected(tipo)
                        expanded = false
                    }
                )
            }
        }
    }
}
