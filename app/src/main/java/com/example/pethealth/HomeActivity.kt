package com.example.pethealth

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import app.akexorcist.bluetotohspp.library.BluetoothSPP
import app.akexorcist.bluetotohspp.library.BluetoothSPP.BluetoothConnectionListener
import app.akexorcist.bluetotohspp.library.BluetoothState
import app.akexorcist.bluetotohspp.library.DeviceList
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import java.sql.DriverManager
import java.time.LocalDate
import java.time.format.DateTimeFormatter


enum class HOME_RESULT(var idx: Int) {
    SELECT_DOG(10), NEW_DOG(11)
}

class HomeActivity: AppCompatActivity() {
    private lateinit var user_id: String
    private lateinit var dog_list_json: String
    private lateinit var activity_today_json: String
    private var dogDatas = ArrayList<DogInfo>()
    private var activity_data = ArrayList<ActivityData>()
    private var dog_index = -1 // save local

    private var pieChart: PieChart? = null

    private var bluetoothAddress: String = "24:6F:28:9D:47:76"


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_layout)
        SetPieChart()
        SetBluetooth()

        var btnDogList: Button = findViewById(R.id.btn_dog_list)

        // 저장된 강아지 정보가 없다면 강아지 추가하기
        // 강아지 정보 변경 및 추가하기
        // 멤버 변경하기
        user_id = intent.getStringExtra("user_id")!!

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

                try {

                    ConvertJsonToDogList();
                    dog_index = 0 // 로컬 데이터 읽어오기
                    val thread = ShowMyDogActivity()
                    thread.start()

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
            btnDogList.setOnLongClickListener(){
                CoroutineScope(Dispatchers.Main).launch {

                }
                true
            }
        }


    }

    // =======================================
    var bt: BluetoothSPP? = null

    fun SetBluetooth() {
        val btnSend =  findViewById<Button>(R.id.btnSend) //데이터 전송
        btnSend.isEnabled = false;

        if (bt != null) {
            bt!!.stopService() //블루투스 중지
            Toast.makeText(this@HomeActivity,"Stop prev bluetooth connection", Toast.LENGTH_SHORT).show()
        }
        bt = BluetoothSPP(this) //Initializing
        if (!bt!!.isBluetoothAvailable) { //블루투스 사용 불가
            Toast.makeText(applicationContext, "Bluetooth is not available", Toast.LENGTH_SHORT).show()
            //finish()
            return
        }





        // --------------------------------------- 데이터 수신 시 --------------------------------------- //
        bt!!.setOnDataReceivedListener { data, message ->
            //데이터 수신
            Toast.makeText(this@HomeActivity, message, Toast.LENGTH_SHORT).show()
            DriverManager.println(message)
            // 명령 실행 ----------------------------------------------
        }

        bt!!.setBluetoothConnectionListener(object : BluetoothConnectionListener {
            //연결됐을 때
            override fun onDeviceConnected(name: String, address: String) {
                Toast.makeText(applicationContext, "Connected to $name\n$address", Toast.LENGTH_SHORT).show()
            }
            override fun onDeviceDisconnected() { //연결해제
                Toast.makeText( applicationContext , "Connection lost", Toast.LENGTH_SHORT).show()
            }
            override fun onDeviceConnectionFailed() { //연결실패
                Toast.makeText( applicationContext, "Unable to connect", Toast.LENGTH_SHORT).show()
            }
        })

        //연결 버튼
        val btnConnect = findViewById<Button>(R.id.btnConnect) //연결시도
        btnConnect.setOnClickListener {
            if (bt!!.serviceState == BluetoothState.STATE_CONNECTED) {
                bt!!.disconnect()
            } else {
                val intent = Intent(applicationContext, DeviceList::class.java)
                startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE)
            }
        }

        if (!bt!!.isBluetoothEnabled) { //


        } else {
            if (!bt!!.isServiceAvailable) {
                bt!!.setupService()
                bt!!.startService(BluetoothState.DEVICE_OTHER) //DEVICE_ANDROID는 안드로이드 기기 끼리 // DEVICE_OTHER
                BluetoothServiceSet()
            }

            //******************* 저장된 블루투스 정보로 바로 연결 시도 ********************************//
            bt!!.connect(bluetoothAddress);
        }


    }

    fun BluetoothServiceSet() {
        val btnSend =  findViewById<Button>(R.id.btnSend) //데이터 전송
        btnSend.isEnabled = true;
        btnSend.setOnClickListener {
            bt!!.send("Text", true)
        }
    }



    //==================================================

    @RequiresApi(Build.VERSION_CODES.O)
    fun SumActivityData (datas: ArrayList<ActivityData> ): ActivityData {
        var sum: ActivityData = ActivityData("","", LocalDate.now(),0,0,0,0,0,0,"","","")

        for( d in datas) {

            sum.ac_walk += d.ac_walk
            sum.ac_run += d.ac_run
            sum.ac_distance += d.ac_distance
            /*
              정보들 취합
             */
        }

        return sum
    }

    fun SetPieChart (){
        pieChart = findViewById(R.id.piechart) as PieChart
        pieChart!!.setUsePercentValues(false)
        pieChart!!.description.isEnabled = false
        pieChart!!.setExtraOffsets(5f, 10f, 5f, 5f)
        pieChart!!.dragDecelerationFrictionCoef = 0.95f
        pieChart!!.isDrawHoleEnabled = true
        pieChart!!.setHoleColor(Color.WHITE)
        pieChart!!.transparentCircleRadius = 1000f


        val description = Description()
        description.setText("활동 정보") //라벨
        description.setTextSize(15f)
        pieChart!!.description = description

    }

    fun AddPieChart (walk:Int, goal: Int){
        val yValues = ArrayList<PieEntry>()
        yValues.add(PieEntry(walk.toFloat(), "Walk"))

        if(walk < goal)
            yValues.add(PieEntry((goal - walk).toFloat(), "Goal"))

        val dataSet = PieDataSet(yValues, "Activity")
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f
        dataSet.setColors(*ColorTemplate.JOYFUL_COLORS)
        val data = PieData(dataSet)
        data.setValueTextSize(10f)
        data.setValueTextColor(Color.YELLOW)
        pieChart!!.data = data
    }

    inner class DrawPieChart : Thread() {
        override fun run() {

            runOnUiThread {
                pieChart!!.animateY(1000, Easing.EasingOption.EaseInOutCubic) //애니메이션
            }

        }

    }

    inner class ShowMyDogActivity : Thread() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun run() {
            if(dog_index >= 0 && dog_index < dogDatas.size){
                /* Get Today Activity Data */

                var data = ConnectToDB()
                data.dog_data = dogDatas[dog_index]
                data.type = DB_MODES.ACTODAY

                activity_today_json = doWork(data)

                var walkCount : Int = 0

                if(activity_today_json != "false" && !activity_today_json.contains(("Error"))){
                    activity_data = ConvertJsonToActivityData(activity_today_json);
                    var todaySum = SumActivityData(activity_data)


                    walkCount = todaySum.ac_walk + todaySum.ac_run
                }

                // 그래프로 활동 정보 표시
                val thread = DrawPieChart()
                thread.start()
                AddPieChart(walkCount, data.dog_data.d_goal_activity)

            }

        }

    }



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Toast.makeText(this, "result: " + resultCode, Toast.LENGTH_SHORT).show()
        if(resultCode == HOME_RESULT.SELECT_DOG.idx) { // -1
            if(data != null){
                dog_index = data.extras!!.getInt("dog_index")
                Toast.makeText(this, "INDEX : " + dog_index, Toast.LENGTH_SHORT).show()
                val thread = ShowMyDogActivity()
                thread.start()
            }
        }
        else {

        }

        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK) {
                val address = data!!.extras!!.getString(BluetoothState.EXTRA_DEVICE_ADDRESS)
                println( "BLUETOOTH ADDRESS : " + address);
                //*************** 블루투스 주소 정보 저장 할 것! ****************************//
                bt!!.connect(address)
            }
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt!!.setupService()
                bt!!.startService(BluetoothState.DEVICE_OTHER)
                BluetoothServiceSet()
            } else {
                Toast.makeText( applicationContext  , "Bluetooth was not enabled." , Toast.LENGTH_SHORT ).show()
                val btnSend =  findViewById<Button>(R.id.btnSend) //데이터 전송
                btnSend.isEnabled = false;
            }
        }
    }



    fun ConvertJsonToDogList (){
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
            var d_goal_activity = jObject.getInt("d_goal_activity")

            var info = DogInfo(LoginData(user_id, "") ,d_id , d_name, d_breed, d_height, d_length, d_weight, d_age, d_goal_activity)
            dogDatas.add(info)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun ConvertJsonToActivityData (_json: String ): ArrayList<ActivityData>{
        var _activity_datas = ArrayList<ActivityData>()
        var jarray = JSONArray(_json);   // JSONArray 생성

        for( i in 0..(jarray.length() - 1)) {

            var jObject = jarray.getJSONObject(i) // JSONObject 추출

            var d_id = jObject.getString("d_id")
            var ac_id = jObject.getString("ac_id")
            var ac_date =  LocalDate.parse(jObject.getString("ac_date") , DateTimeFormatter.ISO_DATE);
            var ac_hour = jObject.getInt("ac_hour")
            var ac_minute = jObject.getInt("ac_minute")
            var ac_walk = jObject.getInt("ac_walk")
            var ac_run  = jObject.getInt("ac_run")
            var ac_distance  = jObject.getInt("ac_distance")
            var ac_heart_rate  = jObject.getInt("ac_heart_rate")
            var ac_location = jObject.getString("ac_location")
            var ac_device_id = jObject.getString("ac_device_id")
            var ac_posture = jObject.getString("ac_posture")


            var info = ActivityData( d_id,ac_id, ac_date , ac_hour , ac_minute, ac_walk ,ac_run, ac_distance ,ac_heart_rate,ac_location,ac_device_id,ac_posture )
            _activity_datas.add(info)
        }

        return _activity_datas
    }

}