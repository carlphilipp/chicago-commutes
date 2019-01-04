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

package fr.cph.chicago.parser

import android.util.Log
import com.univocity.parsers.csv.CsvParser
import com.univocity.parsers.csv.CsvParserSettings
import fr.cph.chicago.core.App
import fr.cph.chicago.util.Util
import java.io.IOException
import java.io.InputStreamReader

object BusStopCsvParser {

    private val TAG = BusStopCsvParser::class.java.simpleName
    private const val STOP_FILE_PATH = "bus_stops.txt"

    private val parser: CsvParser

    init {
        val settings = CsvParserSettings()
        settings.format.setLineSeparator("\n")
        settings.isHeaderExtractionEnabled = true
        settings.setProcessor(BusStopCsvProcessor())
        this.parser = CsvParser(settings)
    }

    fun parse() {
        lateinit var inputStreamReader: InputStreamReader
        try {
            inputStreamReader = InputStreamReader(App.instance.assets.open(STOP_FILE_PATH))
            parser.parse(inputStreamReader)
        } catch (e: IOException) {
            Log.e(TAG, e.message, e)
        } finally {
            Util.closeQuietly(inputStreamReader)
        }
    }
}
