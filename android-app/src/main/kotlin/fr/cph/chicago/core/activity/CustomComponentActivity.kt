package fr.cph.chicago.core.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat

abstract class CustomComponentActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Turn off the decor fitting system windows (top and bottom)
        // It means that now we need to handle manually the top padding
        // This allow to have a somewhat fullscreen
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

}
