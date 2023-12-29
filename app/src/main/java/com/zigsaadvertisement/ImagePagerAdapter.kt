package com.zigsaadvertisement

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.VideoView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide

@Suppress("DEPRECATION")
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
        val view = inflater.inflate(R.layout.data_view, container, false)
        val imageView: ImageView = view.findViewById(R.id.image)
        val videoView: VideoView = view.findViewById(R.id.video)
        val mediaUrl = imageUrls[position]
        val type = type[position]

        if (type.contains("video")){
            Log.i("Exception", "type is if ${type[position]}")
            videoView.visibility = View.VISIBLE
            imageView.visibility = View.GONE
            val videoUri = Uri.parse(mediaUrl)
            videoView.setVideoPath(videoUri.toString())
            videoView.start()

            videoView.setOnCompletionListener {
                switchToNextItem()
            }
        } else {
            Log.i("Exception", "type is else ${type[position]}")
            videoView.visibility = View.GONE
            imageView.visibility = View.VISIBLE
            Glide.with(context).load(mediaUrl).into(imageView)

            startImageChangeTimer()
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
        container.removeView(obj as View)
    }

    fun updateData(newImageUrls: List<String>) {
        imageUrls = newImageUrls
        notifyDataSetChanged()
    }

    private fun switchToNextItem() {
        currentItem = (currentItem + 1) % imageUrls.size
        viewPager.currentItem = currentItem
    }

    private fun startImageChangeTimer() {
        handler.postDelayed({
            switchToNextItem()
        }, 4000)
    }

    private fun getVideoDuration(videoView: VideoView): Long {
        val duration = videoView.duration
        return if (duration > 0) duration.toLong() else DEFAULT_VIDEO_DURATION
    }
}
