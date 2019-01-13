package com.example.kostas.manageoffleet_android_agentside.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class RspnsForAuthentication {

    @SerializedName("result")
    @Expose
    public var result: Int? = null


    override fun toString(): String {
        return "RspnsForAuthentication{" +
                "result='" + result + '\''.toString() +
                '}'.toString()
    }

}