package com.example.skillbee

import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Url

/**
 * Copyright (C) Dailyrounds., 2022
 * All rights reserved.
 * Created by Jagrit Gupta on 04/08/23.
 * jagrit@dailyrounds.org
 */
interface ApiService {
    @Multipart
    @POST
    fun postFormData(
        @Url url: String = "script.google.com/macros/s/AKfycbxF0wmA4OobTK1JuIvse27v8T76brtSwOV25a7TtPcKf6fihpz_MdEGeku-vK5pne3Z1w/exec",
        @Part("Name") key1: RequestBody,
        @Part("Phone") key2: RequestBody,
        @Part("State") key3: RequestBody,
        @Part("Experience") key4: RequestBody,
        @Part("id") key5: RequestBody,
        @Part("sheetName") key6: RequestBody,
        @Part("Content") key7: RequestBody,
        @Part("Param") key8: RequestBody,
    ): Call<FormResponseData>

    @Multipart
    @POST
    fun submitAudioFile(
        @Url url: String,
        @Part("audio") key1: RequestBody,
        @Part("text") key2: RequestBody
    ): Call<AudioSubmitResponseData>
}

data class AudioSubmitResponseData(
    val success: String,
    val message: String,
    val score: List<Scores>
) {
    data class Scores(
        val score : String,
        val name: String
    )
}

data class FormResponseData(
    val result: String,
    val row: Int
)


object RetrofitService {
    val apiService: ApiService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://google.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }
}