package com.example.mobility.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.example.mobility.MyApplication
import com.example.mobility.R
import com.example.mobility.databinding.ActivityUpdateRepairBinding
import com.example.mobility.model.ItemData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UpdateRepairActivity : AppCompatActivity() {
    lateinit var binding: ActivityUpdateRepairBinding
    var data = ItemData()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateRepairBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Intent 에서 데이터 가져오기
        val intent = getIntent()
        val part = intent.getStringExtra("part")

        title = "${part} 교체 정보 입력"

        binding.infoTitle.text = "${getParticle(part.toString(), "을", "를")}\n언제 교체하셨나요?"

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

            var code = ""
            when (part) {
                "엔진오일" -> code = "engine"
                "에어컨 필터" -> code = "ac"
                "타이어" -> code = "tire"
            }
            binding.odo.text = "현재 주행 거리: ${data.CarInfo["odo"]} km\n이전 교체 일자: ${data.RepairInfo["${code}Date"]}"
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}