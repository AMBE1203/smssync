package com.example.dtclnh.presentation.broadcast

import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import android.os.IBinder
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.example.dtclnh.MainActivity
import com.example.dtclnh.R
import com.example.dtclnh.core.Constants.WORK_MANAGER_ID
import com.example.dtclnh.core.Constants.WORK_MANAGER_TAG
import com.example.dtclnh.presentation.page.login.LoginFragment
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.*
import okhttp3.*
import java.io.IOException
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

class SyncService : Service() {

    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)

    // BroadcastReceiver để lắng nghe sự kiện thay đổi mạng
    private val networkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (isNetworkConnected()) {
                // Nếu có kết nối internet, bắt đầu đồng bộ tin nhắn
                syncInboxMessages()
            }
        }
    }

    // BroadcastReceiver để lắng nghe tin nhắn đến thư mục inbox
    private val smsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Xử lý khi nhận được tin nhắn đến thư mục inbox
            // Gửi tin nhắn lên server
            if (intent?.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {

                val bundle = intent.extras
                bundle?.let {
                    val pdus = bundle.get("pdus") as Array<*>
                    val messages = arrayOfNulls<SmsMessage>(pdus.size)
                    for (i in messages.indices) {
                        messages[i] = SmsMessage.createFromPdu(pdus[i] as ByteArray)
                    }
                    for (message in messages) {
                        val senderNumber = message?.originatingAddress
                        val messageBody = message?.messageBody
                        // Xử lý tin nhắn ở đây
                        Log.e("AMBE1203 onReceive", "${messageBody}")
                    }
                }


                val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                for (smsMessage in smsMessages) {
                    val messageBody = smsMessage.messageBody
                    val senderAddress = smsMessage.displayOriginatingAddress
                    // Khởi chạy Service Foreground để gửi tin nhắn lên server
                    Log.e("AMBE1203 onReceive", "${messageBody}")

                }
            }

            val messages = context?.let { fetchInboxMessages(it) }

            messages?.forEach { message ->

                Log.e("AMBE1203 onReceive", "${message.toString()}")
                // Gửi tin nhắn lên server
                sendMessageToServer(message)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        // Đăng ký BroadcastReceiver để lắng nghe sự kiện thay đổi mạng
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkReceiver, filter)

        // Đăng ký BroadcastReceiver để lắng nghe tin nhắn đến
        val smsFilter = IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
        registerReceiver(smsReceiver, smsFilter)

        startForeground(NOTIFICATION_ID, buildNotification())

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<DataSyncWorker>(
            repeatInterval = 15L,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag(WORK_MANAGER_TAG)
            .build()


        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            WORK_MANAGER_ID,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }


    override fun onDestroy() {
        super.onDestroy()
        // Hủy đăng ký BroadcastReceiver khi Service bị hủy
        unregisterReceiver(networkReceiver)
        unregisterReceiver(smsReceiver)
        job.cancel()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun isNetworkConnected(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun syncInboxMessages() {
        coroutineScope.launch {
            val messages = fetchInboxMessages(applicationContext)
            messages.forEach { message ->
                sendMessageToServer(message)
            }
        }
    }

    private fun fetchInboxMessages(context: Context): List<String> {
        val messages = mutableListOf<String>()
        val uri = Telephony.Sms.Inbox.CONTENT_URI
        val projection = arrayOf(Telephony.Sms.Inbox.BODY)
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val bodyIndex = cursor.getColumnIndex(Telephony.Sms.Inbox.BODY)
            while (cursor.moveToNext()) {
                val body = cursor.getString(bodyIndex)
                messages.add(body)
            }
        }
        return messages
    }

    private fun sendMessageToServer(message: String) {

//        // Triển khai phương thức này để gửi tin nhắn lên server
//        val client = OkHttpClient()
//        val requestBody = FormBody.Builder()
//            .add("message", message)
//            .build()
//        val request = Request.Builder()
//            .url("YOUR_SERVER_URL")
//            .post(requestBody)
//            .build()
//        client.newCall(request).enqueue(object : Callback {
//            override fun onResponse(call: Call, response: Response) {
//                // Xử lý phản hồi từ server (nếu cần)
//            }
//
//            override fun onFailure(call: Call, e: IOException) {
//                // Xử lý khi gửi tin nhắn thất bại (nếu cần)
//            }
//        })
    }

    private fun buildNotification(): Notification {
        val channelId = "SyncServiceChannel"
        createNotificationChannel(channelId)
        val notificationIntent = Intent(this, LoginFragment::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Sync Service")
            .setContentText("Syncing messages...")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Sync Service Channel"
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notification channel for Sync Service"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 1
    }
}
