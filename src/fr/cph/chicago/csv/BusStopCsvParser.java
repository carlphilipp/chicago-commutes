package fr.cph.chicago.csv;

import android.support.annotation.NonNull;
import android.util.Log;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import fr.cph.chicago.app.App;
import fr.cph.chicago.entity.BusStop;

public class BusStopCsvParser {

	private static final String TAG = BusStopCsvParser.class.getSimpleName();

	private static final String STOP_FILE_PATH = "stops.txt";

	private final CsvParser parser;
	private final BusStopCsvProcessor rowProcessor;

	public BusStopCsvParser() {
		final CsvParserSettings settings = new CsvParserSettings();
		settings.getFormat().setLineSeparator("\n");
		settings.setHeaderExtractionEnabled(true);
		this.rowProcessor = new BusStopCsvProcessor();
		settings.setRowProcessor(rowProcessor);
		this.parser = new CsvParser(settings);
	}

    @NonNull
	public List<BusStop> parse() {
		try {
			parser.parse(new InputStreamReader(App.getContext().getAssets().open(STOP_FILE_PATH)));
		} catch (final IOException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		return rowProcessor.getRows();
	}
}
