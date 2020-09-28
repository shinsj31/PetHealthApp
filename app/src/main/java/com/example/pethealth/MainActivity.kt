package com.example.pethealth

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import kotlinx.android.synthetic.main.main_drawer_layout.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.logging.Handler


class MainActivity : AppCompatActivity() {
    private lateinit var etID: EditText
    private lateinit var etPW: EditText
    private lateinit var imgLOGIN: ImageView
    private lateinit var mHandler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_layout)

        var option: BitmapFactory.Options = BitmapFactory.Options()
        option.inSampleSize = 2
        var bitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.back_img_lite, option)
        imgLOGIN = findViewById(R.id.back_img)
        imgLOGIN.setImageBitmap(bitmap)

        var pref: SharedPreferences = getSharedPreferences("auto_login", MODE_PRIVATE)
        if(!pref.getString("auto_id", "").equals("")) {
            val intent = Intent(this@MainActivity, HomeActivity::class.java)
            var id: String? = pref.getString("auto_id", "")
            var pw: String? = pref.getString("auto_pw", "")
            intent.putExtra("user_id", id)
            startActivityForResult(intent, 1)
        }
        // Get TextViews by Id
        etID = findViewById(R.id.et_id)
        etPW = findViewById(R.id.et_pw)

        // Get Button & set click listener
        var btnLogin: Button = findViewById(R.id.btn_login)
        var btnJoin: TextView = findViewById(R.id.btn_join)


        btnLogin.setOnClickListener {
            var id: String = etID.text.toString()
            var pw: String = etPW.text.toString()
            var data = ConnectToDB()
            data.type = DB_MODES.LOGIN
            data.login_data = LoginData(id, pw)

            val scope = CoroutineScope(Dispatchers.IO)
            etID.isEnabled = false
            etPW.isEnabled = false
            scope.launch {
                var loginResult = doWork(data)
                if (loginResult.contains("success", true)){
                    println("Login success! :-)")
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(this@MainActivity, "Login success! :-)", Toast.LENGTH_SHORT).show()
                        etID.isEnabled = true
                        etPW.isEnabled = true
                    }

                    var edit: SharedPreferences.Editor = pref.edit()
                    edit.putString("auto_id", id)
                    edit.putString("auto_pw", pw)
                    edit.commit()
                    val intent = Intent(this@MainActivity, HomeActivity::class.java)

                    intent.putExtra("user_id", id)
                    startActivityForResult(intent, 1)
                }
                else{
                    println("Login failed...T^T" + loginResult)

                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(this@MainActivity, "Login failed...T^T", Toast.LENGTH_SHORT).show()
                        etID.isEnabled = true
                        etPW.isEnabled = true
                    }
                }
            }
        }

        btnJoin.setOnClickListener {
            val intent = Intent(this, JoinActivity::class.java)

            startActivityForResult(intent, 1)
        }

        var btnServerAddress: Button = findViewById(R.id.btn_server)
        btnServerAddress.setOnClickListener {
            Toast.makeText(this@MainActivity, "Change Server Address!", Toast.LENGTH_SHORT).show()
            ChangeServerIP()
        }
        /*
        // Set Toolbar on main activity
        setSupportActionBar(main_layout_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(ic_menu_black_)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        */



    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK) {
            Toast.makeText(this, "Sign in OK", Toast.LENGTH_SHORT).show()
            if(data != null)
                println(data.getIntExtra("data", 0))

        }
        else {
            Toast.makeText(this, "Sign in Canceled", Toast.LENGTH_SHORT).show()
        }


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home->{
                main_drawer_layout.openDrawer(GravityCompat.START)
            }
        }
        return super.onOptionsItemSelected(item)
    }





}