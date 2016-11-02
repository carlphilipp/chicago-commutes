package fr.cph.chicago.update;

/**
 * Download the last cta train stops
 *
 * @author cpharmant
 */
public class UpdateTrainStops extends Update {

    public static void main(String[] args) {
        new UpdateTrainStops(args[0], args[1]).run();
    }

    private UpdateTrainStops(final String tempDirectory, final String destinationDirectory) {
        super(tempDirectory, destinationDirectory);
    }

    @Override
    protected void run() {
        String url = "https://data.cityofchicago.org/api/views/8pix-ypme/rows.csv?accessType=DOWNLOAD";
        downloadFile(url, tempDirectory + "rows.csv").ifPresent(file -> moveFile(file, "train_stops.csv"));
    }
}
