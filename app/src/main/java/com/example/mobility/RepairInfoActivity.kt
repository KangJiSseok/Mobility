package com.example.mobility

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.mobility.MyApplication.Companion.db
import com.example.mobility.databinding.ActivityRepairInfoBinding
import com.example.mobility.model.ItemData
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar

class RepairInfoActivity : AppCompatActivity() {

    var data = ItemData()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRepairInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "남은 주행 거리와 기간"

        // 챠량정보를 DB에서 가져오기

        var car = data.CarInfo["model"].toString()
        var year = data.CarInfo["year"].toString()
        var odo = data.CarInfo["odo"].toString()


        var engOdo = data.RepairInfo["engineOdo"].toString()
        var engDate = data.RepairInfo["engineDate"].toString()

        // 엔진오일
        val eOdoRepair = 15000
        val eDateRapair = 365
        val (eRepairNeeded, eDiffOdo, eDiffDate) = calculate(odo, engOdo, engDate, eOdoRepair, eDateRapair)

        val engOdoRatio: Double = (eDiffOdo.toString().toDouble() / eOdoRepair) * 100
        val engDateRatio: Double = (eDiffDate.toString().toDouble() / eDateRapair) * 100

        binding.engOdoProgs.progress = engOdoRatio.toInt()
        binding.engDateProgs.progress = engDateRatio.toInt()

        val engStatus = eRepairNeeded.toString()
        Log.d("ediffOdo", eDiffOdo.toString())

        if (engStatus == "false") {
            binding.engText.text = "교체까지 ${addCommasToNumber(eOdoRepair - eDiffOdo.toString().toInt())}km, " +
                    "${formatDaysToYearsMonthsDays(eDateRapair - eDiffDate.toString().toInt())} 남았습니다."
        }
        else {
            if (engStatus == "odo") {
                binding.engOdoProgs.progress = 100
                binding.engText.text = "주행거리가 도달하여 엔진오일을 교체해야 합니다."
            }
            if (engStatus == "date") {
                binding.engDateProgs.progress = 100
                binding.engText.text = "교체기간이 도달하여 엔진오일을 교체해야 합니다."
            }
            if (engStatus == "both") {
                binding.engOdoProgs.progress = 100
                binding.engDateProgs.progress = 100
                binding.engText.text = "주행거리 및 교체기간이 모두 도달하여 엔진오일을 교체해야 합니다."
            }
        }

        // 에어컨 필터
        var acOdo = data.RepairInfo["acOdo"].toString()
        var acDate = data.RepairInfo["acDate"].toString()
        val acOdoRepair = 15000
        val acDateRepair = 180
        val (acRepairNeeded, acDiffOdo, acDiffDate) = calculate(odo, acOdo, acDate, acOdoRepair, acDateRepair)

        val acOdoRatio: Double = (acDiffOdo.toString().toDouble() / acOdoRepair) * 100
        val acDateRatio: Double = (acDiffDate.toString().toDouble() / acDateRepair) * 100

        binding.acOdoProgs.progress = acOdoRatio.toInt()
        binding.acDateProgs.progress = acDateRatio.toInt()

        val acStatus = acRepairNeeded.toString()

        if (acStatus == "false") {
            binding.acText.text = "교체까지 ${addCommasToNumber(acOdoRepair - acDiffOdo.toString().toInt())}km, " +
                    "${formatDaysToYearsMonthsDays(acDateRepair - acDiffDate.toString().toInt())} 남았습니다."
        }
        else {
            if (acStatus == "odo") {
                binding.acOdoProgs.progress = 100
                binding.acText.text = "주행거리가 도달하여 에어컨 필터를 교체해야 합니다."
            }
            if (acStatus == "date") {
                binding.acDateProgs.progress = 100
                binding.acText.text = "교체기간이 도달하여 에어컨 필터를 교체해야 합니다."
            }
            if (acStatus == "both") {
                binding.acOdoProgs.progress = 100
                binding.acDateProgs.progress = 100
                binding.acText.text = "주행거리 및 교체기간이 모두 도달하여 에어컨 필터를 교체해야 합니다."
            }
        }

        // 타이어
        var tireOdo = data.RepairInfo["tireOdo"].toString()
        var tireDate = data.RepairInfo["tireDate"].toString()
        val tireOdoRepair = 45000
        val tireDateRepair = 1095 // 3년
        val (tireRepairNeeded, tireDiffOdo, tireDiffDate) = calculate(odo, tireOdo, tireDate, tireOdoRepair, tireDateRepair)

        val tireOdoRatio: Double = (tireDiffOdo.toString().toDouble() / tireOdoRepair) * 100
        val tireDateRatio: Double = (tireDiffDate.toString().toDouble() / tireDateRepair) * 100

        binding.tireOdoProgs.progress = tireOdoRatio.toInt()
        binding.tireDateProgs.progress = tireDateRatio.toInt()

        val tireStatus = tireRepairNeeded.toString()

        if (tireStatus == "false") {
            binding.tireText.text = "교체까지 ${addCommasToNumber(tireOdoRepair - tireDiffOdo.toString().toInt())}km, " +
                    "${formatDaysToYearsMonthsDays(tireDateRepair - tireDiffDate.toString().toInt())} 남았습니다."
        }
        else {
            if (tireStatus == "odo") {
                binding.tireOdoProgs.progress = 100
                binding.tireText.text = "주행거리가 도달하여 타이어를 교체해야 합니다."
            }
            if (tireStatus == "date") {
                binding.tireDateProgs.progress = 100
                binding.tireText.text = "교체기간이 도달하여 타이어를 교체해야 합니다."
            }
            if (tireStatus == "both") {
                binding.tireOdoProgs.progress = 100
                binding.tireDateProgs.progress = 100
                binding.tireText.text = "주행거리 및 교체기간이 모두 도달하여 타이어를 교체해야 합니다."
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
            R.id.setting -> startActivity(Intent(this,CarInfoActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

}
