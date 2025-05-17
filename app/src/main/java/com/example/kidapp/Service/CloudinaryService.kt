package com.example.kidapp.Service

import android.content.Context
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.kidapp.BuildConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.util.HashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


class CloudinaryService(private val context: Context) {
    private val TAG = "CloudinaryService"
    private val config = HashMap<String, String>()
    private val CLOUDINARY_API_KEY_SECRET = "FF6aPJ62slGU0ODmD5iyYZL0rHI"
    private val CLOUDINARY_API_KEY_PUBLIC = "314152328472518"
    private val CLOUDINARY_NAME = "dix7ukaf7"

    init {
        initConfig()
    }

    private fun initConfig() {
        config["cloud_name"] = CLOUDINARY_NAME
        config["api_key"] = CLOUDINARY_API_KEY_PUBLIC
        config["api_secret"] = CLOUDINARY_API_KEY_SECRET
        config["secure"] = "true"
        MediaManager.init(context, config)
    }

    // Non-suspend version for Java compatibility
    fun uploadImage(file: File): String {
        var resultUrl = ""
        val latch = java.util.concurrent.CountDownLatch(1)

        MediaManager.get()
            .upload(file.absolutePath)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    Log.d(TAG, "Starting upload...")
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    Log.d(TAG, "Uploading... ${bytes * 100 / totalBytes}%")
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    Log.d(TAG, "Upload success!")
                    resultUrl = resultData["secure_url"] as String
                    latch.countDown()
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    Log.e(TAG, "Upload error: ${error.description}")
                    latch.countDown()
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    Log.e(TAG, "Upload rescheduled: ${error.description}")
                }
            })
            .dispatch()

        try {
            latch.await() // Wait for upload to complete
        } catch (e: InterruptedException) {
            Log.e(TAG, "Upload interrupted", e)
        }

        return resultUrl
    }

    // Original suspend version for Kotlin
    suspend fun uploadImageSuspend(file: File): String {
        return uploadImage(file)
    }
}