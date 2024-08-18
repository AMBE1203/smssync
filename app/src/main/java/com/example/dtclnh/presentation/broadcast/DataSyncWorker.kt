package com.example.dtclnh.presentation.broadcast

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.dtclnh.core.Constants.ACTION_WORK_FAIL
import com.example.dtclnh.core.Constants.ACTION_WORK_RUNNING
import com.example.dtclnh.core.Constants.ACTION_WORK_SUCCESS
import com.example.dtclnh.core.Constants.API_KEY_KEY
import com.example.dtclnh.core.Constants.API_URL_KEY
import com.example.dtclnh.core.Constants.CHUNK_SIZE
import com.example.dtclnh.core.Constants.CLIENT_ID
import com.example.dtclnh.core.Constants.CLIENT_ID_KEY
import com.example.dtclnh.core.IOResults
import com.example.dtclnh.core.getViewStateFlowForNetworkCall
import com.example.dtclnh.di.EndpointInterceptor
import com.example.dtclnh.di.HeaderInterceptor
import com.example.dtclnh.domain.model.*
import com.example.dtclnh.domain.usecase.*
import com.example.dtclnh.presentation.base.ext.toDateTimeString
import com.example.dtclnh.presentation.page.login.LoginViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltWorker
class DataSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val backUpUseCase: BackUpUseCase,
    private val endpointInterceptor: EndpointInterceptor,
    private val headerInterceptor: HeaderInterceptor,
    private val sharedPreferences: SharedPreferences,
    private val fetchAllSmsForBackUpUseCase: FetchAllSmsForBackUpUseCase,
    private val findAndUpdateStatusUseCase: FindAndUpdateStatusUseCase,
    private val loadAllSmsInInboxUseCase: LoadAllSmsInInboxUseCase,
    private val saveSmsUseCase: SaveSmsUseCase,

    ) :
    CoroutineWorker(context, params) {


    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    override suspend fun doWork(): Result {

        return try {

            val listSms = loadAllSmsInInboxUseCase.execute()
            val intentRunning = Intent(ACTION_WORK_RUNNING)
            intentRunning.setPackage(applicationContext.packageName)
            LocalBroadcastManager.getInstance(applicationContext)
                .sendBroadcast(intentRunning)

            if (listSms.isNotEmpty()) {
                saveSmsUseCase.execute(listSms, false)
                val newEndpoint = sharedPreferences.getString(API_URL_KEY, "")
                val authorization =
                    sharedPreferences.getString(API_KEY_KEY, "") ?: ""
                val clientId =
                    sharedPreferences.getString(CLIENT_ID_KEY, "") ?: CLIENT_ID
                newEndpoint?.let {
                    endpointInterceptor.setNewEndpoint(it)
                }
                val headers = mapOf(
                    "Authorization" to authorization,
                    "Content-Type" to "application/json"
                )
                headerInterceptor.setHeaders(headers)
                val smsInbox = fetchAllSmsForBackUpUseCase.execute()
                Log.e("AMBE1203", "smsInbox SIZE: ${smsInbox.size}")


                if (smsInbox.isNotEmpty()) {


                    val chunkedSms = smsInbox.chunked(CHUNK_SIZE)

//                    val jobs = mutableListOf<Job>()

                    for (chunk in chunkedSms) {
                        Log.e("AMBE1203", "chunk SIZE: ${chunk.size}")

                        val job = coroutineScope.launch {
                            try {
                                val params = chunk.map {
                                    SmsParam(
                                        smsId = it.smsId,
                                        clientId = clientId,
                                        sender = it.sender,
                                        content = it.content,
                                        receivedAt = it.receivedAt.toLong().toDateTimeString()
                                    )
                                }

                                val smsDataWrapper = SmsDataWrapper(data = params)
                                getViewStateFlowForNetworkCall {
                                    backUpUseCase.execute(smsDataWrapper)
                                }.collect { r ->
                                    if (r.isLoading) {
                                        val intentRunning = Intent(ACTION_WORK_RUNNING)
                                        intentRunning.setPackage(applicationContext.packageName)

                                        LocalBroadcastManager.getInstance(applicationContext)
                                            .sendBroadcast(intentRunning)
                                    } else if (r.throwable != null) {
                                        Log.e(
                                            "AMBE1203",
                                            "throwable ${r.throwable.localizedMessage}"
                                        )
                                        val intent = Intent(ACTION_WORK_FAIL)
                                        intent.setPackage(applicationContext.packageName)

                                        intent.putExtra(
                                            "error",
                                            "${r.throwable.localizedMessage} $smsDataWrapper"
                                        )
                                        LocalBroadcastManager.getInstance(applicationContext)
                                            .sendBroadcast(intent)
                                    } else if (r.result != null) {
                                        val status =
                                            (r.result as BackupResponse).status
                                        if (status == 200) {
                                            findAndUpdateStatusUseCase.execute(chunk.map { it.receivedAt }
                                                .toList())
                                            Log.e("AMBE1203", "success chunk SIZE: ${chunk.size}")

                                            val intent = Intent(ACTION_WORK_SUCCESS)
                                            intent.setPackage(applicationContext.packageName)

                                            LocalBroadcastManager.getInstance(applicationContext)
                                                .sendBroadcast(intent)
                                        } else {
                                            val intent = Intent(ACTION_WORK_FAIL)
                                            intent.setPackage(applicationContext.packageName)

                                            intent.putExtra(
                                                "error",
                                                r.result.message
                                            )
                                            LocalBroadcastManager.getInstance(applicationContext)
                                                .sendBroadcast(intent)
                                        }
                                    }
                                }

// Print time before delay

                            } catch (e: Exception) {
                                Log.e("AMBE1203", "throwable 1 ${e.localizedMessage}")
                                val intent = Intent(ACTION_WORK_FAIL)
                                intent.setPackage(applicationContext.packageName)

                                intent.putExtra("error", e.localizedMessage)
                                LocalBroadcastManager.getInstance(applicationContext)
                                    .sendBroadcast(intent)
                            }

                        }
                        delay(1000L)

//                        jobs.add(job)
                    }

//                    jobs.forEach { it.join() }

                } else {
                    val intentSuccess = Intent(ACTION_WORK_SUCCESS)
                    intentSuccess.setPackage(applicationContext.packageName)
                    LocalBroadcastManager.getInstance(applicationContext)
                        .sendBroadcast(intentSuccess)
                }

            } else {
                val intentSuccess = Intent(ACTION_WORK_SUCCESS)
                intentSuccess.setPackage(applicationContext.packageName)
                LocalBroadcastManager.getInstance(applicationContext)
                    .sendBroadcast(intentSuccess)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }


}
