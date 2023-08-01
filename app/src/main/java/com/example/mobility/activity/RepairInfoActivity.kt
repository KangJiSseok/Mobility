package com.example.mobility.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.example.mobility.MyApplication
import com.example.mobility.MyApplication.Companion.db
import com.example.mobility.R
import com.example.mobility.databinding.ActivityRepairInfoBinding
import com.example.mobility.model.ItemData
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar

class RepairInfoActivity : AppCompatActivity() {

    var data = ItemData()
    lateinit var binding: ActivityRepairInfoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRepairInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "남은 주행 거리와 기간"

        // 챠량정보를 DB에서 가져오기

        var car = data.CarInfo["model"].toString()
        var year = data.CarInfo["year"].toString()
        var odo = data.CarInfo["odo"].toString()

        // 엔진오일
        var engOdo = data.RepairInfo["engineOdo"].toString()
        var engDate = data.RepairInfo["engineDate"].toString()
        Log.d("engineOdo", engOdo)
        Log.d("engineDate", engDate)
        val eOdoRepair = 15000
        val eDateRepair = 365
        this.dispInfo("엔진오일", odo, engOdo, engDate, eOdoRepair, eDateRepair)

        // 에어컨 필터
        var acOdo = data.RepairInfo["acOdo"].toString()
        var acDate = data.RepairInfo["acDate"].toString()
        Log.d("acOdo", acOdo)
        Log.d("acDate", acDate)
        val acOdoRepair = 15000
        val acDateRepair = 180
        this.dispInfo("에어컨 필터", odo, acOdo, acDate, acOdoRepair, acDateRepair)

        // 타이어
        var tireOdo = data.RepairInfo["tireOdo"].toString()
        var tireDate = data.RepairInfo["tireDate"].toString()
        Log.d("tireOdo", tireOdo)
        Log.d("tireDate", tireDate)
        val tireOdoRepair = 45000
        val tireDateRepair = 1095 // 3년
        this.dispInfo("타이어", odo, tireOdo, tireDate, tireOdoRepair, tireDateRepair)
    }

    /*
    * 교체 주기 계산 함수
    */
    private fun calculate(totalOdo: String, odo: String, date: String, repairOdo: Int, repairDate: Int): Array<Any> {
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
    private fun dispInfo(text: String, odo: String, cOdo: String, cDate: String, cOdoRepair: Int, cDateRepair: Int, ) {
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
                cText.text = "주행거리가 도달하여 ${text}을(를) 교체해야 합니다."
            }
            if (status == "date") {
                cDateProgs.progress = 100
                cText.text = "교체기간이 도달하여 ${text}을(를) 교체해야 합니다."
            }
            if (status == "both") {
                cOdoProgs.progress = 100
                cDateProgs.progress = 100
                cText.text = "주행거리 및 교체기간이 모두 도달하여 ${text}을(를) 교체해야 합니다."
            }
        }
    }

    // 화면이 다시 시작할 때마다 업데이트
    override fun onResume() {
        super.onResume()

//        MyApplication.db.collection(MyApplication.auth.currentUser!!.uid).document("CarInfo").get().addOnSuccessListener { task ->
//            if (task != null){
//                data.CarInfo = task.data as HashMap<String, String>
//                Log.d("kkang", "${task.data?.get("name")}")
//                Log.d("kkang", "${data.CarInfo}")
//            }
//        }

        db.collection(MyApplication.auth.currentUser!!.uid).get().addOnSuccessListener { documents ->
            for (document in documents){
                when (document.id) {
                    "CarInfo" -> data.CarInfo = document.data as HashMap<String, String>
                    "RepairInfo" -> data.RepairInfo = document.data as HashMap<String, String>
                    "Profile" -> data.Profile = document.data as HashMap<String, String>
                }
            }
        }
    }

    // 메뉴 추가
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                FirebaseAuth.getInstance().signOut()
                finish()
            }
            R.id.setting -> startActivity(Intent(this, CarInfoActivity::class.java))
            R.id.setting -> startActivity(Intent(this,AddInfoActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

}
