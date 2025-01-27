package com.sv.group12_rental_app

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sv.group12_rental_app.adapter.RentalPropertyAdapter
import com.sv.group12_rental_app.databinding.ActivityMainBinding
import com.sv.group12_rental_app.models.Rental
import com.sv.group12_rental_app.repositories.RentalRepository

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var sharedPreferences: SharedPreferences
    lateinit var prefEditor: SharedPreferences.Editor

    lateinit var adapter: RentalPropertyAdapter

    private lateinit var rentalRepository: RentalRepository

    // Original list of rentals
    private var originalDatasource:MutableList<Rental>  = mutableListOf()

    var userLoggedIn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Initialize shared preferences
        //sharedPreferences = getPreferences(MODE_PRIVATE)

        setSupportActionBar(this.binding.menuToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        // setup adapter
        adapter = RentalPropertyAdapter(
            originalDatasource,
            {pos -> rowClicked(pos)},
            {pos -> favButtonClickedMain(pos)}
        )

        //configure shared preferences
        this.sharedPreferences = getSharedPreferences("RENTAL_LISTINGS_DATASOURCE", MODE_PRIVATE)
        this.prefEditor = this.sharedPreferences.edit()

        //Retrieve rental list from sharedPreferences
        val rentalListingsfromSP = sharedPreferences.getString("RENTAL_LISTINGS_DATASOURCE", "")
        if(rentalListingsfromSP != "") {
            // convert the string back into a fruit object
            val gson = Gson()
            // define what type of data we should convert the string back to
            val typeToken = object : TypeToken<List<Rental>>() {}.type
            // convert the string back to a list
            val rentalList = gson.fromJson<List<Rental>>(rentalListingsfromSP, typeToken)
            originalDatasource.addAll(rentalList)
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
    }

    // rv: Favorite button click handler
    fun favButtonClickedMain(position:Int) {
//
//        val snackbar = Snackbar.make(binding.root, "Favorite ${position}", Snackbar.LENGTH_LONG)
//        snackbar.show()

        showLoginRegisterDialog(position)

    }



    private fun showLoginRegisterDialog(position: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Login or Register")
        builder.setMessage("Please log in or register to add to favorites")

        builder.setPositiveButton("Login") { _, _ ->
            // Redirect to login activity or perform login action
            startActivity(Intent(this, LoginActivity::class.java))
        }

        builder.setNegativeButton("Register") { _, _ ->
            // Redirect to register activity or perform register action
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        builder.setNeutralButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }




    // rv:  when you click the row...
    private fun rowClicked(rentalProperty: Int) {
        // PropertyDetailsActivity opens and pass the selected RentalProperty
        val intent = Intent(this, PropertyDetailsActivity::class.java)
        //intent.putExtra("SELECTED_PROPERTY", rentalProperty)
        startActivity(intent)
    }

    // rv: remove rental property button
    fun removeButtonClicked(position: Int) {
        // ensure that the position is within the valid range
        if (position >= 0 && position < originalDatasource.size) {
            // removes the property from the datasource
            val removedProperty = originalDatasource.removeAt(position)

            // notify the adapter that the data set has changed
            adapter.notifyDataSetChanged()

            // snackbar to inform the user about the removal
            val snackbar = Snackbar.make(binding.root, "Property removed", Snackbar.LENGTH_LONG)
                .setAction("Undo") {
                    // user can click "Undo", and the the property is back to the list
                    originalDatasource.add(position, removedProperty)
                    adapter.notifyDataSetChanged()
                }
            snackbar.show()
        }
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.option_menu_items, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_login -> {
                startActivity(Intent(this, LoginActivity::class.java))
                true
            }
            R.id.menu_register -> {
                startActivity(Intent(this, RegisterActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}