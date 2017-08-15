package fr.cph.chicago.parser

import android.content.Context
import android.util.Log

import com.univocity.parsers.csv.CsvParser
import com.univocity.parsers.csv.CsvParserSettings
import fr.cph.chicago.client.CtaClient

import org.apache.commons.io.IOUtils

import java.io.IOException
import java.io.InputStreamReader

class BusStopCsvParser private constructor() {

    private object Holder {
        val INSTANCE = BusStopCsvParser()
    }

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

    companion object {

        private val TAG = BusStopCsvParser::class.java.simpleName

        // http://www.transitchicago.com/developers/gtfs.aspx
        // http://www.transitchicago.com/downloads/sch_data/
        private val STOP_FILE_PATH = "bus_stops.txt"
        val INSTANCE: BusStopCsvParser by lazy { Holder.INSTANCE }
    }
}
