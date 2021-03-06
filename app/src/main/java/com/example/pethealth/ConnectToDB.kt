package com.example.pethealth

import android.util.Log
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate

private val TAG: String = "Connect To DB"
private val SERVER_IPs: Array<String> = arrayOf("125.130.64.113" , "220.93.33.240")
private var IP_ADDR: String = "192.168.10.6"// 수정 서버 "125.130.64.113"//"192.168.10.2" (http://192.168.10.1/ WAN IP 주소)
// 석호 서버 "220.93.33.240"
private val db_mode: Array<String> = arrayOf("login", "join", "add", "delete", "modify", "list","add","delete", "update", "info" ,
    "add" , "all", "date", "today" , "curr" ,"get","add" , "weight"
)

enum class DB_MODES(var idx: Int) {
    LOGIN(0), JOIN(1), ADD(2), DEL(3), MODI(4),DLIST(5),DADD(6),DDEL(7),DUPD(8),DINFO(9) ,
    ACADD(10) ,ACALL(11) , ACDATAE(12), ACTODAY(13), ACCURR(14),RESGET(15) , RESADD(16), WEIGHT(17)
}

data class LoginData(var id: String, var pw: String) :Serializable
data class UserInfo(var login_data: LoginData, var name: String, var email: String , var phone: String) :Serializable
data class DogInfo (var login_data: LoginData, var d_id: String, var d_name: String, var d_breed: String,
                    var d_height:String, var d_length:String, var d_weight: String, var d_age : String ,
                    var d_goal_activity : Int, var d_join_date : LocalDate
) :Serializable

data class ActivityData (var d_id: String, var ac_id: String, var ac_date: LocalDate, var ac_hour : Int , var ac_minute : Int,
                         var ac_walk: Int , var ac_run : Int , var ac_distance : Int , var ac_heart_rate : Int , var ac_location : String,
                         var ac_device_id : String , var ac_posture : String) :Serializable

data class DateData (var start: LocalDate, var end:LocalDate ): Serializable

data class WeightChange (var d_id: String, var d_weight: Float , var wc_date: LocalDate)


public fun ChangeServerIP (){
    var i = SERVER_IPs.indexOf(IP_ADDR)
    if(i+1 < SERVER_IPs.size)
        i++
    else
        i = 0

    IP_ADDR = SERVER_IPs[i];
}

