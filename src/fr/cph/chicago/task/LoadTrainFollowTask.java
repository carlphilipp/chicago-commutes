package fr.cph.chicago.task;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import fr.cph.chicago.R;
import fr.cph.chicago.activity.TrainMapActivity;
import fr.cph.chicago.adapter.TrainMapSnippetAdapter;
import fr.cph.chicago.connection.CtaConnect;
import fr.cph.chicago.data.TrainData;
import fr.cph.chicago.entity.Eta;
import fr.cph.chicago.entity.Station;
import fr.cph.chicago.exception.ConnectException;
import fr.cph.chicago.exception.ParserException;
import fr.cph.chicago.util.Util;
import fr.cph.chicago.xml.XmlParser;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static fr.cph.chicago.connection.CtaRequestType.TRAIN_FOLLOW;

public class LoadTrainFollowTask extends AsyncTask<String, Void, List<Eta>> {

	private static final String TAG = LoadTrainFollowTask.class.getSimpleName();

	private TrainMapActivity activity;
	private TrainData trainData;
	private View view;
	private boolean loadAll;

	/**
	 * Constructor
	 *
	 * @param view    the view
	 * @param loadAll a boolean to load everything
	 */
	public LoadTrainFollowTask(final TrainMapActivity activity, final View view, final boolean loadAll, final TrainData trainData) {
		this.activity = activity;
		this.trainData = trainData;
		this.view = view;
		this.loadAll = loadAll;
	}

	@Override
	protected final List<Eta> doInBackground(final String... params) {
		final String runNumber = params[0];
		List<Eta> etas = new ArrayList<>();
		try {
			final CtaConnect connect = CtaConnect.getInstance();
			final MultiValuedMap<String, String> connectParam = new ArrayListValuedHashMap<>();
			connectParam.put("runnumber", runNumber);
			final InputStream content = connect.connect(TRAIN_FOLLOW, connectParam);
			final XmlParser xml = XmlParser.getInstance();
			etas = xml.parseTrainsFollow(content, trainData);
		} catch (final ConnectException | ParserException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		Util.trackAction(activity, R.string.analytics_category_req, R.string.analytics_action_get_train, R.string.analytics_action_get_train_follow, 0);
		if (!loadAll && etas.size() > 7) {
			etas = etas.subList(0, 6);

			// Add a fake Eta cell to alert the user about the fact that only a part of the result is displayed
			final Eta eta = new Eta();
			eta.setIsDly(false);
			eta.setIsApp(false);
			final Date currentDate = Calendar.getInstance().getTime();
			eta.setArrivalDepartureDate(currentDate);
			eta.setPredictionDate(currentDate);
			final Station fakeStation = new Station();
			fakeStation.setName("Display all results");
			eta.setStation(fakeStation);
			etas.add(eta);
		}
		return etas;
	}

	@Override
	protected final void onPostExecute(final List<Eta> result) {
		final ListView arrivals = (ListView) view.findViewById(R.id.arrivals);
		final TextView error = (TextView) view.findViewById(R.id.error);
		if (result.size() != 0) {
			final TrainMapSnippetAdapter ada = new TrainMapSnippetAdapter(result);
			arrivals.setAdapter(ada);
			arrivals.setVisibility(ListView.VISIBLE);
			error.setVisibility(TextView.GONE);
		} else {
			arrivals.setVisibility(ListView.GONE);
			error.setVisibility(TextView.VISIBLE);
		}
		activity.refreshInfoWindow();
	}
}