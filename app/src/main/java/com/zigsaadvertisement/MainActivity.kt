package com.zigsaadvertisement
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.viewpager.widget.ViewPager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager
    private var type: List<String> = emptyList()
    private var controller: Controller = Controller()
    private var newImageUrls = emptyList<String>()
    private lateinit var webViewUrl: String
    private lateinit var token: String
//    private val imageUrls = listOf(
//        "https://image.aitech.work/test_video.mp4",
//        "https://cdn.pixabay.com/photo/2020/06/29/20/12/man-in-red-dress-5354230_1280.png",
//        "https://image.aitech.work/test_video.mp4",
//        "https://cdn.pixabay.com/photo/2020/11/10/15/51/bear-5730216_1280.png",
//        "https://image.aitech.work/test_video.mp4",
//        "https://cdn.pixabay.com/photo/2020/02/25/16/40/mascot-4879416_1280.png"
//    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.viewPager)

        getDataFromSharedPreferences()

        // Start a timer to change images every 4 seconds
//        Timer().scheduleAtFixedRate(timerTask {
//            runOnUiThread {
//                if (viewPager.currentItem == imageUrls.size - 1) {
//                    viewPager.currentItem = 0
//                } else {
//                    viewPager.currentItem = viewPager.currentItem + 1
//                }
//            }
//        }, 2000, 2000)
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

                        checkDownloadedFiles()

                        type = data.map { dataType ->
                            dataType.type
                        }

                        checkDownloadedFiles()
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

    private fun getDataFromSharedPreferences() {
        val sharedPreferences = getSharedPreferences("zigsa_advertisement", Context.MODE_PRIVATE)
        token = sharedPreferences.getString("token", null).toString()
        webViewUrl = sharedPreferences.getString("webViewUrl", null).toString()

        if(token.isEmpty()){
            val intent1 = Intent(this@MainActivity, Login::class.java)
            startActivity(intent1)
            finish()
            Log.i("Exception", "token is in if $token")
        } else {
            Log.i("Exception", "token is in else $token")
            getData()
        }
    }

    private fun checkDownloadedFiles() {
        val downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val files = downloadsDirectory.listFiles()
        Log.i("Exception", "Found file in Downloads: $files")

        for (file in files!!) {
            val fileName = file.name
            // Check if the file name contains the string present in the URL
            if (newImageUrls.any { imageUrl -> fileName.contains(imageUrl) }) {
                Log.i("Exception", "Found file in Downloads: $fileName")
            }
        }
    }
}