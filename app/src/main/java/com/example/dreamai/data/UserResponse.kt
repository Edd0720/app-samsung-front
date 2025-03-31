package com.example.dreamai.data

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val age: Int,
    val gender: Boolean,
    val weight: Double,
    val id_user_type: Int,
    val role: String
)

data class LoginResponse(
    val access_token: String,
    val token_type: String,
    val user: User
)

data class LoginRequest(
    val email: String,
    val password: String
)
