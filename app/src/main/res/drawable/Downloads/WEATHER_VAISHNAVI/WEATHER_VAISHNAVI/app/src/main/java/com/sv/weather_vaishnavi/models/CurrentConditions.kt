package com.sv.weather_vaishnavi.models

data class CurrentConditions(
    val datetime: String,
    val temp: Double,
    val humidity: Double,
    val conditions: String,
)