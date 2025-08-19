package com.sun.weatherapp.utils

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import android.graphics.drawable.Drawable
import com.sun.weatherapp.R
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import android.util.DisplayMetrics
import android.content.Context

object ImageLoader {
    
    /**
     * Load image from Firebase URL with placeholder and error handling
     * @param imageUrl: Firebase image URL
     * @param imageView: ImageView to display the image
     * @param placeholderResId: Placeholder drawable resource ID
     * @param errorResId: Error drawable resource ID
     * @param isCircular: Whether to apply circular crop
     * @param cornerRadius: Corner radius in pixels (0 = no rounding)
     */
    fun loadImage(
        imageUrl: String?,
        imageView: ImageView,
        placeholderResId: Int = R.drawable.bg_image_placeholder,
        errorResId: Int = R.drawable.bg_image_placeholder,
        isCircular: Boolean = false,
        cornerRadius: Int = 0
    ) {
        if (imageUrl.isNullOrEmpty()) {
            imageView.setImageResource(errorResId)
            return
        }
        
        // Convert Google Drive URL to direct download URL if needed
        val processedUrl = FirebaseImageHelper.getDirectImageUrl(imageUrl)
        
        val requestBuilder = Glide.with(imageView.context)
            .load(processedUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(placeholderResId)
            .error(errorResId)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    android.util.Log.w("ImageLoader", "Failed to load image: $processedUrl, error: ${e?.message}")
                    return false
                }
                
                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    android.util.Log.d("ImageLoader", "Successfully loaded image: $processedUrl from $dataSource")
                    return false
                }
            })
        
        // Apply transformations based on parameters
        when {
            isCircular -> {
                requestBuilder.circleCrop()
            }
            cornerRadius > 0 -> {
                requestBuilder.transform(CenterCrop(), RoundedCorners(cornerRadius))
            }
        }
        
        requestBuilder.into(imageView)
    }
    
    /**
     * Load song cover image with rounded corners
     */
    fun loadSongImage(imageUrl: String?, imageView: ImageView) {
        val cornerRadius = getCornerRadius(imageView.context, 18f) // 18dp corner radius
        loadImage(
            imageUrl = imageUrl,
            imageView = imageView,
            placeholderResId = R.drawable.bg_image_placeholder,
            errorResId = R.drawable.bg_image_placeholder,
            isCircular = false,
            cornerRadius = cornerRadius
        )
    }

    fun loadArtistImage(imageUrl: String?, imageView: ImageView) {
        loadImage(
            imageUrl = imageUrl,
            imageView = imageView,
            placeholderResId = R.drawable.bg_image_placeholder_round,
            errorResId = R.drawable.bg_image_placeholder_round,
            isCircular = true,
            cornerRadius = 0
        )
    }

    fun loadProfileImage(imageUrl: String?, imageView: ImageView) {
        loadImage(
            imageUrl = imageUrl,
            imageView = imageView,
            placeholderResId = R.drawable.ic_profile,
            errorResId = R.drawable.ic_profile,
            isCircular = true,
            cornerRadius = 0
        )
    }

    private fun getCornerRadius(context: Context, dp: Float): Int {
        val displayMetrics = context.resources.displayMetrics
        return (dp * (displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }
}
