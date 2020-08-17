/**
 * Copyright 2020 Carl-Philipp Harmant
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

import android.graphics.Color

/**
 * Enumeration, train line
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
enum class TrainLine constructor(private val text: String, private val fullText: String, val color: Int) {

    BLUE("Blue", "Blue", Color.rgb(0, 158, 218)),
    BROWN("Brn", "Brown", Color.rgb(102, 51, 0)),
    GREEN("G", "Green", Color.rgb(0, 153, 0)),
    ORANGE("Org", "Orange", Color.rgb(255, 128, 0)),
    PINK("Pink", "Pink", Color.rgb(204, 0, 102)),
    PURPLE("P", "Purple", Color.rgb(102, 0, 102)),
    RED("Red", "Red", Color.rgb(240, 0, 0)),
    YELLOW("Y", "Yellow", Color.rgb(253, 216, 53)),
    NA("N/A", "N/A", Color.BLACK);

    /**
     * Get to string with line
     */
    fun toStringWithLine(): String {
        return "$fullText Line"
    }

    override fun toString(): String {
        return fullText
    }

    fun toTextString(): String {
        return text
    }

    companion object {

        /**
         * The train line from xml string
         *
         * @param text the text
         * @return the text
         */
        fun fromXmlString(text: String): TrainLine {
            return values()
                .filter { trainLine -> text.equals(trainLine.text, ignoreCase = true) }
                .getOrElse(0) { NA }
        }

        /**
         * The train line from string
         *
         * @param text the text
         * @return a train line
         */
        fun fromString(text: String): TrainLine {
            return values()
                .filter { trainLine -> trainLine.name.equals(text, ignoreCase = true) }
                .getOrElse(0) { NA }
        }

        fun size(): Int {
            return values().size
        }
    }
}
