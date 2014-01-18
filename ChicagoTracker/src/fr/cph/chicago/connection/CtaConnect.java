package fr.cph.chicago.connection;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.collections4.MultiMap;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import android.util.Log;
import fr.cph.chicago.util.Util;

public class CtaConnect {

	private static final String TAG = "CtaConnect";

	private static CtaConnect instance = null;

	private static final String BASE_URL_TRAIN_ARRIVALS = "http://lapi.transitchicago.com/api/1.0/ttarrivals.aspx";
	private static final String BASE_URL_TRAIN_FOLLOW = "http://lapi.transitchicago.com/api/1.0/ttfollow.aspx";
	private static final String BASE_URL_TRAIN_LOCATION = "http://lapi.transitchicago.com/api/1.0/ttpositions.aspx";
	

	private static final String BASE_URL_BUS_ROUTES = "http://www.ctabustracker.com/bustime/api/v1/getroutes";
	private static final String BASE_URL_BUS_DIRECTION = "http://www.ctabustracker.com/bustime/api/v1/getdirections";
	private static final String BASE_URL_BUS_STOPS = "http://www.ctabustracker.com/bustime/api/v1/getstops";
	private static final String BASE_URL_BUS_ARRIVAL = "http://www.ctabustracker.com/bustime/api/v1/getpredictions";
	
	private String CTA_BUS_KEY;
	private String CTA_TRAIN_KEY;

	private DefaultHttpClient client;

	private CtaConnect() {
		this.client = new DefaultHttpClient();
		CTA_TRAIN_KEY = Util.getProperty("cta.train.key");
		CTA_BUS_KEY = Util.getProperty("cta.bus.key");
	}

	public static CtaConnect getInstance() {
		if (instance == null) {
			instance = new CtaConnect();
		}
		return instance;
	}

	private String connectUrl(String adress) throws IOException {
		String toreturn = null;
		client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);
		client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 60000);
		Log.v(TAG, "adress: " + adress);
		HttpGet get = new HttpGet(adress);
		HttpResponse getResponse = client.execute(get);
		HttpEntity responseEntity = getResponse.getEntity();

		Charset charset = Charset.forName("UTF8");
		InputStreamReader in = new InputStreamReader(responseEntity.getContent(), charset);
		int c = in.read();
		StringBuilder build = new StringBuilder();
		while (c != -1) {
			build.append((char) c);
			c = in.read();
		}
		toreturn = build.toString();
		return toreturn;
	}

	public String connect(CtaRequestType requestType, MultiMap<String, String> params) throws IOException {
		StringBuilder adress = null;
		switch (requestType) {
		case TRAIN_ARRIVALS:
			adress = new StringBuilder(BASE_URL_TRAIN_ARRIVALS + "?key=" + CTA_TRAIN_KEY);
			break;
		case TRAIN_FOLLOW:
			adress = new StringBuilder(BASE_URL_TRAIN_FOLLOW + "?key=" + CTA_TRAIN_KEY);
			break;
		case TRAIN_LOCATION:
			adress = new StringBuilder(BASE_URL_TRAIN_LOCATION + "?key=" + CTA_TRAIN_KEY);
			break;
		case BUS_ROUTES:
			adress = new StringBuilder(BASE_URL_BUS_ROUTES + "?key=" + CTA_BUS_KEY);
			break;
		case BUS_DIRECTION:
			adress = new StringBuilder(BASE_URL_BUS_DIRECTION + "?key=" + CTA_BUS_KEY);
			break;
		case BUS_STOP_LIST:
			adress = new StringBuilder(BASE_URL_BUS_STOPS + "?key=" + CTA_BUS_KEY);
			break;
		case BUS_ARRIVALS:
			adress = new StringBuilder(BASE_URL_BUS_ARRIVAL + "?key=" + CTA_BUS_KEY);
			break;

		}
		for (Entry<String, Object> entry : params.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (value instanceof String) {
				adress.append("&" + key + "=" + value);
			} else if (value instanceof List) {
				@SuppressWarnings("unchecked")
				List<String> list = (List<String>) value;
				for (String l : list) {
					adress.append("&" + key + "=" + l);
				}
			}
		}
		String xml = connectUrl(adress.toString());
		Log.v(TAG, "Result: " + xml);
		return xml;
	}
}
