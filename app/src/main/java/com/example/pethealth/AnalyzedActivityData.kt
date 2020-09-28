package com.example.pethealth

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.ZoneId
import java.time.ZonedDateTime


public class AnalyzedActivityData {
    public var recordedWalk = Array<Int>(24*60 ,{0})
    public var recordedRun = Array<Int>(24*60 ,{0})
    public var recordedHeartRate = Array<Int>(24*60 ,{0})
    public var walkPerHour = IntArray(24 , {0})  // 시간 당 걷기 수
    public var runPerHour = IntArray(24 , {0})  // 시간 당 뛰기 수
    public var movementPerHour = IntArray(24 , {0})  // 시간 당 움직임
    public var avgWalkPerHour = IntArray(24 , {0})  // 시간 평균 걷기 수
    public var avgRunPerHour = IntArray(24 , {0})  // 시간 평균 뛰기 수
    public var avgMovementPerHour = IntArray(24 , {0})  // 시간 평균 움직임
    public var avgHeartRatePerHour= IntArray(24 , {0})  // 시간 당 평균 심박수
    public var allMovement: Int = 0  // 하루 총 움직임
    public var avgDailyWalk: Int = 0  // 평균 걷기
    public var avgDailyRun: Int = 0  // 평균 뛰기
    public var avgDailyMovement: Int = 0  // 평균 움직임
    public var avgDailyHeartRate: Int = 0  // 평균 심박수
    public var goal : Int = 0
    public var movementTime: Int = 0  // 움직인 시간

    public var dailyRestTime : Int =0
    public var dailySleepTime : Int =0

    @RequiresApi(Build.VERSION_CODES.O)
    public fun AnalyzeActivity (datas : ArrayList<ActivityData> ,  goal : Int ){

        this.goal = goal
        println("!! AnalyzeActivity")
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
        val date: ZonedDateTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));



        var sumWalk = 0
        var sumRun = 0
        var sumHeartRate = 0
        for(hour in   0.. date.hour) {

            sumWalk = 0
            sumRun = 0
            sumHeartRate = 0

            var min = 59
            if(hour == date.hour )
                min = date.minute

            for(minute in   0..min ){
                var recordTime = hour * 60 + minute

                if(recordedWalk[recordTime] != 0 || recordedRun[recordTime] != 0)
                     movementTime++;
                 else
                 {
                    if(recordedHeartRate[recordTime] > 65){
                        dailyRestTime++;
                    }
                     else {
                        dailySleepTime++;
                    }
                 }

                sumWalk += recordedWalk[recordTime]
                sumRun +=  recordedRun[recordTime]
                sumHeartRate +=  recordedHeartRate[recordTime]

            }
            walkPerHour[hour] = sumWalk
            runPerHour[hour] = sumRun
            movementPerHour[hour] = (sumWalk + sumRun)
            min += 1
            avgWalkPerHour[hour] = sumWalk / min
            avgRunPerHour[hour] = sumRun / min
            avgMovementPerHour[hour] = (sumWalk + sumRun) / min
            avgHeartRatePerHour[hour] = sumHeartRate /min

        }



        var min = date.minute + 1

        sumWalk = 0
        sumRun = 0
        sumHeartRate = 0
        for(hour in   0.. date.hour){
            sumWalk +=  walkPerHour[hour]
            sumRun +=  runPerHour[hour]

            if(hour != date.hour){
                sumHeartRate +=  avgHeartRatePerHour[hour] * 60
            }
            else {
                sumHeartRate +=  avgHeartRatePerHour[hour] * min
            }

        }

        avgDailyWalk = sumWalk / ((date.hour - 1)*60 + min)
        avgDailyRun  = sumRun / ((date.hour - 1)*60 + min)
        avgDailyMovement  = (sumWalk + sumRun) / ((date.hour - 1)*60 + min)
        avgDailyHeartRate = sumHeartRate /((date.hour - 1)*60 + min)



    }

    // 가장 많이 움직인 시각
    // 가장 적게 움직인 시각
    // 가장 높은 분당 심박수 시간당 심박수
    // 가장 낮은 분당 심박수 시간당 심박수

}
