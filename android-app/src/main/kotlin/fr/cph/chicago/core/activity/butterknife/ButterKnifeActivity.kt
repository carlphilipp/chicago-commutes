package fr.cph.chicago.core.activity.butterknife

import android.app.Activity
import android.os.Bundle
import butterknife.ButterKnife

abstract class ButterKnifeActivity(private val contentView: Int) : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!this.isFinishing) {
            setContentView(contentView)
            ButterKnife.bind(this)
            onCreate()
        }
    }

    abstract fun onCreate()
}
