package fr.cph.chicago.core.activity.butterknife

import android.app.ListActivity
import android.os.Bundle
import butterknife.ButterKnife

abstract class ButterKnifeListActivity(private val contentView: Int) : ListActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!this.isFinishing) {
            setContentView(contentView)
            ButterKnife.bind(this)
            create(savedInstanceState)
        }
    }

    abstract fun create(savedInstanceState: Bundle?)
}
