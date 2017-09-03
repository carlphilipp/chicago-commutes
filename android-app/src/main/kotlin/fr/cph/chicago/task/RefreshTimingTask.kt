package fr.cph.chicago.task

import android.os.AsyncTask
import android.util.Log

import fr.cph.chicago.core.adapter.FavoritesAdapter

class RefreshTimingTask(private val favoritesAdapter: FavoritesAdapter) : AsyncTask<Unit, Unit, Unit>() {

    private val TAG = RefreshTimingTask::class.java.simpleName

    override fun onProgressUpdate(vararg values: Unit) {
        super.onProgressUpdate()
        this.favoritesAdapter.updateModel()
    }

    override fun doInBackground(vararg params: Unit) {
        while (!this.isCancelled) {
            Log.v(TAG, "Update time. Thread id: " + Thread.currentThread().id)
            try {
                publishProgress()
                Thread.sleep(10000)
            } catch (e: InterruptedException) {
                Log.d(TAG, "Stopping thread. Normal Behavior")
            }
        }
    }
}
