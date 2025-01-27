package com.sv.weather_vaishnavi.models

data class Weather (
    val queryCost: Long,
    val latitude: Double,
    val longitude: Double,
    val resolvedAddress: String,
    val address: String,
    val timezone: String,
    val tzoffset: Long,
    val days: List<Day>,
    val currentConditions: CurrentConditions,
)