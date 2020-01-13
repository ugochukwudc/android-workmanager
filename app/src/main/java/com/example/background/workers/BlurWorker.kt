package com.example.background.workers

import android.content.Context
import android.graphics.BitmapFactory.decodeStream
import android.net.Uri
import android.net.Uri.parse
import android.text.TextUtils
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.background.KEY_BLUR_LEVEL
import com.example.background.KEY_IMAGE_URI
import timber.log.Timber

/**
 * Blurs Images retrieved from ImageUri in [Worker.getInputData] using [blurBitmap],
 * and outputs blurredImageUri to its Result data
 */
class BlurWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        return try {
            val resourceUri = inputData.getString(KEY_IMAGE_URI)
            val blurLevel = inputData.getInt(KEY_BLUR_LEVEL, 1)
            if (TextUtils.isEmpty(resourceUri)) {
                Timber.e("Invalid input uri")
                throw IllegalArgumentException("Invalid input uri")
            }
            makeStatusNotification("Blurring image $resourceUri", applicationContext)
            val stream = applicationContext.contentResolver.openInputStream(parse(resourceUri))
            val picture = decodeStream(stream)
            val blurredBitmap = blurBitmap(picture, applicationContext, blurLevel)
            val savedFileUri = writeBitmapToFile(applicationContext, blurredBitmap)
            makeStatusNotification("Blurred Image available at $savedFileUri", applicationContext)
            stream?.close()
            picture.recycle()
            blurredBitmap.recycle()
            Result.success(createOutputData(savedFileUri, blurLevel))
        } catch (error: Throwable) {
            Timber.e(error, "Error applying blur")
            Result.failure()
        }
    }

    private fun createOutputData(outputUri: Uri, level: Int = 1): Data {
        return Data.Builder()
            .putString(KEY_IMAGE_URI, outputUri.toString())
            .putInt(KEY_BLUR_LEVEL, level)
            .build()
    }
}