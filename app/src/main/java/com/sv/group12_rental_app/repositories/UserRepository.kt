package com.sv.group12_rental_app.repositories

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sv.group12_rental_app.models.Users

class UserRepository(private val context: Context) {
    private val TAG = this.toString()
    private val db = Firebase.firestore

    private val COLLECTION_USERS = "Users"
    private val FIELD_EMAIL = "email"
    private val FIELD_PASSWORD = "password"
    private val FIELD_NAME = "name"
    private val FIELD_USER_TYPE = "userType"
    private val SUBCOLLECTION_FAVOURITES = "favourites"

    fun addUserToDB(newUser: Users) {
        try {
            val data: MutableMap<String, Any> = HashMap()

            data[FIELD_EMAIL] = newUser.email
            data[FIELD_PASSWORD] = newUser.password
            data[FIELD_NAME] = newUser.name
            data[FIELD_USER_TYPE] = newUser.userType

            // Create a new user document in "Users" collection
            val userDocumentReference = db.collection(COLLECTION_USERS).document(newUser.email)
            userDocumentReference.set(data)
                .addOnSuccessListener {
                    Log.d(TAG, "addUserToDB: User document successfully created with ID ${userDocumentReference.id}")

                    // Add the "favourites" subcollection
                    addFavouritesSubcollection(newUser.email, newUser.favourites)
                }
                .addOnFailureListener { ex ->
                    Log.e(TAG, "addUserToDB: Unable to create user document due to exception : $ex")
                }

        } catch (ex: Exception) {
            Log.e(TAG, "addUserToDB: Couldn't add user document $ex")
        }
    }

    // Function to add "favourites" subcollection
    private fun addFavouritesSubcollection(userEmail: String, favourites: List<String>) {
        try {
            val favoritesCollection = db.collection(COLLECTION_USERS).document(userEmail)
                .collection(SUBCOLLECTION_FAVOURITES)

            // Add each favorite as a document in the "favourites" subcollection
            for (propertyAddress in favourites) {
                val favoriteDocumentReference = favoritesCollection.document(propertyAddress)
                favoriteDocumentReference.set(mapOf("propertyAddress" to propertyAddress))
                    .addOnSuccessListener {
                        Log.d(TAG, "addFavouritesSubcollection: Property added to favorites subcollection: $propertyAddress")
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "addFavouritesSubcollection: Error adding property to favorites subcollection: $exception")
                    }
            }
        } catch (ex: Exception) {
            Log.e(TAG, "addFavouritesSubcollection: Exception occurred: $ex")
        }
    }

    fun updateUserProfile(userToUpdate : Users){
        try{
            val data : MutableMap<String, Any> = HashMap()

            data[FIELD_PASSWORD] = userToUpdate.password
            data[SUBCOLLECTION_FAVOURITES] = userToUpdate.favourites
            data[FIELD_NAME] = userToUpdate.name

            db.collection(COLLECTION_USERS)
                .document(userToUpdate.email)
                .update(data)
                .addOnSuccessListener { docRef ->
                    Log.d(TAG, "updateUserProfile: User document successfully updated $docRef")
                }
                .addOnFailureListener { ex ->
                    Log.e(TAG, "updateUserProfile: Unable to update user document due to exception : $ex", )
                }

        }catch (ex : Exception){
            Log.e(TAG, "updateUserProfile: Couldn't update user document $ex", )
        }
    }

    fun updateUserFavoritesInFirestore(updatedFavorites: List<String>) {
        try {
            val db = FirebaseFirestore.getInstance()
            val userEmail = FirebaseAuth.getInstance().currentUser?.email

            if (userEmail != null) {
                val userDocumentReference = db.collection("Users").document(userEmail)

                // Update the favorites array in Firestore
                userDocumentReference.update("favourites", updatedFavorites)
                    .addOnSuccessListener {
                        Log.d(TAG, "updateUserFavoritesInFirestore: User favorites updated in Firestore")
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "updateUserFavoritesInFirestore: Error updating user favorites in Firestore: $exception")
                    }
            } else {
                Log.e(TAG, "updateUserFavoritesInFirestore: User email is null")
            }
        } catch (ex: Exception) {
            Log.e(TAG, "updateUserFavoritesInFirestore: Exception occurred: $ex")
        }
    }
}