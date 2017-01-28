package fr.cph.chicago.update;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Download the last google_transit archive for Chicago and update bus_stops.txt used in
 * the android app.
 *
 * @author cpharmant
 */
public class UpdateBusStops extends Update {

    public static void main(String[] args) {
        new UpdateBusStops(args[0], args[1]).run();
    }

    private static String STOP_FILE_NAME = "stops.txt";
    private static String STOP_FILE_NAME_DESTINATION = "bus_stops.txt";

    private UpdateBusStops(final String tempDirectory, final String destinationDirectory) {
        super(tempDirectory, destinationDirectory);
    }

    @Override
    protected void run() {
        final String url = "http://www.transitchicago.com/downloads/sch_data/google_transit.zip";
        final Optional<File> fileOptional = downloadFile(url, tempDirectory + "google_transit.zip");
        fileOptional.ifPresent(downloadedFile ->
            extractFile(downloadedFile).ifPresent(file -> moveFile(file, STOP_FILE_NAME_DESTINATION))
        );
        parseAndAnalyseData();
    }

    private Optional<File> extractFile(final File file) {
        ZipFile zf = null;
        try {
            zf = new ZipFile(file);
            final Optional<? extends ZipEntry> entry = Stream.of(Collections.list(zf.entries()))
                .filter(value -> value.getName().equals(STOP_FILE_NAME))
                .findFirst();
            if (entry.isPresent()) {
                final ZipEntry zipEntry = entry.get();
                final File stops = new File(tempDirectory + STOP_FILE_NAME);
                FileUtils.copyInputStreamToFile(zf.getInputStream(zipEntry), stops);
                return Optional.ofNullable(stops);
            } else {
                return Optional.empty();
            }
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            if (zf != null) {
                try {
                    zf.close();
                } catch (IOException ignored) {
                }
            }
        }
        return Optional.empty();
    }

    private void parseAndAnalyseData() {
        try {
            final CsvParserSettings settings = new CsvParserSettings();
            settings.getFormat().setLineSeparator("\n");
            settings.setHeaderExtractionEnabled(true);
            settings.setProcessor(new BusStopCsvProcessor());

            CsvParser parser = new CsvParser(settings);
            parser.parse(new InputStreamReader(new FileInputStream(destinationDirectory + STOP_FILE_NAME_DESTINATION)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
