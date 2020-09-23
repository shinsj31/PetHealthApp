package com.example.pethealth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DogListActivity: AppCompatActivity(){
    private lateinit var dog_lv:ListView
    private var dog_name_list = ArrayList<String>() //

    private var dogDatas = ArrayList<DogInfo>()
    private lateinit var doglist_adapter: DoglistAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dog_list_layout)

        dogDatas = intent.getCharSequenceArrayListExtra("dog_list") as ArrayList<DogInfo>
        dog_lv = findViewById(R.id.lv_dog)

        for(d in dogDatas){
            dog_name_list.add(d.d_name);
        }

        doglist_adapter = DoglistAdapter(applicationContext, dog_name_list, this)
        dog_lv.adapter = doglist_adapter

        var btnNewDog:Button = findViewById(R.id.btn_new_dog)
        if(btnNewDog.isEnabled){
            btnNewDog.setOnClickListener {

                CoroutineScope(Dispatchers.Main).launch {
                    //Toast.makeText(this@DogListActivity, "NEW DOG", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@DogListActivity, NewDogActivity::class.java)

                    startActivityForResult(intent, 1)
                }

            }
        }



    }

}

