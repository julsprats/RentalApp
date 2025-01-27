package com.sv.group12_rental_app

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.google.gson.Gson
import com.sv.group12_rental_app.models.Users

class AuthManager private constructor(private val context: Context) {

    companion object {
        private const val USER_PREFS_KEY = "UserPrefs"
        private const val USER_KEY = "user"

        private var instance: AuthManager? = null

        fun getInstance(context: Context): AuthManager {
            return instance ?: AuthManager(context).also { instance = it }
        }
    }

    fun setCurrentUser(user: Users) {
        val userJson = Gson().toJson(user)
        val sharedPreferences = context.getSharedPreferences(USER_PREFS_KEY, MODE_PRIVATE)
        sharedPreferences.edit().putString(USER_KEY, userJson).apply()
    }

    fun getCurrentUser(): Users? {
        val sharedPreferences = context.getSharedPreferences(USER_PREFS_KEY, MODE_PRIVATE)
        val userJson = sharedPreferences.getString(USER_KEY, "")
        return if (userJson.isNullOrEmpty()) {
            null
        } else {
            Gson().fromJson(userJson, Users::class.java)
        }
    }
}
