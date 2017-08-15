/**
 * Copyright 2017 Carl-Philipp Harmant
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

package fr.cph.chicago.client

import android.util.Log
import fr.cph.chicago.exception.ConnectException
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class HttpClient private constructor() {

    private object Holder {
        val INSTANCE = HttpClient()
    }

    @Throws(ConnectException::class)
    fun connect(address: String): InputStream {
        val inputStream: InputStream
        try {
            Log.v(TAG, "Address: " + address)
            val url = URL(address)
            val urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.connectTimeout = 5000
            urlConnection.readTimeout = 5000
            urlConnection.connect()
            inputStream = BufferedInputStream(urlConnection.inputStream)
        } catch (e: IOException) {
            Log.e(TAG, e.message, e)
            throw ConnectException.defaultException(e)
        }
        return inputStream
    }

    companion object {
        private val TAG = HttpClient::class.java.simpleName
        val INSTANCE: HttpClient by lazy { Holder.INSTANCE }
    }
}
