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

        // 등록하기 버튼 클릭 시 RepairInfoActivity 실행 및 정보 전달
        binding.submit.setOnClickListener {
            val intent = Intent(this, RepairInfoActivity::class.java)

            intent.putExtra("car", car)
            intent.putExtra("year", year)
            intent.putExtra("odo", odo)

            intent.putExtra("eng-odo", binding.engOdo.text.toString())
            intent.putExtra("ac-odo", binding.acOdo.text.toString())
            intent.putExtra("tire-odo", binding.tireOdo.text.toString())

            val engDate = "${binding.engDate.year}-${monthClear(binding.engDate.month)}" +
                    "-${dayClear(binding.engDate.dayOfMonth)}"
            val acDate = "${binding.acDate.year}-${monthClear(binding.acDate.month)}" +
                    "-${dayClear(binding.acDate.dayOfMonth)}"
            val tireDate = "${binding.tireDate.year}-${monthClear(binding.tireDate.month)}" +
                    "-${dayClear(binding.tireDate.dayOfMonth)}"

            intent.putExtra("eng-date", engDate)
            intent.putExtra("ac-date", acDate)
            intent.putExtra("tire-date", tireDate)

            startActivity(intent)
        }
    }

    private fun monthClear(month: Int): String {
        val month: Int = month + 1
        if (month < 10) {
            return "0${month}"
        }
        return month.toString()
    }

    private fun dayClear(day: Int): String {
        if (day < 10) {
            return "0${day}"
        }
        return day.toString()
    }
}