package com.mj.preventbullying.client.http.service

import android.util.ArrayMap
import com.mj.preventbullying.client.Constant
import com.mj.preventbullying.client.SpManager
import com.mj.preventbullying.client.http.result.DeviceRecordResult
import com.mj.preventbullying.client.http.result.DeviceResult
import com.mj.preventbullying.client.http.result.LoginResult
import com.mj.preventbullying.client.http.result.RecordProcessResult
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query


/**
 * Create by MJ on 2023/12/2.
 * Describe : 网络请求接口
 */

interface ApiService {
    companion object {
        const val HTTP_URL = "http://192.168.1.6:9999"
    }

    @POST("/auth/oauth2/token")
    @FormUrlEncoded
    suspend fun login(
        @Query("username") name: String,
        @Query("randomStr") str: String,
        @Query("code") code: String,
        @Query("grant_type") grant_type: String = "password",
        @Query("scope") scope: String = "server",
        @Field("password") ps: String,
        @Header("Authorization") authorization: String = "Basic YXBwOmFwcA=="
    ): LoginResult


    @POST("/auth/oauth2/token")
    suspend fun refreshToken(
        @Query("grant_type") grant_type: String = "refresh_token",
        @Query("refresh_token") refresh_token: String,
        @Query("scope") scope: String = "server"
    )


    @GET("/anti-bullying/record/page")
    suspend fun getAllRecords(
        @Query("current") current: Int = 1,
        @Query("size") size: Int = 100,
        @Header("Authorization") authorization: String = "Bearer ${SpManager.getString(Constant.ACCESS_TOKEN_KEY)}"
    ): DeviceRecordResult


    @GET("/anti-bullying/device/page")
    suspend fun getAllDevices(
        @Query("current") current: Int = 1,
        @Query("size") size: Int = 100,
        @Header("Authorization") authorization: String = "Bearer ${SpManager.getString(Constant.ACCESS_TOKEN_KEY)}"
    ): DeviceResult

    @POST("/anti-bullying/record/process")
    suspend fun recordProcess(
        @Body params: ArrayMap<Any, Any>,
        @Header("Authorization") authorization: String = "Bearer ${SpManager.getString(Constant.ACCESS_TOKEN_KEY)}"
    ): RecordProcessResult


}