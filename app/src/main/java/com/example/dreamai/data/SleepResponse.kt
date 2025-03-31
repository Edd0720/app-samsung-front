package com.example.dreamai.data

data class SleepResponse(
    val prediction: Double
)



data class SleepBody(
    val user_id: Int,
    val gender: Int,
    val age: Int,
    val sleep_duration: Double,
    val sleep_rem: Int,
    val sleep_deep: Int,
    val sleep_light: Int,
    val awakenings: Int,
    val caffeine: Int,
    val alcohol: Int,
    val smoking_status: Boolean,
    val exercise_frequency: Int,
    val bedtime: Double,
    val wakeup: Double
)