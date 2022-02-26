package fr.cph.chicago

import androidx.annotation.NonNull
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng
import fr.cph.chicago.core.model.Position
import fr.cph.chicago.core.model.enumeration.TrainLine
import java.text.SimpleDateFormat
import java.util.Date

@NonNull
fun SimpleDateFormat.parseNotNull(@NonNull date: String): Date {
    return parse(date) ?: Date(0L)
}

@NonNull
fun Position.toLatLng(): LatLng {
    return LatLng(this.latitude, this.longitude)
}

fun TrainLine.toComposeColor(): Color {
    return Color(this.color)
}
