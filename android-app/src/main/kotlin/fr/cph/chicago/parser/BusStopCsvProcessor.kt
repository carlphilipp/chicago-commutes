package fr.cph.chicago.parser

import com.univocity.parsers.common.CommonSettings
import com.univocity.parsers.common.Format
import com.univocity.parsers.common.ParsingContext
import com.univocity.parsers.common.processor.RowProcessor
import fr.cph.chicago.entity.BusStop
import fr.cph.chicago.entity.Position
import fr.cph.chicago.repository.BusStopRepository
import org.apache.commons.lang3.StringUtils
import java.util.*

internal class BusStopCsvProcessor : RowProcessor {

    private val repository = BusStopRepository

    private val rows: MutableList<BusStop> = ArrayList(12000)

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
        repository.saveBuses(rows)
    }
}
