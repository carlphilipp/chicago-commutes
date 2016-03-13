package fr.cph.chicago.task;

import android.os.AsyncTask;

import fr.cph.chicago.activity.BaseActivity;
import fr.cph.chicago.data.BusData;
import fr.cph.chicago.data.DataHolder;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.exception.ParserException;

/**
 * Load Bus and train data into DataHolder. The data are load in a sequence mode. It means that if one of
 * the url contacted does not response, we will still process the other data, and won't throw any
 * exception
 *
 * @author Carl-Philipp Harmant
 */
public class LoadLocalDataTask extends AsyncTask<Void, String, Void> {

    private BaseActivity activity;
    private BusData busData;
    private TrainData trainData;

    public LoadLocalDataTask(final BaseActivity activity) {
        this.activity = activity;
    }

    @Override
    protected final Void doInBackground(final Void... params) {
        // Load local CSV
        trainData = TrainData.getInstance();
        trainData.read();

        busData = BusData.getInstance();
        busData.readBusStops();
        return null;
    }

    @Override
    protected final void onPostExecute(final Void result) {
        // Put data into data holder
        final DataHolder dataHolder = DataHolder.getInstance();
        dataHolder.setBusData(busData);
        dataHolder.setTrainData(trainData);
        try {
            // Load favorites data
            activity.loadFavorites();
        } catch (final ParserException e) {
            activity.displayError(e);
        }
    }
}
