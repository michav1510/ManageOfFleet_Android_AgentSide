package com.example.kostas.manageoffleet_android_agentside.data.model.remote

import com.example.kostas.manageoffleet_android_agentside.data.model.RspnsForAuthentication
import com.example.kostas.manageoffleet_android_agentside.data.model.RspnsForLocationRecording


import org.json.JSONObject
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import rx.Observable

interface APIService {

    @POST("giveauthenticationtoagent.php")
    @Headers("Content-Type: application/json;charset=utf-8", "Accept: application/json;charset=utf-8", "Cache-Control: max-age=640000")
    fun sendCredntlsAndWaitForAuthentication(
//            @Body jsonObject: String
            @Body jsonObject : JSONObject
    ): Observable<RspnsForAuthentication>

    @POST("recordlocationofagent.php")
    @Headers("Content-Type: application/json;charset=utf-8", "Accept: application/json;charset=utf-8", "Cache-Control: max-age=640000")
    fun sendLastLocationRecordingOfAgent(
//            @Body jsonObject: String
            @Body jsonObject : JSONObject
    ): Observable<RspnsForLocationRecording>

}