
package com.sv.group12_rental_app

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sv.group12_rental_app.adapter.RentalPropertyAdapter
import com.sv.group12_rental_app.databinding.ActivityTenantScreenBinding
import com.sv.group12_rental_app.models.Rental
import com.sv.group12_rental_app.models.Users
import com.sv.group12_rental_app.repositories.RentalRepository
import com.sv.group12_rental_app.repositories.UserRepository

class TenantScreen : AppCompatActivity() {
    private val TAG = "Tenant Activity"

    private var userEmail: String? = null

    private var userName: String? = null

    private lateinit var binding: ActivityTenantScreenBinding

    private lateinit var sharedPreferences: SharedPreferences
    lateinit var prefEditor: SharedPreferences.Editor

    lateinit var adapter: RentalPropertyAdapter
    private lateinit var rentalRepository: RentalRepository

    // Original list of rentals
    private var originalDatasource:MutableList<Rental>  = mutableListOf()
    var userLoggedIn = false

    private lateinit var userRepository: UserRepository
    private var userFavorites: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTenantScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize UserRepository with the context
        userRepository = UserRepository(this)

        setSupportActionBar(this.binding.menuToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        // get user mail and name
        userEmail = intent.getStringExtra("userEmail")
        userName = intent.getStringExtra("userName")

        Log.d(TAG, "User email is: $userEmail")
        Log.d(TAG, "User name is: $userName")
        Log.d(TAG, "User favorites: $userFavorites")

        // Retrieve favorites from intent
        val favourites = intent.getStringArrayListExtra("userFavourites") ?: emptyList()

        userFavorites = favourites

        // setup adapter
        adapter = RentalPropertyAdapter(
            originalDatasource.toMutableList(),
            { pos -> rowClicked(pos) },
            { pos -> favButtonClicked(pos) }
        )

        // Pass favorites to adapter
        adapter.setUserFavorites(userFavorites)

        //configure shared preferences
        this.sharedPreferences = getSharedPreferences("RENTAL_LISTINGS_DATASOURCE", MODE_PRIVATE)
        this.prefEditor = this.sharedPreferences.edit()

        //Retrieve rental list from sharedPreferences
        val rentalListingsfromSP = sharedPreferences.getString("RENTAL_LISTINGS_DATASOURCE", "")
        if(rentalListingsfromSP != "") {
            // convert the string back into a rental object
            val gson = Gson()
            // define what type of data we should convert the string back to
            val typeToken = object : TypeToken<List<Rental>>() {}.type
            // convert the string back to a list
            val rentalList = gson.fromJson<List<Rental>>(rentalListingsfromSP, typeToken)
            originalDatasource.addAll(rentalList)

            Log.d(TAG, "Original datasource populated: $originalDatasource")

            // Notify the adapter that the data has changed
            adapter.notifyDataSetChanged()
        }

        // setup rv
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(
                this,
                LinearLayoutManager.VERTICAL
            )
        )

        rentalRepository = RentalRepository(this)

        binding.searchButton.setOnClickListener {
            val searchQuery = binding.editTextSearch.text.toString()
            performSearch(searchQuery)
        }

