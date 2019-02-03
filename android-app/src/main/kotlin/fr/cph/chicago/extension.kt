package fr.cph.chicago

import android.content.Context
import android.os.Build
import android.widget.TextView
import androidx.annotation.StyleRes

fun TextView.setTextAppearance(@StyleRes textAppearance: Int, context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        setTextAppearance(context, textAppearance)
    } else {
        setTextAppearance(textAppearance)
    }
}
