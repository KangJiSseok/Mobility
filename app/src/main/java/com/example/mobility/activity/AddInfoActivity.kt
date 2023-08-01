package com.example.mobility.activity

import android.R
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.DatePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mobility.MyApplication.Companion.db
import com.example.mobility.databinding.ActivityAddInfoBinding
import com.example.mobility.model.ItemData
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.time.LocalDate


class AddInfoActivity : AppCompatActivity() {
    lateinit var car: String
    lateinit var year: String
    lateinit var odo: String

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAddInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true);

        title = "정비 정보 등록"

        val data = ItemData()

        db.collection(MyApplication.auth.currentUser!!.uid).get().addOnSuccessListener { documents ->
            for (document in documents){
                when (document.id) {
                    "CarInfo" -> data.CarInfo = document.data as HashMap<String, String>
                    "RepairInfo" -> data.RepairInfo = document.data as HashMap<String, String>
                    "Profile" -> data.Profile = document.data as HashMap<String, String>
                }
            }
        }
//        // intent 값 받기
//        val intent: Intent = getIntent()
//        val array = intent.getStringArrayExtra("infoArray")
//
//        car = array?.get(0).toString()
//        year = array?.get(1).toString()
//        odo = array?.get(2).toString()

        // 값 전달 테스트
//        Toast.makeText(this, "차종: ${car}, 연식: ${year}, 주행거리: ${odo}", Toast.LENGTH_SHORT).show()

        // 등록하기 버튼 클릭 시 DB 저장
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
            if (engOdo.toInt() > odo.toInt() || acOdo.toInt() > odo.toInt() || tireOdo.toInt() > odo.toInt()) {
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

//            val intent = Intent(this, RepairInfoActivity::class.java)
//
//            intent.putExtra("car", car)
//            intent.putExtra("year", year)
//            intent.putExtra("odo", odo)
//
//            intent.putExtra("eng-odo", engOdo)
//            intent.putExtra("ac-odo", acOdo)
//            intent.putExtra("tire-odo", tireOdo)
//
//
//
//            intent.putExtra("eng-date", engDate)
//            intent.putExtra("ac-date", acDate)
//            intent.putExtra("tire-date", tireDate)
//
            data.RepairInfo["engineOdo"] = engOdo
            data.RepairInfo["engineDate"] = engDate
            data.RepairInfo["acOdo"] = acOdo
            data.RepairInfo["acDate"] = acDate
            data.RepairInfo["tireOdo"] = tireOdo
            data.RepairInfo["tireDate"] = tireDate
            db.collection(MyApplication.auth.currentUser!!.uid).document("RepairInfo").update(data.RepairInfo as Map<String, Any>)
                .addOnCompleteListener{
                    if (it.isSuccessful){
                        Toast.makeText(applicationContext, "성공", Toast.LENGTH_SHORT).show()
                    }
                }
            finish()
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun isFutureDate(dateString: String): Boolean {
        val currentDate = LocalDate.now()
        val inputDate = LocalDate.parse(dateString)

        return inputDate.isAfter(currentDate)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}