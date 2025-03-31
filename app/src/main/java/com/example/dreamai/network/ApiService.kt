package com.example.dreamai.network

import com.example.dreamai.data.LoginRequest
import com.example.dreamai.data.LoginResponse
import com.example.dreamai.data.SleepBody
import com.example.dreamai.data.SleepResponse
import retrofit2.Call
import retrofit2.http.*



interface ApiService {
    @POST("/login")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse

    @POST("/predict/")
    fun getPredict(
        @Query("user_id") userId: Int,   // Parámetro en la URL
        @Body sleepData: SleepBody       // El resto del objeto va en el cuerpo
    ): Call<SleepResponse>

}