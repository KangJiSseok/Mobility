package com.example.mobility.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mobility.R
import com.example.mobility.databinding.ActivityUpdateOdoBinding

class UpdateOdoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityUpdateOdoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "주행거리 업데이트"

    }
}