package fr.cph.chicago.update;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author cpharmant
 */
public class UpdateStops {
    public static void main(String[] args) {
        System.out.println("hello world!");
        new UpdateStops().run();
    }

    private static String STOP_FILE_NAME = "stops.txt";
    private static String WORK_DIRECTORY = "src/test/";

    public void run() {
        // Download archive
        Optional<File> fileOptional = downloadFile();
        fileOptional.ifPresent(downloadedFile ->
            extractFile(downloadedFile).ifPresent(stopFile -> {
                moveFile(stopFile);
                cleanWorkingDirectory();
            })
        );
    }

    private Optional<File> downloadFile() {
        try {
            URL url = new URL("http://www.transitchicago.com/downloads/sch_data/google_transit.zip");
            File file = new File(WORK_DIRECTORY + "google_transit.zip");
            System.out.println("Start downloading file...");
            FileUtils.copyInputStreamToFile(url.openStream(), file);
            System.out.println("Done downloading file! " + file.getAbsolutePath());
            return Optional.ofNullable(file);
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private Optional<File> extractFile(File file) {
        ZipFile zf = null;
        try {
            zf = new ZipFile(file);
            List<? extends ZipEntry> derp = Collections.list(zf.entries());
            Optional<? extends ZipEntry> entry = Stream.of(derp)
                .filter(value -> value.getName().equals(STOP_FILE_NAME))
                .findFirst();
            if (entry.isPresent()) {
                ZipEntry zipEntry = entry.get();
                File stops = new File(WORK_DIRECTORY + STOP_FILE_NAME);
                FileUtils.copyInputStreamToFile(zf.getInputStream(zipEntry), stops);
                return Optional.ofNullable(stops);
            } else {
                return Optional.empty();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (zf != null) {
                try {
                    zf.close();
                } catch (IOException e) {
                }
            }
        }
        return Optional.empty();
    }

    private void moveFile(File file) {
        try {
            System.out.print("Move file");
            FileUtils.copyFile(file, new File("src/main/assets/stops.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cleanWorkingDirectory() {
        if (!new File(WORK_DIRECTORY + "google_transit.zip").delete()) {
            System.out.println("Could not delete " + WORK_DIRECTORY + "google_transit.zip");
        } else {
            System.out.println("Delete " + WORK_DIRECTORY + "google_transit.zip");
        }
        if (!new File(WORK_DIRECTORY + STOP_FILE_NAME).delete()) {
            System.out.println("Could not delete " + WORK_DIRECTORY + STOP_FILE_NAME);
        } else {
            System.out.println("Delete " + WORK_DIRECTORY + STOP_FILE_NAME);
        }
    }
}
