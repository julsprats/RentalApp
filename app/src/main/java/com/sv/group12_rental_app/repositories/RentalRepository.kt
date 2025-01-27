package com.sv.group12_rental_app.repositories

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.firestore
import com.sv.group12_rental_app.models.Rental

class RentalRepository (private val context : Context) {
    private val TAG = this.toString();

    //get an instance of firestore database
    private val db = Firebase.firestore

    private val COLLECTION_RENTALS = "TestSearch";
    private val FIELD_PROPERTYTYPE = "propertyType";
    private val FIELD_CHECK_LANDLORD_NAME = "landlordName";
    private val FIELD_LANDLORD_EMAIL = "landlordEmail"
    private val FIELD_PHONE_NUMBER = "landlordPhoneNumber";
    private val FIELD_NUM_OF_BED_ROOMS = "numberOfBedrooms";
    private val FIELD_NUM_OF_KITCHENS = "numberOfKitchens";
    private val FIELD_NUM_OF_BATH_ROOMS = "numberOfBathrooms";
    private val FIELD_PROPERTY_ADDRESS = "propertyAddress"
    private val FIELD_PRICE = "price";
    private val FIELD_IMAGE_FILE = "imageFilename";
    private val FIELD_LAT = "lat"
    private val FIELD_LUG = "lug";
    private val FIELD_USER_EMAIL = "userEmail";
    var allExpenses: MutableLiveData<List<Rental>> = MutableLiveData<List<Rental>>()

    fun retrieveAllExpenses(){
        db.collection(COLLECTION_RENTALS)
            .addSnapshotListener(EventListener{ result, error ->
                if (error != null){
                    Log.e(TAG,
                        "retrieveAllExpenses: Listening to Expenses collection failed due to error : $error", )
                    return@EventListener
                }

                if (result != null){
                    Log.d(TAG, "retrieveAllExpenses: Number of documents retrieved : ${result.size()}")

                    val tempList : MutableList<Rental> = ArrayList<Rental>()

                    for (docChanges in result.documentChanges){

                        val currentDocument : Rental = docChanges.document.toObject(Rental::class.java)
                        Log.d(TAG, "retrieveAllExpenses: currentDocument : $currentDocument")

                        when(docChanges.type){
                            DocumentChange.Type.ADDED -> {
                                //do necessary changes to your local list of objects
                                tempList.add(currentDocument)
                            }
                            DocumentChange.Type.MODIFIED -> {

                            }
                            DocumentChange.Type.REMOVED -> {

                            }
                        }
                    }//for
                    Log.d(TAG, "retrieveAllExpenses: tempList : $tempList")
                    //replace the value in allExpenses

                    allExpenses.postValue(tempList)

                }else{
                    Log.d(TAG, "retrieveAllExpenses: No data in the result after retrieving")
                }
            })
    }
}