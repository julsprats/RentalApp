package com.sv.group12_rental_app

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.sv.group12_rental_app.databinding.ActivityFavListScreenBinding
import com.sv.group12_rental_app.databinding.ActivityPropertyDetailsBinding
import com.sv.group12_rental_app.models.Rental

class PropertyDetailsActivity : AppCompatActivity() {
    private val TAG = "PropertyDetailsActivity"
    // Firestore
    private val db = FirebaseFirestore.getInstance()
    lateinit var binding: ActivityPropertyDetailsBinding

    private var userEmail: String? = null
    private var userName: String? = null
    private var landlordPhoneNumber: String? = null

    // List of favorite rentals
    private var favoriteRentals: MutableList<Rental> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPropertyDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve user email and name from intent
        userEmail = intent.getStringExtra("userEmail")
        userName = intent.getStringExtra("userName")

        setSupportActionBar(this.binding.menuToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        // Get the selected property ID from the intent
        val selectedPropertyId = intent.getStringExtra("SELECTED_PROPERTY_ID")

        // Load favorite rentals from Firestore and populate UI
        loadFavoriteRentalsFromFirestore {
            // Find the selected rental in favoriteRentals
            val selectedRental = favoriteRentals.find { it.id == selectedPropertyId }

            // If the selected rental is found, populate the UI
            selectedRental?.let { rental ->
                populateUI(rental)

                // Add a button click listener to initiate a phone call
                binding.callbtn.setOnClickListener {
                    if (!rental.landlordPhoneNumber.isNullOrBlank()) {
                        initiatePhoneCall(rental.landlordPhoneNumber)
                    }
                }

                binding.emailbtn.setOnClickListener {
                    if (!rental.landlordEmail.isNullOrBlank()) {
                        sendEmail(rental.landlordEmail)
                    }
                }
            }
        }
    }

    private fun loadFavoriteRentalsFromFirestore(onFavoritesLoaded: () -> Unit) {
        // Check if userEmail is null or empty
        if (userEmail.isNullOrBlank()) {
            // Handle the case where userEmail is not available
            Log.d(TAG, "userEmail is null")
            return
        }

        // Reference to the user's document in Firestore
        val userDocumentReference = db.collection("Users").document(userEmail ?: "")
        val favoritesCollectionReference = userDocumentReference.collection("favourites")

        // Fetch rental properties based on favorite addresses
        favoritesCollectionReference.get()
            .addOnSuccessListener { querySnapshot ->
                // Clear existing favorites
                favoriteRentals.clear()

                // Convert Firestore documents to Rental objects
                for (document in querySnapshot.documents) {
                    val rental = document.toObject(Rental::class.java)
                    rental?.let {
                        favoriteRentals.add(it)
                    }
                }
                onFavoritesLoaded.invoke()
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching rental properties: $exception")
            }
    }

    private fun populateUI(rental: Rental) {
        findViewById<TextView>(R.id.tvPropertyType).text = rental.propertyType
        findViewById<TextView>(R.id.tvOwnerName).text = "Landlord Details: "
        findViewById<TextView>(R.id.tvContactDetails).text = "Contact: ${rental.landlordEmail}, ${rental.landlordPhoneNumber}"
        findViewById<TextView>(R.id.tvBedBath).text = "Bedrooms: ${rental.numberOfBedrooms}, Bathrooms: ${rental.numberOfBathrooms}"
        findViewById<TextView>(R.id.tvKitchen).text = "Kitchen: ${if (rental.numberOfKitchens > 0) "Yes" else "No"}"
        findViewById<TextView>(R.id.tvDescription).text = "Description: ${rental.description}"
        findViewById<TextView>(R.id.tvPropertyAddress).text = "${rental.propertyAddress}"
        findViewById<TextView>(R.id.tvAvailability).text = "Available for Rent: ${if (rental.availableForRent) "Yes" else "No"}"
        findViewById<TextView>(R.id.tvPrice).text = "$${rental.price}"
        findViewById<ImageView>(R.id.ivRental).setImageResource(getImageResourceByFilename(rental.imageFilename))
    }

    // get the resource ID based on the image filename
    private fun getImageResourceByFilename(filename: String): Int {
        return when (filename) {
            "house" -> R.drawable.house
            "apartment" -> R.drawable.apartment
            "condo" -> R.drawable.condo
            "basement" -> R.drawable.basement
            else -> R.drawable.ic_launcher_background
        }
    }

    //phone call to landlord
    private fun initiatePhoneCall(phoneNumber: String) {
        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
        startActivity(dialIntent)
    }

    //email to landlord
    private fun sendEmail(email: String) {
        val snackbar = Snackbar.make(binding.root, "Sending email to: $email", Snackbar.LENGTH_LONG)
        snackbar.show()
    }
}