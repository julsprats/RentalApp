package com.sv.group12_rental_app.models

import java.io.Serializable

class Rental(
    var id: String? = null , // Nullable to handle cases where the ID is not set
    var propertyType: String = "", //"Condo", "Basement", "House", "Apartment"
    var landlordName: String = "",
    //from the UI (if the contact info is different the login)
    var landlordEmail: String = "",
    var landlordPhoneNumber: String ="",
    var numberOfBedrooms: Int = 0,
    var numberOfKitchens: Int = 0,
    var numberOfBathrooms: Int = 0,
    var description: String = "",
    var propertyAddress: String = "",
    var availableForRent: Boolean = true,
    var price: Double = 0.0,
    var imageFilename: String = "",
    var lat: Double = 0.0,
    var long: Double = 0.0,
    //from the DB (tied to user acc and auth)
    var userEmail: String = ""
) {


    // Debug
    override fun toString(): String {
        return ("\nRental Property Details" +
                "\nid = $id"+
                "\npropertyType = $propertyType" +
                "\nownerName = $landlordName" +
                "\nownerEmail= $landlordEmail" +
                "\nownerPhoneNumber = $landlordPhoneNumber" +
                "\nnumberOfBedrooms = $numberOfBedrooms" +
                "\nnumberOfKitchens = $numberOfKitchens" +
                "\nnumberOfBathrooms = $numberOfBathrooms" +
                "\ndescription = $description" +
                "\npropertyAddress = $propertyAddress" +
                "\navailableForRent = $availableForRent" +
                "\nprice = $price" +
                "\nimageFilename = $imageFilename" +
                "\n lat = $lat" +
                "\n long = $long"+
                "\n userEmail = $userEmail"
                )
    }
}