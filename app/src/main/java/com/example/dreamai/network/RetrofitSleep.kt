package com.example.dreamai.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitSleep {
    //  Remplaza con tu ip local, igual en el archivo de network_security_config.xml
    private const val BASE_URL = "http://192.168.100.52:8000/"

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}