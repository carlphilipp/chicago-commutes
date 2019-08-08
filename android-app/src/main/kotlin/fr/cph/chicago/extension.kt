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

fun <T> asParcelableArrayList(list: List<T>): ArrayList<T> {
    // Make sure that we have an ArrayList and not a kotlin.collections.EmptyList
    return if (list.isEmpty()) {
        ArrayList()
    } else {
        list as ArrayList
    }
}

@NonNull
fun SimpleDateFormat.parseNotNull(@NonNull date: String): Date {
    return parse(date) ?: Date(0L)
}

@NonNull
fun SimpleDateFormat.formatNotNull(@NonNull date: Date): String {
    return format(date)
}
