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

package fr.cph.chicago.client

import fr.cph.chicago.exception.ConnectException
import fr.cph.chicago.redux.store
import java.io.InputStream

/**
 * Class that build connect to the Divvy API.
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
object DivvyClient {

    private val httpClient = HttpClient

    @Throws(ConnectException::class)
    fun getStationsInformation(): InputStream {
        return httpClient.connect(store.state.divvyStationInformationUrl)
    }

    @Throws(ConnectException::class)
    fun getStationsStatus(): InputStream {
        return httpClient.connect(store.state.divvyStationStatusUrl)
    }
}
