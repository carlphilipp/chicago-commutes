package fr.cph.chicago.update;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Download the last cta train stops
 *
 * @author cpharmant
 */
public class UpdateTrainStops {

    public static void main(String[] args) {
        Stream.of(args).forEach(System.out::println);
        new UpdateTrainStops(args[0], args[1]).run();
    }

    private String tempDirectory;
    private String destinationDirectory;

    private UpdateTrainStops(final String tempDirectory, final String destinationDirectory) {
        this.tempDirectory = tempDirectory;
        this.destinationDirectory = destinationDirectory;
    }

    private void run() {
        downloadFile().ifPresent(this::moveFile);
    }

    private Optional<File> downloadFile() {
        try {
            final URL url = new URL("https://data.cityofchicago.org/api/views/8pix-ypme/rows.csv?accessType=DOWNLOAD");
            final File file = new File(tempDirectory + "rows.csv");
            System.out.println("Start downloading file...");
            FileUtils.copyInputStreamToFile(url.openStream(), file);
            System.out.println("Done downloading file! " + file.getAbsolutePath());
            return Optional.ofNullable(file);
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private void moveFile(final File file) {
        try {
            final String stopFileNameDest = "train_stops.csv";
            System.out.println("Move file to " + destinationDirectory + stopFileNameDest);
            FileUtils.copyFile(file, new File(destinationDirectory + stopFileNameDest));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
