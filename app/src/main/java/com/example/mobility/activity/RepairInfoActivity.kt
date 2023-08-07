package com.example.mobility.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mobility.MyApplication
import com.example.mobility.MyApplication.Companion.db
import com.example.mobility.R
import com.example.mobility.databinding.ActivityRepairInfoBinding
import com.example.mobility.model.ItemData
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar

class RepairInfoActivity : AppCompatActivity() {

    var data = ItemData()
    lateinit var binding: ActivityRepairInfoBinding

    private var shortAnimationDuration: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRepairInfoBinding.inflate(layoutInflater)
        shortAnimationDuration = resources.getInteger(android.R.integer.config_longAnimTime)
        setContentView(binding.root)

        title = "남은 주행 거리와 기간"

        supportActionBar?.setDisplayHomeAsUpEnabled(true);

        binding.layoutEng.setOnClickListener {
            val intent = Intent(this, UpdateRepairActivity::class.java)
            intent.putExtra("part", "엔진오일")
            startActivity(intent)
        }

        binding.layoutAc.setOnClickListener {
            val intent = Intent(this, UpdateRepairActivity::class.java)
            intent.putExtra("part", "에어컨 필터")
            startActivity(intent)
        }

        binding.layoutTire.setOnClickListener {
            val intent = Intent(this, UpdateRepairActivity::class.java)
            intent.putExtra("part", "타이어")
            startActivity(intent)
        }

    }


    // 화면이 다시 시작할 때마다 업데이트
    override fun onResume() {
        super.onResume()
        binding.layoutLoding.visibility = View.VISIBLE
        //ㅂㅣ동기로 처리
        CoroutineScope(Dispatchers.Main).launch {
            val documents = withContext(Dispatchers.IO){
                db.collection(MyApplication.auth.currentUser!!.uid).get().await()
            }
            for (document in documents){
                when (document.id) {
                    "CarInfo" -> data.CarInfo = document.data as HashMap<String, String>
                    "RepairInfo" -> data.RepairInfo = document.data as HashMap<String, String>
                    "Profile" -> data.Profile = document.data as HashMap<String, String>
                }
            }

            var odo = data.CarInfo["odo"].toString()

            // 엔진오일
            var engOdo = data.RepairInfo["engineOdo"].toString()
            var engDate = data.RepairInfo["engineDate"].toString()
            val eOdoRepair = 15000
            val eDataRepair = 365
            this.dispInfo("엔진오일", odo, engOdo, engDate, eOdoRepair, eDataRepair)

            // 에어컨 필터
            var acOdo = data.RepairInfo["acOdo"].toString()
            var acDate = data.RepairInfo["acDate"].toString()
            val acOdoRepair = 15000
            val acDateRepair = 180
            this.dispInfo("에어컨 필터", odo, acOdo, acDate, acOdoRepair, acDateRepair)

            // 타이어
            var tireOdo = data.RepairInfo["tireOdo"].toString()
            var tireDate = data.RepairInfo["tireDate"].toString()
            val tireOdoRepair = 45000
            val tireDateRepair = 1095 // 3년
            this.dispInfo("타이어", odo, tireOdo, tireDate, tireOdoRepair, tireDateRepair)

            binding.layoutLoding.animate()
                .alpha(0f)
                .setDuration(shortAnimationDuration.toLong())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        binding.layoutLoding.visibility = View.GONE
                    }
                })
        }
    }
    /*
    * 교체 주기 계산 함수
    */
    private fun calculate(totalOdo: String, odo: String, date: String, repairOdo: Int, repairDate: Int): Array<Any> {
        // 교체 주행거리와 현재 누적 주행거리의 차
        val totalOdos = totalOdo.toInt()
        val odos = odo.toInt()
        var diffOdo = totalOdos - odos

        // 교체한지 며칠 지났는지 일수로 계산
        var diffDate = diffDate(date)

        // 주행거리 또는 교체주기 둘 다 초과한 경우
        if (diffOdo >= repairOdo && diffDate >= repairDate) {
            return arrayOf("both", diffOdo, diffDate)
        }
        else if (diffOdo >= repairOdo) { // 주행 거리만 초과한 경우
            return arrayOf("odo", diffOdo, diffDate)
        }
        else if (diffDate >= repairDate) { // 교체 주기만 초과한 경우
            return arrayOf("date", diffOdo, diffDate)
        }

        // 둘다 초과하지 않은 경우
        return arrayOf("false", diffOdo, diffDate)
    }

    /*
    * 화면 출력 함수
    */
    private fun CoroutineScope.dispInfo(text: String, odo: String, cOdo: String, cDate: String, cOdoRepair: Int, cDateRepair: Int, ) {
        val (cRepairNeeded, cDiffOdo, cDiffDate) = calculate(odo, cOdo, cDate, cOdoRepair, cDateRepair)

        val cOdoRatio: Double = (cDiffOdo.toString().toDouble() / cOdoRepair) * 100
        val cDateRatio: Double = (cDiffDate.toString().toDouble() / cDateRepair) * 100

        var cOdoProgs = binding.engOdoProgs
        var cDateProgs = binding.engDateProgs
        var cText = binding.engText

        if (text == "에어컨 필터") {
            cOdoProgs = binding.acOdoProgs
            cDateProgs = binding.acDateProgs
            cText = binding.acText
        }
        else if (text == "타이어") {
            cOdoProgs = binding.tireOdoProgs
            cDateProgs = binding.tireDateProgs
            cText = binding.tireText
        }

        cOdoProgs.progress = cOdoRatio.toInt()
        cDateProgs.progress = cDateRatio.toInt()

        val status = cRepairNeeded.toString()

        if (status == "false") {
            cText.text = "교체까지 ${addCommasToNumber(cOdoRepair - cDiffOdo.toString().toInt())}km, " +
                    "${formatDaysToYearsMonthsDays(cDateRepair - cDiffDate.toString().toInt())} 남았습니다."
        }
        else {
            if (status == "odo") {
                cOdoProgs.progress = 100
                cText.text = "주행거리가 도달하여 ${getParticle(text, "을", "를")} 교체해야 합니다."
            }
            if (status == "date") {
                cDateProgs.progress = 100
                cText.text = "교체기간이 도달하여 ${getParticle(text, "을", "를")} 교체해야 합니다."
            }
            if (status == "both") {
                cOdoProgs.progress = 100
                cDateProgs.progress = 100
                cText.text = "주행거리 및 교체기간이 모두 도달하여 ${getParticle(text, "을", "를")} 교체해야 합니다."
            }
        }
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


    private fun daysToYearMonthDay(days: Int): Triple<Int, Int, Int> {
        val years = days / 365
        val remainingDaysAfterYears = days % 365

        val months = remainingDaysAfterYears / 30
        val remainingDaysAfterMonths = remainingDaysAfterYears % 30

        return Triple(years, months, remainingDaysAfterMonths)
    }

    private fun formatDaysToYearsMonthsDays(days: Int): String {
        val (years, months, remainingDays) = daysToYearMonthDay(days)

        val formattedString = buildString {
            if (years > 0) append("${years}년 ")
            if (months > 0) append("${months}개월 ")
            if (remainingDays > 0) append("${remainingDays}일")
        }

        return formattedString.trim()
    }

    private fun addCommasToNumber(number: Int): String {
        val formattedString = "%,d".format(number)
        return formattedString
    }

    private fun diffDate(date: String): Int {
        // 오늘 날짜
        val today = Calendar.getInstance()

        // date 변수에는 2023-07-26 형식의 날짜가 들어감
        val sf = SimpleDateFormat("yyyy-MM-dd")
        val dateValue = sf.parse(date)

        // 교체한지 며칠 지났는지 일수로 계산
        var diffDate = (today.time.time - dateValue.time) / (60 * 60 * 24 * 1000)
        return diffDate.toInt()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

}
