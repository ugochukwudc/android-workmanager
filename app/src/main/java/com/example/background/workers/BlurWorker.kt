package com.example.background.workers

import android.content.Context
import android.graphics.BitmapFactory.decodeStream
import android.net.Uri
import android.net.Uri.parse
import android.text.TextUtils
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.background.KEY_IMAGE_URI
import timber.log.Timber

class BlurWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        return try {
            val resourceUri = inputData.getString(KEY_IMAGE_URI)
            if (TextUtils.isEmpty(resourceUri)) {
                Timber.e("Invalid input uri")
                throw IllegalArgumentException("Invalid input uri")
            }
            makeStatusNotification("Blurring image $resourceUri", applicationContext)
            val picture = decodeStream(applicationContext.contentResolver.openInputStream(parse(resourceUri)))
            val blurredBitmap = blurBitmap(picture, applicationContext)
            val savedFileUri = writeBitmapToFile(applicationContext, blurredBitmap)
            makeStatusNotification("Blurred Image available at $savedFileUri", applicationContext)
            Result.success(createOutputData(savedFileUri))
        } catch (error: Throwable) {
            Timber.e(error, "Error applying blur")
            Result.failure()
        }
    }

    private fun createOutputData(outputUri: Uri): Data {
        return Data.Builder()
            .putString(KEY_IMAGE_URI, outputUri.toString())
            .build()
    }
}