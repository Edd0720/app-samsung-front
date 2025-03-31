package com.example.dreamai.presentation.screens

import android.util.Log
import java.time.Duration
import java.time.Instant
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.navigation.NavHostController
import com.example.dreamai.data.SleepBody
import com.example.dreamai.data.SleepResponse
import com.example.dreamai.data.User
import com.example.dreamai.network.RetrofitSleep
import com.example.dreamai.viewmodel.LoginViewModel
import com.example.healthconnect.codelab.presentation.screen.sleep.SleepSessionViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.clip
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt


@Composable
fun HomeScreen(
    loginViewModel: LoginViewModel,
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
    val errorId = rememberSaveable { mutableStateOf(UUID.randomUUID()) }
    val predictionText = remember { mutableStateOf("Cargando...") }
    val isLoading = remember { mutableStateOf(false) }

    // Fecha seleccionada por defecto: el día de hoy
    val selectedDate = rememberSaveable {
        mutableStateOf(LocalDate.now(ZoneId.systemDefault()))
    }

    // Función para cargar la predicción
    fun loadPrediction(date: LocalDate) {
        isLoading.value = true
        val sessionsOfDay = sessionsList.filter { session ->
            Instant.ofEpochSecond(session.startTime.epochSecond)
                .atZone(ZoneId.of("UTC"))
                .toLocalDate() == date
        }

        if (sessionsOfDay.isNotEmpty()) {
            val sleepBody = procesarEtapas(
                userId = 1,
                stages = sessionsOfDay.first().stages
            )
            getPredict(sleepBody) { prediction ->
                predictionText.value = prediction ?: "Sin datos"
                isLoading.value = false
            }
        } else {
            predictionText.value = "Sin datos"
            isLoading.value = false
        }
    }

    // Cargar predicción al inicio y cuando cambia la lista de sesiones
    LaunchedEffect(sessionsList) {
        loadPrediction(selectedDate.value)
    }

    LaunchedEffect(uiState) {
        if (uiState is SleepSessionViewModel.UiState.Uninitialized) {
            onPermissionsResult()
        }

        if (uiState is SleepSessionViewModel.UiState.Error && errorId.value != uiState.uuid) {
            onError(uiState.exception)
            errorId.value = uiState.uuid
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1D2630))
            .padding(bottom = 60.dp, top = 20.dp)
    ) {
        HeaderSection(usersito = loginViewModel.userData.collectAsState().value)

        // Carrusel de días
        DiasCarrusel(
            sessionsList = sessionsList,
            selectedDate = selectedDate.value,
            onDaySelected = { date ->
                selectedDate.value = date
                loadPrediction(date)
            }
        )

        // Mostrar estado de carga
        if (isLoading.value) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally),
                color = Color(0xFF8053FF)
            )
        }

        // Filtrar sesiones por la fecha seleccionada
        val sessionsOfDay = sessionsList.filter { session ->
            Instant.ofEpochSecond(session.startTime.epochSecond)
                .atZone(ZoneId.of("UTC"))
                .toLocalDate() == selectedDate.value
        }

        if (sessionsOfDay.isNotEmpty()) {
            sessionsOfDay.forEach { session ->
                SleepSummaryCard(
                    permissions = permissions,
                    permissionsGranted = permissionsGranted,
                    backgroundReadPermissions = backgroundReadPermissions,
                    backgroundReadAvailable = backgroundReadAvailable,
                    backgroundReadGranted = backgroundReadGranted,
                    historyReadPermissions = historyReadPermissions,
                    historyReadAvailable = historyReadAvailable,
                    historyReadGranted = historyReadGranted,
                    sessionsList = sessionsOfDay,
                    uiState = uiState,
                    onPermissionsLaunch = onPermissionsLaunch,
                    sessionDay = session,
                    prediction = predictionText.value
                )
            }
        } else {
            Text(
                text = "No hay datos de sueño para este día",
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = Color.Gray
        )

        SleepRecommendations()
        TipsSection()
    }
}


