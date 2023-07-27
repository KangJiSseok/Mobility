package com.example.mobility

import android.content.Intent
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
            val car = binding.car.text.toString().trim() // 차종
            val year = binding.year.text.toString().trim() // 연식
            val odo = binding.odo.text.toString().trim() // 주행거리

            
            // 차량 정보 등록 (서버)


            // AddInfoActivity 실행
            val intent = Intent(this, AddInfoActivity::class.java)

            // AddInfoActivity에 정보 전달
            val array = arrayOf(car, year, odo)
            intent.putExtra("infoArray", array)

            // Activity 실행
            startActivity(intent)
        }
    }
}