package com.example.skillbee

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.skillbee.databinding.ActivitySplashBinding


/**
 * Copyright (C) Dailyrounds., 2022
 * All rights reserved.
 * Created by Jagrit Gupta on 04/08/23.
 * jagrit@dailyrounds.org
 */
@RequiresApi(Build.VERSION_CODES.S)
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvSplashTitle.postDelayed(formScreenRunnable, 2000)
        super.onCreate(savedInstanceState)
    }

    private val formScreenRunnable = Runnable {
        startActivity(Intent(this, FormActivity::class.java))
        finish()
    }
}