package com.sv.weather_vaishnavi

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.sv.weather_vaishnavi.api.MyInterface
import com.sv.weather_vaishnavi.api.RetrofitInstance
import com.sv.weather_vaishnavi.databinding.ActivityMainBinding
import com.sv.weather_vaishnavi.models.Weather
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val TAG:String = "MY_LOCATION_APP"

    lateinit var binding:ActivityMainBinding

    // Device location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // permissions array
    private val APP_PERMISSIONS_LIST = arrayOf(
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    // showing the permissions dialog box & its result
    private val multiplePermissionsResultLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) {


            resultsList ->
        Log.d(TAG, resultsList.toString())




        var allPermissionsGrantedTracker = true


        for (item in resultsList.entries) {
            if (item.key in APP_PERMISSIONS_LIST && item.value == false) {
                allPermissionsGrantedTracker = false
            }
        }


        if (allPermissionsGrantedTracker == true) {
            var snackbar = Snackbar.make(binding.root, "All permissions granted", Snackbar.LENGTH_LONG)
            snackbar.show()


            // TODO: Get the user's location from the device (GPS, Wifi, etc)
            getDeviceLocation()


        } else {
            var snackbar = Snackbar.make(binding.root, "Some permissions NOT granted", Snackbar.LENGTH_LONG)
            snackbar.show()
            // TODO: Output a rationale for why we need permissions
            // TODO: Disable the get current location button so they can't accidently click on
            //handlePermissionDenied()
        }
    }

    private fun getDeviceLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permissions if not granted
            multiplePermissionsResultLauncher.launch(APP_PERMISSIONS_LIST)
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location == null) {
                    Log.d(TAG, "Location is null")
                    return@addOnSuccessListener
                }else{
                    binding.editText.setText("Toronto")
                }

                val message = "The device is located at: ${location.latitude}, ${location.longitude}"
                Log.d(TAG, message)
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                binding.editText.setText(message)

                try {
                    val geocoder = Geocoder(applicationContext, Locale.getDefault())
                    val searchResults: MutableList<Address>? =
                        geocoder.getFromLocation(location.latitude, location.longitude, 1)

                    if (searchResults.isNullOrEmpty()) {
                        Log.d(TAG, "No matching address found")
                    } else {
                        val matchingAddress: Address = searchResults[0]
                        val output = "${matchingAddress.subThoroughfare} ${matchingAddress.thoroughfare}, ${matchingAddress.locality}, ${matchingAddress.adminArea}, ${matchingAddress.countryName} "
                        binding.editText.setText(output)
                        Log.d(TAG, output)
                    }
                } catch (ex: Exception) {
                    Log.e(TAG, "Error encountered while getting coordinate location.")
                    Log.e(TAG, ex.toString())
                    // Handle the exception and provide feedback to the user
                    Snackbar.make(binding.root, "Error getting location information", Snackbar.LENGTH_LONG)
                        .show()
                }
            }
    }


    private fun checkLocationPermissions(): Boolean {
        return (ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // instantiate the fusedLocationProvider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Check and request location permissions if needed
        if (checkLocationPermissions()) {
            // Permissions are already granted, proceed to get device location
            getDeviceLocation()
        } else {
            // Request location permissions
            multiplePermissionsResultLauncher.launch(APP_PERMISSIONS_LIST)
        }

        binding.buttonGetWeather.setOnClickListener {
            // forward geocoding: (street address --> latitude/longitude)
            // 1. Create an instance of the built in Geocoder class
            val geocoder: Geocoder = Geocoder(applicationContext, Locale.getDefault())
            // - get the address from the user interface
            val addressFromUI = binding.editText.text.toString()
            Log.d(TAG, "Getting coordinates for ${addressFromUI}")
            // 2. try to get the coordinate
            try {
                val searchResults: MutableList<Address>? =
                    geocoder.getFromLocationName(addressFromUI, 1)
                if (searchResults == null) {
                    // Log.e --> outputs the message as an ERROR (red)
                    // Log.d --> outputs the message as a DEBUG message
                    Log.e(TAG, "searchResults variable is null")
                    return@setOnClickListener
                }
                // if not null, then we were able to get some results (and it is possible for the results to be empty)
                if (searchResults.size == 0) {
                    var snackbar = Snackbar.make(binding.root, "Search results are empty.", Snackbar.LENGTH_LONG)
                    snackbar.show()
//                    binding.tvResults.setText("Search results are empty.")
                } else {
                    // 3. Get the coordinate
                    val foundLocation: Address = searchResults.get(0)
                    // 4. output to screen
                    var message =
                        "Coordinates are: ${foundLocation.latitude}, ${foundLocation.longitude}"
                    var snackbar = Snackbar.make(binding.root, "Coordinates are: ${foundLocation.latitude}, ${foundLocation.longitude}", Snackbar.LENGTH_LONG)
                    snackbar.show()
//                    binding.tvResults.setText(message)
                    Log.d(TAG, message)
                    var api: MyInterface = RetrofitInstance.retrofitService
                    //b. launches a background task
                    lifecycleScope.launch {
                        val latitudeFromUI:Double = foundLocation.latitude.toDouble()
                        val longitudeFromUI:Double = foundLocation.longitude.toDouble()
                        // the code you want to execute inside the background task
                        // b. Using that instance, call the MyInterface.kt getSingleUser() function
                        val user4:Weather = api.getWeather(latitudeFromUI,longitudeFromUI)
                        Log.d("MYAPP", user4.toString())
                        // c. Output the user data to the UI (tvResults)
                        // binding.tvResults.setText(user4.toString())
                        val currTempFahrenheit = user4.currentConditions.temp
                        val currTempCelsius = (currTempFahrenheit - 32) * 5 / 9
                        val formattedCurrTemp = String.format("%.2f", currTempCelsius)

                        val currTemp = "Current Temperature: $formattedCurrTemp°C"
                        binding.textViewTemperature.setText(currTemp)

                        val humidity = "Humidity: ${user4.currentConditions.humidity}"
                        binding.textViewHumidity.setText(humidity)

                        val condition = "Condition: ${user4.currentConditions.conditions}"
                        binding.textViewWeatherConditions.setText(condition)

                        val datetime = "Time: ${user4.currentConditions.datetime}"
                        binding.textViewTime.setText(datetime)
                    }
                }
            } catch (ex: Exception) {
                Log.e(TAG, "Error encountered while getting coordinate location.")
                Log.e(TAG, ex.toString())
            }
        }



        binding.editText.setOnClickListener {
            // Check for permissions & do resulting actions
            multiplePermissionsResultLauncher.launch(APP_PERMISSIONS_LIST)
        }
    }
}