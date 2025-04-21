package com.example.crudauthapp.model

data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val user_id: Int
)
