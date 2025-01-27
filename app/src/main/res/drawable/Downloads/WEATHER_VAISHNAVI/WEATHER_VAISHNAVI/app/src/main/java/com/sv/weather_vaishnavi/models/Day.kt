package com.sv.weather_vaishnavi.models

data class Day(
    val datetime: String,
    val temp: Double,
    val humidity: Double,
    val conditions: String,
)