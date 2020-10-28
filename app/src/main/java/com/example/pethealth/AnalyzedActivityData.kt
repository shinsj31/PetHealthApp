package com.example.pethealth

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime


public class AnalyzedActivityData {
    public var dog_id : Int = 0;
    public lateinit var dog : DogInfo
    public lateinit var ac_date : LocalDate;
    public var recordedWalk = Array<Int>(24*60 ,{0})
    public var recordedRun = Array<Int>(24*60 ,{0})
    public var recordedRest = Array<Int>(24*60 ,{0})
    public var recordedSleep = Array<Int>(24*60 ,{0})
    public var recordedHeartRate = Array<Int>(24*60 ,{0})
    public var walkPerHour = IntArray(24 , {0})  // 시간 당 걷기 수
    public var runPerHour = IntArray(24 , {0})  // 시간 당 뛰기 수
    public var restPerHour = IntArray(24 , {0})  // 시간 당 걷기 수
    public var sleepPerHour = IntArray(24 , {0})  // 시간 당 뛰기 수
    public var movementPerHour = IntArray(24 , {0})  // 시간 당 움직임
    public var avgWalkPerHour = IntArray(24 , {0})  // 시간 평균 걷기 수
    public var avgRunPerHour = IntArray(24 , {0})  // 시간 평균 뛰기 수
    public var avgMovementPerHour = IntArray(24 , {0})  // 시간 평균 움직임
    public var avgHeartRatePerHour= IntArray(24 , {0})  // 시간 당 평균 심박수
    public var allMovement: Int = 0  // 하루 총 움직임
    public var allWalk: Int = 0  // 하루 총 움직임
    public var allRun: Int = 0  // 하루 총 움직임
    public var avgDailyWalk: Int = 0  // 평균 걷기
    public var avgDailyRun: Int = 0  // 평균 뛰기
    public var avgDailyMovement: Int = 0  // 평균 움직임
    public var avgDailyHeartRate: Int = 0  // 평균 심박수
    public var goal : Int = 0
    public var movementTime: Int = 0  // 움직인 시간
    public var dailyRestTime : Int =0
    public var dailySleepTime : Int =0
    public var distance : Float = 0F

    @RequiresApi(Build.VERSION_CODES.O)
    public fun AnalyzeActivity ( dog_id : Int ,datas : ArrayList<ActivityData> ,  goal : Int ){
        if(datas.size > 0)
            this.ac_date = datas[0].ac_date
        else
            this.ac_date = LocalDate.now()

        this.dog_id = dog_id

        for (d in HomeActivity.dogDatas){
            if(d.d_id == this.dog_id.toString()){
                this.dog = d
                break
            }
        }
        this.goal = goal
        println("!! AnalyzeActivity")
        for(data in datas){
            var recordTime =  data.ac_hour * 60 + data.ac_minute;
            if(recordTime >= 1440  )
                break;


            if( this.ac_date == LocalDate.now()   ){
                val date: ZonedDateTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
                if(date.hour < data.ac_hour){
                    continue;
                }
                if(date.hour == data.ac_hour && date.minute <= data.ac_hour ){
                    continue;
                }
            }

            if( recordTime < recordedWalk.size && recordedWalk[recordTime] == 0){
                recordedWalk[recordTime] = data.ac_walk;
                recordedRun[recordTime] = data.ac_walk;
                recordedHeartRate[recordTime] = data.ac_heart_rate;
                allWalk += data.ac_walk
                allRun += data.ac_run
                allMovement += data.ac_walk + data.ac_walk;
            }

            if(recordedWalk[recordTime] + recordedRun[recordTime] > 5)
                movementTime++;
            else {
                // "stop", "move", " sitdown", "lieside", "lieupsdown", "stand"
                if(recordedHeartRate[recordTime] > 67 || data.ac_posture == "sitdown" ){
                    dailyRestTime++;
                    recordedRest[recordTime] = 1;
                }
                else if( data.ac_posture ==  "lieside" || data.ac_posture == "lieupsdown" ){
                    dailySleepTime++;
                    recordedSleep[recordTime] = 1;
                }
                else if(recordedHeartRate[recordTime] < 60 ) {
                    dailySleepTime++;
                    recordedSleep[recordTime] = 1;
                }
                else if (recordedHeartRate[recordTime] < 62  && data.ac_posture == "stop") {
                    dailySleepTime++;
                    recordedSleep[recordTime] = 1;
                }
                else {
                    dailyRestTime++;
                    recordedRest[recordTime] = 1;
                }

            }

        }
        val date: ZonedDateTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        var sumHeartRate = 0

        for(hour in   0.. date.hour) {

            sumHeartRate = 0

            var min = 59
            if(hour == date.hour )
                min = date.minute

            for(minute in   0..min ){
                var recordTime = hour * 60 + minute

                walkPerHour[hour] += recordedWalk[recordTime]
                runPerHour[hour]  +=  recordedRun[recordTime]


                sumHeartRate +=  recordedHeartRate[recordTime]
                restPerHour[hour] += recordedRest[recordTime]
                sleepPerHour[hour] += recordedSleep[recordTime]
            }
            movementPerHour[hour] = (walkPerHour[hour] + runPerHour[hour])
            min += 1
            avgWalkPerHour[hour] = walkPerHour[hour] / min
            avgRunPerHour[hour] = runPerHour[hour] / min
            avgMovementPerHour[hour] = (movementPerHour[hour]) / min
            avgHeartRatePerHour[hour] = sumHeartRate /min

        }

        var min = date.minute + 1

        var sumWalk = 0
        var sumRun = 0
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

        avgDailyWalk = sumWalk / ((date.hour)*60 + min)
        avgDailyRun  = sumRun / ((date.hour)*60 + min)
        avgDailyMovement  = (sumWalk + sumRun) / ((date.hour)*60 + min)
        avgDailyHeartRate = sumHeartRate /((date.hour)*60 + min)


        distance = (allWalk * dog.d_height.toFloat() * Math.sqrt(3.0) + allRun * dog.d_length.toFloat() * 0.5f).toFloat()
    }




    // 가장 많이 움직인 시각
    // 가장 적게 움직인 시각
    // 가장 높은 분당 심박수 시간당 심박수
    // 가장 낮은 분당 심박수 시간당 심박수

}
