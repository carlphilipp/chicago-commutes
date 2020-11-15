package fr.cph.chicago

import androidx.annotation.NonNull
import java.text.SimpleDateFormat
import java.util.Date

@NonNull
fun SimpleDateFormat.parseNotNull(@NonNull date: String): Date {
    return parse(date) ?: Date(0L)
}
