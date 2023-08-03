package com.example.mobility.activity

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.example.mobility.MyApplication
import com.example.mobility.R
import com.example.mobility.databinding.ActivityUpdateRepairBinding
import com.example.mobility.model.ItemData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate

class UpdateRepairActivity : AppCompatActivity() {
    lateinit var binding: ActivityUpdateRepairBinding
    var data = ItemData()
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateRepairBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Intent 에서 데이터 가져오기
        val part = intent.getStringExtra("part")

        title = "${part} 교체 정보 입력"

        binding.infoTitle.text = "${getParticle(part.toString(), "을", "를")}\n언제 교체하셨나요?"

        var code = ""
        when (part) {
            "엔진오일" -> code = "engine"
            "에어컨 필터" -> code = "ac"
            "타이어" -> code = "tire"
        }

        CoroutineScope(Dispatchers.Main).launch {
            val documents = withContext(Dispatchers.IO) {
                MyApplication.db.collection(MyApplication.auth.currentUser!!.uid).get().await()
            }
            for (document in documents) {
                when (document.id) {
                    "CarInfo" -> data.CarInfo = document.data as HashMap<String, String>
                    "RepairInfo" -> data.RepairInfo = document.data as HashMap<String, String>
                    "Profile" -> data.Profile = document.data as HashMap<String, String>
                }
            }

            binding.odo.text = "이전 주행 거리: ${data.RepairInfo["${code}Odo"]} km\n이전 교체 일자: ${data.RepairInfo["${code}Date"]}"
        }

        binding.submit.setOnClickListener {
            val odo = binding.newOdo.text.toString().trim()
            val date = "${binding.newDate.year}-${monthClear(binding.newDate.month)}" +
                    "-${dayClear(binding.newDate.dayOfMonth)}"

            if (odo.isEmpty()) {
                var loginfailalert = AlertDialog.Builder(this)
                loginfailalert.setMessage("모든 입력란을 채워 주세요.")
                loginfailalert.setPositiveButton("확인", null)
                loginfailalert.show()
                return@setOnClickListener
            }

            // 부품 교체 시기가 미래인 경우 에러 리턴
            if (isFutureDate(date)) {
                var loginfailalert = AlertDialog.Builder(this)
                loginfailalert.setMessage("부품 교체 당시의 날짜를 입력해 주세요.")
                loginfailalert.setPositiveButton("확인", null)
                loginfailalert.show()
                return@setOnClickListener
            }

            data.RepairInfo["${code}Odo"] = odo
            data.RepairInfo["${code}Date"] = date
            MyApplication.db.collection(MyApplication.auth.currentUser!!.uid).document("RepairInfo").update(data.RepairInfo as Map<String, Any>)
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

    // 한글 받침에 따른 조사 처리
    private fun getParticle(name: String, firstValue: String, secondValue: String?): String? {
        val lastName = name[name.length - 1]

        if (lastName.code < 0xAC00 || lastName.code > 0xD7A3) {
            return name
        }
        val selectedValue = if ((lastName.code - 0xAC00) % 28 > 0) firstValue else secondValue!!
        return name + selectedValue
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}