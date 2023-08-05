package com.example.mobility.activity

import android.R
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mobility.MyApplication
import com.example.mobility.MyApplication.Companion.db
import com.example.mobility.databinding.ActivityCarInfoBinding
import com.example.mobility.model.ItemData
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate


class CarInfoActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityCarInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true);

        title = "차량 정보 등록"

        // 챠량정보를 DB에서 가져오기
        val data = ItemData()

        CoroutineScope(Dispatchers.Main).launch {
            val documents = withContext(Dispatchers.IO) {
                db.collection(MyApplication.auth.currentUser!!.uid).get().await()
            }
            for (document in documents) {
                when (document.id) {
                    "CarInfo" -> data.CarInfo = document.data as HashMap<String, String>
                    "RepairInfo" -> data.RepairInfo = document.data as HashMap<String, String>
                    "Profile" -> data.Profile = document.data as HashMap<String, String>
                }
            }

            binding.car.setText(data.CarInfo["model"])
            binding.year.setText(data.CarInfo["year"])
            binding.odo.setText(data.CarInfo["odo"])
        }

        // 차종, 연식, 주행 거리를 입력받음
        binding.submit.setOnClickListener {
            val car = binding.car.text.toString().trim() // 차종
            val year = binding.year.text.toString().trim() // 연식
            val odo = binding.odo.text.toString().trim() // 주행거리
            val today: LocalDate = LocalDate.now()

            // 입력 유효성 검사
            if (car.isNullOrEmpty() || year.isNullOrEmpty() || odo.isNullOrEmpty()) {
                var loginfailalert = AlertDialog.Builder(this)
                loginfailalert.setMessage("모든 입력란을 채워 주세요.")
                loginfailalert.setPositiveButton("확인", null)
                loginfailalert.show()
                return@setOnClickListener
            }

            // 차량 정보 등록 (서버)
            data.CarInfo["model"] = binding.car.text.toString()
            data.CarInfo["year"] = binding.year.text.toString()
            data.CarInfo["odo"] = binding.odo.text.toString()
            data.CarInfo["lastDate"] = today.toString()
            db.collection(MyApplication.auth.currentUser!!.uid).document("CarInfo").update(data.CarInfo as Map<String, Any>)
                .addOnCompleteListener{
                    if (it.isSuccessful){
                        Toast.makeText(applicationContext, "성공", Toast.LENGTH_SHORT).show()
                    }
                }
            finish()
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        val currentUser = MyApplication.auth?.currentUser
        if (currentUser != null) {
            Log.d("kkang","현재 유저는 ${currentUser.uid} 입니다.")
        }

    }
}