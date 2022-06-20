package dev.pegasus.collage.views

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import dev.pegasus.collage.utils.ImageParams

class CollageView3Image2(
    context: Context,
    attrs: AttributeSet?,
    totalWidth: Int, totalHeight: Int,
    isBorderEnabled: Boolean = false,
    imageUris: Array<Uri?>? = null
) : AbstractCollageView(
    context,
    attrs,
    3,
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
        imageSizeAndPosCache[0] = ImageParams(
            width = layoutWidth / 2f,
            height = layoutHeight / 2f,
            x = 0f,
            y = 0f
        )

        imageSizeAndPosCache[1] = ImageParams(
            width = layoutWidth / 2f,
            height = layoutHeight.toFloat(),
            x = layoutWidth / 2f,
            y = 0f
        )

        imageSizeAndPosCache[2] = ImageParams(
            width = layoutWidth / 2f,
            height = layoutHeight / 2f,
            x = 0f,
            y = layoutHeight / 2f
        )

        syncViewsWithSizeAndPosCache()
    }

    // Do not change
    private fun prepareTouchListeners() {
        for (view in imageViews) view.setOnTouchListener(this)
    }


    // ------------------------ RESIZING HELPERS ------------------------

    private fun resizeImageAt(index: Int, deltaWidth: Float, deltaHeight: Float) {
        when (index) {
            0 -> resizeImage0(deltaWidth, deltaHeight)
            1 -> resizeImage1(deltaWidth)
            2 -> resizeImage2(deltaWidth, deltaHeight)
            else -> {
                Log.d(TAG, "resizeImage: invalid index")
                return
            }
        }
    }

    private fun resizeImage0(deltaWidth: Float, deltaHeight: Float) {
        val okToAdjustWidth = imageSizeAndPosCache[0].width + deltaWidth in minDimension..(layoutWidth - minDimension)
        val okToAdjustHeight = imageSizeAndPosCache[0].height + deltaHeight in minDimension..(layoutHeight - minDimension)

        when (touchedImageEdge) {
            null -> {
                Log.d(TAG, "resizeImage0: invalid edge")
            }
            Edge.TOP_RIGHT_CORNER, Edge.RIGHT_SIDE -> {
                if (okToAdjustWidth) imageSizeAndPosCache[0].width += deltaWidth
            }
            Edge.BOTTOM_LEFT_CORNER, Edge.BOTTOM_SIDE -> {
                if (okToAdjustHeight) imageSizeAndPosCache[0].height += deltaHeight
            }
            Edge.BOTTOM_RIGHT_CORNER -> {
                if (okToAdjustWidth) imageSizeAndPosCache[0].width += deltaWidth
                if (okToAdjustHeight) imageSizeAndPosCache[0].height += deltaHeight
            }
            else -> return
        }

        imageSizeAndPosCache[1].width = layoutWidth - imageSizeAndPosCache[0].width
        imageSizeAndPosCache[1].x = imageSizeAndPosCache[0].width

        imageSizeAndPosCache[2].width = imageSizeAndPosCache[0].width
        imageSizeAndPosCache[2].height = layoutHeight - imageSizeAndPosCache[0].height
        imageSizeAndPosCache[2].y = imageSizeAndPosCache[0].height

        syncViewsWithSizeAndPosCache()
    }

    private fun resizeImage1(deltaWidth: Float) {
        val okToAdjustWidth = imageSizeAndPosCache[1].width - deltaWidth in minDimension..(layoutWidth - minDimension)

        when (touchedImageEdge) {
            null -> {
                Log.d(TAG, "resizeImage1: invalid edge")
                return
            }
            Edge.TOP_LEFT_CORNER, Edge.BOTTOM_LEFT_CORNER, Edge.LEFT_SIDE -> {
                if (okToAdjustWidth) imageSizeAndPosCache[1].width -= deltaWidth
            }
            else -> return
        }

        imageSizeAndPosCache[1].x = layoutWidth - imageSizeAndPosCache[1].width

        imageSizeAndPosCache[0].width = layoutWidth - imageSizeAndPosCache[1].width
        imageSizeAndPosCache[2].width = layoutWidth - imageSizeAndPosCache[1].width

        syncViewsWithSizeAndPosCache()
    }

    private fun resizeImage2(deltaWidth: Float, deltaHeight: Float) {
        val okToAdjustWidth = imageSizeAndPosCache[2].width + deltaWidth in minDimension..(layoutWidth - minDimension)
        val okToAdjustHeight = imageSizeAndPosCache[2].height - deltaHeight in minDimension..(layoutHeight - minDimension)

        when (touchedImageEdge) {
            null -> {
                Log.d(TAG, "resizeImage2: invalid edge")
                return
            }
            Edge.TOP_SIDE, Edge.TOP_LEFT_CORNER -> {
                if (okToAdjustHeight) imageSizeAndPosCache[2].height -= deltaHeight
            }
            Edge.RIGHT_SIDE, Edge.BOTTOM_RIGHT_CORNER -> {
                if (okToAdjustWidth) imageSizeAndPosCache[2].width += deltaWidth
            }
            Edge.TOP_RIGHT_CORNER -> {
                if (okToAdjustWidth) imageSizeAndPosCache[2].width += deltaWidth
                if (okToAdjustHeight) imageSizeAndPosCache[2].height -= deltaHeight
            }
            else -> return
        }

        imageSizeAndPosCache[2].y = layoutHeight - imageSizeAndPosCache[2].height

        imageSizeAndPosCache[0].width = imageSizeAndPosCache[2].width
        imageSizeAndPosCache[0].height = layoutHeight - imageSizeAndPosCache[2].height

        imageSizeAndPosCache[1].width = layoutWidth - imageSizeAndPosCache[2].width
        imageSizeAndPosCache[1].x = imageSizeAndPosCache[2].width

        syncViewsWithSizeAndPosCache()
    }


    // ------------------------ TOUCH HELPER ------------------------

    // Do not change
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return onTouchHelper(v, event, ::resizeImageAt)
    }


    // ------------------------ HELPERS ------------------------

    companion object {
        private const val TAG = "CollageView3Image2"
    }
}