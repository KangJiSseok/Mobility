package com.example.mobility.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import com.example.mobility.MyApplication
import com.example.mobility.R
import com.example.mobility.databinding.ActivityRepairInfoBinding
import com.example.mobility.databinding.ActivitySimplificationBinding
import com.example.mobility.model.ItemData
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar

class SimplificationActivity : AppCompatActivity() {

    var data = ItemData()
    lateinit var binding: ActivitySimplificationBinding
    private var shortAnimationDuration: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySimplificationBinding.inflate(layoutInflater)
        shortAnimationDuration = resources.getInteger(android.R.integer.config_longAnimTime)
        setContentView(binding.root)

        binding.carName.setOnClickListener {
            val intent = Intent(this, CarInfoActivity::class.java)
            startActivity(intent)
        }

        binding.odoLayout.setOnClickListener {
            val intent = Intent(this, UpdateOdoActivity::class.java)
            startActivity(intent)
        }

        binding.neededUpdate.setOnClickListener {
            val intent = Intent(this, UpdateOdoActivity::class.java)
            startActivity(intent)
        }

        binding.mapRepair.setOnClickListener {
            val intent = Intent(this, GoogleMapActivity::class.java)
            intent.putExtra("part", "car_repair")
            startActivity(intent)
        }

        binding.mapGas.setOnClickListener {
            val intent = Intent(this, GoogleMapActivity::class.java)
            intent.putExtra("part", "gas_station")
            startActivity(intent)
        }

        binding.mapWash.setOnClickListener {
            val intent = Intent(this, GoogleMapActivity::class.java)
            intent.putExtra("part", "car_wash")
            startActivity(intent)
        }

        binding.all.setOnClickListener {
            val intent = Intent(this, RepairInfoActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.layoutLoding.visibility = View.VISIBLE
        //ㅂㅣ동기로 처리
        CoroutineScope(Dispatchers.Main).launch {
            val documents = withContext(Dispatchers.IO){
                MyApplication.db.collection(MyApplication.auth.currentUser!!.uid).get().await()
            }
            for (document in documents){
                when (document.id) {
                    "CarInfo" -> data.CarInfo = document.data as HashMap<String, String>
                    "RepairInfo" -> data.RepairInfo = document.data as HashMap<String, String>
                    "Profile" -> data.Profile = document.data as HashMap<String, String>
                }
            }

            // 주행거리 업데이트 확인
            var lastDate = data.CarInfo["lastDate"].toString()
            if (diffDate(lastDate) > 14) {
                binding.neededUpdate.visibility = android.view.View.VISIBLE
                binding.updateDate.text = "업데이트한지 ${diffDate(lastDate)}일이 지났습니다."
            }
            else {
                binding.neededUpdate.visibility = android.view.View.GONE
            }

            var car = data.CarInfo["model"].toString()
            var year = data.CarInfo["year"].toString()
            var odo = data.CarInfo["odo"].toString()
            binding.totalOdo.text = "${addCommasToNumber(odo.toInt())} km"

            if (year != "0" && car != "0")
                binding.carName.text = "${year}년식 ${car}"

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

    // 메뉴 추가
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                FirebaseAuth.getInstance().signOut()
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

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

        lateinit var cProgs:ProgressBar

        when (text) {
            "엔진오일" -> cProgs = binding.sEngProgs
            "에어컨 필터" -> cProgs = binding.sAcProgs
            "타이어" -> cProgs = binding.sTireProgs
        }

        if ( cOdoRatio.toInt() >= cDateRatio.toInt() ) cProgs.progress = cOdoRatio.toInt() else cProgs.progress = cDateRatio.toInt()
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
}