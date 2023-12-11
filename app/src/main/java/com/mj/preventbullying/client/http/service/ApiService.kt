package com.mj.preventbullying.client.http.service

import com.mj.preventbullying.client.Constant
import com.mj.preventbullying.client.http.result.DeviceRecordResult
import com.mj.preventbullying.client.http.result.DeviceResult
import com.mj.preventbullying.client.http.result.LoginResult
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
        @Header("Authorization") authorization: String = "Basic cGlnOnBpZw=="
    ): LoginResult


    @GET("/anti-bullying/record/page")
    suspend fun getAllRecords(
        @Query("current") current: Int = 1,
        @Query("size") size: Int = 10,
        @Header("Authorization") authorization: String = "Bearer ${Constant.accessToken}"
    ): DeviceRecordResult


    @GET("/anti-bullying/device/page")
    suspend fun getAllDevices(
        @Query("current") current: Int = 1,
        @Query("size") size: Int = 10,
        @Header("Authorization") authorization: String = "Bearer ${Constant.accessToken}"
    ): DeviceResult

}