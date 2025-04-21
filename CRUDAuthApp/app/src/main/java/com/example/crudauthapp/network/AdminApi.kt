package com.example.crudauthapp.network

import com.example.crudauthapp.model.Task
import com.example.crudauthapp.model.User
import retrofit2.Call
import retrofit2.http.*

interface AdminApi {

    @GET("/api/auth/users")
    fun getAllUsers(@Header("Authorization") token: String): Call<List<User>>

    @PUT("/api/auth/users/{id}/role")
    fun updateUserRole(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body roleData: Map<String, String>
    ): Call<Map<String, Any>>

    @DELETE("/api/auth/users/{id}")
    fun deleteUser(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<Map<String, Any>>

    @GET("/api/auth/users/{id}/tasks")
    fun getTasksByUserId(
        @Header("Authorization") token: String,
        @Path("id") userId: Int
    ): Call<List<Task>>

    @POST("/api/auth/users/{id}/tasks")
    fun createTaskForUser(
        @Header("Authorization") token: String,
        @Path("id") userId: Int,
        @Body task: Map<String, String>
    ): Call<Map<String, Any>>


}
