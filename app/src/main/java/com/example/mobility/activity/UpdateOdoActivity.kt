package com.example.mobility.activity

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.example.mobility.MyApplication
import com.example.mobility.NotificationReceiver
import com.example.mobility.R
import com.example.mobility.databinding.ActivityUpdateOdoBinding
import com.example.mobility.model.ItemData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar

class UpdateOdoActivity : AppCompatActivity() {
    private lateinit var alarmManager:AlarmManager

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityUpdateOdoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "주행 거리 업데이트"
        supportActionBar?.setDisplayHomeAsUpEnabled(true);

        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        var data = ItemData()

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

            val diff = diffDate(data.CarInfo["lastDate"]!!)
            if (diff > 0) {
                binding.updateText.text = "${diff}일째 업데이트되지 않았어요"
            }
            binding.previousOdo.text = "이전 주행 거리: ${data.CarInfo["odo"]} km"
        }

        binding.submit.setOnClickListener {
            val odo = binding.odo.text.toString().trim()
            if (odo.toInt() < data.CarInfo["odo"]!!.toInt()) {
                var loginfailalert = AlertDialog.Builder(this)
                loginfailalert.setMessage("입력된 주행 거리가 이전 주행 거리보다 작습니다.")
                loginfailalert.setPositiveButton("확인", null)
                loginfailalert.show()
                return@setOnClickListener
            }
            if (odo.isNullOrEmpty()) {
                var loginfailalert = AlertDialog.Builder(this)
                loginfailalert.setMessage("주행 거리를 입력해 주세요.")
                loginfailalert.setPositiveButton("확인", null)
                loginfailalert.show()
                return@setOnClickListener
            }

            val today: LocalDate = LocalDate.now()
            data.CarInfo["lastDate"] = today.toString()
            data.CarInfo["odo"] = binding.odo.text.toString()
            MyApplication.db.collection(MyApplication.auth.currentUser!!.uid).document("CarInfo").update(data.CarInfo as Map<String, Any>)
                .addOnCompleteListener{
                    if (it.isSuccessful){
                        Toast.makeText(applicationContext, "주행 거리가 업데이트 되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            CoroutineScope(Dispatchers.Default).launch {
                setNotification(today.toString())
            }
            finish()
        }
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

    private fun setNotification(date: String){
        val intent = Intent(this, NotificationReceiver::class.java)

        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(this,111,intent,PendingIntent.FLAG_MUTABLE )
        } else {
            PendingIntent.getBroadcast(this,111,intent,PendingIntent.FLAG_UPDATE_CURRENT )
        }

        val calender = Calendar.getInstance()
        val alarmDate = "$date 18:00:00"
        val sf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        calender.time = sf.parse(alarmDate)
        calender.add(Calendar.DATE, 14)

        alarmManager.set(AlarmManager.RTC, calender.timeInMillis, pendingIntent);
    }
}
