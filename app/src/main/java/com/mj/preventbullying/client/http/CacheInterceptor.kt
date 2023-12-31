package com.mj.preventbullying.client.http


import com.mj.preventbullying.client.NetworkUtil
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
/**
 * Created by yi on 2021/11/4.
 */
class CacheInterceptor(var day: Int = 7) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        if (NetworkUtil.isAvailable()) {
            request = request.newBuilder()
                .cacheControl(CacheControl.FORCE_CACHE)
                .build()
        }
        val response = chain.proceed(request)
        if (NetworkUtil.isAvailable()) {
            val maxAge = 60 * 60
            response.newBuilder()
                .removeHeader("Pragma")
                //.header("Cache-Control", "public, max-age=$maxAge")
                .header("Cache-Control", "no-cache")
                .build()
        } else {
            val maxStale = 60 * 60 * 24 * day // tolerate 4-weeks stale
            response.newBuilder()
                .removeHeader("Pragma")
                .header("Cache-Control", "public, only-if-cached, max-stale=$maxStale")
                //.header("Cache-Control", "max-age=2000000, no-cache")
                .build()
        }
        return response
    }
}