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

package fr.cph.chicago.exception

/**
 * Tracker exception
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
open class TrackerException : Exception {

    /**
     * The constructor
     *
     * @param message the message
     */
    internal constructor(message: String) : super(message)

    /**
     * The constructor
     *
     * @param message the message
     * @param e       the exception
     */
    internal constructor(message: String, e: Exception) : super(message, e)

    companion object {

        private const val serialVersionUID = 1L
        const val ERROR = "Error, please try again later"
    }
}
