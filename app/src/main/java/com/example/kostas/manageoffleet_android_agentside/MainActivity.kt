package com.example.kostas.manageoffleet_android_agentside

import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.google.android.gms.common.api.GoogleApiClient


import android.view.View
import android.widget.Button
import android.widget.EditText
import com.example.kostas.manageoffleet_android_agentside.data.model.RspnsForAuthentication
import com.example.kostas.manageoffleet_android_agentside.data.model.remote.APIService
import com.example.kostas.manageoffleet_android_agentside.data.model.remote.ApiUtils
import kotlinx.android.synthetic.main.activity_main.view.*
import org.json.JSONObject
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers


class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private lateinit var mGoogleApiClient: GoogleApiClient
    private var mLocationManager: LocationManager? = null
    lateinit var mLocation: Location
    private val listener: com.google.android.gms.location.LocationListener? = null
    private val FASTEST_INTERVAL: Long = 2000 /* 2 sec */

    private var mResponseTv: TextView? = null
    private var mAPIService: APIService? = null
    lateinit var  mdebugView: TextView
    lateinit var usertext: EditText
    lateinit var passtext: EditText
    lateinit var submitBtn : Button

    var succesffull_recordings_to_the_database:Int = 0

    var debug_counter : Int = 1;
    fun writeOnDebugger(message : String?){
        if(message!=null) {
            if (debug_counter % 10 == 0) mdebugView.text = ""
            mdebugView.append(">> " + message + "\n")
            debug_counter++
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        usertext = findViewById<View>(R.id.user) as EditText
        passtext = findViewById<View>(R.id.pass) as EditText
        submitBtn = findViewById<View>(R.id.btn_submit) as Button
        mResponseTv = findViewById<TextView>(R.id.tv_response)
        mdebugView = findViewById<TextView>(R.id.debug_view)
        //mdebugView.visibility = View.INVISIBLE // IN ORDER TO SEE THE DEBUG VIEW ADD HERE A COMMENT

        mAPIService = ApiUtils.apiService

        submitBtn.setOnClickListener {
            writeOnDebugger("setOnClickListener lambda expression")
            sendCredentials(usertext.text.toString(),passtext.text.toString())
        }

        writeOnDebugger("onCreate()")
    }

    fun sendCredentials(user: String, pwd: String) {
        writeOnDebugger("sendCredentials()")
        val jsonObject = JSONObject()
        jsonObject.put("username", user)
        jsonObject.put("pass",pwd)

        writeOnDebugger(jsonObject.toString())

        mAPIService?.sendCredntlsAndWaitForAuthentication(jsonObject)?.subscribeOn(Schedulers.io())?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(object : Subscriber<RspnsForAuthentication>() {
                    override fun onCompleted() {
                        writeOnDebugger("onCompleted()")
                    }

                    override fun onError(e: Throwable) {
                        writeOnDebugger("onError()")
                        writeOnDebugger(e.message)
                    }

                    override fun onNext(respns: RspnsForAuthentication) {
                        writeOnDebugger("onNext()")
                        showResponse(""+respns)
                        if(respns.result == 1)
                        {
//                            val resIntent = Intent(this@MainActivity,SuccessfullLoginActivity::class.java)
//                            val bundle:Bundle = Bundle()
//                            bundle.putString("user", usertext.text.toString());
//                            resIntent.putExtras(bundle);
//                            startActivity(resIntent)

                            val i = Intent(this@MainActivity, SuccessfullLoginService::class.java)
                            startService(i)
                        }
                    }
                })
    }


    fun showResponse(response: String) {
        writeOnDebugger("showResponse()")
        if (mResponseTv?.getVisibility() === View.GONE) {
            mResponseTv?.setVisibility(View.VISIBLE)
        }
        mResponseTv?.setText(response)
    }



}