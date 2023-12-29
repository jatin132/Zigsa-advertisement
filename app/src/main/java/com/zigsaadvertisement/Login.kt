package com.zigsaadvertisement

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import com.pusher.client.connection.ConnectionEventListener
import com.zigsawaitlist.network.TVCodeRequest
import com.zigsawaitlist.network.TVCodeResponse
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Login : AppCompatActivity() {

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var isLocationPermissionGranted = false
    private var isINTERNETPermissionGranted = false
    private var isAccessNetworkPermissionGranted = false
    private var isAccessBluetoothPermissionGranted = false
    private var isAccessWriteExternalStoragePermissionGranted = false
    private lateinit var token: String
    private lateinit var webViewUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permissions ->

            isAccessWriteExternalStoragePermissionGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: isAccessWriteExternalStoragePermissionGranted
            isLocationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: isLocationPermissionGranted
            isAccessNetworkPermissionGranted = permissions[Manifest.permission.ACCESS_NETWORK_STATE] ?: isAccessNetworkPermissionGranted
            isINTERNETPermissionGranted = permissions[Manifest.permission.INTERNET] ?: isINTERNETPermissionGranted
            isAccessBluetoothPermissionGranted = permissions[Manifest.permission.BLUETOOTH] ?: isAccessBluetoothPermissionGranted

        }
        requestPermission()
    }

    private fun requestPermission(){
        isAccessWriteExternalStoragePermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        isLocationPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        isINTERNETPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.INTERNET
        ) == PackageManager.PERMISSION_GRANTED

        isAccessNetworkPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_NETWORK_STATE
        ) == PackageManager.PERMISSION_GRANTED

        isAccessBluetoothPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.BLUETOOTH
        ) == PackageManager.PERMISSION_GRANTED

        val permissionRequest: MutableList<String> = ArrayList()

        if (!isAccessWriteExternalStoragePermissionGranted){
            permissionRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (!isLocationPermissionGranted){
            permissionRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (!isINTERNETPermissionGranted){
            permissionRequest.add(Manifest.permission.INTERNET)
        }
        if (!isAccessNetworkPermissionGranted){
            permissionRequest.add(Manifest.permission.ACCESS_NETWORK_STATE)
        }
        if (permissionRequest.isNotEmpty()){
            permissionLauncher.launch(permissionRequest.toTypedArray())
        }
        if (!isAccessBluetoothPermissionGranted){
            permissionRequest.add(Manifest.permission.BLUETOOTH)
        }
    }

    @SuppressLint("HardwareIds")
    @Suppress("DEPRECATION")
    private fun setupPusher() {
        val sessionManager = SessionManager(this)
        val deviceId = Settings.Secure.getString(
            applicationContext.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        val channelName = "tv." + deviceId

        val options = PusherOptions().setEncrypted(true)
        options.setCluster(Constants.PUSHER_CLUSTER)
        val pusher = Pusher(Constants.PUSHER_KEY, options)
        pusher.connect(object : ConnectionEventListener {
            override fun onConnectionStateChange(change: ConnectionStateChange) {
                Log.e("pusher: State", " changed to " + change.currentState)
            }

            override fun onError(message: String, code: String?, e: Exception?) {
                Log.e("pusher:problem", " connecting! msg:$message")
            }
        }, ConnectionState.ALL)

        val channel = pusher.subscribe(channelName)

        channel.bind("connect-tv") {
            Log.i("Exception", "pusher event" + it.channelName + it.eventName + it.data.toString())
            val jsonObject = JSONObject(it.data)
            token = jsonObject.getString("token")
            webViewUrl = jsonObject.getString("webview_uuid")

            // Store token and webViewUrl in SharedPreferences
            saveDataToSharedPreferences(token, webViewUrl)
        }
    }

    private fun saveDataToSharedPreferences(token: String, webViewUrl: String) {
        val sharedPreferences = getSharedPreferences("zigsa_advertisement", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("token", token)
        editor.putString("webViewUrl", webViewUrl)
        editor.apply()

        val intent = Intent(applicationContext, Logout::class.java)
        startActivity(intent)
    }

    @SuppressLint("SetTextI18n", "HardwareIds")
    override fun onStart() {
        super.onStart()
        setContentView(R.layout.activity_login)

//        setContentView(R.layout.activity_first_screen)
        val apiClient = Controller()

        val login = findViewById<Button>(R.id.login)
        val code = findViewById<TextView>(R.id.code)
        login.setOnClickListener {
            login.isEnabled = false
            login.text = "Getting Code"
            val deviceId = Settings.Secure.getString(
                applicationContext.contentResolver,
                Settings.Secure.ANDROID_ID
            )

            apiClient.controller().dataInterface().getTvCode(TVCodeRequest(deviceId))
                .enqueue(object : Callback<TVCodeResponse> {
                    override fun onFailure(call: Call<TVCodeResponse>, t: Throwable) {
                        login.text = "Login"
                        login.isEnabled = true
                        login.visibility = View.VISIBLE
                        Toast.makeText(this@Login, "Internet not enabled", Toast.LENGTH_LONG).show()
                    }

                    override fun onResponse(
                        call: Call<TVCodeResponse>,
                        response: Response<TVCodeResponse>
                    ) {
                        login.visibility = View.GONE
                        val tvCodeResponse = response.body()
                        val tvCode = tvCodeResponse?.code
                        Log.i("tv code response", tvCodeResponse.toString())

                        if (tvCodeResponse?.code != null) {
                            login.isEnabled = true
                            code.text = tvCode

                            setupPusher()
                        } else {
                            login.visibility = View.VISIBLE
                            Toast.makeText(this@Login, "Invalid Credential", Toast.LENGTH_LONG).show()
                        }
                    }
                })

        }
    }
}