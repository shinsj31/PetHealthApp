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
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class HeartbeatActivity: AppCompatActivity(){

    private var daily_barChart:  BarChart? = null
    private var today_lineChart:  LineChart? = null

    private lateinit var date : LocalDate

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.heartbeat_layout)

        date = LocalDate.now()
        daily_barChart = findViewById(R.id.daily_heartbeat_chart) as BarChart
        today_lineChart = findViewById(R.id.today_heartbeat_chart) as LineChart


        InitChart(daily_barChart!!)
        InitChart(today_lineChart!!)

        DrawAllDateChart()
        DrawTodayChart(date)


    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun DrawAllDateChart (){
        val entries1: ArrayList<BarEntry> = ArrayList()
        var valList = HomeActivity.result_activitys
        val labelList: ArrayList<String> = ArrayList()
        val dates: ArrayList<LocalDate> = ArrayList()
        var cnt =0

        var colors1 : ArrayList<Int> = ArrayList()


        for (i in 0..valList.size-1) {
            if(valList[i].dog_id == HomeActivity.dogDatas[HomeActivity.dog_index].d_id.toInt()){
                entries1.add(BarEntry(cnt.toFloat() - 0.15f, valList[i].avgDailyHeartRate.toFloat()))

                cnt++
                labelList.add(
                    valList[i].ac_date.format(DateTimeFormatter.ofPattern("MM-dd")).toString()
                )
                dates.add(valList[i].ac_date)

                if( 1500 < valList[i].dailyRestTime   ) {
                    colors1.add(Color.rgb(0x0C, 0x90, 0xAD))
                }else{
                    // 0C90AD
                    colors1.add(Color.rgb(145, 224, 244))
                }

            }
        }



        entries1.add(
            BarEntry(
                valList.size.toFloat(),
                HomeActivity.analyzedActivityData.avgDailyHeartRate.toFloat()
            )
        )


        if(1500 < HomeActivity.analyzedActivityData.avgDailyHeartRate ) {
            colors1.add(Color.rgb(0x0C, 0x90, 0xAD))

        }else{
            colors1.add(Color.rgb(145, 224, 244))
        }


        labelList.add(LocalDate.now().format(DateTimeFormatter.ofPattern("MM-dd")).toString())
        dates.add(LocalDate.now())
        var fillCnt = (5 - entries1.count() % 5)%5
        for(i in 1..fillCnt){
            entries1.add(
                BarEntry(
                    entries1.count().toFloat(),
                    0f
                )
            )
            labelList.add("")
            colors1.add(Color.rgb(0, 0, 0))
        }



        val set1 = BarDataSet(entries1, "평균 심박수")
        set1.setColors(colors1)
        set1.valueTextColor = R.color.colorBlueGreen
        set1.valueTextSize = 10f
        set1.axisDependency = YAxis.AxisDependency.RIGHT

        val data = BarData()
        data.addDataSet(set1)
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
                            this@HeartbeatActivity,
                            "" + e.x + "," + e.y,
                            Toast.LENGTH_SHORT
                        ).show()
                        //DrawTodayChart(dates[e.x.toInt()-1])
                        date = dates[e.x.toInt()]
                        var txt_r = findViewById(R.id.txt_heartbeat1) as TextView
                        txt_r.text = e.y.toString()  + "bpm"

                        var txt_date = findViewById(R.id.txt_date) as TextView
                        txt_date.text = date.toString()
                        DrawTodayChart(date)
                    }
                }
            }
        )

        daily_barChart!!.setVisibleXRangeMaximum(5f); // allow 5 values to be displayed 5개만 보이게

        daily_barChart!!.axisLeft.axisMinimum = 60f
        daily_barChart!!.axisLeft.granularity = 3f // 30분 간격?

        daily_barChart!!.moveViewToX(labelList.size - 1f - fillCnt);
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
    fun InitChart(chart: LineChart) {
        //chart!!.setBackgroundColor(Color.WHITE)

        chart!!.setScaleEnabled(false) // 확대 막기
        chart!!.description.isEnabled = false //차트 옆에 별도로 표기되는 description이다. false로 설정하여 안보이게 했다.
        chart!!.setPinchZoom(false) // 핀치줌(두손가락으로 줌인 줌 아웃하는것) 설정

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
        val entries: ArrayList<Entry> = ArrayList()
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
        var valList = a.avgHeartRatePerHour


        for (i in 0..valList.size-1) {
            entries.add(BarEntry(i.toFloat(), valList[i].toFloat()))
            labelList.add(i.toString())
        }


        val set = LineDataSet(entries, "심박수")
        set.color = Color.rgb(145, 224, 244)
        set.valueTextColor = R.color.colorBlueGreen
        set.valueTextSize = 0f
        set.axisDependency = YAxis.AxisDependency.RIGHT

        val data = LineData(set)
        //data.barWidth = 0.1f;
        val xAxis: XAxis = today_lineChart!!.xAxis // 라벨 적용
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

        today_lineChart!!.data = data

        today_lineChart!!.moveViewToX(labelList.size - 1.toFloat());

        today_lineChart!!.axisLeft.isEnabled = true
        today_lineChart!!.axisLeft.axisMinimum = 50f
        today_lineChart!!.axisLeft.granularity = 10f

        today_lineChart!!.animateXY(1000, 1000)
        today_lineChart!!.invalidate()



        //var txt_s = findViewById(R.id.txt_heartbeat2) as TextView



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

