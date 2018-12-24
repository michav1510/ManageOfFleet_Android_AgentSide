package com.example.kostas.manageoffleet_android_agentside.data.model.remote



object ApiUtils {

    val BASE_URL = "http://havistudio.com/MonitoringOfPhones/"

    val apiService: APIService
        get() = RetrofitClient.getClient(BASE_URL)!!.create(APIService::class.java)
}