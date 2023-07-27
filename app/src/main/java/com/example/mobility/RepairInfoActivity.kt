package com.example.mobility

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.mobility.databinding.ActivityRepairInfoBinding
import java.text.SimpleDateFormat
import java.util.Calendar

class RepairInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRepairInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // intent 값 받기
        val intent: Intent = getIntent()

        val car = intent.getStringExtra("car")
        val odo = intent.getStringExtra("odo").toString()
        val engOdo = intent.getStringExtra("eng-odo").toString()
        val engDate = intent.getStringExtra("eng-date").toString()

        // 엔진오일
        val (erepairNeeded, ediffOdo, ediffDate) = calcEngineOil(odo, engOdo, engDate)

        val engOdoRatio: Double = (ediffOdo.toString().toDouble() / 15000) * 100
        val engDateRatio: Double = (ediffDate.toString().toDouble() / 365) * 100

        // 엔진오일
        binding.engOdoProgs.progress = engOdoRatio.toInt()
        binding.engDateProgs.progress = engDateRatio.toInt()

        val isEngRepair = erepairNeeded.toString().toBoolean()
        if (!isEngRepair) {
            binding.engText.text = "교체까지 ${15000 - ediffOdo.toString().toInt()}km, ${365 - ediffDate.toString().toInt()}일 남았습니다."
        }
        else {
            if (ediffOdo.toString().toInt() < 0) {
                binding.engOdoProgs.progress = 100
                binding.engText.text = "주행거리가 도달하여 엔진오일을 교체해야 합니다."
            }
            if (ediffDate.toString().toInt() < 0) {
                binding.engDateProgs.progress = 100
                binding.engText.text = "교체기간이 도달하여 엔진오일을 교체해야 합니다."
            }
        }
    }

    /*
    * 엔진오일 교체 주기 계산
    */
    fun calcEngineOil(totalOdo: String, odo: String, date: String): Array<Any> {
        // 교체 주행거리와 현재 누적 주행거리의 차
        val totalOdos = totalOdo.toInt()
        val odos = odo.toInt()
        var diffOdo = totalOdos - odos

        // 오늘 날짜
        val today = Calendar.getInstance()

        // date 변수에는 2023-07-26 형식의 날짜가 들어감
        val sf = SimpleDateFormat("yyyy-MM-dd")
        val dateValue = sf.parse(date)

        // 교체한지 며칠 지났는지 일수로 계산
        var diffDate = (today.time.time - dateValue.time) / (60 * 60 * 24 * 1000)

        // 정식 교체 주기 (주행거리)
        val repairOdo = 15000
        val repairDay = 365

        // 주행거리 또는 교체주기 둘 중 하나가 정식 교체 주기를 초과한 경우
        if (diffOdo > repairOdo || diffDate > repairDay) {
            // 교체가 필요함.
            return arrayOf(true, diffOdo, diffDate)
        }


        return arrayOf(false, diffOdo, diffDate)
    }
    
}