public /*suspend*/ fun doWork(data: ConnectToDB): String{
    lateinit var postParams: String
    lateinit var phpFileName: String

    when (data.type) {
        DB_MODES.LOGIN -> {
            postParams = "u_id=" + data.login_data.id + "&u_pw=" + data.login_data.pw
            phpFileName =  "userinfo"
        }

        DB_MODES.JOIN -> {
            postParams = ("u_id=" + data.user_data.login_data.id + "&u_pw=" + data.user_data.login_data.pw
                    +"&m_name=" + data.user_data.name + "&m_phone=" + data.user_data.phone + "&m_email=" + data.user_data.email)

            phpFileName =  "userinfo"
        }

        DB_MODES.ADD ->{
            postParams = ("u_id=" + data.user_data.login_data.id + "&m_name="
                    + data.user_data.name + "&m_phone=" + data.user_data.phone  + "&m_email=" + data.user_data.email)

            phpFileName =  "userinfo"
        }

        // 저장한 반려견들의 리스트를 받아온다.
        DB_MODES.DLIST -> {
            postParams = ("u_id=" + data.login_data.id)
            phpFileName =  "doginfo"
        }
        // 새로운 반려견을 추가한다.
        DB_MODES.DADD ->{
            // u_id, d_name, d_breed, d_height , d_length , d_weight , d_age, d_goal_activity
            postParams = (   "u_id=" + data.login_data.id    + "&d_name=" + data.dog_data.d_name  + "&d_breed=" + data.dog_data.d_breed
                    + "&d_height=" + data.dog_data.d_height  + "&d_length=" + data.dog_data.d_length
                    + "&d_weight=" + data.dog_data.d_weight  + "&d_age=" + data.dog_data.d_age +"&d_goal_activity=" + data.dog_data.d_goal_activity)

            phpFileName =  "doginfo"
        }
        // 반려견 정보를 제거한다.
        DB_MODES.DDEL -> {
            postParams = ("u_id=" + data.login_data.id + "&d_id=" + data.dog_data.d_id)
            phpFileName =  "doginfo"
        }
        // 반려견 정보를 수정한다.
        DB_MODES.DUPD -> {
            postParams = ("u_id=" + data.login_data.id + "&d_id=" + data.dog_data.d_id
                    + "&d_name=" + data.dog_data.d_name  + "&d_breed=" + data.dog_data.d_breed
                    + "&d_height=" + data.dog_data.d_height  + "&d_length=" + data.dog_data.d_length
                    + "&d_weight=" + data.dog_data.d_weight  + "&d_age=" + data.dog_data.d_age + "&d_goal_activity=" + data.dog_data.d_goal_activity)
            phpFileName =  "doginfo"
        }
        // 반려견 정보를 얻는다.
        DB_MODES.DINFO -> {
            postParams = ("d_id=" + data.dog_data.d_id)
            phpFileName =  "doginfo"
        }

        DB_MODES.ACADD -> {
            postParams = data.msg
            phpFileName =  "activitydata"
        }
        DB_MODES.ACALL -> {
            postParams = ("d_id=" + data.dog_data.d_id)
            phpFileName =  "activitydata"
        }
        DB_MODES.ACDATAE -> {
            postParams = ("d_id=" + data.dog_data.d_id + "&ac_date=" + data.date_data.start )
            phpFileName =  "activitydata"
        }
        DB_MODES.ACTODAY -> {
            postParams = ("d_id=" + data.dog_data.d_id )
            phpFileName =  "activitydata"
        }
        DB_MODES.ACCURR -> {
            postParams = ("d_id=" + data.dog_data.d_id )
            phpFileName =  "activitydata"
        }

        DB_MODES.RESGET -> {
            postParams = ("d_id=" + data.dog_data.d_id )
            phpFileName =  "result_activity"
        }
        DB_MODES.RESADD -> {
            postParams = (
                    "d_id=" + data.dog_data.d_id
                    +"&ac_date=" + data.analyzedActivityData.walkPerHour
            + "&res_walk_ph=" + ConvertArrayListToString(data.analyzedActivityData.walkPerHour)
            + "&res_run_ph=" + ConvertArrayListToString(data.analyzedActivityData.runPerHour)
            + "&res_move_ph=" + ConvertArrayListToString(data.analyzedActivityData.movementPerHour)
            + "&res_avg_walk_ph=" + ConvertArrayListToString(data.analyzedActivityData.avgWalkPerHour)
            + "&res_avg_run_ph="+ ConvertArrayListToString(data.analyzedActivityData.avgRunPerHour)
            + "&res_avg_move_ph=" + ConvertArrayListToString(data.analyzedActivityData.avgMovementPerHour)
            + "&res_avg_heart_ph=" + ConvertArrayListToString(data.analyzedActivityData.avgHeartRatePerHour)
            + "&res_all_wark=" + data.analyzedActivityData.allMovement // 데이터 베이스에서도 오타있음
            + "&res_avg_daily_walk=" + data.analyzedActivityData.avgDailyWalk
            + "&res_avg_daily_run=" + data.analyzedActivityData.avgDailyRun
            + "&res_avg_daily_move=" + data.analyzedActivityData.avgDailyMovement
            + "&res_avg_daily_heart=" + data.analyzedActivityData.avgDailyHeartRate
            + "&res_move_time=" + data.analyzedActivityData.movementTime
            + "&res_rest_time="+ data.analyzedActivityData.dailyRestTime
            + "&res_sleep_time=" + data.analyzedActivityData.dailySleepTime )
            phpFileName =  "result_activity"
        }
        DB_MODES.WEIGHT -> {
            postParams = ("d_id=" + data.dog_data.d_id )
            phpFileName =  "doginfo"
        }

    }

    var serverURL: String = "http://" + IP_ADDR + "/" + phpFileName + ".php?mode="+db_mode[data.type.idx]

    println("Params: $postParams, URL: $serverURL")

     try {
        var url = URL(serverURL)
        var httpURLCon: HttpURLConnection = url.openConnection() as HttpURLConnection

        httpURLCon.readTimeout = 10000
        httpURLCon.connectTimeout = 10000
        httpURLCon.requestMethod = "POST"
        httpURLCon.connect()

        var outputStream: OutputStream = httpURLCon.outputStream
        outputStream.write(postParams.toByteArray(Charsets.UTF_8))
        outputStream.flush()
        outputStream.close()

        var respStatCode: Int = httpURLCon.responseCode
        Log.d(TAG, "POST response code - $respStatCode")

        lateinit var inputStream: InputStream
        if (respStatCode == HttpURLConnection.HTTP_OK) {
            inputStream = httpURLCon.inputStream
        }
        else {
            inputStream = httpURLCon.errorStream
        }

        var inputStreamReader = InputStreamReader(inputStream, "UTF-8")
        var bufferedReader: BufferedReader = BufferedReader(inputStreamReader)

        var sb = StringBuilder()
        var line: String? = null

        do {
            line = bufferedReader.readLine()
            if (line != null)
                sb.append(line)
        } while(line != null)

        bufferedReader.close()

        return sb.toString()

    } catch (e: Exception) {
        Log.d(TAG, "InsertData: Error $e")
        return "Error: " + e.message
    }
}



class ConnectToDB {
    lateinit var type:DB_MODES
    lateinit var login_data:LoginData
    lateinit var user_data:UserInfo
    lateinit var dog_data:DogInfo
    lateinit var date_data:DateData
    lateinit var analyzedActivityData:AnalyzedActivityData
    lateinit var msg: String
}


