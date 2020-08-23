package fr.cph.chicago.client

import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import timber.log.Timber

private val connectionPool = ConnectionPool(5, 10000, TimeUnit.MILLISECONDS);

private var loggingInterceptor: HttpLoggingInterceptor = HttpLoggingInterceptor(object: HttpLoggingInterceptor.Logger {
    override fun log(message: String) {
        Timber.tag("OkHttp").d(message);
    }
}).setLevel(HttpLoggingInterceptor.Level.BODY)

val httpClient = OkHttpClient.Builder()
    .readTimeout(5, TimeUnit.SECONDS)
    .connectTimeout(5, TimeUnit.SECONDS)
    .connectionPool(connectionPool)
    .addInterceptor(loggingInterceptor)
    .build()
