package com.example.dtclnh.core

import android.util.Log
import androidx.constraintlayout.motion.utils.ViewState
import com.example.dtclnh.presentation.base.viewstate.BaseViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import retrofit2.Response
import java.io.IOException

typealias NetworkAPIInvoke<T> = suspend () -> Response<T>


suspend fun <T : Any> performSafeNetworkApiCall(
    messageInCaseOfError: String = "Network error",
    allowRetries: Boolean = true,
    numberOfRetries: Int = 2,
    networkApiCall: NetworkAPIInvoke<T>
): Flow<IOResults<T>> {
    var delayDuration = 1000L
    val delayFactor = 2

    return flow {
        val response = networkApiCall()
        if (response.isSuccessful) {
            response.body()?.let {
                emit(IOResults.OnSuccess(it))
            }
                ?: emit(IOResults.OnFailed(IOException("API call successful but empty response body")))
            return@flow
        }
        Log.e("AMBE1203", "2: ${response.toString()}")

        emit(
            IOResults.OnFailed(
                IOException(
                    "Error - ${
                        response.errorBody()
                            ?.string() ?: messageInCaseOfError
                    }"
                )
            )
        )
        return@flow
    }.catch { e ->
        Log.e("AMBE1203", "1: ${e.toString()}")

        emit(IOResults.OnFailed(IOException("Exception: ${e.message}")))
        return@catch
    }.retryWhen { cause, attempt ->
        if (!allowRetries || attempt > numberOfRetries || cause !is IOException) return@retryWhen false
        delay(delayDuration)
        delayDuration *= delayFactor
        return@retryWhen true
    }.flowOn(Dispatchers.IO)
}

suspend fun <T : Any> getViewStateFlowForNetworkCall(ioOperation: suspend () -> Flow<IOResults<T>>) =
    flow {
        emit(BaseViewState(isLoading = true, result = null, throwable = null))
        ioOperation().map {

            Log.e("AMBE1203", "getViewStateFlowForNetworkCall: $it")
            when (it) {
                is IOResults.OnSuccess -> BaseViewState(
                    result = it.data,
                    isLoading = false,
                    throwable = null
                )
                is IOResults.OnFailed -> BaseViewState(
                    throwable = it.error,
                    isLoading = false,
                    result = false
                )
            }
        }.collect {
            emit(it)
        }
        emit(BaseViewState(isLoading = false, result = null, throwable = null))
    }.flowOn(Dispatchers.IO)