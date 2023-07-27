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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAddInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // intent 값 받기
        val intent: Intent = getIntent()
        val array = intent.getStringArrayExtra("infoArray")

        val car = array?.get(0)
        val year = array?.get(1)
        val odo = array?.get(2)

        // 값 전달 테스트
        Toast.makeText(this, "차종: ${car}, 연식: ${year}, 주행거리: ${odo}", Toast.LENGTH_SHORT).show()
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