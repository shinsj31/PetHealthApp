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


class WeightActivity: AppCompatActivity(){

    private var daily_lineChart:  LineChart? = null
    private lateinit var valList: ArrayList<WeightChange>
    private var dates: ArrayList<LocalDate> = ArrayList<LocalDate>()
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.weight_change_layout)
        var server = ConnectToServer()
        var data = ConnectToDB()
        data.type = DB_MODES.WEIGHT
        data.dog_data = HomeActivity.dogDatas[HomeActivity.dog_index];
        server.data = data;



        daily_lineChart = findViewById(R.id.today_weight_change_chart) as LineChart
        InitChart(daily_lineChart!!)
        server.start();


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

            valList = ConvertJsonToDogWeight(result_to_connect);

            runOnUiThread{
                DrawAllDateChart()
                DrawTodayChart(valList.size - 1 )
            }

        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun DrawAllDateChart (){
        val entries1: ArrayList<Entry> = ArrayList()

        val labelList: ArrayList<String> = ArrayList()

        var cnt =0

        var colors1 : ArrayList<Int> = ArrayList()


        for (i in 0 until (valList?.size ?: 0)) {
            entries1.add(Entry(cnt.toFloat(), valList[i].d_weight ))

            cnt++
            labelList.add(
                valList[i].wc_date.format(DateTimeFormatter.ofPattern("MM-dd")).toString()
            )
            dates.add(valList[i].wc_date)

            if( 3 < valList[i].d_weight   ) {
                colors1.add(Color.rgb(0x0C, 0x90, 0xAD))
            }else{
                // 0C90AD
                colors1.add(Color.rgb(145, 224, 244))
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



        val set1 = LineDataSet(entries1, "평균 심박수")

        set1.setColors(colors1)
        set1.valueTextColor = R.color.colorBlueGreen
        set1.valueTextSize = 10f
        set1.axisDependency = YAxis.AxisDependency.RIGHT

        val data = LineData()
        data.addDataSet(set1)


        val xAxis: XAxis = daily_lineChart!!.xAxis // 라벨 적용
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


        daily_lineChart!!.data = data



        daily_lineChart!!.setOnChartValueSelectedListener(
            //https://stackoverflow.com/questions/35268971/mpandroidchart-click-listener-on-chart
            object : OnChartValueSelectedListener {
                override fun onNothingSelected() {
                }

                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    if (e != null && e.x.toInt() < dates.count() ) {
                        Toast.makeText(
                            this@WeightActivity,
                            "" + e.x + "," + e.y,
                            Toast.LENGTH_SHORT
                        ).show()
                        //DrawTodayChart(dates[e.x.toInt()-1])

                        DrawTodayChart(e.x.toInt())
                    }
                }
            }
        )

        daily_lineChart!!.setVisibleXRangeMaximum(5f); // allow 5 values to be displayed 5개만 보이게

        //daily_barChart!!.axisLeft.axisMinimum = 0f
        //daily_barChart!!.axisLeft.granularity = 60.0f // 30분 간격?

        daily_lineChart!!.moveViewToX(labelList.size - 1f - fillCnt);
        //////////////////////////////////////////////////////////////////////////////////////////////////////////
        daily_lineChart!!.animateXY(1000, 1000)
        daily_lineChart!!.invalidate()


    }

    fun InitChart(chart: LineChart) {
        chart!!.setBackgroundColor(Color.WHITE)

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
    fun DrawTodayChart(index: Int){

        var txt_r = findViewById(R.id.txt_weight1) as TextView

        if(index < valList.size)
        txt_r.text = (valList[index].d_weight).toString()  + "kg"
        var txt_date = findViewById(R.id.txt_date) as TextView
        txt_date.text = dates[index].toString()
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

