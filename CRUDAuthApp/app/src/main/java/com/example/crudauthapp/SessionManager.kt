package com.example.crudauthapp

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString("token", token)
        editor.apply()
    }

    fun fetchAuthToken(): String? {
        return prefs.getString("token", null)
    }

    fun clearToken() {
        prefs.edit().remove("token").apply()
    }

    fun saveUserRole(role: String) {
        prefs.edit().putString("role", role).apply()
    }

    fun fetchUserRole(): String? {
        return prefs.getString("role", null)
    }

}
