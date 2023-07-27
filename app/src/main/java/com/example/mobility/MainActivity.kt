package com.example.mobility

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.mobility.databinding.ActivityCarInfoBinding
import com.example.mobility.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "모빌리티 관리 앱"


        binding.btnSkip.setOnClickListener {
            val intent = Intent(this, CarInfoActivity::class.java)
            startActivity(intent)
        }

        //회원가입 시작
        binding.btnGotoSignup.setOnClickListener {
            val email = binding.ID.text.toString()
            val password = binding.PassWord.text.toString()

            MyApplication.auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this){task ->
                binding.ID.text.clear()
                binding.PassWord.text.clear()
                //회원가입 성공, 실패 판단
                if(task.isSuccessful)
                {
                    MyApplication.auth.currentUser?.sendEmailVerification()?.addOnCompleteListener { sendTask ->
                        if(sendTask.isSuccessful)
                        {
                            Toast.makeText(baseContext,"회원가입 성공, 전송된 메일을 확인해 주세요.",Toast.LENGTH_SHORT).show()
                            //로그아웃화면으로


                        }
                        else
                        {
                            Toast.makeText(baseContext,"메일 발송 실패",Toast.LENGTH_SHORT).show()
                            //로그아웃화면으로

                        }
                    }
                }
                else
                {
                    Toast.makeText(baseContext,"회원가입 실패",Toast.LENGTH_SHORT).show()
                    //로그아웃화면으로
                }
            }
        }

        //로그인 시작
        binding.btnLogin.setOnClickListener {
            val email = binding.ID.text.toString()
            val password = binding.PassWord.text.toString()
            Log.d("kkang","email:$email, password:$password")
            MyApplication.auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this){task->
                binding.ID.text.clear()
                binding.PassWord.text.clear()
                if(task.isSuccessful)
                {
                    if(MyApplication.checkAuth())
                    {
                        MyApplication.email=email
                        //로그인화면으로 이동
                        startActivity(Intent(this,CarInfoActivity::class.java))
                    }
                    else
                    {
                        Toast.makeText(baseContext, "전송된 메일로 이메일 인증이 되지 않았습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                else
                {
                    Toast.makeText(baseContext, "로그인 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}