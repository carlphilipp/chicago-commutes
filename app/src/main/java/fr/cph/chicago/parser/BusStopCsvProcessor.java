package fr.cph.chicago.parser;

import android.support.annotation.NonNull;

import com.univocity.parsers.common.CommonSettings;
import com.univocity.parsers.common.Format;
import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.processor.RowProcessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.cph.chicago.entity.BusStop;
import fr.cph.chicago.entity.Position;

class BusStopCsvProcessor implements RowProcessor {

	private List<BusStop> rows;

	@Override
	public void processStarted(final ParsingContext context) {
		rows = new ArrayList<>(11232);
	}

	/**
	 * Stores the row extracted by the parser into a list.
	 *
	 * @param row     the data extracted by the parser for an individual record. Note that:
	 *                <ul>
	 *                <li>it will never by null. </li>
	 *                <li>it will never be empty unless explicitly configured using {@link CommonSettings#setSkipEmptyLines(boolean)}</li>
	 *                <li>it won't contain lines identified by the parser as comments. To disable comment processing set {@link Format#setComment(char)} to '\0'</li>
	 *                </ul>
	 * @param context A contextual object with information and controls over the current state of the parsing process
	 */
	@Override
	public void rowProcessed(final String[] row, final ParsingContext context) {
		final int stopId = Integer.parseInt(row[0]); // stop_id
		// String stopCode = TrainDirection.fromString(row[1]); // stop_code
		final String stopName = row[2]; // stop_name
		// String stopDesc = row[3]; // stop_desc

		final double latitude = Double.parseDouble(row[4]);// stop_lat
		final double longitude = Double.parseDouble(row[5]);// stop_lon

		final BusStop busStop = new BusStop();
		busStop.setId(stopId);
		busStop.setName(stopName);
		final Position position = new Position();
		position.setLatitude(latitude);
		position.setLongitude(longitude);
		busStop.setPosition(position);

		rows.add(busStop);
	}

	@Override
	public void processEnded(final ParsingContext context) {
	}

    @NonNull
	List<BusStop> getRows() {
		return rows == null ? Collections.emptyList() : rows;
	}
}