        getFavoritesFromFirestore(userEmail ?: "")

    }

    private fun getFavoritesFromFirestore(email: String) {
        val db = FirebaseFirestore.getInstance()
        val userDocument = db.collection("Users").document(email)
        val favoritesCollection = userDocument.collection("favourites")

        favoritesCollection.get()
            .addOnSuccessListener { favoritesSnapshot ->
                val favorites = favoritesSnapshot.documents.map { document ->
                    document.getString("propertyAddress") ?: ""
                }
                Log.d(TAG, "Favorites retrieved from Firestore: $favorites")
                handleFavoritesFromFirestore(favorites)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error retrieving favorites from Firestore: $exception")
            }
    }

    // Function to handle the favorites obtained from Firestore
    private fun handleFavoritesFromFirestore(favourites: List<String>) {
        // Update userFavorites and notify the adapter
        userFavorites = favourites
        adapter.setUserFavorites(userFavorites)
        adapter.notifyDataSetChanged()
    }

    // rv: Favorite button click handler
    fun favButtonClicked(position: Int) {
        Log.d(TAG, "Favourites button clicked at position $position")

        if (position >= 0 && position < originalDatasource.size) {
            val selectedProperty = originalDatasource[position]
            val propertyAddress = selectedProperty.propertyAddress

            // Assuming 'userEmail' is not null at this point
            userEmail?.let { email ->
                val isFavorite = userFavorites.contains(propertyAddress)

                if (isFavorite) {
                    Log.d(TAG, "Removing from favorites: $propertyAddress")
                    removeFavoriteInFirestore(email)
                } else {
                    Log.d(TAG, "Adding to favorites: $propertyAddress")
                    addFavoriteInFirestore(email)
                }

                // Update the heart icon based on the new favorite status
                val newFavoriteStatus = !isFavorite
                updateHeartIcon(newFavoriteStatus, position)
            }
        }
    }

    // Function to update the heart icon based on the favorite status
    private fun updateHeartIcon(isFavorite: Boolean, position: Int) {
        // Get the ViewHolder at the specified position
        val viewHolder = binding.recyclerView.findViewHolderForAdapterPosition(position) as? RentalPropertyAdapter.RentalPropertyViewHolder

        // Update the heart icon based on the favorite status
        viewHolder?.updateHeartIcon(isFavorite)
    }

    private fun addFavoriteInFirestore(propertyAddress: String) {
        val db = FirebaseFirestore.getInstance()
        val userDocument = userEmail?.let { db.collection("Users").document(it) }
        val favoritesCollection = userDocument?.collection("favourites")

        // Add the property to the "favourites" subcollection
        favoritesCollection?.document(propertyAddress)
            ?.set(mapOf("propertyAddress" to propertyAddress))
            ?.addOnSuccessListener {
                // Update the userFavorites list in the adapter
                userFavorites = userFavorites + propertyAddress
                // Notify the adapter that the data has changed
                Log.d(TAG, "Property added to favorites in Firestore")
                adapter.notifyDataSetChanged()
                val snackbar = Snackbar.make(binding.root, "Added to favorites", Snackbar.LENGTH_LONG)
                snackbar.show()
            }
            ?.addOnFailureListener { exception ->
                // Handle the failure scenario
                Log.e(TAG, "Error adding property to favorites in Firestore: $exception")
            }
    }

    //not removing but letting them know it's already in favs
    private fun removeFavoriteInFirestore(propertyAddress: String) {
        val db = FirebaseFirestore.getInstance()
        val userDocument = db.collection("Users").document(userEmail ?: "")
        val favoritesCollection = userDocument.collection("favourites")

        // Check if the rental is already on the "favourites" subcollection
        favoritesCollection.document(propertyAddress)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "Property already on favorites in Firestore")
                val snackbar = Snackbar.make(binding.root, "Already in your favorites!", Snackbar.LENGTH_LONG)
                snackbar.show()
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

    private fun performSearch(query: String) {
        // Use rentalRepository to perform search in Firestore
        rentalRepository.retrieveAllExpenses()

        // Observe changes in the LiveData and update UI accordingly
        rentalRepository.allExpenses.observe(this) { rentalList ->
            // Filter the list based on the search query
            val filteredList = rentalList.filter { rental ->
                rental.propertyAddress.contains(query, ignoreCase = true) ||
                        rental.description.contains(query, ignoreCase = true)
            }

            // Update the current list in the adapter
            adapter.updateList(filteredList.toMutableList())

            // Notify the adapter of the dataset change
            adapter.notifyDataSetChanged()

            // Show the RecyclerView after performing the search
            binding.recyclerView.visibility = View.VISIBLE
        }
    }

    //MENU
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.option_tenant_menu_items, menu)
        return true

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_favorites -> {
                val intent = Intent(this, FavListScreen::class.java)
                intent.putExtra("userEmail", userEmail)
                intent.putExtra("userName", userName)
                intent.putStringArrayListExtra("userFavorites", ArrayList(userFavorites))
                startActivity(intent)
                true
            }
            R.id.menu_logout -> {
                // Retrieve the user name from the intent
                val userName = intent.getStringExtra("userName") ?: "Unknown User"

                // Show a toast with the user name
                Toast.makeText(this, "Logged out as: $userName", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Logged out as: $userName")

                // Start the MainActivity
                startActivity(Intent(this, MainActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}