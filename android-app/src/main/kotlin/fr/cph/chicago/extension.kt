package fr.cph.chicago

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.annotation.NonNull
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.enumeration.TrainLine
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

fun Context.getActivity(): ComponentActivity = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> throw RuntimeException("activity not found")
}

fun TrainLine.getZoom(): Float {
    return when(this){
        TrainLine.BLUE -> 10.8f
        TrainLine.BROWN -> 12.0728f
        TrainLine.GREEN -> 11.3f
        TrainLine.ORANGE -> 11.98f
        TrainLine.PINK -> 11.87f
        TrainLine.PURPLE -> 11.7f
        TrainLine.RED -> 10.76f
        TrainLine.YELLOW -> 12.52f
        else -> 11f
    }
}

fun TrainLine.getDefaultPosition(): Position {
    return when(this){
        TrainLine.BLUE -> Position(41.90, -87.76)
        TrainLine.BROWN -> Position(41.91279956, -87.6583735)
        TrainLine.GREEN -> Position(41.82709704, -87.709279)
        TrainLine.ORANGE -> Position(41.82979, -87.679172)
        TrainLine.PINK -> Position(41.849431, -87.69153501)
        TrainLine.PURPLE -> Position(41.97, -87.65)
        TrainLine.RED -> Position(41.838029, -87.65660025)
        TrainLine.YELLOW -> Position(42.0190246, -87.715918309)
        else -> Position(41.866, -87.651)
    }
}
