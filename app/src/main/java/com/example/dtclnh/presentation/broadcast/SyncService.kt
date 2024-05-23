package com.example.dtclnh.presentation.broadcast

import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Build
import android.os.IBinder
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.*
import com.example.dtclnh.MainActivity
import com.example.dtclnh.R
import com.example.dtclnh.core.Constants
import com.example.dtclnh.core.Constants.WORK_MANAGER_ID
import com.example.dtclnh.core.Constants.WORK_MANAGER_TAG
import com.example.dtclnh.core.getViewStateFlowForNetworkCall
import com.example.dtclnh.di.EndpointInterceptor
import com.example.dtclnh.di.HeaderInterceptor
import com.example.dtclnh.domain.model.BackupResponse
import com.example.dtclnh.domain.model.BackupStatus
import com.example.dtclnh.domain.model.BaseResponse
import com.example.dtclnh.domain.model.SmsDataWrapper
import com.example.dtclnh.domain.model.SmsModel
import com.example.dtclnh.domain.model.SmsParam
import com.example.dtclnh.domain.usecase.BackUpUseCase
import com.example.dtclnh.domain.usecase.FindAndUpdateStatusUseCase
import com.example.dtclnh.domain.usecase.SaveSmsUseCase
import com.example.dtclnh.presentation.base.ext.goAsync
import com.example.dtclnh.presentation.base.ext.toDateTimeLong
import com.example.dtclnh.presentation.base.ext.toDateTimeString
import com.example.dtclnh.presentation.page.login.LoginFragment
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import okhttp3.*
import java.io.IOException
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class SyncService : Service() {

    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)

    @Inject
    lateinit var backUpUseCase: BackUpUseCase;

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var saveSmsUseCase: SaveSmsUseCase

    @Inject
    lateinit var endpointInterceptor: EndpointInterceptor

    @Inject
    lateinit var headerInterceptor: HeaderInterceptor

    // BroadcastReceiver để lắng nghe sự kiện thay đổi mạng
    private val networkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (isNetworkConnected()) {
                // Nếu có kết nối internet, bắt đầu đồng bộ tin nhắn
//                syncInboxMessages()
            }
        }
    }

    // BroadcastReceiver để lắng nghe tin nhắn đến thư mục inbox
    private val smsReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
//            if (intent?.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
//                if (isNetworkConnected()) {
//                    coroutineScope.launch {
//                        try {
//                            val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
//
//                            val newEndpoint = sharedPreferences.getString(Constants.API_URL_KEY, "")
//                            val authorization =
//                                sharedPreferences.getString(Constants.API_KEY_KEY, "") ?: ""
//                            val clientId =
//                                sharedPreferences.getString(Constants.CLIENT_ID_KEY, "")
//                                    ?: Constants.CLIENT_ID
//                            newEndpoint?.let {
//                                endpointInterceptor.setNewEndpoint(it)
//                            }
//                            val headers = mapOf(
//                                "Authorization" to authorization,
//                                "Content-Type" to "application/json"
//                            )
//                            headerInterceptor.setHeaders(headers)
//
//                            val params = smsMessages.map {
//                                SmsParam(
//                                    smsId = generateUniqueID(
//                                        it.timestampMillis,
//                                        it.displayOriginatingAddress
//                                    ),
//                                    clientId = clientId,
//                                    sender = it.displayOriginatingAddress,
//                                    content = it.messageBody,
//                                    receivedAt = it.timestampMillis.toDateTimeString()
//                                )
//                            }.toList()
//                            params.forEach {
//                                Log.e("AMBE1203 onReceive", "${it}")
//
//                            }
//                            val smsDataWrapper = SmsDataWrapper(data = params)
//                            getViewStateFlowForNetworkCall {
//                                backUpUseCase.execute(smsDataWrapper)
//                            }.collect { r ->
//
//                                if (r.isLoading) {
//                                    val intentRunning = Intent(Constants.ACTION_WORK_RUNNING)
//                                    LocalBroadcastManager.getInstance(applicationContext)
//                                        .sendBroadcast(intentRunning)
//
//                                } else if (r.throwable != null) {
//                                    Log.e(
//                                        "AMBE1203",
//                                        "throwable ${r.throwable.localizedMessage}"
//                                    )
//                                    val a = Intent(Constants.ACTION_WORK_FAIL)
//                                    a.putExtra("error", r.throwable.localizedMessage)
//                                    LocalBroadcastManager.getInstance(applicationContext)
//                                        .sendBroadcast(a)
//                                } else if (r.result != null) {
//                                    val status =
//                                        (r.result as BackupResponse).status
//                                    if (status == 200) {
//                                        saveSmsUseCase.execute(params.map {
//                                            SmsModel(
//                                                smsId = generateUniqueID(
//                                                    it.receivedAt.toDateTimeLong(),
//                                                    it.sender
//                                                ),
//                                                sender = it.sender,
//                                                content = it.content,
//                                                receivedAt = it.receivedAt.toDateTimeLong()
//                                                    .toString(),
//                                                status = "1",
//                                                backupStatus = BackupStatus.SUCCESS,
//                                                isSmsCome = 1
//                                            )
//                                        }.toMutableList(), true)
//
//                                        val x = Intent(Constants.ACTION_WORK_SUCCESS)
//                                        LocalBroadcastManager.getInstance(applicationContext)
//                                            .sendBroadcast(x)
//
//                                    } else {
//                                        val y = Intent(Constants.ACTION_WORK_FAIL)
//                                        y.putExtra(
//                                            "error",
//                                            r.result.message
//                                        )
//                                        LocalBroadcastManager.getInstance(applicationContext)
//                                            .sendBroadcast(y)
//                                    }
//
//                                }
//                            }
//
//                        } catch (e: Exception) {
//                            Log.e("AMBE1203", "throwable 1 ${e.localizedMessage}")
//                            val z = Intent(Constants.ACTION_WORK_FAIL)
//                            z.putExtra("error", e.localizedMessage)
//                            LocalBroadcastManager.getInstance(applicationContext)
//                                .sendBroadcast(z)
//                        }
//                    }
//
//                }
//
//
//            }
        }

    }

    override fun onCreate() {
        super.onCreate()

//        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
//        registerReceiver(networkReceiver, filter)
//
//        val smsFilter = IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
//        registerReceiver(smsReceiver, smsFilter)

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
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
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