@Composable
fun DiasCarrusel(
    sessionsList: List<SleepSessionRecord>,
    selectedDate: LocalDate,
    onDaySelected: (LocalDate) -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("EEE dd", Locale("es", "ES"))

    // Obtener fechas únicas de las sesiones disponibles
    val availableDates = remember(sessionsList) {
        sessionsList.map { session ->
            Instant.ofEpochSecond(session.startTime.epochSecond)
                .atZone(ZoneId.of("UTC"))  // Usar UTC para evitar problemas de huso horario
                .toLocalDate()
        }.distinct().sortedDescending()
    }

    // Si no hay sesiones, mostrar los últimos 6 días desde hoy
    val datesToShow = if (availableDates.isEmpty()) {
        (0..5).map { i ->
            LocalDate.now(ZoneId.systemDefault()).minusDays(i.toLong())
        }
    } else {
        availableDates.take(6) // Mostrar un máximo de 6 días
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(datesToShow) { date ->
            val isSelected = date == selectedDate

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .size(55.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color(0xFF5A29E4) else Color(0xFF7B42F6))
                    .clickable {
                        onDaySelected(date)
                    }
            ) {
                Text(
                    text = date.format(dateFormatter).replaceFirstChar { it.uppercase() },
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}


@Composable
fun SleepSummaryCard(
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
    onPermissionsLaunch: (Set<String>) -> Unit = {},
    sessionDay: SleepSessionRecord,
    prediction: String // Nuevo parámetro para la predicción
) {
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE dd 'de' MMMM yyyy", Locale("es", "ES"))
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale("es", "ES"))

    val sessionStart = Instant.ofEpochSecond(sessionDay.startTime.epochSecond)
        .atZone(ZoneId.systemDefault())
    val sessionEnd = Instant.ofEpochSecond(sessionDay.endTime.epochSecond)
        .atZone(ZoneId.systemDefault())

    val fechaFormateada = remember(sessionStart) {
        sessionStart.toLocalDate().format(dateFormatter).replaceFirstChar { it.uppercase() }
    }

    val sleepDuration = remember(sessionDay) {
        Duration.between(sessionStart, sessionEnd).toMinutes()
    }
    val prediccionEntero = try {
        (prediction.toDouble() * 100).toInt()
    } catch (e: NumberFormatException) {
        0 // Valor predeterminado en caso de error
    }
    val hours = sleepDuration / 60
    val minutes = sleepDuration % 60
    val sleepDurationText = "$hours h $minutes m"
    val predictionRounded = remember(prediction) {
        try {
            if (prediction.toDoubleOrNull() != null) {
                prediction.toInt()
            } else {
                prediction // Mantener el valor original si no es numérico
            }
        } catch (e: Exception) {
            prediction // Mantener el valor original si hay error
        }
    }

    if (uiState != SleepSessionViewModel.UiState.Uninitialized) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (!permissionsGranted) {
                Button(onClick = { onPermissionsLaunch(permissions) }) {
                    Text(text = "Solicitar permisos")
                }
            } else {
                Text(
                    text = fechaFormateada,
                    fontSize = 16.sp,
                    color = Color.Gray
                )

                Text(
                    text = "${sessionStart.format(timeFormatter)} - ${sessionEnd.format(timeFormatter)}",
                    fontSize = 14.sp,
                    color = Color.LightGray,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color(0xFF8053FF), shape = CircleShape)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = prediccionEntero.toString(), // Mostramos la predicción aquí
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Predicción",
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = sleepDurationText,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8053FF)
                        )
                        Text(
                            text = "Tiempo dormido",
                            fontSize = 14.sp,
                            color = Color(0xFF8053FF)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun HeaderSection(usersito: User?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Bienvenido",
                color = Color.White,
                fontSize = 14.sp
            )
            if (usersito != null) {
                Text(
                    text = usersito.name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

        }
        IconButton(onClick = { /* Acción de notificación */ }) {
            Icon(Icons.Default.Notifications, contentDescription = "Notificaciones", tint = Color(0xFF9146FF))
        }

    }
}



@Composable
fun SleepRecommendations() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(10.dp)
    ) {
        CardItem(
            icon = Icons.Default.AccountCircle,
            value = "0",
            label = "Frecuencia Cardiaca",
            modifier = Modifier.weight(1f)
        )

        CardItem(
            icon = Icons.Default.Star,
            value = "9:30 pm - 8:00 am",
            label = "Horario Ideal",
            modifier = Modifier.weight(1f)
        )
    }
}
@Composable
fun CardItem(icon: ImageVector, value: String, label: String,modifier: Modifier) {
    Column(
        modifier = modifier.height(100.dp).padding(12.dp).background(Color(0xFF0C121C),shape = RoundedCornerShape(10.dp)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,

        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.Gray
        )
    }
}


