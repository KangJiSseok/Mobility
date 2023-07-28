package com.example.mobility

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mobility.databinding.ActivityAddInfoBinding
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.time.LocalDate
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

        title = "정비 정보 등록"


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
            val engOdo = binding.engOdo.text.toString()
            val acOdo = binding.acOdo.text.toString()
            val tireOdo = binding.tireOdo.text.toString()

            val engDate = "${binding.engDate.year}-${monthClear(binding.engDate.month)}" +
                    "-${dayClear(binding.engDate.dayOfMonth)}"
            val acDate = "${binding.acDate.year}-${monthClear(binding.acDate.month)}" +
                    "-${dayClear(binding.acDate.dayOfMonth)}"
            val tireDate = "${binding.tireDate.year}-${monthClear(binding.tireDate.month)}" +
                    "-${dayClear(binding.tireDate.dayOfMonth)}"

            // 모든 주행 거리 입력란을 제대로 입력했는지 확인
            if (engOdo.isNullOrEmpty() || acOdo.isNullOrEmpty() || tireOdo.isNullOrEmpty()) {
                var loginfailalert = AlertDialog.Builder(this)
                loginfailalert.setMessage("모든 입력란을 채워 주세요.")
                loginfailalert.setPositiveButton("확인", null)
                loginfailalert.show()
                return@setOnClickListener
            }

            // 부품 교체의 주행 거리가 현재 주행 거리보다 많은 경우 에러 리턴
            if (engOdo > odo || acOdo > odo || tireOdo > odo) {
                var loginfailalert = AlertDialog.Builder(this)
                loginfailalert.setMessage("부품 교체 당시의 주행 거리를 입력해 주세요.")
                loginfailalert.setPositiveButton("확인", null)
                loginfailalert.show()
                return@setOnClickListener
            }

            // 부품 교체 시기가 미래인 경우 에러 리턴
            if (isFutureDate(engDate) || isFutureDate(acDate) || isFutureDate(tireDate)) {
                var loginfailalert = AlertDialog.Builder(this)
                loginfailalert.setMessage("부품 교체 당시의 날짜를 입력해 주세요.")
                loginfailalert.setPositiveButton("확인", null)
                loginfailalert.show()
                return@setOnClickListener
            }

            val intent = Intent(this, RepairInfoActivity::class.java)

            intent.putExtra("car", car)
            intent.putExtra("year", year)
            intent.putExtra("odo", odo)

            intent.putExtra("eng-odo", engOdo)
            intent.putExtra("ac-odo", acOdo)
            intent.putExtra("tire-odo", tireOdo)



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

    private fun isFutureDate(dateString: String): Boolean {
        val currentDate = LocalDate.now()
        val inputDate = LocalDate.parse(dateString)

        return inputDate.isAfter(currentDate)
    }
}