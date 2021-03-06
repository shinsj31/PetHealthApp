package com.example.pethealth

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import app.akexorcist.bluetotohspp.library.BluetoothSPP
import app.akexorcist.bluetotohspp.library.BluetoothSPP.BluetoothConnectionListener
import app.akexorcist.bluetotohspp.library.BluetoothState
import app.akexorcist.bluetotohspp.library.DeviceList
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.*
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import java.sql.DriverManager
import java.time.ZoneId
import java.time.ZonedDateTime

enum class HOME_RESULT(var idx: Int) {
    SELECT_DOG(10), NEW_DOG(11) , MODI_DOG(12)
}

val MATERIAL_COLORS = intArrayOf(
    Color.rgb(0x0C ,0x90 ,0xAD) ,
    Color.rgb(0x91,0xE0,0xF4),
    Color.rgb(0xDA,0xEF,0xF5 ),
    Color.rgb(0xF8,0xF1,0xE9 )


)
val mQuarter = arrayOf(
    "0",
    "1",
    "2",
    "3",
    "4",
    "5",
    "6",
    "7",
    "8",
    "9",
    "10",
    "11",
    "12",
    "13",
    "14",
    "15",
    "16",
    "17",
    "18",
    "19",
    "20",
    "21",
    "22",
    "23"
)

public class HomeActivity: AppCompatActivity() {
    companion object {
        lateinit var prefs: PreferenceUtil
        public var dog_index = -1 // save local

        @JvmStatic
        fun SetDogIndex(_index: Int) {
            dog_index = _index
            prefs.setString("dog_index", _index.toString())
        }

        lateinit var user_id: String
        lateinit var dog_list_json: String
        lateinit var activity_today_json: String
        var dogDatas = ArrayList<DogInfo>()
        var activity_data = ArrayList<ActivityData>()

        var analyzedActivityData = AnalyzedActivityData()
        var result_activitys = ArrayList<AnalyzedActivityData>()

    }


    private var isRunning = false
    private var isReady = false
    private val showActivityThread = ShowMyDogActivity()
    private val getAllDataThread = GetAllDataThread()
    private var pieChart: PieChart? = null
    private var combinedChart: CombinedChart? = null


    private lateinit var txt_name :TextView
    private lateinit var btnDogList: Button
    private lateinit var layout_activity_info :ViewGroup
    private lateinit var layout_rest : ViewGroup
    private lateinit var layout_heart_rate : ViewGroup
    private lateinit var layout_weight : ViewGroup

    private lateinit var txt_movement_time :TextView
    private lateinit var txt_movement_distance :TextView
    private lateinit var txt_rest_time :TextView
    private lateinit var txt_sleep_time :TextView
    private lateinit var txt_heart_rate :TextView
    private lateinit var txt_weight :TextView


