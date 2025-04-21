package com.example.crudauthapp.network

import com.example.crudauthapp.model.AuthResponse
import com.example.crudauthapp.model.UserRequest
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface AuthApi {
    @POST("/api/auth/register")
    fun register(@Body request: UserRequest): Call<AuthResponse>

    @POST("/api/auth/login")
    fun login(@Body request: UserRequest): Call<AuthResponse>

    @Multipart
    @POST("/api/auth/upload-photo")
    fun uploadProfilePhoto(
        @Header("Authorization") token: String,
        @Part photo: MultipartBody.Part
    ): Call<String> // o tu respuesta JSON

    @POST("/api/auth/register-admin")
    fun registerUserAsAdmin(
        @Header("Authorization") token: String,
        @Body userData: Map<String, String>
    ): Call<Map<String, Any>>


}
