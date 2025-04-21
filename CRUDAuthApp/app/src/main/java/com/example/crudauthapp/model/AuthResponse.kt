package com.example.crudauthapp.model

data class AuthResponse(
    val message: String,
    val token: String? = null,
    val role: String? = null,
    val error: String? = null
)
