package com.zigsaadvertisement

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.VideoView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    lateinit var viewPager: ViewPager
    private var controller: Controller = Controller()
    private lateinit var customPagerAdapter: CustomPagerAdapter
    private lateinit var newImageUrls: List<String>
//    private var newImageUrls = listOf(
//        "https://cdn.pixabay.com/photo/2020/06/29/20/12/man-in-red-dress-5354230_1280.png",
//        "https://image.aitech.work/test_video.mp4",
//        "https://image.aitech.work/test_video.mp4",
//        "https://cdn.pixabay.com/photo/2020/11/10/15/51/bear-5730216_1280.png",
//        "https://image.aitech.work/test_video.mp4",
//        "https://cdn.pixabay.com/photo/2020/02/25/16/40/mascot-4879416_1280.png"
//    )
    lateinit var handler: Handler
    private lateinit var logRunnable: Runnable
    private var currentIndex = 0
    private var durations: MutableList<Int> = mutableListOf()
    private lateinit var token: String
    private lateinit var webViewUrl: String
    private var isTv: String = "landscape"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestedOrientation = if (isTv == "landscape") {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        viewPager = findViewById(R.id.viewPager)

        if (!hasToken()){
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        } else {
            getData()
        }
    }

    private fun hasToken(): Boolean {
        val sharedPreferences: SharedPreferences = getSharedPreferences("zigsa_advertisement", MODE_PRIVATE)
        token = sharedPreferences.getString("token", "").toString()
        webViewUrl = sharedPreferences.getString("webViewUrl", "").toString()
        isTv = sharedPreferences.getString("orientation", "landscape").toString()
        return !Objects.equals(token, "")
    }

    private fun retrieveVideoDuration(videoUrl: String) {
        try {
            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(videoUrl)
            mediaPlayer.setOnPreparedListener { player ->
                val duration = player.duration
                durations.add(duration)

                Log.i("Exception", "Video URL: $videoUrl, Duration: $duration milliseconds")

                player.release()

                handler.postDelayed(logRunnable, duration.toLong())
            }
            mediaPlayer.prepareAsync()
        } catch (e: Exception) {
            Log.e("Exception", "Error retrieving video duration for $videoUrl: ${e.message}")

            // If an exception occurs, log with a default duration of 4 seconds
            Log.i("Exception", "URL: $videoUrl, Default Duration: 4 seconds")
            durations.add(4000)

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

                        val type = data.map { dataType ->
                            dataType.type
                        }

                        handler = Handler()

                        logRunnable = object : Runnable {
                            override fun run() {
                                if (currentIndex < newImageUrls.size) {
                                    val videoUrl = newImageUrls[currentIndex]

                                    if (videoUrl.endsWith(".mp4")) {
                                        retrieveVideoDuration(videoUrl)
                                        Log.i("Exception", "Video ends with .mp4: $videoUrl")
                                    } else {
                                        Log.i("Exception", "URL does not end with .mp4: $videoUrl, Duration: 4 seconds")
                                        durations.add(4000)
                                        handler.postDelayed(this, 4000)
                                    }

                                    currentIndex++

                                    // Check if we reached the end of the list
                                    if (currentIndex == newImageUrls.size) {
                                        // Reset currentIndex to 0 to start from the beginning
                                        currentIndex = 0
                                    }
                                }
                            }
                        }

                        customPagerAdapter = CustomPagerAdapter(this@MainActivity, newImageUrls)
                        viewPager.adapter = customPagerAdapter

                        // Start automatic slide with the first item
                        handler.post(logRunnable)
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

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val intent = Intent(applicationContext, Logout::class.java)
        startActivity(intent)
        super.onBackPressed()
    }
}

class CustomPagerAdapter(private val context: MainActivity, private val items: List<String>) : PagerAdapter() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getCount(): Int {
        return items.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layout = inflater.inflate(R.layout.data_view, container, false)
        container.addView(layout)

        val imageView = layout.findViewById<ImageView>(R.id.image)
        val videoView = layout.findViewById<VideoView>(R.id.video)

        val item = items[position]

        if (item.endsWith(".mp4")) {
            imageView.visibility = View.GONE
            videoView.visibility = View.VISIBLE

            // Load video in VideoView
            videoView.setVideoPath(item)
            videoView.start()

            videoView.setOnErrorListener { _, what, extra ->
                Log.e("Exception", "Error during playback: $what, $extra")
                // Handle the error as needed
                // Returning true indicates that the error has been handled
                true
            }

            // Set a listener to detect when the video is completed
            videoView.setOnCompletionListener {
                // Move to the next item after video completion
                moveToNextItem(position)
            }
        } else {
            imageView.visibility = View.VISIBLE
            videoView.visibility = View.GONE

            // Load image in ImageView
            Glide.with(context)
                .load(item)
                .into(imageView)

            // Move to the next item after a fixed duration for images (4 seconds)
            context.handler.postDelayed({
                moveToNextItem(position)
            }, 4000)
        }

        return layout
    }


    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    private fun moveToNextItem(currentPosition: Int) {
        // Move to the next item in ViewPager
        val nextPosition = (currentPosition + 1) % count
        context.viewPager.currentItem = nextPosition

        // Delay the next automatic slide by a fixed duration (e.g., 4 seconds)
        context.handler.postDelayed({
            moveToNextItem(nextPosition)
        }, 4000)
    }
}