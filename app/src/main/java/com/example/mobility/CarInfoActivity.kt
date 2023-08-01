package com.example.mobility

import android.R
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mobility.MyApplication.Companion.db
import com.example.mobility.databinding.ActivityCarInfoBinding
import com.example.mobility.model.ItemData
import com.google.firebase.auth.FirebaseAuth



class CarInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityCarInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true);

        title = "차량 정보 등록"

        // 챠량정보를 DB에서 가져오기
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

        // 차종, 연식, 주행 거리를 입력받음
        binding.submit.setOnClickListener {
            val car = binding.car.text.toString().trim() // 차종
            val year = binding.year.text.toString().trim() // 연식
            val odo = binding.odo.text.toString().trim() // 주행거리

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
            db.collection(MyApplication.auth.currentUser!!.uid).document("CarInfo").update(data.CarInfo as Map<String, Any>)
                .addOnCompleteListener{
                    if (it.isSuccessful){
                        Toast.makeText(applicationContext, "성공", Toast.LENGTH_SHORT).show()
                    }
                }

////            // AddInfoActivity 실행
////            val intent = Intent(this, AddInfoActivity::class.java)
//            // RepairActivity 실행
//            val intent = Intent(this, RepairInfoActivity::class.java)
////
////            // AddInfoActivity에 정보 전달
////            val array = arrayOf(car, year, odo)
////            intent.putExtra("infoArray", array)
//
//            // Activity 실행
//            startActivity(intent)
            finish()
        }

        binding.logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this,MainActivity::class.java))
            Toast.makeText(this,"로그아웃",Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}