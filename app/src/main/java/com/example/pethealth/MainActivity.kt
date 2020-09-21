package com.example.pethealth

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
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

    private lateinit var mHandler: Handler


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_layout)

        // Get TextViews by Id
        etID = findViewById(R.id.et_id)
        etPW = findViewById(R.id.et_pw)

        // Get Button & set click listener
        var btnLogin: Button = findViewById(R.id.btn_login)
        var btnJoin: Button = findViewById(R.id.btn_join)


        var btnConnectBluetooth: Button = findViewById(R.id.btn_connect_bluetooth)
        btnConnectBluetooth.setOnClickListener {

            //btService.enableBluetooth();
            val intent = Intent(this@MainActivity, BluetoothService::class.java)

            //intent.putExtra("user_id", id)
            startActivityForResult(intent, 1)

        }
        // BLUETOOTH -------------------------------------------


        var btnChart: Button = findViewById(R.id.btn_chart)

        btnChart.setOnClickListener {

            //btService.enableBluetooth();
            val intent = Intent(this@MainActivity, ChartActivity::class.java)

            //intent.putExtra("user_id", id)
            startActivityForResult(intent, 1)

        }



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
                if (doWork(data).contains("success", true)){
                    println("Login success! :-)")
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(this@MainActivity, "Login success! :-)", Toast.LENGTH_SHORT).show()
                        etID.isEnabled = true
                        etPW.isEnabled = true
                    }

                    val intent = Intent(this@MainActivity, HomeActivity::class.java)

                    intent.putExtra("user_id", id)
                    startActivityForResult(intent, 1)
                }
                else{
                    println("Login failed...T^T")
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