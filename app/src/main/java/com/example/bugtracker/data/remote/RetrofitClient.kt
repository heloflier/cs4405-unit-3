package com.example.bugtracker.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // placeholder URL — in a real app this would point to a live backend
    private const val BASE_URL = "https://6a4832eaabfcbaade11942a8.mockapi.io/api/v3/"

    val bugApiService: BugApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BugApiService::class.java)
    }
}
