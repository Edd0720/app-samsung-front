package com.example.dreamai.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.dreamai.data.LoginRequest
import com.example.dreamai.data.LoginResponse
import com.example.dreamai.data.User
import com.example.dreamai.network.RetrofitSleep

class LoginViewModel : ViewModel() {
    private val _userData = MutableStateFlow<User?>(null)
    val userData = _userData.asStateFlow()

    private val _token = MutableStateFlow("")
    val token = _token.asStateFlow()

    fun login(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitSleep.instance.login(LoginRequest(email, password))

                // Actualizar el estado con el usuario recibido
                _userData.update { response.user }
                _token.update { response.access_token }

                Log.i("etiquetita", "Usuario actualizado: ${response.user}")
                onSuccess()
            } catch (e: Exception) {
                onError("Error: ${e.message}")
            }
        }
    }
}
