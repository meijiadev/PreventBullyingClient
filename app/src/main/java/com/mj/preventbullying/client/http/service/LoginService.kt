package com.mj.preventbullying.client.http.service

import com.mj.preventbullying.client.http.request.BaseResponse
import com.mj.preventbullying.client.http.result.LoginResult
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query


/**
 * Create by MJ on 2023/12/2.
 * Describe : 网络请求接口
 */

interface LoginService {
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
        @Field("password") ps:String,
        @Header("Authorization")authorization:String="Basic cGlnOnBpZw=="
    ):LoginResult




}