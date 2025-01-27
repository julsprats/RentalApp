package com.sv.weather_vaishnavi.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitInstance {
    private const val BASE_URL: String = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/"

    // setup a client with logging
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor(
            HttpLoggingInterceptor.Logger { message ->
                println("LOG-APP: $message")
            }).apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    // used to ensure Moshi annotations work with Kotlin
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // instantiate a Retrofit instance with Moshi as the data converter
    private val retrofit = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .baseUrl(BASE_URL)
        .client(httpClient)
        .build()

    // create a custom interceptor for dynamic URL
    private val dynamicUrlInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        // Add logic here to modify the originalRequest's URL based on dynamic values
        // For example, you can add latitude and longitude as query parameters
        val modifiedUrl = originalRequest.url.newBuilder()
            .addQueryParameter("latitude", "your_latitude_value")
            .addQueryParameter("longitude", "your_longitude_value")
            .build()

        // Create a new request with the modified URL
        val modifiedRequest = originalRequest.newBuilder()
            .url(modifiedUrl)
            .build()

        // Continue the request with the modified URL
        chain.proceed(modifiedRequest)
    }

    // add the dynamicUrlInterceptor to the OkHttpClient
    private val dynamicUrlClient = httpClient.newBuilder()
        .addInterceptor(dynamicUrlInterceptor)
        .build()

    // update this to return an instance of the Retrofit instance associated
    // with your base URL and dynamic URL capabilities
    val retrofitService: MyInterface by lazy {
        retrofit.newBuilder()
            .client(dynamicUrlClient)
            .build()
            .create(MyInterface::class.java)
    }
}
