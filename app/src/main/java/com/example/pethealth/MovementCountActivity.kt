package com.example.pethealth

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class MovementCountActivity: AppCompatActivity(){


    private var daily_barChart:  BarChart? = null
    private var today_barChart:  BarChart? = null


    private lateinit var date : LocalDate


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.movement_count_layout)

        date = LocalDate.now()
        daily_barChart = findViewById(R.id.daily_movement_chart) as BarChart
        today_barChart = findViewById(R.id.today_movement_chart) as BarChart


        InitChart(daily_barChart!!)
        InitChart(today_barChart!!)

        DrawAllDateChart()
        DrawTodayChart(LocalDate.now())


    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun DrawAllDateChart (){
        val entries: ArrayList<BarEntry> = ArrayList()
        var valList = HomeActivity.result_activitys
        val labelList: ArrayList<String> = ArrayList()
        val dates: ArrayList<LocalDate> = ArrayList()
        var cnt =0

        var colors : ArrayList<Int> = ArrayList()


        for (i in 0..valList.size-1) {
            if(valList[i].dog_id == HomeActivity.dogDatas[HomeActivity.dog_index].d_id.toInt()){
                entries.add(BarEntry(cnt++.toFloat(), valList[i].allMovement.toFloat()))
                labelList.add(
                    valList[i].ac_date.format(DateTimeFormatter.ofPattern("MM-dd")).toString()
                )
                dates.add(valList[i].ac_date)

                if(HomeActivity.dogDatas[HomeActivity.dog_index].d_goal_activity < valList[i].allMovement ) {
                    colors.add(Color.rgb(0x0C, 0x90, 0xAD))
                }else{
                    // 0C90AD

                    colors.add(Color.rgb(145, 224, 244))

                }
            }
        }

        entries.add(
            BarEntry(
                valList.size.toFloat(),
                HomeActivity.analyzedActivityData.allMovement.toFloat()
            )
        )
        if(HomeActivity.dogDatas[HomeActivity.dog_index].d_goal_activity < HomeActivity.analyzedActivityData.allMovement ) {
            colors.add(Color.rgb(0x0C, 0x90, 0xAD))

        }else{
            colors.add(Color.rgb(145, 224, 244))
        }
        labelList.add(LocalDate.now().format(DateTimeFormatter.ofPattern("MM-dd")).toString())
        dates.add(LocalDate.now())
        var fillCnt =   (5 - entries.count() % 5)%5
        for(i in 1..fillCnt){
            entries.add(
                BarEntry(
                    entries.count().toFloat() ,
                    0f
                )
            )
            labelList.add("")
            colors.add(Color.rgb(0, 0, 0))
        }




        val set = BarDataSet(entries, "걸음수")
        set.setColors(colors)
        //set.color = R.color.colorBlueGreen
        set.valueTextColor = R.color.colorBlueGreen
        set.valueTextSize = 10f
        set.axisDependency = YAxis.AxisDependency.RIGHT

        val data = BarData(set)
        data.barWidth = 0.1f;


        val xAxis: XAxis = daily_barChart!!.xAxis // 라벨 적용
        xAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase): String? {
                return if (labelList.size > value.toInt()) {
                    labelList.get(value.toInt())
                } else null
            }
            val decimalDigits: Int
                get() = 0
        }
        xAxis.setGranularity(1f);

        daily_barChart!!.data = data


        daily_barChart!!.setOnChartValueSelectedListener(
            //https://stackoverflow.com/questions/35268971/mpandroidchart-click-listener-on-chart
            object : OnChartValueSelectedListener {
                override fun onNothingSelected() {
                }

                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    if (e != null && e.x.toInt() < dates.count() ) {
                        Toast.makeText(
                            this@MovementCountActivity,
                            "" + e.x + "," + e.y,
                            Toast.LENGTH_SHORT
                        ).show()
                        //DrawTodayChart(dates[e.x.toInt()-1])
                        date = dates[e.x.toInt()]

                        DrawTodayChart(date)
                    }
                }
            }
        )
        daily_barChart!!.setVisibleXRangeMaximum(5f); // allow 5 values to be displayed 5개만 보이게

        daily_barChart!!.axisLeft.axisMinimum = 0f
        daily_barChart!!.axisLeft.granularity = HomeActivity.dogDatas[HomeActivity.dog_index].d_goal_activity.toFloat()

        daily_barChart!!.moveViewToX(labelList.size - 1.toFloat() - fillCnt);
        //////////////////////////////////////////////////////////////////////////////////////////////////////////

        daily_barChart!!.animateXY(1000, 1000)
        daily_barChart!!.invalidate()
    }

    fun InitChart(chart: BarChart) {
        //chart!!.setBackgroundColor(Color.WHITE)
        chart!!.isHighlightFullBarEnabled = false
        chart!!.setScaleEnabled(false) // 확대 막기
        chart!!.description.isEnabled = false //차트 옆에 별도로 표기되는 description이다. false로 설정하여 안보이게 했다.
        chart!!.setPinchZoom(false) // 핀치줌(두손가락으로 줌인 줌 아웃하는것) 설정
        chart!!.setDrawBarShadow(false) // 그래프의 그림자
        chart!!.setDrawGridBackground(false)//격자구조 넣을건지
        chart!!.axisLeft.isEnabled = true
        chart!!.axisRight.isEnabled = false
        chart!!.xAxis.isEnabled = true
        chart!!.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart!!.xAxis.setDrawGridLines(false);

        chart!!.getLegend().setEnabled(false);

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun DrawTodayChart(date: LocalDate){
        val entries: ArrayList<BarEntry> = ArrayList()
        val labelList: ArrayList<String> = ArrayList()

        var a :AnalyzedActivityData = HomeActivity.analyzedActivityData

        if(date != LocalDate.now()){
            var results = HomeActivity.result_activitys

            for (r in results){
                if(r.dog_id == (HomeActivity.dogDatas[HomeActivity.dog_index].d_id.toInt()) && r.ac_date == date){
                    a =r
                    break;
                }
            }

        }
        var valList = a.movementPerHour


        for (i in 0..valList.size-1) {
            entries.add(BarEntry(i.toFloat(), valList[i].toFloat()))
            labelList.add(i.toString())
        }


        val set = BarDataSet(entries, "걸음수")
        set.color = Color.rgb(145, 224, 244)
        set.valueTextColor = R.color.colorBlueGreen
        set.valueTextSize = 10f
        set.axisDependency = YAxis.AxisDependency.RIGHT

        val data = BarData(set)
        data.barWidth = 0.1f;
        val xAxis: XAxis = today_barChart!!.xAxis // 라벨 적용
        xAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase): String? {
                return if (labelList.size > value.toInt()) {
                    labelList.get(value.toInt())
                } else null
            }
            val decimalDigits: Int
                get() = 0
        }


        today_barChart!!.data = data
        today_barChart!!.animateXY(1000, 1000)
        today_barChart!!.invalidate()

        //today_barChart!!.setVisibleXRangeMaximum(5f); // allow 5 values to be displayed 5개만 보이게
        today_barChart!!.moveViewToX(labelList.size - 1.toFloat());

        var txt_d = findViewById(R.id.txt_distance) as TextView
        var txt_w = findViewById(R.id.txt_walk_count) as TextView
        var txt_r = findViewById(R.id.txt_run_count) as TextView



        today_barChart!!.axisLeft.isEnabled = true
        daily_barChart!!.axisLeft.axisMinimum = 0f
        daily_barChart!!.axisLeft.granularity = 100f

        txt_d.text = (a.distance / 100.0).toInt().toString()  + "m"
        txt_w.text = a.allWalk.toString() + "걸음"
        txt_r.text = a.allRun.toString() + "걸음"

        var txt_date = findViewById(R.id.txt_date) as TextView
        txt_date.text = date.toString()
        //today_barChart!!.xAxis.valueFormatter =   IAxisValueFormatter { value, axis -> mQuarter[value.toInt() % mQuarter.size] }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK) { //

        }
        else {

        }


    }

}

