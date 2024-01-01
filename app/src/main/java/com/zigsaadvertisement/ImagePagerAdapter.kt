package com.zigsaadvertisement

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.VideoView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide

@Suppress("DEPRECATION", "UNREACHABLE_CODE")
class ImagePagerAdapter(
    private val context: Context,
    private var imageUrls: List<String>,
    private var viewPager: ViewPager,
    private var type: List<String>
) : PagerAdapter() {

    private var duration: Int = 0
    private lateinit var handler: Handler
    private lateinit var logRunnable: Runnable
    private var currentIndex = 0

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(context)
        val mediaUrl = imageUrls[position]

        // Check if the media file ends with ".mp4"
        val isVideo = mediaUrl.endsWith(".mp4")

        val layoutId = R.layout.data_view
        val view = inflater.inflate(layoutId, container, false)

        val imageView: ImageView = view.findViewById(R.id.image)
        val videoView: VideoView = view.findViewById(R.id.video)

        handler = Handler()

        logRunnable = object : Runnable {
            override fun run() {
                if (currentIndex < imageUrls.size) {
                    val videoUrl = imageUrls[currentIndex]

                    if (videoUrl.endsWith(".mp4")) {
                        retrieveVideoDuration(videoUrl)

                        val videoUri = Uri.parse(mediaUrl)
                        videoView.setVideoPath(videoUri.toString())
                        videoView.start()
                    } else {
                        Log.i("Exception", "URL does not end with .mp4: $videoUrl, Duration: 4 seconds")
                        handler.postDelayed(this, 4000)
                    }

                    currentIndex++
                }
            }
        }

        handler.post(logRunnable)

//        if (isVideo) {

//
//            val duration = videoView.duration
//

//        } else {
//            Glide.with(context).load(mediaUrl).into(imageView)
//
//        }

        container.addView(view)
        return view
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

    override fun getCount(): Int {
        return imageUrls.size
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        if (obj is View) {
            container.removeView(obj)
        }
        // Handle other types if needed
    }

    fun updateData(newImageUrls: List<String>) {
        imageUrls = newImageUrls
        notifyDataSetChanged()
    }
}
