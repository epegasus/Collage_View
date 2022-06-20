package dev.pegasus.collage.views

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import dev.pegasus.collage.utils.ImageParams

class CollageViewVertical(
    context: Context,
    attrs: AttributeSet?, imageCount: Int,
    totalWidth: Int,
    totalHeight: Int,
    isBorderEnabled: Boolean = false,
    imageUris: Array<Uri?>? = null
) : AbstractCollageView(
    context,
    attrs,
    imageCount,
    totalWidth,
    totalHeight,
    isBorderEnabled,
    imageUris
), View.OnTouchListener {

    // ------------------------ INITIALIZATION ------------------------

    // Do not change
    init {
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (height > 0) {
                    addViewsToLayout()
                    initImageLayout()
                    addImagesToViews()
                    prepareTouchListeners()
                    enableBorder(isBorderEnabled)

                    isLayoutInflated = true
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            }
        })
    }

    override fun initImageLayout() {
        val height = layoutHeight.toFloat() / imageCount().toFloat()

        for (i in imageViews.indices) {
            imageSizeAndPosCache[i] = ImageParams(layoutWidth.toFloat(), height, 0f, i * height)
        }

        syncViewsWithSizeAndPosCache()
    }

    // Do not change
    private fun prepareTouchListeners() {
        for (view in imageViews) view.setOnTouchListener(this)
    }


    // ------------------------ RESIZING HELPERS ------------------------

    private fun resizeImageAt(
        index: Int,
        @Suppress("UNUSED_PARAMETER") deltaWidth: Float,
        deltaHeight: Float
    ) {
        when (touchedImageEdge) {
            null -> {
                Log.d(TAG, "resizeImageAt: invalid edge")
            }

            Edge.BOTTOM_LEFT_CORNER, Edge.BOTTOM_SIDE, Edge.BOTTOM_RIGHT_CORNER -> {
                if (index == imageViews.size - 1) return

                val okToAdjustHeight = imageSizeAndPosCache[index].height + deltaHeight > minDimension &&
                        imageSizeAndPosCache[index + 1].height - deltaHeight > minDimension

                if (!okToAdjustHeight) Log.d(TAG, "resizeImageAt: unsafe to resize")
                else {
                    imageSizeAndPosCache[index].height += deltaHeight

                    imageSizeAndPosCache[index + 1].height -= deltaHeight
                    imageSizeAndPosCache[index + 1].y += deltaHeight

                    syncViewsWithSizeAndPosCache()
                }
            }

            Edge.TOP_LEFT_CORNER, Edge.TOP_SIDE, Edge.TOP_RIGHT_CORNER -> {
                if (index == 0) return

                val okToAdjustHeight = imageSizeAndPosCache[index].height - deltaHeight > minDimension &&
                        imageSizeAndPosCache[index - 1].height + deltaHeight > minDimension

                if (!okToAdjustHeight) Log.d(TAG, "resizeImageAt: unsafe to resize")
                else {
                    imageSizeAndPosCache[index].height -= deltaHeight
                    imageSizeAndPosCache[index].y += deltaHeight

                    imageSizeAndPosCache[index - 1].height += deltaHeight

                    syncViewsWithSizeAndPosCache()
                }
            }

            else -> return
        }
    }


    // ------------------------ TOUCH HELPER ------------------------  //

    // Do not change
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return onTouchHelper(v, event, ::resizeImageAt)
    }


    // ------------------------ HELPERS ------------------------ //

    companion object {
        private const val TAG = "CollageViewVertical"
    }
}