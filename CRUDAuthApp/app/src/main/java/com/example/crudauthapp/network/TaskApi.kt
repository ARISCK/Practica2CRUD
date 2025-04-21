package com.example.crudauthapp.network

import com.example.crudauthapp.model.Task
import retrofit2.Call
import retrofit2.http.*

interface TaskApi {
    @GET("/api/tasks/mine")
    fun getMyTasks(@Header("Authorization") token: String): Call<List<Task>>

    @POST("/api/tasks")
    fun createTask(
        @Header("Authorization") token: String,
        @Body task: Map<String, String>
    ): Call<Map<String, Any>>

    @PUT("/api/tasks/{id}")
    fun updateTask(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body task: Map<String, String>
    ): Call<Map<String, Any>>

    @DELETE("/api/tasks/{id}")
    fun deleteTask(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<Map<String, Any>>

    @GET("/api/tasks")
    fun getAllTasks(@Header("Authorization") token: String): Call<List<Task>>


}
