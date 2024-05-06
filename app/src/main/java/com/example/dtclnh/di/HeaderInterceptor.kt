package com.example.dtclnh.di

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class HeaderInterceptor : Interceptor {

    private var headers: Map<String, String> = emptyMap()

    fun setHeaders(headers: Map<String, String>) {
        this.headers = headers
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest: Request = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        // Thêm header vào request
        for ((key, value) in headers) {
            requestBuilder.addHeader(key, value)
        }

        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}
