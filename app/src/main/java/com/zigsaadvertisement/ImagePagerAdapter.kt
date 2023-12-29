package com.zigsaadvertisement

import android.content.Context
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

    private val handler = Handler()
    private var currentItem = 0
    private val DEFAULT_VIDEO_DURATION = 4000L

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(context)
        val mediaUrl = imageUrls[position]

        // Check if the media file ends with ".mp4"
        val isVideo = mediaUrl.endsWith(".mp4")

        val layoutId = R.layout.data_view
        val view = inflater.inflate(layoutId, container, false)

        val imageView: ImageView = view.findViewById(R.id.image)
        val videoView: VideoView = view.findViewById(R.id.video)

        if (isVideo) {
            val videoUri = Uri.parse(mediaUrl)
            videoView.setVideoPath(videoUri.toString())
            videoView.start()

            val duration = videoView.duration

            return if (duration > 0) {
                startImageChangeTimer(duration.toLong().toInt())
                duration.toLong()
            } else {
                startImageChangeTimer(DEFAULT_VIDEO_DURATION.toInt())
                DEFAULT_VIDEO_DURATION
            }
        } else {
            Glide.with(context).load(mediaUrl).into(imageView)

            startImageChangeTimer(4000)
        }

        container.addView(view)
        return view
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

    private fun switchToNextItem() {
        currentItem = (currentItem + 1) % imageUrls.size
        viewPager.currentItem = currentItem
    }

    private fun startImageChangeTimer(duration: Int) {
        handler.postDelayed({
            switchToNextItem()
        }, duration.toLong())
    }
}
