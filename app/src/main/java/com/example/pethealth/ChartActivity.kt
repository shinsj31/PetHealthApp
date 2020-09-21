package com.example.pethealth

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate

//https://github.com/PhilJay/MPAndroidChart#more-examples
class ChartActivity: AppCompatActivity(){
    var isrunning = false
    var pieChart: PieChart? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chart_layout)
        pieChart = findViewById(R.id.piechart) as PieChart
        pieChart!!.setUsePercentValues(true)
        pieChart!!.description.isEnabled = false
        pieChart!!.setExtraOffsets(5f, 10f, 5f, 5f)
        pieChart!!.dragDecelerationFrictionCoef = 0.95f
        pieChart!!.isDrawHoleEnabled = true
        pieChart!!.setHoleColor(Color.WHITE)
        pieChart!!.transparentCircleRadius = 1000f

        val yValues = ArrayList<PieEntry>()
        yValues.add(PieEntry(34f, "Japen"))
        yValues.add(PieEntry(23f, "USA"))
        yValues.add(PieEntry(14f, "UK"))
        yValues.add(PieEntry(35f, "India"))
        yValues.add(PieEntry(40f, "Russia"))
        yValues.add(PieEntry(40f, "Korea"))
        val description = Description()
        description.setText("세계 국가") //라벨
        description.setTextSize(15f)
        pieChart!!.description = description
        pieChart!!.animateY(1000, Easing.EasingOption.EaseInOutCubic) //애니메이션
        val dataSet = PieDataSet(yValues, "Countries")
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f
        dataSet.setColors(*ColorTemplate.JOYFUL_COLORS)
        val data = PieData(dataSet)
        data.setValueTextSize(10f)
        data.setValueTextColor(Color.YELLOW)
        pieChart!!.data = data
    }

    /*
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chart_layout)

        startButton.setOnClickListener {
            if (isrunning == false) {
                isrunning = true
                startButton.text = "그래프 구현중"
                startButton.isClickable = false
                val thread = ThreadClass()
                thread.start()
            }
        }


    }


    inner class ThreadClass : Thread() {
        override fun run() {
            val input = Array<Double>(1000,{Math.random()})
            // Entry 배열 생성
            var entries: ArrayList<Entry> = ArrayList()
            // Entry 배열 초기값 입력
            entries.add(Entry(0F , 0F))
            // 그래프 구현을 위한 LineDataSet 생성
            var dataset: LineDataSet = LineDataSet(entries, "input")
            // 그래프 data 생성 -> 최종 입력 데이터
            var data: LineData = LineData(dataset)
            // chart.xml에 배치된 lineChart에 데이터 연결
            lineChart.data = data

            runOnUiThread {
                // 그래프 생성
                lineChart.animateXY(1, 1)
            }

            for (i in 0 until input.size){

                SystemClock.sleep(10)
                data.addEntry(Entry(i.toFloat(), input[i].toFloat()), 0)
                data.notifyDataChanged()
                lineChart.notifyDataSetChanged()
                lineChart.invalidate()
            }
            startButton.text = "난수 생성 시작"
            startButton.isClickable = true

            isrunning = false;
        }
    }
    */

}