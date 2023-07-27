package com.example.mobility

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.mobility.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "회원가입"

        binding.btnSignup.setOnClickListener {
            val email = binding.signupId.text.toString()
            val password = binding.signupPassword.text.toString()

            MyApplication.auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this){task ->
                binding.signupId.text.clear()
                binding.signupPassword.text.clear()
                //회원가입 성공, 실패 판단
                if(task.isSuccessful)
                {
                    MyApplication.auth.currentUser?.sendEmailVerification()?.addOnCompleteListener { sendTask ->
                        if(sendTask.isSuccessful)
                        {
                            Toast.makeText(baseContext,"회원가입 성공, 전송된 메일을 확인해 주세요.", Toast.LENGTH_SHORT).show()
                            //로그아웃화면으로
                            finish()


                        }
                        else
                        {
                            Toast.makeText(baseContext,"메일 발송 실패", Toast.LENGTH_SHORT).show()
                            //로그아웃화면으로

                        }
                    }
                }
                else
                {
                    Toast.makeText(baseContext,"회원가입 실패", Toast.LENGTH_SHORT).show()
                    //로그아웃화면으로
                    finish()
                }
            }
        }
    }
}