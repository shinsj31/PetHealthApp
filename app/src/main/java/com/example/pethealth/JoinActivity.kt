package com.example.pethealth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class JoinActivity: AppCompatActivity() {
    private lateinit var et_id: EditText
    private lateinit var et_pw: EditText
    private lateinit var et_name: EditText
    private lateinit var et_mail: EditText
    private lateinit var et_phone: EditText
    private lateinit var btn_join: Button
    private lateinit var arr_et: Array<EditText>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.join_layout)

        // Get intent
        val intent: Intent = getIntent()
        setResult(Activity.RESULT_CANCELED, intent)

        // Get edit text by id
        et_id = findViewById(R.id.et_join_id)
        et_pw = findViewById(R.id.et_join_pw)
        et_name = findViewById(R.id.et_join_name)
        et_mail = findViewById(R.id.et_join_mail)
        et_phone = findViewById(R.id.et_join_phone)

        arr_et = arrayOf(et_name, et_id, et_pw, et_mail,et_phone)

        // Get button & set click listener
        btn_join = findViewById(R.id.btn_join)
        btn_join.setOnClickListener {
            // check blank edit text
            if (checkBlank() == false)
                return@setOnClickListener

            edit_text_en(false)

            var data = ConnectToDB()
            var login_info = LoginData(et_id.text.toString(), et_pw.text.toString())
            data.type = DB_MODES.JOIN
            data.user_data = UserInfo(login_info, et_name.text.toString(), et_mail.text.toString(), et_phone.text.toString())

            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                if (doWork(data).contains("success", true)){
                    println("Login success! :-)")
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(this@JoinActivity, "Join success! :-)", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK, intent)
                        intent.putExtra("joined_id", data.user_data.login_data.id)
                        intent.putExtra("joined_pw", data.user_data.login_data.pw)
                        finish()
                    }
                }
                else{
                    println("Login failed...T^T")
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(this@JoinActivity, "Join failed...T^T", Toast.LENGTH_SHORT).show()
                        edit_text_en(true)
                    }
                }
            }
        }
    }


    private fun checkBlank(): Boolean{
        val arr_str: Array<String> = arrayOf("이름을", "아이디를", "패스워드를", "이메일을")
        var idx: Int = 0

        for(et in arr_et){
            if (et.text.toString() == ""){
                Toast.makeText(this, arr_str[idx]+" 입력해주세요.", Toast.LENGTH_SHORT).show()
                return false
            }
            idx++
        }
        return true
    }

    private fun edit_text_en(enable:Boolean) {
        for (et in arr_et){
            et.isEnabled = enable
        }
    }
}