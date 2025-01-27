package com.sv.weather_vaishnavi.api

import com.sv.weather_vaishnavi.models.Weather
import retrofit2.http.GET
import retrofit2.http.Path

interface MyInterface {
    // ENDPOINT: https://jsonplaceholder.typicode.com/users/4
    @GET("timeline/{latitude},{longitude}?unitGroup=us&key=NVE8LZBWVEL2LWHBAGXV984FS&contentType=json")
    suspend fun getWeather(
        @Path("latitude") latitude: Double,
        @Path("longitude") longitude: Double
    ): Weather

}