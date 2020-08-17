package com.example.pethealth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException

class HomeActivity: AppCompatActivity() {
    private lateinit var user_id: String
    private lateinit var dog_list_json: String
    private var dogDatas = ArrayList<DogInfo>()
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_layout)

        var btnDogList: Button = findViewById(R.id.btn_dog_list)


        // json -> class 변경
        // 강아지 정보 받아오기
        // 만약 저장된 강아지 정보가 없다면 강아지 추가하기
        // 만약 여려 마리의 강아지 리스트를 받는다면 강아지 선택하기
        // 강아지 활동 내역 보기 (그래프 형식)
        // 다른 강아지 보기
        // 강아지 정보 변경 및 추가하기
        // 멤버 변경하기
        user_id = intent.getStringExtra("user_id")!!
        //Toast.makeText(this, user_id, Toast.LENGTH_SHORT).show()

        var data = ConnectToDB()
        data.type = DB_MODES.DLIST
        data.login_data = LoginData(user_id, "")

        val scope = CoroutineScope(Dispatchers.IO)
        val job_get_dog_list = scope.launch{
            dog_list_json = doWork(data)
            if (dog_list_json.contains("false", true)) {
                CoroutineScope(Dispatchers.Main).launch{
                    Toast.makeText(this@HomeActivity, "get list failed...T^T", Toast.LENGTH_SHORT).show()
                }
            }
            else {
                CoroutineScope(Dispatchers.Main).launch{
                    Toast.makeText(this@HomeActivity, "get list success! :-)", Toast.LENGTH_SHORT).show()
                }

                try {
                    var jarray = JSONArray(dog_list_json);   // JSONArray 생성
                    for( i in 0..(jarray.length() - 1)) {

                        var jObject = jarray.getJSONObject(i) // JSONObject 추출
                        var d_id = jObject.getString("d_id")
                        var d_name = jObject.getString("d_name")
                        var d_breed = jObject.getString("d_breed")

                        var d_height = jObject.getString("d_height")
                        var d_length = jObject.getString("d_length")
                        var d_weight = jObject.getString("d_weight")
                        var d_age = jObject.getString("d_age")
                        var info = DogInfo(LoginData(user_id, "") ,d_id , d_name, d_breed, d_height, d_length, d_weight, d_age)
                        dogDatas.add(info)
                    }

                    /*
                    CoroutineScope(Dispatchers.Main).launch {
                        val intent: Intent = Intent(this@HomeActivity, DogListActivity::class.java)
                        intent.putExtra("dog_list",dogDatas)
                        startActivityForResult(intent, 1)
                    }
                    */

                }catch (e: JSONException ) {

                    e.printStackTrace();
                }
            }

        }

        if(btnDogList.isEnabled){
            btnDogList.setOnClickListener {

                CoroutineScope(Dispatchers.Main).launch {
                    job_get_dog_list.join()
                    val intent: Intent = Intent(this@HomeActivity, DogListActivity::class.java)
                    intent.putExtra("dog_list",dogDatas)
                    startActivityForResult(intent, 1)
                }

            }
        }





    }



}