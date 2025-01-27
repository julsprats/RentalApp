package com.sv.group12_rental_app

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.sv.group12_rental_app.databinding.ActivityLoginBinding
import com.sv.group12_rental_app.models.Users

class LoginActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG = this.javaClass.canonicalName
    private lateinit var binding: ActivityLoginBinding
    private lateinit var prefs: SharedPreferences
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener(this)
        binding.btnRegister.setOnClickListener(this)

        this.firebaseAuth = FirebaseAuth.getInstance()

        prefs = applicationContext.getSharedPreferences(packageName, MODE_PRIVATE)

        if (prefs.contains("USER_EMAIL")) {
            binding.editTextEmail.setText(this.prefs.getString("USER_EMAIL", ""))
        }
        if (prefs.contains("USER_PASSWORD")) {
            binding.editTextPassword.setText(this.prefs.getString("USER_PASSWORD", ""))
        }
    }

    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                R.id.btnLogin -> {
                    Log.d(TAG, "onClick: Sign In Button Clicked")
                    this.validateData()
                }

                R.id.btnRegister -> {
                    Log.d(TAG, "onClick: Sign Up Button Clicked")
                    val signUpIntent = Intent(this, RegisterActivity::class.java)
                    startActivity(signUpIntent)
                }
            }
        }
    }

    private fun validateData() {
        var validData = true
        var email = ""
        var password = ""
        if (binding.editTextEmail.text.toString().isEmpty()) {
            binding.editTextEmail.error = "Email Cannot be Empty"
            validData = false
        } else {
            email = binding.editTextEmail.text.toString()
        }
        if (binding.editTextPassword.text.toString().isEmpty()) {
            binding.editTextPassword.error = "Password Cannot be Empty"
            validData = false
        } else {
            password = binding.editTextPassword.text.toString()
        }
        if (validData) {
            signIn(email, password)
        } else {
            Toast.makeText(this, "Please provide correct inputs", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signIn(email: String, password: String) {
        //signIn using FirebaseAuth
        this.firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signIn: Login successful")
                    saveToPrefs(email, password)
                    // Fetch user's favourites from Firestore
                    getFavoritesFromFirestore(email)
                } else {
                    Log.e(TAG, "signIn: Login Failed : ${task.exception}",)
                    Toast.makeText(
                        this@LoginActivity,
                        "Authentication failed. Check the credentials",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun saveToPrefs(email: String, password: String) {
        if (binding.swtRemember.isChecked) {
            prefs.edit().putString("USER_EMAIL", email).apply()
            prefs.edit().putString("USER_PASSWORD", password).apply()
        } else {
            if (prefs.contains("USER_EMAIL")) {
                prefs.edit().remove("USER_EMAIL").apply()
            }
            if (prefs.contains("USER_PASSWORD")) {
                prefs.edit().remove("USER_PASSWORD").apply()
            }
        }
    }

    private fun getFavoritesFromFirestore(email: String) {
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("Users")

        usersCollection.document(email).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val user = documentSnapshot.toObject(Users::class.java)
                    if (user != null) {
                        // Assuming that 'favourites' is the field in Users collection
                        val favourites = user.favourites
                        // Pass the favourites to the next method
                        getUserInfoAndNavigate(email, favourites)
                    } else {
                        Log.e(TAG, "Failed to convert Firestore document to Users object")
                    }
                } else {
                    Log.e(TAG, "No user found in Firestore with email $email")
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error retrieving user details from Firestore: $exception")
            }
    }

    private fun getUserInfoAndNavigate(email: String, favourites: List<String>) {
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("Users")

        // Assuming you have a field named 'email' in Firestore that uniquely identifies users
        usersCollection.whereEqualTo("email", email).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Assuming there's only one user with a unique email
                    val userDocument = querySnapshot.documents[0]
                    val user = userDocument.toObject(Users::class.java)

                    if (user != null) {
                        val usertype = user.userType
                        val username = user.name
                        val userFavourites = user.favourites ?: emptyList()

                        Log.d(TAG, "User type is $usertype")

                        // Create an Intent
                        val intent: Intent = when (usertype) {
                            "Tenant" -> {
                                val tenantIntent = Intent(this, TenantScreen::class.java)
                                // Pass user email and name as extras to TenantScreen
                                tenantIntent.putExtra("userEmail", email)
                                tenantIntent.putExtra("userName", username)
                                tenantIntent
                            }
                            "Landlord" -> {
                                val tenantIntent = Intent(this, TenantScreen::class.java)
                                // Pass user email and name as extras to TenantScreen
                                tenantIntent.putExtra("userEmail", email)
                                tenantIntent.putExtra("userName", username)
                                tenantIntent
                            }
                            else -> Intent(this, MainActivity::class.java)
                        }

                        // Pass user email as an extra to the next activity
                        intent.putExtra("userEmail", email)

                        // Start the activity
                        startActivity(intent)
                    } else {
                        Log.e(TAG, "Failed to convert Firestore document to Users object")
                    }
                } else {
                    Log.e(TAG, "No user found in Firestore with email $email")
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error retrieving user details from Firestore: $exception")
            }
    }
}