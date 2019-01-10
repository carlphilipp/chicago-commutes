package fr.cph.chicago

import android.content.Context
import android.os.Build
import android.support.annotation.StyleRes
import android.widget.TextView

fun TextView.setTextAppearance(@StyleRes textAppearance: Int, context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        setTextAppearance(context, textAppearance)
    } else {
        setTextAppearance(textAppearance)
    }
}
