package com.example.dreamai.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit,
    onNavigateToLogin: () -> Unit
) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    // Color de fondo personalizado (puedes cambiarlo)
    val backgroundColor = Color(0xFF1E293B)  // Fondo oscuro
    val inputBackground = Color(0xFF334155)  // Fondo de inputs
    val textColor = Color.White              // Texto blanco
    val hintColor = Color.White.copy(alpha = 0.7f)  // Texto de hint
    val errorColor = Color(0xFFEF5350)       // Rojo para errores
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Iniciar sesión",
                style = MaterialTheme.typography.headlineMedium,
                color = textColor,
                modifier = Modifier
                    .padding(bottom = 24.dp)

            )

            // Campo de email con validación
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = it.isEmpty() || !it.contains("@")
                },
                label = { Text("Correo electrónico",color = textColor) },
                isError = emailError,
                supportingText = {
                    if (emailError) {
                        Text(if (email.isEmpty()) "Este campo es requerido" else "Email inválido",color = errorColor)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // Campo de contraseña con validación
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = it.length < 6
                },
                label = { Text("Contraseña",color = textColor) },
                isError = passwordError,
                supportingText = {
                    if (passwordError) {
                        Text("Mínimo 6 caracteres",color = errorColor)
                    }
                },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de registro con validación completa
            Button(
                onClick = {
                    // Validar todos los campos
                    emailError = email.isEmpty() || !email.contains("@")
                    passwordError = password.length < 6


                    // Si no hay errores, proceder con el registro
                    if (!emailError && !passwordError) {
                        onLoginClick(email, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("INICIAR SESIÓN")
            }

            TextButton(
                onClick = onNavigateToLogin,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("¿No tienes cuenta? Crea una",color = textColor)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    MaterialTheme {
        LoginScreen(
            onLoginClick = { _,_, -> },
            onNavigateToLogin = {}
        )
    }
}