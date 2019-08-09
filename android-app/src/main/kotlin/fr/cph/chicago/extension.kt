package fr.cph.chicago

import android.content.Context
import android.os.Build
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.StyleRes
import java.text.SimpleDateFormat
import java.util.Date

fun TextView.setTextAppearance(@StyleRes textAppearance: Int, context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        setTextAppearance(context, textAppearance)
    } else {
        setTextAppearance(textAppearance)
    }
}

@NonNull
fun SimpleDateFormat.parseNotNull(@NonNull date: String): Date {
    return parse(date) ?: Date(0L)
}
