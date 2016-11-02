package fr.cph.chicago.update;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Download the last google_transit archive for Chicago and update bus_stops.txt used in
 * the android app.
 *
 * @author cpharmant
 */
public class UpdateBusStops {

    public static void main(String[] args) {
        Stream.of(args).forEach(System.out::println);
        new UpdateBusStops(args[0], args[1]).run();
    }

    private static String STOP_FILE_NAME = "stops.txt";

    private String tempDirectory;
    private String destinationDirectory;

    private UpdateBusStops(final String tempDirectory, final String destinationDirectory) {
        this.tempDirectory = tempDirectory;
        this.destinationDirectory = destinationDirectory;
    }

    private void run() {
        final Optional<File> fileOptional = downloadFile();
        fileOptional.ifPresent(downloadedFile ->
            extractFile(downloadedFile).ifPresent(this::moveFile)
        );
    }

    private Optional<File> downloadFile() {
        try {
            final URL url = new URL("http://www.transitchicago.com/downloads/sch_data/google_transit.zip");
            final File file = new File(tempDirectory + "google_transit.zip");
            System.out.println("Start downloading file...");
            FileUtils.copyInputStreamToFile(url.openStream(), file);
            System.out.println("Done downloading file! " + file.getAbsolutePath());
            return Optional.ofNullable(file);
        } catch (final IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private Optional<File> extractFile(File file) {
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

    private void moveFile(final File file) {
        try {
            final String stopFileNameDest = "bus_stops.txt";
            System.out.println("Move file to " + destinationDirectory + stopFileNameDest);
            FileUtils.copyFile(file, new File(destinationDirectory + stopFileNameDest));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