@Composable
fun TipsSection() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Tips para mejorar tu calidad del sueño",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        LazyColumn{
            items(4) { index ->
                TipItem()
            }
        }
    }
}

@Composable
fun TipItem() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color(0xFF2A3A47), shape = RoundedCornerShape(10.dp))
            .padding(16.dp)
    ) {
//        Image(
//            painter = painterResource(id = R.drawable.sleep_icon), // Imagen de ejemplo
//            contentDescription = "Tip Image",
//            modifier = Modifier.size(60.dp)
//        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = "Tip1 Lorem ipsum dolo", color = Color.White, fontWeight = FontWeight.Bold)
            Text(text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit...", color = Color.Gray, fontSize = 14.sp)
        }
    }
}


fun getPredict(sleepData: SleepBody, onResult: (String?) -> Unit) {
    Log.d("Retrofit", "Payload enviado: $sleepData")

    // Obtener el user_id del objeto sleepData
    val userId = sleepData.user_id

    // Llamada a la API con el user_id como parámetro en la URL
    val call = RetrofitSleep.instance.getPredict(userId, sleepData)

    call.enqueue(object : Callback<SleepResponse> {
        override fun onResponse(call: Call<SleepResponse>, response: Response<SleepResponse>) {
            if (response.isSuccessful) {
                val predictions = response.body()

                // Asegúrate de imprimir el valor directamente para verificar
                val rawPrediction = predictions?.prediction
                Log.d("Retrofit", "Predicción cruda: $rawPrediction")

                // Verificar si el valor es nulo o cero
                if (rawPrediction == null) {
                    Log.e("Retrofit", "La predicción es nula")
                    onResult("Predicción nula")
                } else {
                    val formattedPrediction = "%.2f".format(rawPrediction)
                    Log.d("Retrofit", "Predicción formateada: $formattedPrediction")
                    onResult(formattedPrediction)
                }
            } else {
                Log.e("Retrofit", "Error en la respuesta: ${response.code()} - ${response.errorBody()?.string()}")
                onResult("Error en la respuesta: ${response.code()}")
            }
        }

        override fun onFailure(call: Call<SleepResponse>, t: Throwable) {
            Log.e("Retrofit", "Error en la petición: ${t.message}")
            onResult("Error en la petición")
        }
    })
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
    val duracionLigero = calcularDuracion(ligero).toDouble()
    val duracionProfundo = calcularDuracion(profundo).toDouble()
    val duracionREM = calcularDuracion(rem).toDouble()
    val duracionTotal = duracionVigilia + duracionLigero + duracionProfundo + duracionREM
    val awakenings = vigilia.size

    val totalSueño = duracionLigero + duracionProfundo + duracionREM
    val porcentajeREM = if (totalSueño > 0) (duracionREM / totalSueño) * 100 else 0.0
    val porcentajeProfundo = if (totalSueño > 0) (duracionProfundo / totalSueño) * 100 else 0.0
    val porcentajeLigero = if (totalSueño > 0) (duracionLigero / totalSueño) * 100 else 0.0

    // Formatear porcentajes como enteros
    val remFormateado = porcentajeREM.toInt()
    val profundoFormateado = porcentajeProfundo.toInt()
    val ligeroFormateado = porcentajeLigero.toInt()

    // Calcular horas de acostarse y despertarse en formato decimal con 1 decimal
    val bedtimeInstant = stages.minOfOrNull { it.startTime } ?: Instant.now()
    val wakeupInstant = stages.maxOfOrNull { it.endTime } ?: Instant.now()

    val bedtimeZoned = bedtimeInstant.atZone(ZoneId.systemDefault())
    val wakeupZoned = wakeupInstant.atZone(ZoneId.systemDefault())

    val bedtimeHour = "%.1f".format(bedtimeZoned.hour + (bedtimeZoned.minute / 60.0)).toDouble()
    val wakeupHour = "%.1f".format(wakeupZoned.hour + (wakeupZoned.minute / 60.0)).toDouble()


    return SleepBody(
        user_id = userId,
        gender = 0,
        age = 36,
        sleep_duration = "%.1f".format(duracionTotal / 60.0).toDouble(), // en horas con 1 decimal
        sleep_rem = remFormateado,
        sleep_deep = profundoFormateado,
        sleep_light = ligeroFormateado,
        awakenings = awakenings,
        caffeine = 0,
        alcohol = 0,
        smoking_status = false,
        exercise_frequency = 0,
        bedtime = bedtimeHour,
        wakeup = wakeupHour
    )
}