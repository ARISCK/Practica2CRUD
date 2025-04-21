package com.example.crudauthapp.model

data class UserRequest(
    val name: String? = null,
    val email: String,
    val password: String
)
