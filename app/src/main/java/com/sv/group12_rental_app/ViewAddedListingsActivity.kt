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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sv.group12_rental_app.adapter.AddedListingAdapter
import com.sv.group12_rental_app.databinding.ActivityViewAddedListingsBinding
import com.sv.group12_rental_app.models.Rental

class ViewAddedListingsActivity : AppCompatActivity() {
    //setup binding and tags
    lateinit var binding: ActivityViewAddedListingsBinding
    lateinit var adapter: AddedListingAdapter

    val TAG = this@ViewAddedListingsActivity::class.java.canonicalName

    private var userEmail: String? = null

    private var userName: String? = null

    //define datasource variable
    private var datasource:MutableList<Rental>  = mutableListOf()

    lateinit var sharedPreferences: SharedPreferences
    lateinit var prefEditor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.binding = ActivityViewAddedListingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get user mail and name
        userEmail = intent.getStringExtra("userEmail")
        userName = intent.getStringExtra("userName")

        // Now, you can use 'userEmail' and 'userName' in your TenantScreen activity as needed
        Log.d(TAG, "User email is: $userEmail")
        Log.d(TAG, "User name is: $userName")

        //Set up Adapter
        adapter = AddedListingAdapter(
            datasource,
            {pos -> rowClicked(pos)},
            {pos -> detailButtonClicked(pos)}
        )

        //configure shared preferences
        this.sharedPreferences = getSharedPreferences("RENTAL_LISTINGS_DATASOURCE", MODE_PRIVATE)
        this.prefEditor = this.sharedPreferences.edit()

        //Retrieve rental list from sharedPreferences
        val rentalListingsfromSP = sharedPreferences.getString("RENTAL_LISTINGS_DATASOURCE", "")
        if(rentalListingsfromSP != ""){
            // convert the string back into a fruit object
            val gson = Gson()
            // define what type of data we should convert the string back to
            val typeToken = object : TypeToken<List<Rental>>() {}.type
            // convert the string back to a list
            val rentalList = gson.fromJson<List<Rental>>(rentalListingsfromSP, typeToken)
            Log.d(TAG, "Retrieved data from SharedPreferences: $rentalList")

            // Add all items from rentalList to the existing datasource
            datasource.addAll(rentalList)
            Log.d(TAG, "Datasource: $datasource")
            // Notify the adapter that the data has changed
            adapter.notifyDataSetChanged()

        }

        binding.addListingBtn.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        //Set up Recycler View
        binding.rentalListings.adapter = adapter
        binding.rentalListings.layoutManager = LinearLayoutManager(this)
        binding.rentalListings.addItemDecoration(
            DividerItemDecoration(
                this,
                LinearLayoutManager.VERTICAL
            )
        )

        setSupportActionBar(this.binding.menuToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
    } //end of onCreate

    override fun onResume(){
        super.onResume()
        Log.d(TAG, "Use returned to View Added Listings Activity")

        //Get the updated list of data from sharedPreferences
        val rentalListingsfromSP = sharedPreferences.getString("RENTAL_LISTINGS_DATASOURCE", "")
        if(rentalListingsfromSP != "") {
            // convert the string back into a fruit object
            val gson = Gson()
            // define what type of data we should convert the string back to
            val typeToken = object : TypeToken<List<Rental>>() {}.type
            // convert the string back to a list
            val rentalList = gson.fromJson<List<Rental>>(rentalListingsfromSP, typeToken)

            //Update the adapter with the new list of data
            //delete all existing items
            datasource.clear()
            //add back all of the items from shared preferences
            datasource.addAll(rentalList)
            //notify the adapter something has changed
            adapter.notifyDataSetChanged()
        }

    }

    fun rowClicked(position:Int){
    }

    fun detailButtonClicked(position: Int){
        // Pass the position to PropertyDetailsActivity
        val intent = Intent(this, EditPropertyDetailActivity::class.java)
        intent.putExtra("POSITION", position)
        startActivity(intent)


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.oprion_landlord_menu_items, menu)
        return true

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.propertyList -> {
                val intent = Intent(this, ViewAddedListingsActivity::class.java)
                intent.putExtra("userEmail", userEmail)
                intent.putExtra("userName", userName)
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