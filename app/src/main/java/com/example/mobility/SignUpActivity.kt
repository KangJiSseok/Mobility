package com.example.mobility

import android.R
import android.content.Intent
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import com.example.mobility.MyApplication.Companion.db
import com.example.mobility.databinding.ActivitySignUpBinding
import com.example.mobility.model.ItemData
import com.google.firebase.auth.FirebaseAuth
import java.lang.Exception

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);

        title = "회원가입"

        binding.btnSignup.setOnClickListener {
            val email = binding.signupId.text.toString()
            val password = binding.signupPassword.text.toString()

            if (binding.signupPassword.text.toString() != binding.checkPassword.text.toString()) {
                Toast.makeText(applicationContext, "비밀번호를 다시 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            MyApplication.auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    //회원가입 성공, 실패 판단
                    if (task.isSuccessful) {
                        MyApplication.auth.currentUser?.sendEmailVerification()
                            ?.addOnCompleteListener { sendTask ->
                                if (sendTask.isSuccessful) {
                                    val data = ItemData()
                                    data.profile.replace("id", binding.signupId.text.toString())
                                    data.profile.replace("name", binding.signupName.text.toString())
                                    data.profile.replace(
                                        "phone number",
                                        binding.signupPhone.text.toString()
                                    )
                                    db.collection(MyApplication.auth.currentUser!!.uid)
                                        .document("Profile").set(data.profile)
                                    Toast.makeText(
                                        this,
                                        "회원가입 성공, 전송된 메일을 확인해 주세요.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    FirebaseAuth.getInstance().signOut()
                                    startActivity(Intent(this,MainActivity::class.java))
                                    finish()
                                } else {
                                    Toast.makeText(this, "메일 발송 실패", Toast.LENGTH_SHORT).show()
                                    //로그아웃화면으로

                                }
                            }
                    } else {
                        val exception = task.exception
                        if (exception is FirebaseAuthUserCollisionException) {
                            // Handle the email already exists error here
                            Toast.makeText(
                                this,
                                "이미 존재하는 이메일입니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            // Handle other errors here
                            Toast.makeText(
                                this,
                                "회원가입 실패",
                                Toast.LENGTH_SHORT
                            ).show()
                            //로그아웃화면으로
                        }
                    }
                }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}