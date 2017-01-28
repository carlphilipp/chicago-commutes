package fr.cph.chicago.update;

import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.processor.RowProcessor;

import java.util.HashMap;
import java.util.Map;

class BusStopCsvProcessor implements RowProcessor {

    private Map<Double, Double> coordinates;

    @Override
    public void processStarted(ParsingContext context) {
        coordinates = new HashMap<>();
    }

    @Override
    public void rowProcessed(String[] row, ParsingContext context) {
        final int stopId = Integer.parseInt(row[0]); // stop_id
        final int stopCode = row[1] == null ? -1 : Integer.parseInt(row[1]); // stop_code
        if (stopCode != -1) {
            final double latitude = Double.parseDouble(row[4]);// stop_lat
            final double longitude = Double.parseDouble(row[5]);// stop_lon

            if (coordinates.containsKey(latitude) && coordinates.get(latitude) == longitude) {
                System.out.println("[warn] Duplicated coordinates please verify stop id [" + stopId + "] [" + latitude + "," + longitude + "] http://www.ctabustracker.com/bustime/api/v1/getpredictions?key=mxx8serAYe2zgZu9BqSyy5NVs&stpid=" + stopId);
            }

            coordinates.put(latitude, longitude);
        } else {
            System.out.println("[info] Will be ignore in app [" + stopId + "] http://www.ctabustracker.com/bustime/api/v1/getpredictions?key=mxx8serAYe2zgZu9BqSyy5NVs&stpid=" + stopId);
        }
    }

    @Override
    public void processEnded(ParsingContext context) {

    }
}
