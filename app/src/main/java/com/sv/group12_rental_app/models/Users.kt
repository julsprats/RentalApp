package com.sv.group12_rental_app.models

class Users (
    var id : String = "",
    val userType: String = "",
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val favourites: List<String>
){

        // Add a no-argument constructor
        constructor() : this("", "", "", "", "", mutableListOf())
}
