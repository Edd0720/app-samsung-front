package com.example.dreamai.repository

import com.example.dreamai.data.LoginRequest
import com.example.dreamai.data.LoginResponse
import com.example.dreamai.network.RetrofitSleep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository {

    private val api = RetrofitSleep.instance

    suspend fun login(
        email: String,
        password: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val request = LoginRequest(email, password)
        try {
            val response = withContext(Dispatchers.IO) {
                api.login(request)
            }
            if (response.access_token.isNotEmpty()) {
                onSuccess(response.access_token)
            } else {
                onError("Error: Credenciales incorrectas")
            }
        } catch (e: Exception) {
            onError("Error: ${e.message}")
        }
    }
}
