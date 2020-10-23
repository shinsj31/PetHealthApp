package com.example.pethealth

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DogListActivity: AppCompatActivity(){
    private lateinit var dog_lv:ListView

    private var dogDatas = ArrayList<DogInfo>()
    private lateinit var doglist_adapter: DoglistAdapter
    private lateinit var user_id : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dog_list_layout)

        user_id = HomeActivity.user_id
        dogDatas =  HomeActivity.dogDatas //intent.getCharSequenceArrayListExtra("dog_list") as ArrayList<DogInfo>


        dog_lv = findViewById(R.id.lv_dog)

        doglist_adapter = DoglistAdapter(applicationContext, dogDatas, this)
        dog_lv.adapter = doglist_adapter

        var btnNewDog:Button = findViewById(R.id.btn_new_dog)
        if(btnNewDog.isEnabled){
            btnNewDog.setOnClickListener {

                CoroutineScope(Dispatchers.Main).launch {
                    //Toast.makeText(this@DogListActivity, "NEW DOG", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@DogListActivity, NewDogActivity::class.java)
                    intent.putExtra("d_id", "")
                    startActivityForResult(intent, 1)
                }

            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Toast.makeText(this, "result: " + resultCode, Toast.LENGTH_SHORT).show()
        if(resultCode == HOME_RESULT.NEW_DOG.idx) { //
            if(data != null){
                var result = data.extras!!.getString("new_dog_result")

                if (result != null) {
                    var doginfo = ConvertJsonToDogInfo(result, user_id)
                    if (doginfo != null) {
                        dogDatas.add(doginfo)

                        doglist_adapter = DoglistAdapter(
                            applicationContext,dogDatas, this  )
                        dog_lv.adapter = doglist_adapter
                    }
                }
            }
        }
        else if(resultCode == HOME_RESULT.MODI_DOG.idx) { //
            if(data != null){
                var result = data.extras!!.getString("new_dog_result")
                if (result != null) {
                    var doginfo = ConvertJsonToDogInfo(result, user_id)
                    if (doginfo != null) {
                        for(  i  in 0 .. dogDatas.size){
                            if(dogDatas[i].d_id == doginfo.d_id){
                                dogDatas[i] = doginfo
                                doglist_adapter = DoglistAdapter(
                                    applicationContext,
                                    dogDatas,
                                    this

                                )
                                dog_lv.adapter = doglist_adapter

                                break
                            }
                        }
                    }
                }


            }
        }
        else {

        }


    }

}

