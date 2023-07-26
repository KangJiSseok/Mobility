package com.example.mobility

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mobility.databinding.ActivityCarInfoBinding

class CarInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityCarInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 차종, 연식, 주행 거리를 입력받음
        binding.submit.setOnClickListener {
            val car = binding.car.text.toString() // 차종
            val year = binding.year.text.toString() // 연식
            val odo = binding.odo.text.toString() // 주행거리

            // 차량 정보 등록
        }
    }
}