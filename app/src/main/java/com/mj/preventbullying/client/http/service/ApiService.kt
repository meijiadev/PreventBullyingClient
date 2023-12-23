package com.mj.preventbullying.client.http.service

import android.util.ArrayMap
import com.mj.preventbullying.client.Constant
import com.mj.preventbullying.client.SpManager
import com.mj.preventbullying.client.http.result.BaseResult
import com.mj.preventbullying.client.http.result.DevTypeResult
import com.mj.preventbullying.client.http.result.DeviceRecordResult
import com.mj.preventbullying.client.http.result.DeviceResult
import com.mj.preventbullying.client.http.result.LoginResult
import com.mj.preventbullying.client.http.result.OrgTreeResult
import com.mj.preventbullying.client.http.result.PreviewAudioResult
import com.mj.preventbullying.client.http.result.RecordProcessResult
import com.mj.preventbullying.client.http.result.RefreshTokenResult
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query


/**
 * Create by MJ on 2023/12/2.
 * Describe : 网络请求接口
 */

interface ApiService {
    companion object {
        const val BASE_HTTP_URL = "http://cloud.zyq0407.com:8080/"
        const val DEV_HTTP_URL = "http://192.168.1.6:80/"
        const val API = "api/"
    }


    // 401 密码错误
    // 428 校验验证码错误
    // 424 令牌过期
    @POST("auth/oauth2/token")
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


    @POST("auth/oauth2/token")
    suspend fun refreshToken(
        @Query("grant_type") grant_type: String = "refresh_token",
        @Query("refresh_token") refresh_token: String? = SpManager.getString(Constant.FRESH_TOKEN_KEY),
        @Query("scope") scope: String = "server"
    ): RefreshTokenResult


    @GET("anti-bullying/record/page")
    suspend fun getAllRecords(
        @Query("current") current: Int = 1,
        @Query("size") size: Int = 100
        //@Header("Authorization") authorization: String = "Bearer ${SpManager.getString(Constant.ACCESS_TOKEN_KEY)}"
    ): DeviceRecordResult


    @GET("anti-bullying/device/page")
    suspend fun getAllDevices(
        @Query("current") current: Int = 1,
        @Query("size") size: Int = 100
        // @Header("Authorization") authorization: String = "Bearer ${SpManager.getString(Constant.ACCESS_TOKEN_KEY)}"
    ): DeviceResult

    @POST("anti-bullying/record/process")
    suspend fun recordProcess(
        @Body params: ArrayMap<Any, Any>
        //@Header("Authorization") authorization: String = "Bearer ${SpManager.getString(Constant.ACCESS_TOKEN_KEY)}"
    ): RecordProcessResult


    @GET("admin/sys-file/generatePreviewUrl")
    suspend fun getPreviewPcm(
        @Query("fileId") fileId: String
        //@Header("Authorization") authorization: String = "Bearer ${SpManager.getString(Constant.ACCESS_TOKEN_KEY)}"
    ): PreviewAudioResult


    //添加设备
    @POST("anti-bullying/device")
    suspend fun addDevice(
        @Body params: ArrayMap<Any, Any>
    ): BaseResult


    @PUT("anti-bullying/device")
    suspend fun amendDev(@Body params: ArrayMap<Any, Any>):BaseResult

    @HTTP(method = "DELETE", path = "anti-bullying/device/{deviceId}", hasBody = false)
    suspend fun deleteDev(@Path("deviceId") deviceId: Long): BaseResult

    @GET("admin/org/tree")
    suspend fun getOrgTree(): OrgTreeResult

    @GET("admin/dict/type/anti_bullying_device_model")
    suspend fun getDevType(): DevTypeResult


}