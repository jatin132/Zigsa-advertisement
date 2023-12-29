package com.zigsaadvertisement
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
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
//    private var newImageUrls = emptyList<String>()
    private lateinit var webViewUrl: String
    private lateinit var token: String
    private var newImageUrls = listOf(
        "https://cdn.pixabay.com/photo/2020/06/29/20/12/man-in-red-dress-5354230_1280.png",
        "https://image.aitech.work/test_video.mp4",
        "https://image.aitech.work/test_video.mp4",
        "https://cdn.pixabay.com/photo/2020/11/10/15/51/bear-5730216_1280.png",
        "https://image.aitech.work/test_video.mp4",
        "https://cdn.pixabay.com/photo/2020/02/25/16/40/mascot-4879416_1280.png"
    )
    private var duration: Int = 0
    private lateinit var handler: Handler
    private lateinit var logRunnable: Runnable
    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.viewPager)

        handler = Handler()

        logRunnable = object : Runnable {
            override fun run() {
                if (currentIndex < newImageUrls.size) {
                    val videoUrl = newImageUrls[currentIndex]

                    if (videoUrl.endsWith(".mp4")) {
                        retrieveVideoDuration(videoUrl)
                    } else {
                        Log.i("Exception", "URL does not end with .mp4: $videoUrl, Duration: 4 seconds")
                        handler.postDelayed(this, 4000)
                    }

                    currentIndex++
                }
            }
        }

        handler.post(logRunnable)

//        val sharedPreferences: SharedPreferences = getSharedPreferences("zigsa_advertisement", MODE_PRIVATE)
//        token = sharedPreferences.getString("token", "").toString()
//        webViewUrl = sharedPreferences.getString("webViewUrl", "").toString()

        val imagePagerAdapter = ImagePagerAdapter(applicationContext, newImageUrls, viewPager,
            listOf("")
        )
        viewPager.adapter = imagePagerAdapter
        imagePagerAdapter.updateData(newImageUrls)
//        getData()
    }

    private fun retrieveVideoDuration(videoUrl: String) {
        try {
            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(videoUrl)
            mediaPlayer.setOnPreparedListener { player ->
                duration = player.duration
                Log.i("Exception", "Video URL: $videoUrl, Duration: $duration milliseconds")

                player.release()

                handler.postDelayed(logRunnable, duration.toLong())
            }
            mediaPlayer.prepareAsync()
        } catch (e: Exception) {
            Log.e("Exception", "Error retrieving video duration for $videoUrl: ${e.message}")

            Log.i("Exception", "URL: $videoUrl, Default Duration: 4 seconds")
            handler.postDelayed(logRunnable, 4000)
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

                        type = data.map { dataType ->
                            dataType.type
                        }

                        val imagePagerAdapter = ImagePagerAdapter(applicationContext, newImageUrls, viewPager, type)
                        viewPager.adapter = imagePagerAdapter
                        imagePagerAdapter.updateData(newImageUrls)

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

    private fun checkDownloadedFiles() {
        val downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val files = downloadsDirectory.listFiles()

        for (file in files ?: emptyArray()) {
            val fileName = file.name
            // Sanitize the file name and convert to lowercase for case-insensitive comparison
            val sanitizedFileName = fileName.replace("[^a-zA-Z0-9.-]".toRegex(), "").toLowerCase(
                Locale.ROOT)

            // Check if the sanitized file name contains the string present in the URL
            if (newImageUrls.any { imageUrl -> sanitizedFileName.contains(imageUrl) }) {
                Log.i("Exception", "Found file in Downloads: $fileName")
            }
        }
    }
}