package com.sv.group12_rental_app

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sv.group12_rental_app.databinding.ActivityEditPropertyDetailBinding
import com.sv.group12_rental_app.databinding.ActivityPropertyDetailsBinding
import com.sv.group12_rental_app.models.Rental

class EditPropertyDetailActivity : AppCompatActivity() {
    //setup binding and tags
    lateinit var binding: ActivityEditPropertyDetailBinding
    lateinit var sharedPreferences: SharedPreferences
    lateinit var prefEditor: SharedPreferences.Editor
    private lateinit var datasource: MutableList<Rental>

    val TAG = this@EditPropertyDetailActivity::class.java.canonicalName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivityEditPropertyDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Retrieve position from intent
        val position = intent.getIntExtra("POSITION", -1)

        // Retrieve rental list from sharedPreferences
        sharedPreferences = getSharedPreferences("RENTAL_LISTINGS_DATASOURCE", MODE_PRIVATE)

        // Initialize the prefEditor instance
        prefEditor = sharedPreferences.edit()

        val rentalListingsfromSP = sharedPreferences.getString("RENTAL_LISTINGS_DATASOURCE", "")

        //Convert string back into a Rental List
        if (position != -1 && rentalListingsfromSP != ""){
            val gson = Gson()
            val typeToken = object : TypeToken<List<Rental>>() {}.type
            // convert the string back to a list
            datasource = gson.fromJson<List<Rental>>(rentalListingsfromSP, typeToken).toMutableList()

            //Get the selected Rental object from datasource
            val selectedRental: Rental = datasource[position]

            //Populate the screen with the selectedRental object properties
            populateUI(selectedRental)

            //Click Handler for update property button - id updateBtn
            binding.updateBtn.setOnClickListener{
                // Get values from the inputs, including availabilityCheckBox
                val updatedPropertyType = binding.propertyTypeEditText.text.toString()
                val updatedPropertyAddress = binding.propertyAddressEditText.text.toString()
                val updatedNumberOfBedrooms = binding.numberofBedroomsEditText.text.toString().toInt()
                val updatedNumberOfBathrooms = binding.numberofBathroomsEditText.text.toString().toInt()
                val updatedNumberOfKitchens = binding.numberofKitchensEditText.text.toString().toInt()
                val updatedPropertyDescription = binding.propertyDescriptionEditText.text.toString()
                val updatedPrice = binding.priceEditText.text.toString().toDoubleOrNull() ?: 0.0
                val updatedLandlordName = binding.landlordNameEditText.text.toString()
                val updatedLandlordEmail = binding.landlordEmailEditText.text.toString()
                val updatedLandlordPhoneNumber = binding.landlordPhoneNumberEditText.text.toString()
                val updatedAvailability = binding.availabilityCheckBox.isChecked
                val updatedAvailableForRental = if (updatedAvailability) true else false

                //update the selected Rental object in the sharedPreferences list
                selectedRental.propertyType = updatedPropertyType
                selectedRental.propertyAddress = updatedPropertyAddress
                selectedRental.numberOfBedrooms = updatedNumberOfBedrooms
                selectedRental.numberOfBathrooms = updatedNumberOfBathrooms
                selectedRental.numberOfKitchens = updatedNumberOfKitchens
                selectedRental.description = updatedPropertyDescription
                selectedRental.price = updatedPrice
                selectedRental.landlordName = updatedLandlordName
                selectedRental.landlordEmail = updatedLandlordEmail
                selectedRental.landlordPhoneNumber = updatedLandlordPhoneNumber
                selectedRental.availableForRent = updatedAvailableForRental

                //Save the updated rental back to SharedPreferences
                val rentalListAsString = gson.toJson(datasource)
                prefEditor.putString("RENTAL_LISTINGS_DATASOURCE", rentalListAsString)
                prefEditor.apply()
                //Commit the changes made to Shared Preferences
                Snackbar.make(binding.propertyDetailsParentLayout,"Listing updated", Snackbar.LENGTH_LONG ).show()
                finish()

            }

            //Click Handler for remove property button - id removeBtn
            binding.removeBtn.setOnClickListener{
                //Delete property from the sharedPreferences list
                datasource.removeAt(position)

                //Save the updated rental back to SharedPreferences
                val rentalListAsString = gson.toJson(datasource)
                prefEditor.putString("RENTAL_LISTINGS_DATASOURCE", rentalListAsString)
                prefEditor.apply()

                //Show snackbar to user
                Snackbar.make(binding.propertyDetailsParentLayout,"Listing updated", Snackbar.LENGTH_LONG ).show()
                finish()

                //Commit the changes made to Shared Preferences
                Snackbar.make(binding.propertyDetailsParentLayout,"Listing removed", Snackbar.LENGTH_LONG ).show()

                //Dismiss activity
                finish()

            }

        }






        setSupportActionBar(this.binding.menuToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
    } //end of onCreate




    private fun populateUI(rental: Rental){
        Log.d(TAG,  "rental object values: $rental")
        //Populate UI elements with values from the selectedRental object
        binding.propertyTypeEditText.setText(rental.propertyType)
        binding.propertyAddressEditText.setText(rental.propertyAddress)
        binding.numberofBedroomsEditText.setText(rental.numberOfBedrooms.toString())
        binding.numberofBathroomsEditText.setText(rental.numberOfBathrooms.toString())
        binding.numberofKitchensEditText.setText(rental.numberOfKitchens.toString())
        binding.propertyDescriptionEditText.setText(rental.description)
        binding.priceEditText.setText(rental.price.toString())
        binding.landlordNameEditText.setText(rental.landlordName)
        binding.landlordEmailEditText.setText(rental.landlordEmail)
        binding.landlordPhoneNumberEditText.setText(rental.landlordPhoneNumber)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.oprion_landlord_menu_items, menu)
        return true

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.propertyList -> {
                startActivity(Intent(this, ViewAddedListingsActivity::class.java))
                true
            }

            R.id.menu_logout -> {
                startActivity(Intent(this, MainActivity::class.java))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}