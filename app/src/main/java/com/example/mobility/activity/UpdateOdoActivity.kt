package com.example.mobility.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.mobility.MyApplication
import com.example.mobility.R
import com.example.mobility.databinding.ActivityUpdateOdoBinding
import com.example.mobility.model.ItemData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UpdateOdoActivity : AppCompatActivity() {
    var data = ItemData()
    lateinit var binding: ActivityUpdateOdoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateOdoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "주행 거리 업데이트"

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

            binding.previousOdo.text = "이전 주행 거리: ${data.CarInfo["odo"]} km"
        }

        binding.submit.setOnClickListener {
            val odo = binding.odo.text.toString()
            if (odo.isNotEmpty()) {
                // 차량 정보 등록 (서버)
                data.CarInfo["odo"] = binding.odo.text.toString()
                MyApplication.db.collection(MyApplication.auth.currentUser!!.uid).document("CarInfo").update(data.CarInfo as Map<String, Any>)
                    .addOnCompleteListener{
                        if (it.isSuccessful){
                            Toast.makeText(applicationContext, "성공", Toast.LENGTH_SHORT).show()
                        }
                    }
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
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

            binding.previousOdo.text = "이전 주행 거리: ${data.CarInfo["odo"]} km"
        }
    }
}