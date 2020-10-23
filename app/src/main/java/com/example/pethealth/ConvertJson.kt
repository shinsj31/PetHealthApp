package com.example.pethealth

import android.os.Build
import androidx.annotation.RequiresApi
import org.json.JSONArray
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
public fun ConvertJsonToDogInfos (json :String, user_id : String ) :  ArrayList<DogInfo>{
    var result :  ArrayList<DogInfo> = ArrayList()
    var jarray = JSONArray(json);   // JSONArray 생성
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
        var d_join_date =  LocalDate.parse(jObject.getString("d_join_date") , DateTimeFormatter.ISO_DATE);

        var info = DogInfo(LoginData(user_id, "") ,d_id , d_name, d_breed, d_height, d_length, d_weight, d_age, d_goal_activity , d_join_date)
        result.add(info)
    }

    return result;
}

@RequiresApi(Build.VERSION_CODES.O)
public fun ConvertJsonToDogInfo (json :String, user_id : String ) : DogInfo? {
    var jarray = JSONArray(json);   // JSONArray 생성
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
        var d_join_date =  LocalDate.parse(jObject.getString("d_join_date") , DateTimeFormatter.ISO_DATE);

        var info = DogInfo(LoginData(user_id, "") ,d_id , d_name, d_breed, d_height, d_length, d_weight, d_age, d_goal_activity , d_join_date)
        return info;
    }
    return null
}

@RequiresApi(Build.VERSION_CODES.O)
public fun ConvertJsonToDogWeight (json :String ) : ArrayList<WeightChange> {
    var infos = ArrayList<WeightChange>();
    if( json.contains(("false")) || json.contains(("Error"))){

        return infos
    }

    var jarray = JSONArray(json);   // JSONArray 생성

    for( i in 0..(jarray.length() - 1)) {

        var jObject = jarray.getJSONObject(i) // JSONObject 추출
        var d_id = jObject.getString("d_id")
        var d_weight = jObject.getDouble("d_weight").toFloat()
        var wc_date =  LocalDate.parse(jObject.getString("wc_date") , DateTimeFormatter.ISO_DATE);

        var info = WeightChange( d_id , d_weight, wc_date)
        infos.add(info);
    }
    return infos
}

@RequiresApi(Build.VERSION_CODES.O)
fun ConvertJsonToActivityData (_json: String ): ArrayList<ActivityData>{

    var _activity_datas = ArrayList<ActivityData>()

    if(_json == "false" || _json.contains(("Error"))){

        return _activity_datas
    }


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
fun ConvertJsonToAnalyzedActivity (_json: String ): ArrayList<AnalyzedActivityData>{

    var result = ArrayList<AnalyzedActivityData>()

    if(_json == "false" || _json.contains(("Error"))){

        return result
    }

    var jarray = JSONArray(_json);   // JSONArray 생성

    for( i in 0..(jarray.length() - 1)) {

        var jObject = jarray.getJSONObject(i) // JSONObject 추출
        var a :AnalyzedActivityData = AnalyzedActivityData()
        a.dog_id = jObject.getInt("d_id")
        for (d in HomeActivity.dogDatas){
            if(d.d_id == a.dog_id.toString()){
                a.dog = d
                break
            }
        }
        a.goal = a.dog.d_goal_activity
        a.ac_date = LocalDate.parse(jObject.getString("ac_date") , DateTimeFormatter.ISO_DATE);
        a.walkPerHour =  ConvertStringToArrayList(jObject.getString("res_walk_ph"))
        a.runPerHour = ConvertStringToArrayList(jObject.getString("res_run_ph"))
        a.movementPerHour = ConvertStringToArrayList(jObject.getString("res_move_ph"))
        a.avgWalkPerHour = ConvertStringToArrayList(jObject.getString("res_avg_walk_ph"))
        a.avgRunPerHour  = ConvertStringToArrayList(jObject.getString("res_avg_run_ph"))
        a.avgMovementPerHour  = ConvertStringToArrayList(jObject.getString("res_avg_move_ph"))
        a.avgHeartRatePerHour  = ConvertStringToArrayList(jObject.getString("res_avg_heart_ph"))
        a.allMovement = jObject.getInt("res_all_wark")
        a.avgDailyWalk = jObject.getInt("res_avg_daily_walk")
        a.avgDailyRun = jObject.getInt("res_avg_daily_run")
        a.avgDailyMovement = jObject.getInt("res_avg_daily_move")
        a.avgDailyHeartRate = jObject.getInt("res_avg_daily_heart")
        a.movementTime = jObject.getInt("res_move_time")
        a.dailyRestTime = jObject.getInt("res_rest_time")
        a.dailySleepTime = jObject.getInt("res_sleep_time")


        for( i in 0..a.walkPerHour.count() - 1){
            a.allWalk += a.walkPerHour[i]
            a.allRun += a.runPerHour[i]
        }


        a.distance =( a.allWalk * a.dog.d_height.toFloat() * Math.sqrt(3.0) +  a.allRun * a.dog.d_length.toFloat() * 0.5f).toFloat()

        result.add(a)
    }

    return result
}


fun ConvertArrayListToString (array : IntArray ) : String{
    var result = ""

    for( i in 0..array.size - 1){
        if(i != array.size - 1)
            result = result + array[i] + ","
        else
            result += array[i]
    }

    return result
}
fun ConvertArrayListToString (array : Array<String> ) : String{
    var result = ""

    for( i in 0..array.size - 1){
        if(i != array.size - 1)
            result = result + array[i] + ","
        else
            result += array[i]
    }

    return result
}


fun ConvertStringToArrayList (str : String ) : IntArray{

    var token = str.split(',')
    var result : IntArray = IntArray(token.size)
    var cnt =0
    for( t in token){
        result[cnt++] = t.toInt()
    }

    return result
}
