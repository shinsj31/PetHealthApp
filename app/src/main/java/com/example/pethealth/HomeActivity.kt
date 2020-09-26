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
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import java.sql.DriverManager
import java.time.ZoneId
import java.time.ZonedDateTime

enum class HOME_RESULT(var idx: Int) {
    SELECT_DOG(10), NEW_DOG(11) , MODI_DOG(12) , CANCLE_DOG(13)
}

val MATERIAL_COLORS = intArrayOf(
    R.color.colorBlueGreen,
    R.color.colorSkyBlue,
    R.color.colorLiteSkyBlue,
    R.color.colorBeige
)

class HomeActivity: AppCompatActivity() {
    private var isRunning = false
    private val showActivityThread = ShowMyDogActivity()

    private lateinit var user_id: String
    private lateinit var dog_list_json: String
    private lateinit var activity_today_json: String
    private var dogDatas = ArrayList<DogInfo>()
    private var activity_data = ArrayList<ActivityData>()
    private var dog_index = -1 // save local

    private var pieChart: PieChart? = null
    private var combinedChart: CombinedChart? = null

    private var bluetoothAddress: String = "24:6F:28:9D:47:76"

    private val mQuarter = arrayOf(
        "0", "3", "6", "9", "12", "15", "18", "21"
    )

    private var analyzedActivityData = AnalyzedActivityData()

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        isRunning = true


        setContentView(R.layout.home_layout)
        SetPieChart()
        SetBluetooth()
        showActivityThread.start()

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

                    dogDatas = ConvertJsonToDogInfos(dog_list_json, user_id)
                    dog_index = 0 //TODO:로컬 데이터 읽어오기


