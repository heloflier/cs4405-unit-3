package com.example.bugtracker.data.remote

import com.example.bugtracker.model.Bug
import retrofit2.Response
import retrofit2.http.*

interface BugApiService {
    @GET("bugs")
    suspend fun getBugs(): List<Bug>

    @POST("bugs")
    suspend fun createBug(@Body bug: Bug): Response<Bug>

    @PUT("bugs/{id}")
    suspend fun updateBug(@Path("id") id: Int, @Body bug: Bug): Response<Bug>

    @DELETE("bugs/{id}")
    suspend fun deleteBug(@Path("id") id: Int): Response<Unit>
}
