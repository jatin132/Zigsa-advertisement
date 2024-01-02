package com.zigsaadvertisement

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.PrivateChannelEventListener
import com.pusher.client.channel.PusherEvent
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import com.pusher.client.util.HttpAuthorizer
import org.json.JSONObject
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
    private var isTv: String = ""
    private var downloadedFilesArray: List<String> = emptyList()
    private lateinit var orientation: String
    private lateinit var viewType: String
    private lateinit var locationUUID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.viewPager)
        val advertisement = findViewById<TextView>(R.id.advertisement)
        advertisement.isSelected = true
        if (!hasToken()){
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        } else {
            if (viewType == "adv"){
                getData()
            } else {
                val intent = Intent(applicationContext, EmptyView::class.java)
                startActivity(intent)
            }
            requestedOrientation = if (isTv == "landscape") {
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }

            setupPusher()
        }
    }

    private fun hasToken(): Boolean {
        val sharedPreferences: SharedPreferences = getSharedPreferences("zigsa_advertisement", MODE_PRIVATE)
        token = sharedPreferences.getString("token", "").toString()
        webViewUrl = sharedPreferences.getString("webViewUrl", "").toString()
        isTv = sharedPreferences.getString("orientation", "").toString()
        viewType = sharedPreferences.getString("view_type", "").toString()
        locationUUID = sharedPreferences.getString("location", "").toString()
        return !Objects.equals(token, "")
    }

    @Suppress("DEPRECATION")
    private fun setupPusher() {
        val channelName = "private-publicLocation.$locationUUID"

        val authorizer = HttpAuthorizer(Constants.BASE_URL+ Constants.PUSHER_AUTH)

        val options = PusherOptions().setEncrypted(true).setAuthorizer(authorizer)
        options.setCluster(Constants.PUSHER_CLUSTER)

        val pusher = Pusher(Constants.PUSHER_KEY, options)
        pusher.connect(object : ConnectionEventListener {
            override fun onConnectionStateChange(change: ConnectionStateChange) {
                Log.i("Exception", " changed to " + change.currentState)
            }

            override fun onError(message: String, code: String?, e: Exception?) {
                Log.i("Exception", " connecting! msg:$message")
            }
        }, ConnectionState.ALL)

        val channel = pusher.subscribePrivate(channelName)

        channel.bind("update_campaign_view", object : PrivateChannelEventListener {
            override fun onAuthenticationFailure(string: String, ex: Exception) {
                Log.i("Exception", "OnFailure")
            }

            override fun onEvent(event: PusherEvent?) {
                val jsonObject = JSONObject(event!!.data)
                val publicViewIds = jsonObject.optJSONArray("publicview_ids")

                if (publicViewIds != null) {
                    for (i in 0 until publicViewIds.length()) {
                        val publicViewId = publicViewIds.getString(i)

                        if (publicViewId == webViewUrl) {
                            Log.i("Exception", "Public view id found: $publicViewId")
                            getData()
                            return
                        }
                    }
                }
                Log.i("Exception", "Public view id not found: $webViewUrl")
            }

            override fun onSubscriptionSucceeded(string: String) {
                Log.i("Exception", string)
            }
        })
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

                        val duration = data.map { durationPeriod ->
                            durationPeriod.duration ?: 5000L
                        }
                        logDownloadedFiles()

                        handler = Handler()

                        logRunnable = object : Runnable {
                            override fun run() {
                                if (currentIndex < downloadedFilesArray.size) {
                                    val imageUrl = downloadedFilesArray[currentIndex]
                                    val fileName = getFileNameFromUrl(Uri.parse(imageUrl))

                                    if (imageUrl.endsWith(".mp4")) {
                                        retrieveVideoDuration(imageUrl)
//                                        Log.i("Exception", "Video ends with .mp4: $imageUrl")
                                    } else {
                                        if (isFilePresentInDownloads(fileName)) {
//                                            Log.i("Exception", "File $fileName found in Downloads directory")
                                        } else {
//                                            Log.i("Exception", "File $fileName not found in Downloads directory. Downloading...")
                                            downloadFile(imageUrl, fileName)
                                        }

                                        val delayDuration = duration.getOrElse(currentIndex) { 5000L }
//                                        Log.i("Exception", "URL does not end with .mp4: $imageUrl, Duration: ${delayDuration} seconds")
                                        durations.add(5000)
                                        handler.postDelayed(this, 5000)

                                    }

                                    currentIndex++

                                    // Check if we reached the end of the list
                                    if (currentIndex == downloadedFilesArray.size) {
                                        // Reset currentIndex to 0 to start from the beginning
                                        currentIndex = 0
                                    }
                                }
                            }
                        }

                        customPagerAdapter = CustomPagerAdapter(this@MainActivity, downloadedFilesArray)
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

    private fun getFileNameFromUrl(uri: Uri): String {
        val path = uri.path
        return path?.substring(path.lastIndexOf('/') + 1) ?: "unknown_file"
    }

    private fun logDownloadedFiles() {
        val downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val files = downloadsDirectory.listFiles()

        val downloadedFilePaths = mutableListOf<String>()

//        Log.i("Exception", "List of Files in Downloads Directory:")
        for (file in files ?: emptyArray()) {
            val fileName = file.name
//            Log.i("Exception", fileName)

            // Add the file path to the list
            downloadedFilePaths.add(file.absolutePath)
        }

        // Assign the list to the global variable
        downloadedFilesArray = downloadedFilePaths.toList()

        // Log the array
//        Log.i("Exception", "Array of Downloaded Files:")
//        Log.i("Exception", downloadedFilesArray.toString())
    }

    private fun downloadFile(fileUrl: String, fileName: String) {
        val request = DownloadManager.Request(Uri.parse(fileUrl))
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            .setAllowedOverRoaming(false)
            .setTitle(fileName)
            .setDescription("Downloading")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }

    private fun isFilePresentInDownloads(fileName: String): Boolean {
        val downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val files = downloadsDirectory.listFiles()

        for (file in files ?: emptyArray()) {
            val currentFileName = file.name
            if (currentFileName == fileName) {
                return true
            }
        }
        return false
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
            val bitmap = BitmapFactory.decodeFile(item)
            imageView.setImageBitmap(bitmap)
//            Glide.with(context)
//                .load(File(item))
//                .into(imageView)

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