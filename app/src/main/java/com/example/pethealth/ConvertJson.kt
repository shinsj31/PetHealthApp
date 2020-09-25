package com.example.pethealth

import android.os.Build
import androidx.annotation.RequiresApi
import org.json.JSONArray
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

        var info = DogInfo(LoginData(user_id, "") ,d_id , d_name, d_breed, d_height, d_length, d_weight, d_age, d_goal_activity)
        result.add(info)
    }

    return result;
}

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

        var info = DogInfo(LoginData(user_id, "") ,d_id , d_name, d_breed, d_height, d_length, d_weight, d_age, d_goal_activity)
        return info;
    }
    return null
}

//TODO: CovertJson으로 이동
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