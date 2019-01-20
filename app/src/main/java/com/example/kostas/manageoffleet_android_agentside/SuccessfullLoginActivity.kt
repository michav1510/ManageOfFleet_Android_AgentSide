package com.example.kostas.manageoffleet_android_agentside

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.support.multidex.MultiDex
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import android.Manifest
import android.icu.util.Calendar
import android.support.v4.app.ActivityCompat


import android.view.View
import com.example.kostas.manageoffleet_android_agentside.data.model.RspnsForLocationRecording
import com.example.kostas.manageoffleet_android_agentside.data.model.remote.APIService
import com.example.kostas.manageoffleet_android_agentside.data.model.remote.ApiUtils
import org.json.JSONObject
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.text.SimpleDateFormat


class SuccessfullLoginActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private val TAG = "SuccessfullLoginActivity"
    private lateinit var mGoogleApiClient: GoogleApiClient
    private var mLocationManager: LocationManager? = null
    lateinit var mLocation: Location
    private var mLocationRequest: LocationRequest? = null
    private val listener: com.google.android.gms.location.LocationListener? = null
    private val UPDATE_INTERVAL = (2 * 1000).toLong()  /* 10 secs */
    private val FASTEST_INTERVAL: Long = 2000 /* 2 sec */


    private var mResponseTv: TextView? = null
    private var mAPIService: APIService? = null
    lateinit var  mdebugView: TextView
    lateinit var user: String
    var counter_Of_successfull_location_sending:Int = 0



    var debug_counter : Int = 1;
    fun writeOnDebugger(message : String?){
        if(message!=null) {
            if (debug_counter % 13 == 0) mdebugView.text = ""
            mdebugView.append(">> " + message + "\n")
            debug_counter++
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_successfull_login)

        val bundle = intent.extras
        user = bundle.getString("user")
//        user = "michav"

        mdebugView = findViewById<TextView>(R.id.debug_view)
        //mdebugView.visibility = View.INVISIBLE // IN ORDER TO SEE THE DEBUG VIEW ADD HERE A COMMENT

        mAPIService = ApiUtils.apiService

        writeOnDebugger("onCreate()")

        MultiDex.install(this)

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()

        mLocationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        checkLocation()

    }


    override fun onStart() {
        writeOnDebugger("onStart()")
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }


    fun sendCredentials(user: String, latitude: String, longitude: String, timeMilli:String, date:String) {
        writeOnDebugger("sendCredentials()")
        val jsonObject = JSONObject()
        jsonObject.put("username", user)
        jsonObject.put("latitude",latitude)
        jsonObject.put("longitude",longitude)
        jsonObject.put("timeinmilli",timeMilli)
        jsonObject.put("date",date)

        writeOnDebugger(jsonObject.toString())

        // RxJava

        mAPIService?.sendLastLocationRecordingOfAgent(jsonObject)?.subscribeOn(Schedulers.io())?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(object : Subscriber<RspnsForLocationRecording>() {
                    override fun onCompleted() {
                        writeOnDebugger("onCompleted()")
                    }

                    override fun onError(e: Throwable) {
                        writeOnDebugger("onError()")
                        writeOnDebugger(e.message)
                    }

                    override fun onNext(post: RspnsForLocationRecording) {
                        writeOnDebugger("onNext()")
                        if(post.result == 1)
                        {
                            counter_Of_successfull_location_sending++
                        }
                        showResponse(" "+counter_Of_successfull_location_sending)
                        //showResponse("Succesfull Recordings "+counter_Of_successfull_location_sending )
                        //showResponse(post.toString())
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


    override fun onStop() {
        writeOnDebugger("onStop")
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }


    override fun onConnectionSuspended(p0: Int) {
        writeOnDebugger("onConnectionSuspended()")
        Log.i(TAG, "Connection Suspended");
        mGoogleApiClient.connect();
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        writeOnDebugger("onConnectionFailed()")
        Log.i(TAG, "Connection failed. Error: " + connectionResult.getErrorCode());
    }

    override fun onLocationChanged(location: Location) {
        writeOnDebugger("onLocationChanged()")
        var msg = "Updated Location: Latitude " + location.longitude.toString() + location.longitude;
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        if (location != null) {
            // Logic to handle location object
            mLocation = location;
            //EDO EGRAFA STO txt_latitude kai txt_longitude tis sintetagemens location.latitude
            if(mLocation!= null  /* && convertLatLongDifferenceToMeters(mLocation.latitude,mLocation.longitude,location.latitude,location.longitude) > 20*/) {
                mLocation = location;
                //ipologismos tou xronou se date kai milli
                val string_username = user
                val string_time_milli = "" + System.currentTimeMillis()
                val string_formattedDate = CalculateDateTimeInString()
                val string_latitude = "" + mLocation.latitude
                val string_longitude = "" + mLocation.longitude
                sendCredentials(string_username, string_latitude, string_longitude, string_time_milli, string_formattedDate)
            }
        }else {
            writeOnDebugger("\"OnSuccesListener: the location = null")
        }
    }

    fun CalculateDateTimeInString():String{
        writeOnDebugger("CalculateDateTimeInString()")
        val cal = Calendar.getInstance()
        val date = cal.time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return dateFormat.format(date)
    }

    override fun onConnected(p0: Bundle?) {
        writeOnDebugger("onConnected()")
//        val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
//        ActivityCompat.requestPermissions(this, permissions,0)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            writeOnDebugger("Exei thema sta permission")
            return;
        }

        startLocationUpdates();

        var fusedLocationProviderClient :
                FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient .getLastLocation()
                .addOnSuccessListener(this, OnSuccessListener<Location> { location ->
                    if (location != null) {
                        // Logic to handle location object
                        mLocation = location;
                        //EDO EGRAFA STO txt_latitude kai txt_longitude tis sintetagemens location.latitude
                        if(mLocation!= null /* && convertLatLongDifferenceToMeters(mLocation.latitude,mLocation.longitude,location.latitude,location.longitude) > 20*/) {
                            mLocation = location;
                            //ipologismos tou xronou se date kai milli
                            val string_username = user
                            val string_time_milli = "" + System.currentTimeMillis()
                            val string_formattedDate = CalculateDateTimeInString()
                            val string_latitude = "" + mLocation.latitude
                            val string_longitude = "" + mLocation.longitude
                            sendCredentials(string_username, string_latitude, string_longitude, string_time_milli, string_formattedDate)
                        }
                    }else {
                        writeOnDebugger("\"OnSuccesListener: the location = null")
                    }
                })
    }

    private fun checkLocation(): Boolean {
        writeOnDebugger("checkLocation()")
        if(!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    private fun isLocationEnabled(): Boolean {
        writeOnDebugger("isLocationEnabled()")
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun showAlert() {
        writeOnDebugger("showAlert()")
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " + "use this app")
                .setPositiveButton("Location Settings", DialogInterface.OnClickListener { paramDialogInterface, paramInt ->
                    val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(myIntent)
                })
                .setNegativeButton("Cancel", DialogInterface.OnClickListener { paramDialogInterface, paramInt -> })
        dialog.show()
    }

    protected fun startLocationUpdates() {
        writeOnDebugger("startLocationUpdates()")

        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
    }

    fun convertLatLongDifferenceToMeters(latitude1: Double, longitude1: Double, latitude2: Double, longitude2: Double): Double
    {
        var R = 6378.137; // Radius of earth in KM
        var dLat = latitude2 * Math.PI / 180 - latitude1 * Math.PI / 180;
        var dLon = longitude2 * Math.PI / 180 - longitude1 * Math.PI / 180;
        var a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(latitude1 * Math.PI / 180) * Math.cos(latitude2 * Math.PI / 180) *
                Math.sin(dLon/2) * Math.sin(dLon/2);
        var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        var d = R * c;
        return d * 1000; // meters
    }
}