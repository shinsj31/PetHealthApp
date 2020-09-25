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
import java.time.LocalDate
import java.time.LocalDateTime


enum class HOME_RESULT(var idx: Int) {
    SELECT_DOG(10), NEW_DOG(11) , MODI_DOG(12) , CANCLE_DOG(13)
}

public class AnalyzedActivityData {
    private var recordedWalk = Array<Int>(24*60 ,{0})
    private var recordedRun = Array<Int>(24*60 ,{0})
    private var recordedHeartRate = Array<Int>(24*60 ,{0})
    private var walkPerHour = IntArray(24 , {0})  // 시간 당 걷기 수
    private var runPerHour = IntArray(24 , {0})  // 시간 당 뛰기 수
    private var movementPerHour = IntArray(24 , {0})  // 시간 당 움직임
    private var heartRatePerHour= IntArray(24 , {0})  // 시간 당 평균 심박수
    private var allMovement: Int = 0  // 하루 총 움직임
    private var avgDailyWalk: Int = 0  // 평균 걷기
    private var avgDailyRun: Int = 0  // 평균 뛰기
    private var avgDailyMovement: Int = 0  // 평균 움직임
    private var avgDailyHeartRate: Int = 0  // 평균 심박수

    @RequiresApi(Build.VERSION_CODES.O)
    public fun AnalyzeActivity (datas : ArrayList<ActivityData> ){
        for(data in datas){
            var recordTime =  data.ac_hour * 60 + data.ac_minute;
            println( "Record Time : " + recordTime);
            if( recordTime < recordedWalk.size && recordedWalk[recordTime] == 0){
                recordedWalk[recordTime] = data.ac_walk;
                recordedRun[recordTime] = data.ac_walk;
                recordedHeartRate[recordTime] = data.ac_heart_rate;

                allMovement += data.ac_walk + data.ac_walk;
            }
        }
        val date: LocalDateTime = LocalDateTime.now()
        var cnt = 0
        var sumWalk = 0
        var sumRun = 0
        var sumHeartRate = 0

        for(minute in   0.. date.minute){
            var recordTime = date.hour * 60 + minute

            cnt++;
            sumWalk +=  recordedWalk[recordTime]
            sumRun +=  recordedRun[recordTime]
            sumHeartRate +=  recordedHeartRate[recordTime]

        }
        walkPerHour[date.hour] = sumWalk / cnt
        runPerHour[date.hour] = sumRun / cnt
        movementPerHour[date.hour] = (sumWalk + sumRun) / cnt
        heartRatePerHour[date.hour] = sumHeartRate /cnt

        var lastCnt = cnt
        cnt = 0
        sumWalk = 0
        sumRun = 0
        sumHeartRate = 0
        for(hour in   0.. date.hour){
            if(hour != date.hour){
                cnt++;
                sumWalk +=  walkPerHour[hour] * 60
                sumRun +=  runPerHour[hour] * 60
                sumHeartRate +=  heartRatePerHour[hour] * 60
            }
            else {
                sumWalk +=  walkPerHour[hour] * lastCnt
                sumRun +=  runPerHour[hour] * lastCnt
                sumHeartRate +=  heartRatePerHour[hour] * lastCnt
            }

        }

        avgDailyWalk = sumWalk / (cnt*60 + lastCnt)
        avgDailyRun  = sumRun / (cnt*60 + lastCnt)
        avgDailyMovement  = (sumWalk + sumRun) / (cnt*60 + lastCnt)
        avgDailyHeartRate = sumHeartRate /(cnt*60 + lastCnt)


    }

    // 가장 많이 움직인 시각
    // 가장 적게 움직인 시각
    // 가장 높은 분당 심박수 시간당 심박수
    // 가장 낮은 분당 심박수 시간당 심박수

}


class HomeActivity: AppCompatActivity() {
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

                    dogDatas = ConvertJsonToDogInfos(dog_list_json, user_id)
                    dog_index = 0 //TODO:로컬 데이터 읽어오기
                    val thread = ShowMyDogActivity()
                    thread.start()

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
    fun SumActivityData(datas: ArrayList<ActivityData>): ActivityData {
        var sum: ActivityData = ActivityData("", "", LocalDate.now(), 0, 0, 0, 0, 0, 0, "", "", "")

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

        val data = CombinedData()
        data.setData(generateLineData())
        data.setData(generateBarData())
        data.setData(generateBarData())
        xAxis.axisMaximum = data.xMax + 0.25f
         combinedChart!!.data = data
         combinedChart!!.invalidate()
    }

    private fun generateLineData(): LineData? {
        val d = LineData()
        val entries = ArrayList<Entry>()
        for (index in 0 until mQuarter.size) {
            entries.add(Entry(index.toFloat(), getRandom(15f, 5f)))
        }
        val set = LineDataSet(entries, "Line DataSet")
        set.color = Color.rgb(0, 100, 0)
        set.lineWidth = 2.5f
        set.setCircleColor(Color.rgb(0, 100, 0))
        set.circleRadius = 5f
        set.fillColor = Color.rgb(0, 100, 0)
        set.mode = LineDataSet.Mode.CUBIC_BEZIER
        set.setDrawValues(true)
        set.valueTextSize = 10f
        set.valueTextColor = Color.rgb(0, 100, 0)
        set.axisDependency = YAxis.AxisDependency.LEFT
        d.addDataSet(set)
        return d
    }

    private fun generateBarData(): BarData? {
        val entries = ArrayList<BarEntry>()
        for (index in 0 until mQuarter.size) {
            //var entry =
            entries.add(BarEntry( index.toFloat() , getRandom(25f, 25f)))
       }
        val set = BarDataSet(entries, "Bar DataSet")
        set.color = Color.rgb(60, 220, 78)
        set.valueTextColor = Color.rgb(60, 220, 78)
        set.valueTextSize = 10f
        set.axisDependency = YAxis.AxisDependency.RIGHT
        return BarData(set)
    }

    private fun getRandom(range: Float, startsfrom: Float): Float {
        return (Math.random() * range).toFloat() + startsfrom
    }





    fun AddPieChart(walk: Int, goal: Int){
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
        if(resultCode == HOME_RESULT.SELECT_DOG.idx || resultCode == HOME_RESULT.CANCLE_DOG.idx ) { // -1
            if(data != null){
                if(resultCode == HOME_RESULT.SELECT_DOG.idx){
                    Toast.makeText(this, "INDEX : " + dog_index, Toast.LENGTH_SHORT).show()
                    dog_index = data.extras!!.getInt("dog_index")

                }

                dogDatas = data.getCharSequenceArrayListExtra("dog_list") as ArrayList<DogInfo>

                val thread = ShowMyDogActivity()
                thread.start()

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