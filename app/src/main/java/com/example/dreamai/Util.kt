import androidx.compose.material3.*
import com.example.dreamai.data.dateTimeWithOffsetOrDefault
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Muestra detalles de una excepción en el Snackbar
 */
fun showExceptionSnackbar(
  snackbarHostState: SnackbarHostState,
  scope: CoroutineScope,
  throwable: Throwable?,
) {
  scope.launch {
    snackbarHostState.showSnackbar(
      message = throwable?.localizedMessage ?: "Excepción desconocida",
      duration = SnackbarDuration.Short
    )
  }
}

/**
 * Formato para mostrar la hora de inicio y fin
 */
fun formatDisplayTimeStartEnd(
  startTime: Instant,
  startZoneOffset: ZoneOffset?,
  endTime: Instant,
  endZoneOffset: ZoneOffset?,
): String {
  val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
  val start = timeFormatter.format(dateTimeWithOffsetOrDefault(startTime, startZoneOffset))
  val end = timeFormatter.format(dateTimeWithOffsetOrDefault(endTime, endZoneOffset))
  return "$start - $end"
}
