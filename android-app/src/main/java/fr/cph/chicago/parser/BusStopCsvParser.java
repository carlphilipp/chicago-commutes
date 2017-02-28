package fr.cph.chicago.parser;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStreamReader;

public enum BusStopCsvParser {

    INSTANCE;

    private static final String TAG = BusStopCsvParser.class.getSimpleName();

    // http://www.transitchicago.com/developers/gtfs.aspx
    // http://www.transitchicago.com/downloads/sch_data/
    private static final String STOP_FILE_PATH = "bus_stops.txt";

    private final CsvParser parser;

    BusStopCsvParser() {
        final CsvParserSettings settings = new CsvParserSettings();
        settings.getFormat().setLineSeparator("\n");
        settings.setHeaderExtractionEnabled(true);
        settings.setProcessor(new BusStopCsvProcessor());
        this.parser = new CsvParser(settings);
    }

    public void parse(@NonNull final Context context) {
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(context.getAssets().open(STOP_FILE_PATH));
            parser.parse(inputStreamReader);
        } catch (final IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(inputStreamReader);
        }
    }
}
