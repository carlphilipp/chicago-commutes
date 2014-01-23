package fr.cph.chicago;

import java.util.Date;

import android.app.Application;
import android.content.Context;

public class ChicagoTracker extends Application {

	private static Context context;
	public static final String PREFERENCE_FAVORITES = "ChicagoTrackerFavoritesTrain";
	private static Date lastTrainUpdate = null;

	public void onCreate() {
		super.onCreate();
		ChicagoTracker.context = getApplicationContext();
	}

	public static Context getAppContext() {
		return ChicagoTracker.context;
	}
	
	public static void modifyLastUpdate(Date date){
		lastTrainUpdate = date;
	}
	
	public static Date getLastTrainUpdate(){
		return lastTrainUpdate;
	}

}
