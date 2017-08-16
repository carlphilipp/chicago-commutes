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

package fr.cph.chicago.entity.enumeration

import android.util.Log
import java.util.*

/**
 * Enumeration, bus direction
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */

class BusDirection(val busTextReceived: String) {
    var busDirectionEnum: BusDirectionEnum? = null
        private set

    val isOk: Boolean
        get() {
            try {
                busDirectionEnum = BusDirectionEnum.fromString(busTextReceived)
                return true
            } catch (e: Exception) {
                return false
            }
        }

    enum class BusDirectionEnum constructor(private val text: String, val shortUpperCase: String, val shortLowerCase: String) {

        NORTHBOUND("Northbound", "NORTH", "North"),
        WESTBOUND("Westbound", "WEST", "West"),
        SOUTHBOUND("Southbound", "SOUTH", "South"),
        EASTBOUND("Eastbound", "EAST", "East");

        override fun toString(): String {
            return text
        }

        companion object {

            fun fromString(text: String): BusDirectionEnum {
                for (busDirectionEnum in BusDirectionEnum.values()) {
                    if (text.equals(busDirectionEnum.text, ignoreCase = true)) {
                        return busDirectionEnum
                    } else if (text.equals(busDirectionEnum.shortUpperCase, ignoreCase = true)) {
                        return busDirectionEnum
                    } else if (busDirectionEnum.text.toLowerCase(Locale.US).contains(text.toLowerCase(Locale.US))) {
                        return busDirectionEnum
                    }
                }
                Log.w(TAG, "Bus direction enum not found: " + text)
                throw IllegalStateException()
            }
        }
    }

    companion object {
        private val TAG = BusDirection::class.java.simpleName
    }
}

