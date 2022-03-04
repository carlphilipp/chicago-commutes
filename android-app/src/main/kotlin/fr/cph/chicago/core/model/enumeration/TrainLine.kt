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

import androidx.compose.ui.graphics.Color
import fr.cph.chicago.core.composable.theme.blue_line
import fr.cph.chicago.core.composable.theme.brown_line
import fr.cph.chicago.core.composable.theme.default_line
import fr.cph.chicago.core.composable.theme.green_line
import fr.cph.chicago.core.composable.theme.orange_line
import fr.cph.chicago.core.composable.theme.pink_line
import fr.cph.chicago.core.composable.theme.purple_line
import fr.cph.chicago.core.composable.theme.red_line
import fr.cph.chicago.core.composable.theme.yellow_line

/**
 * Enumeration, train line
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
enum class TrainLine constructor(private val text: String, private val fullText: String, val color: Color) {

    BLUE("Blue", "Blue", blue_line),
    BROWN("Brn", "Brown", brown_line),
    GREEN("G", "Green", green_line),
    ORANGE("Org", "Orange", orange_line),
    PINK("Pink", "Pink", pink_line),
    PURPLE("P", "Purple", purple_line),
    RED("Red", "Red", red_line),
    YELLOW("Y", "Yellow", yellow_line),
    NA("N/A", "N/A", default_line);

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
