package com.example.pethealth

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry


class MovementCountActivity: AppCompatActivity(){


    private var daily_barChart:  BarChart? = null
    private var today_barChart:  BarChart? = null

    val labelList: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.movement_count_layout)
        SetChart()

    }

    fun SetChart (){
        daily_barChart = findViewById(R.id.daily_movement_chart) as BarChart
        today_barChart = findViewById(R.id.today_movement_chart) as BarChart


        daily_barChart!!.setBackgroundColor(Color.WHITE)


        daily_barChart!!.isHighlightFullBarEnabled = false

        daily_barChart!!.setScaleEnabled(false) // 확대 막기

        daily_barChart!!.description.isEnabled = false //차트 옆에 별도로 표기되는 description이다. false로 설정하여 안보이게 했다.
        daily_barChart!!.setMaxVisibleValueCount(0) // 최대 보이는 그래프 개수를 7개로 정해주었다.
        daily_barChart!!.setPinchZoom(false) // 핀치줌(두손가락으로 줌인 줌 아웃하는것) 설정
        daily_barChart!!.setDrawBarShadow(false) // 그래프의 그림자
        daily_barChart!!.setDrawGridBackground(false)//격자구조 넣을건지
        daily_barChart!!.axisLeft.isEnabled = false
        daily_barChart!!.axisRight.isEnabled = false
        daily_barChart!!.xAxis.isEnabled = true
        daily_barChart!!.xAxis.position = XAxis.XAxisPosition.BOTTOM


        val barChart =daily_barChart

        val entries: ArrayList<BarEntry> = ArrayList()

        var valList = HomeActivity.analyzedActivityData.walkPerHour

        for (i in 0 until valList.size) {
            entries.add(BarEntry(i.toFloat(),valList[i].toFloat()))
        }


        /*
        val depenses = BarDataSet(entries, "전국 가입자수") // 변수로 받아서 넣어줘도 됨

        depenses.axisDependency = YAxis.AxisDependency.LEFT

        val labels = ArrayList<String>()
        for (i in 0 until labelList.size) {
            labels.add(labelList.get(i) as String)
        }
        */


        val set = BarDataSet(entries, "Bar DataSet")

        set.color = R.color.colorBlueGreen
        set.valueTextColor = R.color.colorBlueGreen
        set.valueTextSize = 10f
        set.axisDependency = YAxis.AxisDependency.RIGHT
        val data = BarData(set)

       // val data = BarData(labels, depenses) // 라이브러리 v3.x 사용하면 에러 발생함

       //  depenses.setColors(*ColorTemplate.COLORFUL_COLORS) //


        barChart!!.data = data
        barChart.animateXY(1000, 1000)
        barChart.invalidate()

    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK) { //

        }
        else {

        }


    }

}

