package com.example.dtclnh.di

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class EndpointInterceptor : Interceptor {

    private var newEndpoint: String? = null

    fun setNewEndpoint(endpoint: String) {
        this.newEndpoint = endpoint
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest: Request = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        // Thay đổi endpoint của request
        newEndpoint?.let {
            requestBuilder.url(it)
        }

        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}
