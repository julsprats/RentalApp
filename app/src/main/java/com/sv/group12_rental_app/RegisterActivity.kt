package com.sv.group12_rental_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.sv.group12_rental_app.databinding.ActivityRegisterBinding
import com.sv.group12_rental_app.models.Users
import com.sv.group12_rental_app.repositories.UserRepository

class RegisterActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = this.javaClass.canonicalName
    private lateinit var binding: ActivityRegisterBinding
    private  lateinit var firebaseAuth : FirebaseAuth
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())

        binding.btnRegister.setOnClickListener(this)

        this.firebaseAuth = FirebaseAuth.getInstance()
        this.userRepository = UserRepository(applicationContext)
    }

    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                R.id.btnRegister -> {
                    Log.d(TAG, "onClick: Create Account button Clicked")
                    validateData()
                }
            }
        }
    }

    private fun validateData() {
        var validData = true
        var email = ""
        var password = ""

        if (binding.editTextEmail.getText().toString().isEmpty()) {
            binding.editTextEmail.setError("Email Cannot be Empty")
            validData = false
        } else {
            email = binding.editTextEmail.getText().toString()
        }

        if (binding.editTextPassword.getText().toString().isEmpty()) {
            binding.editTextPassword.setError("Password Cannot be Empty")
            validData = false
        } else {
            if (binding.editTextConfirmPassword.getText().toString().isEmpty()) {
                binding.editTextConfirmPassword.setError("Confirm Password Cannot be Empty")
                validData = false
            } else {
                if (!binding.editTextPassword.getText().toString()
                        .equals(binding.editTextConfirmPassword.getText().toString())
                ) {
                    binding.editTextConfirmPassword.setError("Both passwords must be same")
                    validData = false
                } else {
                    password = binding.editTextPassword.getText().toString()
                }
            }
        }

        if (validData) {
            createAccount(email, password)
        } else {
            Toast.makeText(this, "Please provide correct inputs", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createAccount(email: String, password: String) {

        val userTypeRadioGroup: RadioGroup = findViewById(R.id.userType)
        val nameEditText: EditText = findViewById(R.id.editTenantTextName)

        // Get the name from the EditText
        val name: String = nameEditText.text.toString()

//        SignUp using FirebaseAuth

        this.firebaseAuth
            .createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){task ->

                if (task.isSuccessful){
                    // Find the selected radio button
                    val selectedRadioButtonId = userTypeRadioGroup.checkedRadioButtonId

                    // Get the selected user type
                    val userType: String = when (selectedRadioButtonId) {
                        R.id.tenantUserType -> "Tenant"
                        R.id.landLordUserType -> "Landlord"
                        else -> "" // Handle the case when no radio button is selected
                    }
                    //create user document with default profile info
                    val userToAdd = Users(
                        id = email,
                        email = email,
                        password = password,
                        name = name.toString(),
                        userType = userType,
                        favourites = mutableListOf()
                    )
                    userRepository.addUserToDB(userToAdd)

                    Log.d(TAG, "createAccount: User account successfully create with email $email")
                    saveToPrefs(email, password)
                    goToLogin()
                }else{
                    Log.d(TAG, "createAccount: Unable to create user account : ${task.exception}", )
                    Toast.makeText(this@RegisterActivity, "Account creation failed", Toast.LENGTH_SHORT).show()
                }
            }

    }

    private fun saveToPrefs(email: String, password: String) {
        val prefs = applicationContext.getSharedPreferences(packageName, MODE_PRIVATE)
        prefs.edit().putString("USER_EMAIL", email).apply()
        prefs.edit().putString("USER_PASSWORD", password).apply()
    }

    private fun goToLogin() {
//        val mainIntent = Intent(this, MainActivity::class.java)
        val mainIntent = Intent(this, LoginActivity::class.java)
        startActivity(mainIntent)
    }
}

//val userType = when (radioGroupUserType.checkedRadioButtonId) {
//    R.id.tenantUserType -> "Tenant"
//    R.id.landLordUserType -> "Landlord"
//    // Add cases for other radio buttons representing user types as needed
//    else -> "RegularUser" // Default to RegularUser if none selected
//}