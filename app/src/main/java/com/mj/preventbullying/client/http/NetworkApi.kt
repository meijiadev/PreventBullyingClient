package com.mj.preventbullying.client.http


import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.google.gson.GsonBuilder
import com.mj.preventbullying.client.Constant
import com.mj.preventbullying.client.MyApp
import com.mj.preventbullying.client.SpManager
import com.mj.preventbullying.client.http.service.ApiService
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit


//双重校验锁式-单例 封装NetApiService 方便直接快速调用简单的接口
val apiService: ApiService by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED){
    NetworkApi.INSTANCE.getApi(ApiService::class.java, ApiService.HTTP_URL)
}
class NetworkApi : BaseNetworkApi() {

    companion object {
        val INSTANCE: NetworkApi by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            NetworkApi()
        }
    }

    /**
     * 实现重写父类的setHttpClientBuilder方法，
     * 在这里可以添加拦截器，可以对 OkHttpClient.Builder 做任意操作
     */
    override fun setHttpClientBuilder(builder: OkHttpClient.Builder): OkHttpClient.Builder {
        builder.apply {
            //设置缓存配置 缓存最大10M
            cache(Cache(File(MyApp.context.cacheDir, "cache"), 10 * 1024 * 1024))
            //添加Cookies自动持久化
            cookieJar(cookieJar)
            //示例：添加公共heads 注意要设置在日志拦截器之前，不然Log中会不显示head信息
            addInterceptor(MyHeadInterceptor())
            //添加缓存拦截器 可传入缓存天数，不传默认7天
           // addInterceptor(CacheInterceptor())
            // 日志拦截器
            addInterceptor(LoggerInterceptor())
            //超时时间 连接、读、写
            connectTimeout(15, TimeUnit.SECONDS)
            readTimeout(10, TimeUnit.SECONDS)
            writeTimeout(10, TimeUnit.SECONDS)
        }
        return builder

    }

    /**
     * 实现重写父类的setRetrofitBuilder方法，
     * 在这里可以对Retrofit.Builder做任意操作，比如添加GSON解析器，protobuf等
     */
    override fun setRetrofitBuilder(builder: Retrofit.Builder): Retrofit.Builder {
        return builder.apply {
            addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
        }
    }

    private val cookieJar: PersistentCookieJar by lazy {
        PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(MyApp.context))
    }

    /**
     * 自定义头部参数拦截器，传入heads
     */
    class MyHeadInterceptor : Interceptor {

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val builder = chain.request().newBuilder()
            builder.addHeader("key", "Accept-Language").build()
            builder.addHeader("value", "zh-TW").build()
            builder.addHeader("type", "text").build()
            if (SpManager.getString(Constant.ACCESS_TOKEN_KEY)?.isNotEmpty() == true) {
                builder.addHeader("Authorization", "Bearer ${SpManager.getString(Constant.ACCESS_TOKEN_KEY)}")
            }
            return chain.proceed(builder.build())
        }

    }


}



