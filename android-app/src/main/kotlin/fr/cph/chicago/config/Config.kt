package fr.cph.chicago.config

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.concurrent.TimeUnit
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber

val objectMapper: ObjectMapper by lazy {
    jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
}

private val connectionPool by lazy {
    ConnectionPool(5, 10000, TimeUnit.MILLISECONDS)
}

private val loggingInterceptor by lazy {
    HttpLoggingInterceptor { message ->
        Timber.tag("OkHttp").d(message)
    }
        .setLevel(HttpLoggingInterceptor.Level.BODY)
}

val httpClient by lazy {
    OkHttpClient.Builder()
        .readTimeout(5, TimeUnit.SECONDS)
        .connectTimeout(5, TimeUnit.SECONDS)
        .connectionPool(connectionPool)
        .addInterceptor(loggingInterceptor)
        .build()
}
