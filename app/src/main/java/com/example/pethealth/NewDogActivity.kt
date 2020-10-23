package com.example.pethealth

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class NewDogActivity: AppCompatActivity() {
    private lateinit var et_dname: EditText
    private lateinit var et_dbreed: EditText
    private lateinit var et_dheight: EditText
    private lateinit var et_dlength: EditText
    private lateinit var et_dweight: EditText
    private lateinit var et_dage: EditText
    private lateinit var et_dgoal: EditText
    private lateinit var arr_et: Array<EditText>

    private lateinit var btn_join: Button



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_dog_layout)
        et_dname = findViewById(R.id.et_new_dog_name)
        et_dbreed = findViewById(R.id.et_new_dog_breed)
        et_dheight = findViewById(R.id.et_new_dog_height)

        et_dlength = findViewById(R.id.et_new_dog_length)
        et_dweight = findViewById(R.id.et_new_dog_weight)
        et_dage = findViewById(R.id.et_new_dog_age)
        et_dgoal = findViewById(R.id.et_new_dog_goal)

        // Get intent
        val intent: Intent = getIntent()

        setResult(Activity.RESULT_CANCELED, intent)
        et_dname.setText(intent.getStringExtra("dname"))
        et_dbreed.setText(intent.getStringExtra("dbreed"))
        et_dheight.setText(intent.getStringExtra("dheight"))
        et_dlength.setText(intent.getStringExtra("dlength"))
        et_dweight.setText(intent.getStringExtra("dweight"))
        et_dage.setText(intent.getStringExtra("dage"))
        et_dgoal.setText(intent.getStringExtra("dgoal"))

        var  d_id = intent.getStringExtra("d_id")!!




        arr_et = arrayOf(et_dname, et_dbreed, et_dheight , et_dlength, et_dweight, et_dage, et_dgoal)

        // Get button & set click listener
        btn_join = findViewById(R.id.btn_join)
        btn_join.setOnClickListener {
            // check blank edit text
            if (checkBlank() == false)
                return@setOnClickListener

            edit_text_en(false)

            var data = ConnectToDB()
            var login_info = LoginData( HomeActivity.user_id, "")
            if(d_id == "")
                data.type = DB_MODES.DADD
            else
                data.type = DB_MODES.DUPD

            data.login_data = login_info;
            data.dog_data = DogInfo(
                login_info, d_id, et_dname.text.toString(), et_dbreed.text.toString(),
                et_dheight.text.toString(), et_dlength.text.toString(), et_dweight.text.toString(),
                et_dage.text.toString(), Integer.parseInt(et_dgoal.text.toString()) , LocalDate.now()
            )





            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                var result = doWork(data)
                if (!result.contains("false")){
                    println("Add dog success! :-)")
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText( this@NewDogActivity, "Add dog success! :-)",Toast.LENGTH_SHORT).show()

                        if(d_id == "")
                            setResult(HOME_RESULT.NEW_DOG.idx, intent)
                        else
                            setResult(HOME_RESULT.MODI_DOG.idx, intent)

                        intent.putExtra("new_dog_result", result) // 개 정보 json

                        finish()
                    }
                }
                else{
                    println("Add dog failed...T^T :: " + result!! ) //SQLSTATE[42S22]: Column not found: 1054 Unknown column 'ac_id' in 'order clause'false
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(
                            this@NewDogActivity,
                            "Add dog failed...T^T",
                            Toast.LENGTH_SHORT
                        ).show()
                        edit_text_en(true)
                    }
                }
            }
        }
    }


    private fun checkBlank(): Boolean{
        val arr_str: Array<String> = arrayOf("이름을", "종을", "키를", "길이를", "몸무게를", "나이를", "목표 걸음을")
        var idx: Int = 0

        for(et in arr_et){
            if (et.text.toString() == ""){
                Toast.makeText(this, arr_str[idx] + " 입력해주세요.", Toast.LENGTH_SHORT).show()
                return false
            }
            idx++
        }
        return true
    }

    private fun edit_text_en(enable: Boolean) {
        for (et in arr_et){
            et.isEnabled = enable
        }
    }

}