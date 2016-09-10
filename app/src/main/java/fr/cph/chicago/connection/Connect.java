package fr.cph.chicago.connection;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import fr.cph.chicago.exception.ConnectException;

public enum Connect {
    INSTANCE;

    private static final String TAG = DivvyConnect.class.getSimpleName();

    /**
     * Generic connect URL.
     *
     * @param address
     * @return
     * @throws ConnectException
     */
    @NonNull
    public final InputStream connect(@NonNull final String address) throws ConnectException {
        final InputStream inputStream;
        try {
            Log.v(TAG, "Address: " + address);
            final URL url = new URL(address);
            final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);
            inputStream = new BufferedInputStream(urlConnection.getInputStream());
        } catch (final IOException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new ConnectException(ConnectException.ERROR, e);
        }
        return inputStream;
    }
}
