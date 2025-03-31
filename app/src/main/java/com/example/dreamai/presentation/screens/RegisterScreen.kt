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
import androidx.navigation.NavHostController

@Composable
fun RegisterScreen(
    navHostController: NavHostController
) {
    fun onRegisterClick () {

    }
    // Estados para los campos
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    val genderOptions = listOf("Masculino", "Femenino")
    var expanded by remember { mutableStateOf(false) }
    var selectedGender by remember { mutableStateOf("") }

    // Estados para errores
    var nameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var confirmError by remember { mutableStateOf(false) }
    var ageError by remember { mutableStateOf(false) }
    var genderError by remember { mutableStateOf(false) }

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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Registro",
                style = MaterialTheme.typography.headlineMedium,
                color = textColor,
                modifier = Modifier
                    .padding(bottom = 24.dp)

            )

            // Campo de nombre con validación
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = it.isEmpty()
                },
                label = { Text("Nombre completo", color = textColor) },
                isError = nameError,
                supportingText = {
                    if (nameError) {
                        Text("Este campo es requerido",color = errorColor)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
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

            // Confirmación de contraseña con validación
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    confirmError = it != password
                },
                label = { Text("Confirmar contraseña",color = textColor) },
                isError = confirmError,
                supportingText = {
                    if (confirmError) {
                        Text("Las contraseñas no coinciden",color = errorColor)
                    }
                },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Campo de edad con validación
            OutlinedTextField(
                value = age,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() } && newValue.length <= 3) {
                        age = newValue
                        ageError = newValue.isEmpty() || newValue.toInt() < 1
                    }
                },
                label = { Text("Edad",color = textColor) },
                isError = ageError,
                supportingText = {
                    if (ageError) {
                        Text("Edad inválida",color = errorColor)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Dropdown de género con validación
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedGender,
                    onValueChange = {},
                    label = { Text("Género",color = textColor) },
                    isError = genderError,
                    supportingText = {
                        if (genderError) {
                            Text("Selecciona una opción")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true },
                    readOnly = true,
                    trailingIcon = {
                        Text("▼")
                    }
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = {
                        expanded = false
                        genderError = selectedGender.isEmpty()
                    }
                ) {
                    genderOptions.forEach { gender ->
                        DropdownMenuItem(
                            text = { Text(gender) },
                            onClick = {
                                selectedGender = gender
                                expanded = false
                                genderError = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de registro con validación completa
            Button(
                onClick = {
                    // Validar todos los campos
                    nameError = name.isEmpty()
                    emailError = email.isEmpty() || !email.contains("@")
                    passwordError = password.length < 6
                    confirmError = confirmPassword != password
                    ageError = age.isEmpty() || age.toIntOrNull()?.takeIf { it > 0 } == null
                    genderError = selectedGender.isEmpty()

                    // Si no hay errores, proceder con el registro
                    if (!nameError && !emailError && !passwordError && !confirmError && !ageError && !genderError) {
                        onRegisterClick()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("REGISTRARSE")
            }

            TextButton(
                onClick = { navHostController.navigate("login") },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("¿Ya tienes cuenta? Inicia sesión",color = textColor)
            }
        }
    }
}

