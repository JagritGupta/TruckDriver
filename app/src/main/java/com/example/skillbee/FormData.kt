package com.example.skillbee

import com.google.gson.annotations.SerializedName

/**
 * Copyright (C) Dailyrounds., 2022
 * All rights reserved.
 * Created by Jagrit Gupta on 07/08/23.
 * jagrit@dailyrounds.org
 */

data class FormData(
    @SerializedName("Name")
    val Name: String = "",
    @SerializedName("Phone")
    val Phone: String = "",
    @SerializedName("State")
    val State: String = "",
    @SerializedName("Experience")
    val Experience: String = "",
    @SerializedName("id")
    val id: String = "",
    @SerializedName("sheetName")
    val sheetName: String = "SpeechDriverPage",
    @SerializedName("Content")
    val Content: String = "",
    @SerializedName("Param")
    val Param: String = "",
    @SerializedName("URL")
    val URL: String = "https://eu.skillbee.com/driver-speech"
)
