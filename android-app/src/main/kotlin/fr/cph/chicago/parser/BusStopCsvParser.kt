package fr.cph.chicago.parser

import android.util.Log
import com.univocity.parsers.csv.CsvParser
import com.univocity.parsers.csv.CsvParserSettings
import fr.cph.chicago.core.App
import org.apache.commons.io.IOUtils
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
            IOUtils.closeQuietly(inputStreamReader)
        }
    }
}
