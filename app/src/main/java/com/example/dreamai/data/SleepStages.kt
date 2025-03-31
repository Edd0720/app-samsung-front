package com.example.dreamai.data

import androidx.health.connect.client.records.SleepSessionRecord
import java.time.Duration
import java.time.Instant

data class ProcessedSleepStages(
    val userId: Int,
    val totalDuration: Double, // en minutos
    val wakeStages: List<SleepSessionRecord.Stage>,
    val lightStages: List<SleepSessionRecord.Stage>,
    val deepStages: List<SleepSessionRecord.Stage>,
    val remStages: List<SleepSessionRecord.Stage>,
    val wakeDuration: Double,
    val lightDuration: Double,
    val deepDuration: Double,
    val remDuration: Double,
    val bedtime: Instant?,
    val wakeup: Instant?,
    val awakenings: Int
)