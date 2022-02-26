package fr.cph.chicago

import androidx.annotation.NonNull
import androidx.compose.ui.graphics.Color
import fr.cph.chicago.core.model.enumeration.TrainLine
import java.text.SimpleDateFormat
import java.util.Date

@NonNull
fun SimpleDateFormat.parseNotNull(@NonNull date: String): Date {
    return parse(date) ?: Date(0L)
}

fun TrainLine.toComposeColor(): Color {
    return Color(this.color)
}