    private var bluetoothAddress: String = "24:6F:28:9D:47:76"




    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }

    @SuppressLint("WrongViewCast")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_layout)

        txt_name = findViewById(R.id.txt_name)
        btnDogList = findViewById(R.id.btn_dog_list)
        layout_activity_info =findViewById(R.id.layout_activity_time) // 활동 정보 요약
        layout_rest =findViewById(R.id.layout_rest_time) // 휴식 정보
        layout_heart_rate =findViewById(R.id.layout_heart_rate) // 심박수 정보
        layout_weight =findViewById(R.id.layout_weight) // 몸무게 정보

        txt_movement_time =findViewById(R.id.txt_movement_time)
        txt_movement_distance =findViewById(R.id.txt_movement_distance)
        txt_rest_time =findViewById(R.id.txt_rest_time)
        txt_sleep_time =findViewById(R.id.txt_sleep_time)
        txt_heart_rate =findViewById(R.id.txt_heart_rate)
        txt_weight =findViewById(R.id.txt_weight)

        SetPieChart()
        SetCombineChart()

        SetBluetooth()

        prefs = PreferenceUtil(applicationContext) // preference !!

        isRunning = true
        showActivityThread.start() // thread run animation of chart


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
                    dog_index = prefs.getString("dog_index", "0").toInt() //로컬 데이터 읽어옴

                    var data = ConnectToDB()
                    data.dog_data = dogDatas[dog_index]
                    data.type = DB_MODES.ACTODAY
                    activity_today_json = doWork(data)
                    analyzedActivityData = AnalyzedActivityData()

                    showActivityThread.interrupt()
                    getAllDataThread.start()

                    isReady = true;
                    SetBtn()

                }catch (e: JSONException) {
                    e.printStackTrace();
                }
            }
        }

        btnDogList.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                // job_get_dog_list.join()
                val intent: Intent = Intent(this@HomeActivity, DogListActivity::class.java)
                startActivityForResult(intent, 1)
            }

        }
        btnDogList.setOnLongClickListener(){
            CoroutineScope(Dispatchers.Main).launch {

            }
            true
        }
        layout_activity_info.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                //활동 정보 요약을 클릭 시 상세한 정보를 볼 수 있다.

                val intent: Intent = Intent(this@HomeActivity, MovementCountActivity::class.java)
                startActivityForResult(intent, 1)

            }

        }
        layout_rest.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(this@HomeActivity, "click layout_rest", Toast.LENGTH_SHORT).show()
                val intent: Intent = Intent(this@HomeActivity, RestActivity::class.java)
                startActivityForResult(intent, 1)
            }

        }

        layout_heart_rate.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(this@HomeActivity, "click layout_heart_rate", Toast.LENGTH_SHORT).show()
                val intent: Intent = Intent(this@HomeActivity, HeartbeatActivity::class.java)
                startActivityForResult(intent, 1)
            }

        }

        layout_weight.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(this@HomeActivity, "click layout_weight", Toast.LENGTH_SHORT).show()
                val intent: Intent = Intent(this@HomeActivity, WeightActivity::class.java)
                startActivityForResult(intent, 1)
            }

        }


    }

    fun SetBtn (){
        if(isReady){

        }
    }
    // =======================================
    var bt: BluetoothSPP? = null

    @RequiresApi(Build.VERSION_CODES.O)
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
            GetBluetoothMessage(message)
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun GetBluetoothMessage(msg: String){

        if(msg.contains("d_id")){
            var tokens = msg.split('=', '&')

            var connectToServer = ConnectToServer()
            connectToServer.data.msg = msg
            connectToServer.data.type =  DB_MODES.ACADD
            connectToServer.start()
        }

    }

    inner class ConnectToServer : Thread() {
        var data = ConnectToDB();
        @RequiresApi(Build.VERSION_CODES.O)
        override fun start() {
            super.start()
        }
        @RequiresApi(Build.VERSION_CODES.O)
        override fun run() {
            var result_to_connect = doWork(data)
            print("result_to_connect : " + result_to_connect)
        }
    }


    //==================================================

    @RequiresApi(Build.VERSION_CODES.O)
    fun SetPieChart (){
        pieChart = findViewById(R.id.piechart) as PieChart
        pieChart!!.setUsePercentValues(false)
        pieChart!!.description.isEnabled = true
        pieChart!!.setExtraOffsets(5f, 10f, 5f, 5f)
        pieChart!!.dragDecelerationFrictionCoef = 0.95f
        pieChart!!.isDrawHoleEnabled = true
        //pieChart!!.setHoleColor(Color.WHITE)
        pieChart!!.transparentCircleRadius = 1000f


        val description = Description()
        description.setText("활동 정보") //라벨
        description.setTextSize(15f)
        pieChart!!.description = description

        // 차트 클릭 시
        pieChart!!.setOnChartValueSelectedListener(
            //https://stackoverflow.com/questions/35268971/mpandroidchart-click-listener-on-chart 
            object : OnChartValueSelectedListener {
                override fun onNothingSelected() {
                }

                override fun onValueSelected(e: Entry?, h: Highlight?) {

                    val intent: Intent = Intent(
                        this@HomeActivity,
                        MovementCountActivity::class.java
                    )
                    startActivityForResult(intent, 1)

                }
            }
        )







    }

     fun SetCombineChart() {

         combinedChart = findViewById(R.id.combinedChart) as CombinedChart

         combinedChart!!.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
             override fun onNothingSelected() {
             }

             override fun onValueSelected(e: Entry?, h: Highlight?) {
             }

         })

        combinedChart!!.description.isEnabled = false
        //combinedChart!!.setBackgroundColor(Color.WHITE)
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


         combinedChart!!.setScaleEnabled(false) // 확대 막기
         combinedChart!!.setVisibleXRangeMaximum(24f);

         val l = combinedChart!!.legend
        l.isWordWrapEnabled = true
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(false)

        val rightAxis = combinedChart!!.axisRight
        rightAxis.setDrawGridLines(false)
        rightAxis.granularity = 200f
        rightAxis.axisMinimum = 0f
         rightAxis.isEnabled = true
         rightAxis.textColor = MATERIAL_COLORS[0]
        val leftAxis = combinedChart!!.axisLeft
        leftAxis.setDrawGridLines(false)
        leftAxis.granularity = 10f // 한칸의 크기
        leftAxis.axisMinimum = 50f
         leftAxis.isEnabled = true
         leftAxis.textColor = MATERIAL_COLORS[1]
         val xAxis = combinedChart!!.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
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
        data.barData.barWidth = 0.1f

        combinedChart!!.xAxis.axisMaximum = data.xMax + 0.25f
        combinedChart!!.data = data
        combinedChart!!.invalidate()

        val l: Legend = combinedChart!!.legend
        val l1 = LegendEntry("Movement", Legend.LegendForm.DEFAULT, 10f, 2f, null, MATERIAL_COLORS[0])
        val l2 = LegendEntry("Heartbeat", Legend.LegendForm.CIRCLE, 10f, 2f, null, MATERIAL_COLORS[1])
        l.setCustom(arrayOf(l1, l2))

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun generateLineData(): LineData? {
        val date: ZonedDateTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));

        val d = LineData()
        val entries = ArrayList<Entry>()

        var value: Int= 0
        for (index in 0..23) { // date.hour
            /*
            value += analyzedActivityData.avgHeartRatePerHour[index]
            if(index%3 == 2){
                entries.add(Entry((index / 3).toFloat(), value.toFloat()))
                value = 0
            }
            else if(index == date.hour){
                entries.add(Entry((index / 3).toFloat(), value.toFloat()))
                value = 0
            }

             */
            if(index < date.hour){
                entries.add(
                    Entry(
                        index.toFloat(),
                        analyzedActivityData.avgHeartRatePerHour[index].toFloat()
                    )
                )
            }



        }

        val set = LineDataSet(entries, "Heartbeat")
        set.color = Color.rgb(145, 224, 244)
        set.lineWidth = 1f
        set.setCircleColor(Color.rgb(145, 224, 244))
        set.circleRadius = 3f
        set.fillColor = Color.rgb(145, 224, 244)//Color.rgb(0, 100, 0)
        set.mode = LineDataSet.Mode.CUBIC_BEZIER
        set.setDrawValues(false)
        set.valueTextSize = 0f
        set.valueTextColor = Color.rgb(145, 224, 244)
        set.axisDependency = YAxis.AxisDependency.LEFT
        d.addDataSet(set)
        return d
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun generateBarData(): BarData? {
        val date: ZonedDateTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        val entries = ArrayList<BarEntry>()

        var value: Int= 0
        for (index in 0..23) {
            /*
            value += analyzedActivityData.movementPerHour[index]
            if(index%3 == 2){
                entries.add(BarEntry((index / 3).toFloat(), value.toFloat()))
                value = 0
            }
            else if(index == date.hour){
                entries.add(BarEntry((index / 3).toFloat(), value.toFloat()))
                value = 0
            }
            */
            if(index <= date.hour){
                entries.add(
                    BarEntry(
                        index.toFloat(),
                        analyzedActivityData.movementPerHour[index].toFloat()
                    )
                )
            }
            else {
                entries.add(
                    BarEntry(
                        index.toFloat(),
                        0f
                    )
                )
            }

        }

        val set = BarDataSet(entries, "Movement")
        set.color = MATERIAL_COLORS[0]
        set.valueTextColor =  MATERIAL_COLORS[0]
        set.setDrawValues(false)
        set.valueTextSize = 10f
        set.axisDependency = YAxis.AxisDependency.RIGHT
        return BarData(set)
    }



    fun DrawPieChart(walk: Int, goal: Int){
        val yValues = ArrayList<PieEntry>()
        yValues.add(PieEntry(walk.toFloat(), "활동"))

        if(walk < goal)
            yValues.add(PieEntry((goal - walk).toFloat(), "목표"))

        val dataSet = PieDataSet(yValues, "")
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f

        var colors : ArrayList<Int> = ArrayList()
        colors.add(MATERIAL_COLORS[0])
        colors.add(MATERIAL_COLORS[1])
      //  dataSet.setColors(*ColorTemplate.JOYFUL_COLORS)
       // dataSet.addColor(MATERIAL_COLORS[0])
        dataSet.setColors(colors)

        pieChart!!.getLegend().setEnabled(false);

        pieChart!!.centerText = walk.toString() + "/" + goal.toString()

        val data = PieData(dataSet)
        data.setValueTextSize(10f)

        data.setValueTextColor(MATERIAL_COLORS[0])
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
                pieChart!!.animateY(1000 /*Easing.EasingOption.EaseInOutCubic*/) //애니메이션
                combinedChart!!.animateXY(1000, 1000)

            }
        }
    }

    inner class GetAllDataThread : Thread() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun start() {
            super.start()

        }
        @RequiresApi(Build.VERSION_CODES.O)
        override fun run() {
            val today: ZonedDateTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
            var data = ConnectToDB()
            data.type = DB_MODES.RESGET
            for( d in dogDatas){
                data.dog_data = d
                var alldatas = doWork(data)
                var infos = ConvertJsonToAnalyzedActivity(alldatas)
                result_activitys.addAll(infos)
            }


             println("Finish result activity " + result_activitys.size)
        }
    }

    inner class ShowMyDogActivity : Thread() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun run() {
            var eFlag =  false;
            var firstFlag = true;
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
                            dogDatas[dog_index].d_id.toInt(),
                            activity_data,
                            data.dog_data.d_goal_activity
                        )

                        // 그래프로 활동 정보 표시
                        val thread = DrawChart()
                        thread.start()

                        runOnUiThread {
                            txt_name.text = dogDatas[dog_index].d_name
                            txt_weight.text = dogDatas[dog_index].d_weight.toString() + " kg"
                            txt_heart_rate.text = analyzedActivityData.avgDailyHeartRate.toString() + " bpm"
                            if(analyzedActivityData.dailyRestTime > 60 )
                                txt_rest_time.text = (analyzedActivityData.dailyRestTime / 60.0).toInt().toString() +" 시간 " + (analyzedActivityData.dailyRestTime % 60.0).toInt().toString()+ "분"
                            else
                                txt_rest_time.text =(analyzedActivityData.dailyRestTime).toString()+ "분"

                            if(analyzedActivityData.dailySleepTime > 60 )
                                txt_sleep_time.text =(analyzedActivityData.dailySleepTime / 60.0).toInt().toString() +" 시간 " + (analyzedActivityData.dailySleepTime % 60.0).toInt().toString()+ "분"
                            else
                                txt_sleep_time.text = (analyzedActivityData.dailySleepTime).toString()+ "분"

                            if(analyzedActivityData.movementTime > 60 )
                                txt_movement_time.text = (analyzedActivityData.movementTime / 60.0).toInt().toString() +" 시간 " + (analyzedActivityData.movementTime % 60.0).toInt().toString()+ "분"
                            else
                                txt_movement_time.text =(analyzedActivityData.movementTime).toString()+ "분"

                            txt_movement_distance.text = (analyzedActivityData.distance / 100.0).toInt().toString() + " m"
                        }
                    }

                    var endTime = System.currentTimeMillis()
                    var spentTime = endTime-startTime

                    sleep(60 * 1000 - (spentTime))
                }
                catch (e: InterruptedException){
                    eFlag = true;
                    println("INTERRUPT " + e) // interrupt 발생 시 강제로 sleep 을 깨울 수 있음
                }
            }
        }
    }



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Toast.makeText(this, "result: " + resultCode, Toast.LENGTH_SHORT).show()
        if(resultCode == HOME_RESULT.SELECT_DOG.idx  ) { // -1

            showActivityThread.interrupt()
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