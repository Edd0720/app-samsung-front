package com.example.healthconnect.codelab.presentation.screen.sleep

import android.media.audiofx.DynamicsProcessing.Stage
import android.util.Log
import java.time.Duration
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
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.records.SleepSessionRecord
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
    onPermissionsLaunch: (Set<String>) -> Unit = {}
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
                                Text("Datos: ${procesarEtapas(session.stages)}")
                                Log.i("etapas:", session.stages.toString())
                            }

                        }

                    }
                }
            }
        }
    }
}




fun procesarEtapas(stages: List<SleepSessionRecord.Stage>): Map<String, Long> {
    // Crear listas separadas para cada grupo de etapas
    val etapas1a3 = mutableListOf<SleepSessionRecord.Stage>()
    val etapas4 = mutableListOf<SleepSessionRecord.Stage>()
    val etapas5 = mutableListOf<SleepSessionRecord.Stage>()
    val etapas6 = mutableListOf<SleepSessionRecord.Stage>()

    // Clasificar las etapas en sus respectivos grupos
    stages.forEach { stage ->
        when (stage.stage) {
            1, 2, 3 -> etapas1a3.add(stage)
            4 -> etapas4.add(stage)
            5 -> etapas5.add(stage)
            6 -> etapas6.add(stage)
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
    val duracionEtapas1a3 = calcularDuracion(etapas1a3)
    val duracionEtapas4 = calcularDuracion(etapas4)
    val duracionEtapas5 = calcularDuracion(etapas5)
    val duracionEtapas6 = calcularDuracion(etapas6)

    // Crear el mapa resultado con la duración de cada etapa
    val resultado = mapOf(
        "Etapas 1-3" to duracionEtapas1a3,
        "Etapa 4" to duracionEtapas4,
        "Etapa 5" to duracionEtapas5,
        "Etapa 6" to duracionEtapas6
    )

    return resultado
}





