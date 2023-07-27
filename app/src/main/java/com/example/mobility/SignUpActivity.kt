package com.example.mobility

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mobility.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "회원가입"
    }
}