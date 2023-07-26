package com.example.mobility

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import com.example.mobility.databinding.ActivityAddInfoBinding
import java.util.Calendar


class AddInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAddInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}