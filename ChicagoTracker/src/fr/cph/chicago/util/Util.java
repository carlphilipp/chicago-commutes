package fr.cph.chicago.util;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import fr.cph.chicago.ChicagoTracker;

public class Util {
	private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

	/**
	 * Generate a value suitable for use in {@link #setId(int)}. This value will not collide with ID values generated at build time by aapt for R.id.
	 * 
	 * @return a generated ID value
	 */
	public static int generateViewId() {
		for (;;) {
			final int result = sNextGeneratedId.get();
			// aapt-generated IDs have the high byte nonzero; clamp to the range under that.
			int newValue = result + 1;
			if (newValue > 0x00FFFFFF)
				newValue = 1; // Roll over to 1, not 0.
			if (sNextGeneratedId.compareAndSet(result, newValue)) {
				return result;
			}
		}
	}

	public static String getProperty(String property){
		Properties prop = new Properties();
		try {
			prop.load(ChicagoTracker.getAppContext().getAssets().open("app.properties"));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return prop.getProperty(property, null);
	}
}
