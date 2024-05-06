package com.example.dtclnh.presentation.broadcast

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.dtclnh.di.EndpointInterceptor
import com.example.dtclnh.di.HeaderInterceptor
import com.example.dtclnh.domain.model.SyncEvent
import com.example.dtclnh.domain.model.SyncStatus
import com.example.dtclnh.domain.usecase.BackUpUseCase
import com.example.dtclnh.presentation.page.login.LoginViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect

@HiltWorker
class DataSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val backUpUseCase: BackUpUseCase,
    private val endpointInterceptor: EndpointInterceptor,
    private val headerInterceptor: HeaderInterceptor,

) :
    CoroutineWorker(context, params) {


    companion object {
        const val Progress = "Progress"
        private const val delayDuration = 1L
        const val WORK_SUCCESS_FLAG = "work_success_flag"
        const val ACTION_WORK_RUNNING = "action_work_running"
        const val ACTION_WORK_SUCCESS = "action_work_success"

    }


    override suspend fun doWork(): Result {
        val intentRunning = Intent(ACTION_WORK_RUNNING)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentRunning)
        Log.e("AMBE1203", "dowork")
        val newEndpoint = "http://125.212.238.157:8460/api/v1/sms/new/batch"
        endpointInterceptor.setNewEndpoint(newEndpoint)
        val headers = mapOf(
            "Authorization" to "Basic c21zc3luYzpzbXNzeW5jQDIwMjQ=",
            "Content-Type" to "application/json"
        )
        headerInterceptor.setHeaders(headers)
        backUpUseCase.execute(mutableListOf()).collect {
            Log.e("AMBE1203", it.toString())
        }

//        return if (success) {
//            Result.success()
//        } else {
//            Result.retry()
//        }

        val firstUpdate = workDataOf(Progress to 0)
        val lastUpdate = workDataOf(Progress to 100)


        setProgressAsync(firstUpdate)
        delay(3000L)
        setProgressAsync(lastUpdate)
        val successData = workDataOf(WORK_SUCCESS_FLAG to true)
        val intent = Intent(ACTION_WORK_SUCCESS)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)


        return  Result.success(successData);

//        syncEventFlow.emit(SyncEvent.SyncStarted)

//        return try {
////            val result = syncDataToServer()
//            //todo call API
////            syncEventFlow.emit(SyncEvent.SyncSuccess("Success"))
//            Result.success()
//        } catch (e: Exception) {
////            syncEventFlow.emit(SyncEvent.SyncError(e))
//            Result.retry()
//        }
    }
}
