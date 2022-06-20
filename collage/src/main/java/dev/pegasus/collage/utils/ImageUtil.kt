package dev.pegasus.collage.utils

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class ImageUtil constructor(
    private val mainDispatcher: CoroutineDispatcher, private val defaultDispatcher: CoroutineDispatcher, private val ioDispatcher: CoroutineDispatcher
) {

    fun printDispatchers() {
        Log.d(TAG, "main: $mainDispatcher")
        Log.d(TAG, "default: $defaultDispatcher")
        Log.d(TAG, "io: $ioDispatcher")
    }

    /**
     * Saves [view] to internal storage and sends the file [Uri] to the listener on completion.
     */
    fun prepareViewForSharing(context: Context, view: View, listener: ImageSavedListener) {
        CoroutineScope(defaultDispatcher).launch {
            val bitmap = createBitmapFrom(view)
            saveBitmapAndNotifyListener(context, bitmap, listener)
        }
    }

    private suspend fun saveBitmapAndNotifyListener(context: Context, bitmap: Bitmap, listener: ImageSavedListener) {
        withContext(ioDispatcher) {
            val uri = saveBitmapToInternalStorage(context, bitmap)
            notifyListenerBitmapSaveAttempted(listener, uri)
        }
    }

    private fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): Uri? {
        try {
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "temp.jpg")
            saveImageToStream(bitmap, FileOutputStream(file))
            return FileProvider.getUriForFile(context, context.packageName + ".provider", file)
        } catch (e: IOException) {
            Log.d(TAG, "Error saving image to internal storage.")
        }
        return null
    }

    private suspend fun notifyListenerBitmapSaveAttempted(listener: ImageSavedListener, uri: Uri?) {
        withContext(mainDispatcher) {
            listener.onReadyToShareImage(uri)
        }
    }

    /**
     * Saves [view] to internal storage and notifies the listener on completion.
     */
    fun saveViewToGallery(activity: Activity, view: View, listener: ImageSavedListener) {
        requestStoragePermission(activity)
        if (!isStoragePermissionGranted(activity, listener)) return
        CoroutineScope(defaultDispatcher).launch {
            val bitmap = createBitmapFrom(view)
            saveBitmapToGalleryAndNotifyListener(activity, bitmap, listener)
        }
    }

    private suspend fun saveBitmapToGalleryAndNotifyListener(context: Context, bitmap: Bitmap, listener: ImageSavedListener) {
        withContext(ioDispatcher) {
            val uri = saveBitmapToGallery(context, bitmap)
            notifyListenerOfImageSavedToGalleryResult(listener, uri)
        }
    }

    private fun saveBitmapToGallery(context: Context, bitmap: Bitmap): Uri? {
        val folderName = "Collage"
        return if (android.os.Build.VERSION.SDK_INT >= 29) {
            saveBitmapToGalleryAndroidQ(context, bitmap, getFilename(), folderName)
        } else {
            saveBitmapToGalleryAndroidM(context, bitmap, getFilename(), folderName)
        }
    }

    @RequiresApi(29)
    private fun saveBitmapToGalleryAndroidQ(context: Context, bitmap: Bitmap, filename: String, folderName: String): Uri? {
        val contentValues = getContentValues(filename).apply {
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/$folderName")
            put(MediaStore.Images.Media.IS_PENDING, true)
            put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        }

        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        var isSavedSuccessfully = false

        if (uri != null) {
            isSavedSuccessfully = saveImageToStream(bitmap, context.contentResolver.openOutputStream(uri))
            contentValues.put(MediaStore.Images.Media.IS_PENDING, false)
            context.contentResolver.update(uri, contentValues, null, null)
        }

        return if (isSavedSuccessfully) uri else null
    }

    @Suppress("DEPRECATION")
    @TargetApi(21)
    private fun saveBitmapToGalleryAndroidM(
        context: Context, bitmap: Bitmap,
        filename: String,
        folderName: String
    ): Uri? {

        val dir = File(Environment.getExternalStorageDirectory().toString() + "/" + folderName)
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, filename)
        val isSavedSuccessfully = saveImageToStream(bitmap, FileOutputStream(file))

        val values = getContentValues(filename)
        values.put(MediaStore.Images.Media.DATA, file.absolutePath)
        context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        return if (isSavedSuccessfully) file.toUri() else null
    }

    private suspend fun notifyListenerOfImageSavedToGalleryResult(listener: ImageSavedListener, uri: Uri?) {
        withContext(mainDispatcher) {
            listener.onCollageSavedToGallery(uri != null, uri)
        }
    }


    private fun createBitmapFrom(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    @SuppressLint("SimpleDateFormat") // not relevant - format for filename purpose only
    private fun getFilename(): String {
        val sdf = SimpleDateFormat("yyyyMMddHHmmss")
        val dateAndTime = sdf.format(Calendar.getInstance().time)

        return "collage$dateAndTime"
    }

    private fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?): Boolean {
        try {
            outputStream?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                it.flush()
                it.close()
                return true
            }

        } catch (e: FileNotFoundException) {
            Log.d(TAG, "Save to gallery failed due to FileNotFoundException.")
            e.printStackTrace()

        } catch (e: IOException) {
            Log.d(TAG, "Save to gallery failed due to IOException.")
            e.printStackTrace()
        }

        return false
    }

    private fun getContentValues(filename: String): ContentValues {
        return ContentValues().apply {
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        }
    }

    private fun requestStoragePermission(activity: Activity) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

            val requiredPermission = Manifest.permission.WRITE_EXTERNAL_STORAGE
            val shouldRequestPermission = activity.checkSelfPermission(requiredPermission) != PackageManager.PERMISSION_GRANTED

            if (shouldRequestPermission) {
                if (activity.shouldShowRequestPermissionRationale(requiredPermission)) {
                    Toast.makeText(activity, "Storage permission is needed to save to gallery.", Toast.LENGTH_LONG).show()
                }
                val storagePermissions = arrayOf(requiredPermission)
                activity.requestPermissions(storagePermissions, 1)
            }
        }
    }

    private fun isStoragePermissionGranted(activity: Activity, listener: ImageSavedListener): Boolean {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val requiredPermission = Manifest.permission.WRITE_EXTERNAL_STORAGE

            if (activity.checkSelfPermission(requiredPermission) == PackageManager.PERMISSION_DENIED) {
                Log.d(TAG, "Save to gallery failed. Permission denied.")
                listener.onCollageSavedToGallery(false, null)
                return false
            }
        }
        return true
    }

    interface ImageSavedListener {
        fun onCollageSavedToGallery(isSaveSuccessful: Boolean, uri: Uri?)
        fun onReadyToShareImage(uri: Uri?)
    }

    companion object {
        private const val TAG = "ImageUtil"
    }
}