package com.example.pethealth

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomeActivity: AppCompatActivity() {
    private lateinit var user_id: String
    private lateinit var dog_list_json: String
    private lateinit var activity_today_json: String
    private var dogDatas = ArrayList<DogInfo>()
    private var activity_data = ArrayList<ActivityData>()
    private var dog_index = -1 // save local

    private var pieChart: PieChart? = null


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_layout)


        SetPieChart()

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

                try {

                    ConvertJsonToDogList();

                    dog_index = 0
                    if(dog_index != -1){

                        CoroutineScope(Dispatchers.Main).launch{
                            Toast.makeText(this@HomeActivity,   dogDatas[dog_index].d_name , Toast.LENGTH_SHORT).show()
                        }


                        /* Get Today Activity Data */
                        data.dog_data = dogDatas[dog_index]
                        data.type = DB_MODES.ACTODAY
                        activity_today_json = doWork(data)

                        activity_data = ConvertJsonToActivityData(activity_today_json);
                        var todaySum = SumActivityData(activity_data)

                        CoroutineScope(Dispatchers.Main).launch{
                            Toast.makeText(this@HomeActivity,  "" + todaySum.ac_walk + "//" + todaySum.ac_run , Toast.LENGTH_SHORT).show()
                        }
                        // 그래프로 활동 정보 표시
                        val thread = DrawPieChart()
                        thread.start()
                        DrawPieChart()
                        AddPieChart(todaySum.ac_walk + todaySum.ac_run, data.dog_data.d_goal_activity)
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

}