                    showActivityThread.interrupt()

                }catch (e: JSONException) {

                    e.printStackTrace();
                }
            }
        }


        if(btnDogList.isEnabled){
            btnDogList.setOnClickListener {
                CoroutineScope(Dispatchers.Main).launch {
                    job_get_dog_list.join()
                    val intent: Intent = Intent(this@HomeActivity, DogListActivity::class.java)
                    intent.putExtra("user_id", user_id)
                    intent.putExtra("dog_list", dogDatas)
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
            Toast.makeText(this@HomeActivity, "Stop prev bluetooth connection", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(
                    applicationContext,
                    "Connected to $name\n$address",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onDeviceDisconnected() { //연결해제
                Toast.makeText(applicationContext, "Connection lost", Toast.LENGTH_SHORT).show()
            }

            override fun onDeviceConnectionFailed() { //연결실패
                Toast.makeText(applicationContext, "Unable to connect", Toast.LENGTH_SHORT).show()
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

        combinedChart = findViewById(R.id.combinedChart) as CombinedChart


        SetCombineChart()

    }

     fun SetCombineChart() {

        combinedChart = findViewById(R.id.combinedChart) as CombinedChart
        combinedChart!!.description.isEnabled = false
        combinedChart!!.setBackgroundColor(Color.WHITE)
        combinedChart!!.setDrawGridBackground(false)
        combinedChart!!.setDrawBarShadow(false)
        combinedChart!!.isHighlightFullBarEnabled = false
        combinedChart!!.drawOrder = arrayOf<CombinedChart.DrawOrder>(
            CombinedChart.DrawOrder.BAR,
            CombinedChart.DrawOrder.BUBBLE,
            CombinedChart.DrawOrder.CANDLE,
            CombinedChart.DrawOrder.LINE,
            CombinedChart.DrawOrder.SCATTER
        )
        val l = combinedChart!!.legend
        l.isWordWrapEnabled = true
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(false)

        val rightAxis = combinedChart!!.axisRight
        rightAxis.setDrawGridLines(false)
        rightAxis.granularity = 10f
        rightAxis.axisMinimum = 0f

        val leftAxis = combinedChart!!.axisLeft
        leftAxis.setDrawGridLines(false)
        leftAxis.granularity = 10f // 한칸의 크기
        leftAxis.axisMinimum = 0f
        val xAxis = combinedChart!!.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTH_SIDED
        xAxis.axisMinimum = 0f
        xAxis.granularity = 1f

        xAxis.valueFormatter =
            IAxisValueFormatter { value, axis -> mQuarter[value.toInt() % mQuarter.size] }


    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun DrawCombinedChart (){
        val data = CombinedData()
        data.setData(generateLineData())
        data.setData(generateBarData())

        combinedChart!!.xAxis.axisMaximum = data.xMax + 0.25f
        combinedChart!!.data = data
        combinedChart!!.invalidate()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun generateLineData(): LineData? {
        val date: ZonedDateTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));

        val d = LineData()
        val entries = ArrayList<Entry>()

        var value: Int= 0
        for (index in 0..date.hour) {
            value += analyzedActivityData.avgHeartRatePerHour[index]
            if(index%3 == 2){
                entries.add(Entry((index / 3).toFloat(), value.toFloat()))
                value = 0
            }
            else if(index == date.hour){
                entries.add(Entry((index / 3).toFloat(), value.toFloat()))
                value = 0
            }
        }

        val set = LineDataSet(entries, "Line DataSet")
        set.color = Color.rgb(0, 100, 0)
        set.lineWidth = 2.5f
        set.setCircleColor(Color.rgb(0, 100, 0))
        set.circleRadius = 5f
        set.fillColor = R.color.colorSkyBlue //Color.rgb(0, 100, 0)
        set.mode = LineDataSet.Mode.CUBIC_BEZIER
        set.setDrawValues(true)
        set.valueTextSize = 10f
        set.valueTextColor = Color.rgb(0, 100, 0)
        set.axisDependency = YAxis.AxisDependency.LEFT
        d.addDataSet(set)
        return d
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun generateBarData(): BarData? {
        val date: ZonedDateTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        val entries = ArrayList<BarEntry>()

        var value: Int= 0
        for (index in 0..date.hour) {
            value += analyzedActivityData.movementPerHour[index]
            if(index%3 == 2){
                entries.add(BarEntry((index / 3).toFloat(), value.toFloat()))
                value = 0
            }
            else if(index == date.hour){
                entries.add(BarEntry((index / 3).toFloat(), value.toFloat()))
                value = 0
            }
        }

        val set = BarDataSet(entries, "Bar DataSet")
        set.color = Color.rgb(60, 220, 78)
        set.valueTextColor = Color.rgb(60, 220, 78)
        set.valueTextSize = 10f
        set.axisDependency = YAxis.AxisDependency.RIGHT
        return BarData(set)
    }



    fun DrawPieChart(walk: Int, goal: Int){
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

    inner class DrawChart : Thread() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun start() {
            super.start()
            DrawCombinedChart()
            DrawPieChart(analyzedActivityData.allMovement, analyzedActivityData.goal)
        }
        override fun run() {
            runOnUiThread {
                pieChart!!.animateY(1000, Easing.EasingOption.EaseInOutCubic) //애니메이션
                combinedChart!!.animateXY(1000, 1000)
            }

        }

    }

    inner class ShowMyDogActivity : Thread() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun run() {
            while(isRunning){
                try {
                    var startTime = System.currentTimeMillis()
                    if(dog_index >= 0 && dog_index < dogDatas.size){
                        /* Get Today Activity Data */
                        var data = ConnectToDB()
                        data.dog_data = dogDatas[dog_index]
                        data.type = DB_MODES.ACTODAY

                        activity_today_json = doWork(data)

                        analyzedActivityData = AnalyzedActivityData()
                        activity_data = ConvertJsonToActivityData(activity_today_json);

                        analyzedActivityData.AnalyzeActivity(
                            activity_data,
                            data.dog_data.d_goal_activity
                        )

                        // 그래프로 활동 정보 표시
                        val thread = DrawChart()
                        thread.start()
                    }

                    var endTime = System.currentTimeMillis()
                    var spentTime = endTime-startTime

                    sleep(60 * 1000 - (spentTime))
                }
                catch (e: InterruptedException){
                    println("INTTERUPT")
                }


            }
        }

    }



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Toast.makeText(this, "result: " + resultCode, Toast.LENGTH_SHORT).show()
        if(resultCode == HOME_RESULT.SELECT_DOG.idx || resultCode == HOME_RESULT.CANCLE_DOG.idx ) { // -1
            if(data != null){

                if(resultCode == HOME_RESULT.SELECT_DOG.idx){
                    Toast.makeText(this, "INDEX : " + dog_index, Toast.LENGTH_SHORT).show()

                    dog_index = data.extras!!.getInt("dog_index")

                }
                dogDatas = data.getCharSequenceArrayListExtra("dog_list") as ArrayList<DogInfo>
                showActivityThread.interrupt()

            }
        }

        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK) {
                val address = data!!.extras!!.getString(BluetoothState.EXTRA_DEVICE_ADDRESS)
                println("BLUETOOTH ADDRESS : " + address);
                //*************** 블루투스 주소 정보 저장 할 것! ****************************//
                bt!!.connect(address)
            }
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt!!.setupService()
                bt!!.startService(BluetoothState.DEVICE_OTHER)
                BluetoothServiceSet()
            } else {
                Toast.makeText(applicationContext, "Bluetooth was not enabled.", Toast.LENGTH_SHORT).show()
                val btnSend =  findViewById<Button>(R.id.btnSend) //데이터 전송
                btnSend.isEnabled = false;
            }
        }
    }






}