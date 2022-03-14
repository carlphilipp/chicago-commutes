package fr.cph.chicago

import androidx.annotation.NonNull
import java.text.SimpleDateFormat
import java.util.Date
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Delay
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@NonNull
fun SimpleDateFormat.parseNotNull(@NonNull date: String): Date {
    return parse(date) ?: Date(0L)
}

fun CoroutineScope.launchWithDelay(
    timeMillis: Long,
    block: suspend CoroutineScope.() -> Unit
) {
    this.launch {
        delay(timeMillis)
        block()
    }
}
