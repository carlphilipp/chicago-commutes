package fr.cph.chicago.client

import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

val pool = ConnectionPool(5, 10000, TimeUnit.MILLISECONDS);
val okHttpClient = OkHttpClient.Builder()
    .readTimeout(5, TimeUnit.SECONDS)
    .connectTimeout(5, TimeUnit.SECONDS)
    .connectionPool(pool)
    .build()
