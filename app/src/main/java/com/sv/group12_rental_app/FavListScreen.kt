package com.sv.group12_rental_app

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sv.group12_rental_app.adapter.FavoritesAdapter
import com.sv.group12_rental_app.databinding.ActivityFavListScreenBinding
import com.sv.group12_rental_app.models.Rental

class FavListScreen : AppCompatActivity() {
    private val TAG = "FavListScreen"

    private var userEmail: String? = null
    private var userName: String? = null

    lateinit var binding: ActivityFavListScreenBinding
    lateinit var adapter: FavoritesAdapter

    // List of favorite rentals
    private var favoriteRentals: MutableList<Rental> = mutableListOf()

    // Firestore
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavListScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve user email and name from intent
        userEmail = intent.getStringExtra("userEmail")
        userName = intent.getStringExtra("userName")

        setSupportActionBar(this.binding.menuToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        // Set up adapter
        adapter = FavoritesAdapter(
            favoriteRentals,
            { rentalProperty -> rowClicked(rentalProperty) },
            { position -> removeButtonClicked(position) }
        )

        // Set up rv
        binding.rvItemss.adapter = adapter
        binding.rvItemss.layoutManager = LinearLayoutManager(this)
        binding.rvItemss.addItemDecoration(
            DividerItemDecoration(
                this,
                LinearLayoutManager.VERTICAL
            )
        )

        // Load favorite rentals from Firestore
        loadFavoriteRentalsFromFirestore()
    }

    private fun loadFavoriteRentalsFromFirestore() {
        // Reference to the user's document in Firestore
        val userDocumentReference = db.collection("Users").document(userEmail ?: "")
        val favoritesCollectionReference = userDocumentReference.collection("favourites") // Update the collection reference

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

                // Notify the adapter of the data set change
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching rental properties: $exception")
            }
    }

    // rv:  when you click the row...
    private fun rowClicked(rentalProperty: Int) {
        // PropertyDetailsActivity opens and pass the selected RentalProperty
        val intent = Intent(this, PropertyDetailsActivity::class.java)
        intent.putExtra("SELECTED_PROPERTY", rentalProperty)
        intent.putExtra("userEmail", userEmail)
        intent.putExtra("userName", userName)
        startActivity(intent)
    }

    // rv: remove rental property button
    private fun removeButtonClicked(position: Int) {
        if (position >= 0 && position < favoriteRentals.size) {
            // Get the selected rental property
            val removedProperty = favoriteRentals[position]

            // Remove the rental property from the user's favorites array in Firestore
            removeFavoriteFromFirestore(removedProperty.propertyAddress)

            // Remove the rental property from the local list
            favoriteRentals.removeAt(position)

            // Notify the adapter that the data set has changed
            adapter.notifyDataSetChanged()

            val snackbar = Snackbar.make(binding.rootLayout, "Property removed", Snackbar.LENGTH_LONG)
                .setAction("Undo") {
                    // User can click "Undo", and the property is back in the list
                    favoriteRentals.add(position, removedProperty)
                    adapter.notifyDataSetChanged()
                    // Add the rental property back to the user's favorites array in Firestore
                    addFavoriteToFirestore(removedProperty.propertyAddress)
                }
            snackbar.show()
        }
    }

    private fun removeFavoriteFromFirestore(propertyAddress: String) {
        val userDocumentReference = db.collection("Users").document(userEmail ?: "")

        // Remove the property address from the "favourites" array in Firestore
        userDocumentReference.update("favourites", FieldValue.arrayRemove(propertyAddress))
            .addOnSuccessListener {
                Log.d(TAG, "Removed property from favorites in Firestore")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error removing property from favorites in Firestore: $exception")
            }
    }

    private fun addFavoriteToFirestore(propertyAddress: String) {
        val userDocumentReference = db.collection("Users").document(userEmail ?: "")

        // Add the property address to the "favourites" array in Firestore
        userDocumentReference.update("favourites", FieldValue.arrayUnion(propertyAddress))
            .addOnSuccessListener {
                Log.d(TAG, "Added property to favorites in Firestore")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error adding property to favorites in Firestore: $exception")
            }
    }

    //MENU
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.option_menu_details, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_logout -> {
                // Retrieve the user name from the intent
                val userName = intent.getStringExtra("userName")
                val userEmail = intent.getStringExtra("userEmail")
                // Show a toast with the user name
                Toast.makeText(this, "Logged out as: $userEmail", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Logged out as: $userEmail")

                // Start the MainActivity
                startActivity(Intent(this, MainActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}