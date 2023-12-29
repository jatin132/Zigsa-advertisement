package com.zigsaadvertisement

import android.Manifest
import android.app.DownloadManager
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.WindowManager
import android.webkit.MimeTypeMap
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

@Suppress("DEPRECATION")
class DownloadingAdvertisements : AppCompatActivity() {

    private lateinit var imagePagerAdapter: ImagePagerAdapter
    private var imageUrls: List<String> = emptyList()
    private var type: List<String> = emptyList()
    private var controller: Controller = Controller()
    private lateinit var webViewUrl: String
    private lateinit var token: String
    private var newImageUrls = emptyList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_downloading_advertisements)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        if (!hasToken()){
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        } else {
            val intent = Intent(this, Logout::class.java)
            startActivity(intent)
        }
    }

    private fun hasToken(): Boolean {
        val sharedPreferences: SharedPreferences = getSharedPreferences("zigsa_advertisement", MODE_PRIVATE)
        token = sharedPreferences.getString("token", "").toString()
        webViewUrl = sharedPreferences.getString("webViewUrl", "").toString()
        return !Objects.equals(token, "")
    }

    private fun getDataFromSharedPreferences() {
        val sharedPreferences = getSharedPreferences("zigsa_advertisement", Context.MODE_PRIVATE)
        token = sharedPreferences.getString("token", null).toString()
        webViewUrl = sharedPreferences.getString("webViewUrl", null).toString()

        if(token.isEmpty()){
            val intent1 = Intent(this@DownloadingAdvertisements, Login::class.java)
            startActivity(intent1)
            finish()
            Log.i("Exception", "token is in if $token")
        } else {
            Log.i("Exception", "token is in else $token")
            getData()
        }
    }

    private fun getData() {
        val call: Call<List<AdvertisementModel>> =
            controller.controller().dataInterface().getData("Bearer $token", webViewUrl)
        call.enqueue(object : Callback<List<AdvertisementModel>> {
            override fun onResponse(
                call: Call<List<AdvertisementModel>>,
                response: Response<List<AdvertisementModel>>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null && data.isNotEmpty()) {
                        newImageUrls = data.map { fakeData ->
                            fakeData.content
                        }
                        downloadContent()

                        type = data.map { dataType ->
                            dataType.type
                        }
                    } else {
                        Log.i("Exception", "API response is empty or null")
                    }
                } else {
                    Log.i("Exception", "Failed to fetch data from API ${response.message()}")
                }
            }

            override fun onFailure(call: Call<List<AdvertisementModel>>, t: Throwable) {
                Log.i("Exception", "Failed to fetch data from API $t")
            }
        })
    }

    private val PERMISSION_REQUEST_CODE = 123

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission already granted, proceed with downloading
            downloadContent()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with downloading
                Log.i("Exception", "in if")
                downloadContent()
            } else {
                Log.i("Exception", "in else")
                // Permission denied, handle accordingly
                // You may inform the user or take appropriate action
            }
        }
    }

    private fun downloadContent() {
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        for ((index, imageUrl) in newImageUrls.withIndex()) {
            val uri = Uri.parse(imageUrl)
            val fileName = getFileNameFromUrl(uri)
            val mimeType = MimeTypeMap.getFileExtensionFromUrl(uri.toString())

            // Check if the file already exists in the Downloads directory
            if (!isFileAlreadyDownloaded(fileName)) {
                val request = DownloadManager.Request(uri)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                    .setMimeType(mimeType)
                Log.i("Exception", "image url is $imageUrl")
                downloadManager.enqueue(request)
            } else {
                val newIntent = Intent(this, MainActivity::class.java)
                startActivity(newIntent)
                Log.i("Exception", "File already downloaded: $fileName")
            }
        }

        // Register the receiver to listen for download completion
        registerReceiver(DownloadCompleteReceiver(), IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    private fun isFileAlreadyDownloaded(fileName: String): Boolean {
        val downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val files = downloadsDirectory.listFiles()

        return files?.any { file -> file.name == fileName } ?: false
    }

    private fun getFileNameFromUrl(uri: Uri): String {
        val path = uri.path
        return path?.substring(path.lastIndexOf('/') + 1) ?: "unknown_file"
    }

    class DownloadCompleteReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == action) {
                Log.i("Exception", "Download Complete")
                val newIntent = Intent(context, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context?.startActivity(newIntent)
            }
        }
    }
}