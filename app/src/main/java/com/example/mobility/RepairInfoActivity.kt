package com.example.mobility

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mobility.databinding.ActivityRepairInfoBinding
import java.text.SimpleDateFormat
import java.util.Calendar

class RepairInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRepairInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    /*
    * 엔진오일 교체 주기 계산
    */
    fun calcEngineOil(totalOdo: Int, odo: Int, date: String) {
        // 교체 주행거리와 현재 누적 주행거리의 차
        val diffOdo = totalOdo - odo

        // 오늘 날짜
        val today = Calendar.getInstance()

        // date 변수에는 2023-07-26 형식의 날짜가 들어감
        val sf = SimpleDateFormat("yyyy-MM-dd")
        val dateValue = sf.parse(date)

        // 교체한지 며칠 지났는지 일수로 계산
        val diffDate = (today.time.time - dateValue.time) / (60 * 60 * 24 * 1000)

        // 정식 교체 주기 (주행거리)
        val repairOdo = 15000
        val repairDay = 365

        // 주행거리 또는 교체주기 둘 중 하나가 정식 교체 주기를 초과한 경우
        if (diffOdo > repairOdo || diffDate > repairDay) {
            // 교체가 필요함.
        }

    }
    
}