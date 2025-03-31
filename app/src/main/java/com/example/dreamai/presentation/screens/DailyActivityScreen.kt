package com.example.dreamai.presentation.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun DailyActivityScreen() {
    val backgroundColor = Color(0xFF1E293B)  // Fondo oscuro
    val inputBackground = Color(0xFF334155)  // Fondo de inputs
    val textColor = Color.White              // Texto blanco
    val currentDate = remember { // Obtener la fecha de hoy
        java.time.LocalDate.now().toString()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Fecha de hoy
            Text(
                text = "Fecha: $currentDate",
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Encabezado "Ingesta"
            Text(
                text = "Ingesta",
                style = MaterialTheme.typography.headlineMedium,
                color = textColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            // Título principal
            Text(
                text = "Actividades diarias",
                style = MaterialTheme.typography.headlineMedium,
                color = textColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Input de café
            CustomNumericInputField(
                label = "Consumo de cafeína",
                hint = "Introduce cantidad en tazas"
            )

            // Input de alcohol
            CustomNumericInputField(
                label = "Consumo de alcohol",
                hint = "Introduce cantidad en cervezas"
            )

            // Botón para añadir detalles
            Button(
                onClick = { /* Acción para añadir consumo */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(text = "Añadir más consumos")
            }
            // Encabezado "Ingesta"
            Text(
                text = "Actividad fisica",
                style = MaterialTheme.typography.headlineMedium,
                color = textColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Mostrar pasos
            StepsDisplay(steps = 234) // Ejemplo: 234 pasos
        }
    }
}

@Composable
fun CustomNumericInputField(label: String, hint: String) {
    var numericValue by remember { mutableStateOf(0) } // Estado para manejar el valor numérico
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón para decrementar el valor
            Button(
                onClick = {
                    if (numericValue > 0) { // Validación para no aceptar negativos
                        numericValue -= 1
                    }
                },
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(text = "-")
            }

            // Campo de entrada numérica
            TextField(
                value = numericValue.toString(),
                onValueChange = { input ->
                    val newValue = input.toIntOrNull() ?: numericValue
                    if (newValue in 0..5) { // Validación para valores entre 0 y 5
                        numericValue = newValue
                    }
                },
                placeholder = {
                    Text(text = hint)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )

            // Botón para incrementar el valor
            Button(
                onClick = {
                    if (numericValue < 5) { // Validación para no superar 5
                        numericValue += 1
                    }
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(text = "+")
            }
        }
    }
}
@Composable
fun StepsDisplay(steps: Int) {
    val backgroundColor = Color(0xFF334155) // Fondo oscuro para el contenedor
    val textColor = Color.White             // Color del texto
    val iconColor = Color(0xFF4CAF50)       // Color del ícono (verde)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .background(backgroundColor, shape = RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Ícono de pasos
            Icon(
                imageVector = Icons.Default.Info, // Cambiar a un ícono disponible
                contentDescription = "Ícono de pasos",
                tint = iconColor,
                modifier = Modifier
                    .size(48.dp)
                    .padding(end = 16.dp)
            )

            // Texto principal con cantidad de pasos
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Pasos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
                Text(
                    text = "$steps pasos",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDailyActivityScreen() {
    DailyActivityScreen()
}
