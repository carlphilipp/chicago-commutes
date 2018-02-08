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

package fr.cph.chicago.parser

import com.univocity.parsers.common.CommonSettings
import com.univocity.parsers.common.Format
import com.univocity.parsers.common.ParsingContext
import com.univocity.parsers.common.processor.RowProcessor
import fr.cph.chicago.entity.BusStop
import fr.cph.chicago.entity.Position
import fr.cph.chicago.service.BusService
import org.apache.commons.lang3.StringUtils

internal class BusStopCsvProcessor : RowProcessor {

    private val rows: MutableList<BusStop> = ArrayList(12000)
    private val busService = BusService

    override fun processStarted(context: ParsingContext) {
        if (rows.isNotEmpty()) {
            rows.clear()
        }
    }

    /**
     * Stores the row extracted by the parser into a list.

     * @param row     the data extracted by the parser for an individual record. Note that:
     * *
     * *                 * it will never by null.
     * *                 * it will never be empty unless explicitly configured using [CommonSettings.setSkipEmptyLines]
     * *                 * it won't contain lines identified by the parser as comments. To disable comment processing set [Format.setComment] to '\0'
     * *
     * *
     * @param context A contextual object with information and controls over the current state of the parsing process
     */
    override fun rowProcessed(row: Array<String?>, context: ParsingContext) {
        if (row[1] != null) {
            val latitude = row[4]!!.toDouble() // stop_lat
            val longitude = row[5]!!.toDouble() // stop_lon
            val busStop = BusStop(
                id = row[0]!!.toInt(),
                name = row[2]!!,
                description = if (row[3] == null) StringUtils.EMPTY else row[3]!!,
                position = Position(latitude, longitude)
            )
            rows.add(busStop)
        }
    }

    override fun processEnded(context: ParsingContext) {
        busService.saveBusStops(rows)
    }
}
