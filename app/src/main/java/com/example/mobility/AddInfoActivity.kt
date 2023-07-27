package com.example.mobility

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mobility.databinding.ActivityAddInfoBinding
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date


class AddInfoActivity : AppCompatActivity() {
    lateinit var car: String
    lateinit var year: String
    lateinit var odo: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAddInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // intent 값 받기
        val intent: Intent = getIntent()
        val array = intent.getStringArrayExtra("infoArray")

        car = array?.get(0).toString()
        year = array?.get(1).toString()
        odo = array?.get(2).toString()

        // 값 전달 테스트
        Toast.makeText(this, "차종: ${car}, 연식: ${year}, 주행거리: ${odo}", Toast.LENGTH_SHORT).show()
    }
}