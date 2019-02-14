package com.example.mike.manageoffleet_android_agentside.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Post {

    @SerializedName("result")
    @Expose
    public var result: Int? = null


    override fun toString(): String {
        return "Post{" +
                "result='" + result + '\''.toString() +
                '}'.toString()
    }

}