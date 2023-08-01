package com.example.mobility

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.core.text.set
import com.example.mobility.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity() {
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
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        //로그인 시작
        binding.btnLogin.setOnClickListener {
            val email = binding.ID.text.toString()
            val password = binding.PassWord.text.toString()

            if (isValidEmail(email))
            {
                Log.d("kkang", "email : $email, password : $password")

                MyApplication.auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        binding.ID.text.clear()
                        binding.PassWord.text.clear()
                        if (task.isSuccessful) {
                            if (MyApplication.checkAuth()) {
                                MyApplication.email = email
                                //로그인화면으로 이동
                                startActivity(Intent(this, RepairInfoActivity::class.java))
                            } else {
                                Toast.makeText(
                                    baseContext,
                                    "전송된 메일로 이메일 인증이 되지 않았습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            val exception = task.exception
                            if (exception is FirebaseAuthInvalidUserException) {
                                Toast.makeText(
                                    baseContext,
                                    "해당 이메일로 가입한 유저가 없습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else if (exception is FirebaseAuthInvalidCredentialsException) {
                                binding.ID.setText(email)
                                Toast.makeText(baseContext, "비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(baseContext, "로그인 실패", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

            }
            else
            {
                Toast.makeText(baseContext, "이메일 형식이 아닙니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }

    override fun onStart() {
        super.onStart()
        moveMainpage(MyApplication.auth?.currentUser)
    }

    private fun moveMainpage(user:FirebaseUser?) {
        if(user != null)
        {
            startActivity(Intent(this,RepairInfoActivity::class.java))
            Toast.makeText(applicationContext, user.email.toString() + "님 환영합니다.", Toast.LENGTH_SHORT).show()
        }
    }
}