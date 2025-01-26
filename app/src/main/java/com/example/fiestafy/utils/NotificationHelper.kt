package com.example.fiestafy.utils

import android.content.Context
import android.util.Log
import com.example.fiestafy.R
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.messaging.FirebaseMessaging
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

object NotificationHelper {
    private const val FCM_API = "https://fcm.googleapis.com/v1/projects/fiestafy-dde38/messages:send"
    private val client = OkHttpClient()
    private val JSON = "application/json; charset=utf-8".toMediaType()
    private lateinit var context: Context

    fun init(context: Context) {
        this.context = context
        FirebaseMessaging.getInstance().subscribeToTopic("all")
            .addOnSuccessListener {
                Log.d("NotificationHelper", "Suscrito exitosamente al tema 'all'")
            }
            .addOnFailureListener { e ->
                Log.e("NotificationHelper", "Error al suscribirse al tema 'all'", e)
            }
    }

    private fun getAccessToken(callback: (String) -> Unit) {
        Thread {
            try {
                val inputStream = context.resources.openRawResource(R.raw.service_account)
                val credentials = GoogleCredentials.fromStream(inputStream)
                    .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
                credentials.refresh()
                callback(credentials.accessToken.tokenValue)
            } catch (e: Exception) {
                Log.e("NotificationHelper", "Error getting access token", e)
            }
        }.start()
    }

    fun sendNotificationToAllUsers(title: String, message: String) {
        Log.d("NotificationHelper", "Intentando enviar notificación: $title - $message")
        val notification = JSONObject().apply {
            put("message", JSONObject().apply {
                put("topic", "all")
                put("notification", JSONObject().apply {
                    put("title", title)
                    put("body", message)
                })
            })
        }

        getAccessToken { token ->
            val request = Request.Builder()
                .url(FCM_API)
                .addHeader("Authorization", "Bearer $token")
                .post(notification.toString().toRequestBody(JSON))
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("NotificationHelper", "Error sending notification", e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        Log.e("NotificationHelper", "Error response: ${response.code} - ${response.body?.string()}")
                    } else {
                        Log.d("NotificationHelper", "Notification sent successfully")
                    }
                    response.close()
                }
            })
        }
    }
} 