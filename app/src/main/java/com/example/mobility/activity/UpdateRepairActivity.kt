package com.example.mobility.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mobility.R
import com.example.mobility.databinding.ActivityUpdateRepairBinding

class UpdateRepairActivity : AppCompatActivity() {
    lateinit var binding: ActivityUpdateRepairBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateRepairBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Intent 에서 데이터 가져오기
        val intent = getIntent()
        val part = intent.getStringExtra("part")

        title = "${part} 교체 정보 입력"

        binding.infoTitle.text = "${getParticle(part.toString(), "을", "를")}\n언제 교체하셨나요?"
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
}