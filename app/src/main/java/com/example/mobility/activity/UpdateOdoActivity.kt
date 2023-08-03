package com.example.mobility.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
        supportActionBar?.setDisplayHomeAsUpEnabled(true);

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

            data.CarInfo["odo"] = binding.odo.text.toString()
            MyApplication.db.collection(MyApplication.auth.currentUser!!.uid).document("CarInfo").update(data.CarInfo as Map<String, Any>)
                .addOnCompleteListener{
                    if (it.isSuccessful){
                        Toast.makeText(applicationContext, "주행 거리가 업데이트 되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}