package com.example.crudauthapp.model

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val photo: String? = null
)
