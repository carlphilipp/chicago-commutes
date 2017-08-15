package fr.cph.chicago.parser

import android.content.Context
import android.util.Log
import com.univocity.parsers.csv.CsvParser
import com.univocity.parsers.csv.CsvParserSettings
import org.apache.commons.io.IOUtils
import java.io.IOException
import java.io.InputStreamReader

object BusStopCsvParser {

    private val TAG = BusStopCsvParser::class.java.simpleName
    private val STOP_FILE_PATH = "bus_stops.txt"

    private val parser: CsvParser

    init {
        val settings = CsvParserSettings()
        settings.format.setLineSeparator("\n")
        settings.isHeaderExtractionEnabled = true
        settings.setProcessor(BusStopCsvProcessor())
        this.parser = CsvParser(settings)
    }

    fun parse(context: Context) {
        var inputStreamReader: InputStreamReader? = null
        try {
            inputStreamReader = InputStreamReader(context.assets.open(STOP_FILE_PATH))
            parser.parse(inputStreamReader)
        } catch (e: IOException) {
            Log.e(TAG, e.message, e)
        } finally {
            IOUtils.closeQuietly(inputStreamReader)
        }
    }
}
