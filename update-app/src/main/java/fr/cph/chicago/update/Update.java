package fr.cph.chicago.update;

import com.annimon.stream.Optional;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Abstract update
 *
 * @author cpharmant
 */
abstract class Update {
    String tempDirectory;
    private String destinationDirectory;

    Update(final String tempDirectory, final String destinationDirectory) {
        this.tempDirectory = tempDirectory;
        this.destinationDirectory = destinationDirectory;
    }

    protected abstract void run();

    Optional<File> downloadFile(final String urlString, final String destination) {
        try {
            final URL url = new URL(urlString);
            final File file = new File(destination);
            System.out.println("Start downloading file...");
            FileUtils.copyInputStreamToFile(url.openStream(), file);
            System.out.println("Done downloading file! " + file.getAbsolutePath());
            return Optional.ofNullable(file);
        } catch (final IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    void moveFile(final File file, final String destination) {
        try {
            System.out.println("Move file to " + destinationDirectory + destination);
            FileUtils.copyFile(file, new File(destinationDirectory + destination));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
