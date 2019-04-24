/**
 * Copyright 2019 Carl-Philipp Harmant
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.task

import android.os.AsyncTask
import android.util.Log
import fr.cph.chicago.core.adapter.FavoritesAdapter

class RefreshTimingTask(private val favoritesAdapter: FavoritesAdapter) : AsyncTask<Unit, Unit, Unit>() {

    override fun onProgressUpdate(vararg values: Unit) {
        super.onProgressUpdate()
        this.favoritesAdapter.refreshLastUpdateView()
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

    companion object {
        private val TAG = RefreshTimingTask::class.java.simpleName
    }
}
