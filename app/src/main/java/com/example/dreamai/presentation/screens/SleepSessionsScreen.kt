package com.example.healthconnect.codelab.presentation.screen.sleep

import java.time.Duration
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.navigation.NavHostController
import com.example.dreamai.data.SleepBody
import com.example.dreamai.presentation.navigation.Screen
import java.util.UUID


@Composable
fun SleepSessionScreen(
    permissions: Set<String>,
    permissionsGranted: Boolean,
    backgroundReadPermissions: Set<String>,
    backgroundReadAvailable: Boolean,
    backgroundReadGranted: Boolean,
    historyReadPermissions: Set<String>,
    historyReadAvailable: Boolean,
    historyReadGranted: Boolean,
    sessionsList: List<SleepSessionRecord>,
    uiState: SleepSessionViewModel.UiState,
    onInsertClick: () -> Unit = {},
    onError: (Throwable?) -> Unit = {},
    onPermissionsResult: () -> Unit = {},
    onPermissionsLaunch: (Set<String>) -> Unit = {},
    navController: NavHostController
) {

    // Recordar el último error para evitar re-lanzarlo en recomposiciones
    val errorId = rememberSaveable { mutableStateOf(UUID.randomUUID()) }

    LaunchedEffect(uiState) {
        // Intentar cargar los datos si el estado no está inicializado
        if (uiState is SleepSessionViewModel.UiState.Uninitialized) {
            onPermissionsResult()
        }

        // Manejar errores específicos del estado
        if (uiState is SleepSessionViewModel.UiState.Error && errorId.value != uiState.uuid) {
            onError(uiState.exception)
            errorId.value = uiState.uuid
        }
    }

    if (uiState != SleepSessionViewModel.UiState.Uninitialized) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!permissionsGranted) {
                item {
                    Button (
                        onClick = {
                            onPermissionsLaunch(permissions)
                        }
                    ) {
                        Text(text = "Solicitar permisos")
                    }
                    Button(
                        onClick = {
                            navController.navigate(Screen.HomeScreen.route)
                        }
                    ) {
                        Text(text = "IR al home")
                    }
                }
            } else {
                item {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(4.dp),
                        onClick = {
                            onInsertClick()
                        }
                    ) {
                        Text("Agregar sesión de sueño")
                    }
                }
                items(sessionsList) { session ->
                    // Estado para controlar el menú desplegable
                    var expanded by remember { mutableStateOf(false) }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Text("Título: ${session.title ?: "Sin título"}")
                        Text("Inicio: ${session.startTime}")
                        Text("Fin: ${session.endTime}")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (expanded){
                                Button(
                                    onClick = { !expanded}
                                ) {
                                    Text("Opciones")
                                }
                            } else {
                                Text("Datos: ${procesarEtapas(userId = 7,session.stages)}")
                                Log.i("etapas:", session.stages.toString())
                            }

                        }

                    }
                }
            }
        }
    }
}




// Función para procesar las etapas del sueño
fun procesarEtapas(userId: Int, stages: List<SleepSessionRecord.Stage>): SleepBody {
    // Crear listas separadas para cada grupo de etapas
    val vigilia = mutableListOf<SleepSessionRecord.Stage>()
    val ligero = mutableListOf<SleepSessionRecord.Stage>()
    val profundo = mutableListOf<SleepSessionRecord.Stage>()
    val rem = mutableListOf<SleepSessionRecord.Stage>()

    // Clasificar las etapas en sus respectivos grupos
    stages.forEach { stage ->
        when (stage.stage) {
            1 -> vigilia.add(stage)    // Vigilia
            4 -> ligero.add(stage)     // Sueño Ligero
            5 -> profundo.add(stage)   // Sueño Profundo
            6 -> rem.add(stage)        // Sueño REM
        }
    }

    // Función para calcular la duración total de una lista de etapas
    fun calcularDuracion(etapas: List<SleepSessionRecord.Stage>): Long {
        return etapas.sumOf { stage ->
            val start = stage.startTime
            val end = stage.endTime
            Duration.between(start, end).toMinutes()
        }
    }

    // Calcular la duración total de cada grupo de etapas
    val duracionVigilia = calcularDuracion(vigilia).toDouble()
    val duracionLigero = calcularDuracion(ligero).toDouble().toInt()
    val duracionProfundo = calcularDuracion(profundo).toDouble().toInt()
    val duracionREM = calcularDuracion(rem).toDouble().toInt()
    val duracionTotal = duracionVigilia + duracionLigero + duracionProfundo + duracionREM

    // Calcular el tiempo de acostarse y despertar
    val bedtime = (stages.minOfOrNull { it.startTime.epochSecond }?.toDouble() ?: 0.0).let { String.format("%.1f", it).toDouble() }
    val wakeup = (stages.maxOfOrNull { it.endTime.epochSecond }?.toDouble() ?: 0.0).let { String.format("%.1f", it).toDouble() }
    val awakenings = vigilia.size

    // Crear el objeto SleepBody con los resultados
    return SleepBody(
        user_id = userId,
        sleep_duration = duracionTotal,
        sleep_rem = duracionREM,
        sleep_deep = duracionProfundo,
        sleep_light = duracionLigero,
        bedtime = bedtime,
        awakenings = awakenings,
        wakeup = wakeup,
        smoking_status = true,
        caffeine = 12,
        alcohol = 12,
        exercise_frequency = 2,
        gender = 0,
        age = 24,
    )
}





