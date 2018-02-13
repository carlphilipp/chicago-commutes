/**
 * Copyright 2018 Carl-Philipp Harmant
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

import fr.cph.chicago.Constants.Companion.DIVYY_URL
import fr.cph.chicago.exception.ConnectException
import java.io.InputStream

/**
 * Class that build url and getBikeStations to Divvy API.
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
object DivvyClient {

    @Throws(ConnectException::class)
    fun getBikeStations(): InputStream {
        return HttpClient.connect(DIVYY_URL)
    }
}
