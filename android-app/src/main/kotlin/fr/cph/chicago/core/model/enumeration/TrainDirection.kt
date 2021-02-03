/**
 * Copyright 2021 Carl-Philipp Harmant
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

package fr.cph.chicago.core.model.enumeration

/**
 * Enumeration, train direction
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
enum class TrainDirection(private val text: String, private val formattedText: String) {

    NORTH("N", "North"),
    SOUTH("S", "South"),
    EAST("E", "East"),
    WEST("W", "West"),
    UNKNOWN("U", "Unknown");

    override fun toString(): String {
        return this.formattedText
    }

    fun toTextString(): String {
        return this.text
    }

    companion object {
        fun fromString(text: String): TrainDirection {
            return values()
                .filter { trainDirection -> text.equals(trainDirection.text, ignoreCase = true) }
                .getOrElse(0) { UNKNOWN }
        }
    }
}
