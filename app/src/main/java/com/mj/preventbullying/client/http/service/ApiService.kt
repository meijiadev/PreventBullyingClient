package com.mj.preventbullying.client.http.service

import android.util.ArrayMap
import com.mj.preventbullying.client.Constant
import com.mj.preventbullying.client.tool.SpManager
import com.mj.preventbullying.client.http.result.BaseResult
import com.mj.preventbullying.client.http.result.DevTypeResult
import com.mj.preventbullying.client.http.result.DeviceRecordResult
import com.mj.preventbullying.client.http.result.DeviceResult
import com.mj.preventbullying.client.http.result.LoginResult
import com.mj.preventbullying.client.http.result.OrgTreeResult
import com.mj.preventbullying.client.http.result.PreviewAudioResult
import com.mj.preventbullying.client.http.result.RecordProcessResult
import com.mj.preventbullying.client.http.result.RefreshTokenResult
import com.mj.preventbullying.client.http.result.UpdateAppResult
import retrofit2.http.Body
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
        private const val BASE_HTTP_URL = "http://cloud.hdvsiot.com:8080/"
        private const val DEV_HTTP_URL = "http://192.168.1.6:80/"
        const val API = "api/"
        var isDevVersion = false
        fun getHostUrl(): String {
            //  isDevVersion = SpManager.getBoolean(Constant.SERVICE_URL_KEY, true)
            return if (isDevVersion) {
                DEV_HTTP_URL
            } else {
                BASE_HTTP_URL
            }
        }
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

    // /user/password

    /**
     * 修改密码
     */
    @PUT("admin/user/password")
    suspend fun amendPs(@Body params: ArrayMap<Any, Any>): BaseResult


    /**
     * 刷新token
     */
    @POST("auth/oauth2/token")
    suspend fun refreshToken(
        @Query("grant_type") grant_type: String = "refresh_token",
        @Query("refresh_token") refresh_token: String? = SpManager.getString(Constant.FRESH_TOKEN_KEY),
        @Query("scope") scope: String = "server"
    ): RefreshTokenResult


    /**
     * 获取 1-100条设备记录
     */
    @GET("anti-bullying/record/page")
    suspend fun getAllRecords(
        @Query("current") current: Int = 1,
        @Query("size") size: Int = 20
        //@Header("Authorization") authorization: String = "Bearer ${SpManager.getString(Constant.ACCESS_TOKEN_KEY)}"
    ): DeviceRecordResult


    /**
     * 获取设备信息
     * @param current 获取的当前页数
     * @param size 每页的数据条数
     */
    @GET("anti-bullying/device/page")
    suspend fun getAllDevices(
        @Query("current") current: Int = 1,
        @Query("size") size: Int = 100
        // @Header("Authorization") authorization: String = "Bearer ${SpManager.getString(Constant.ACCESS_TOKEN_KEY)}"
    ): DeviceResult

    /**
     * 处理设备记录
     */
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
    suspend fun amendDev(@Body params: ArrayMap<Any, Any>): BaseResult

    @HTTP(method = "DELETE", path = "anti-bullying/device/{deviceId}", hasBody = false)
    suspend fun deleteDev(@Path("deviceId") deviceId: Long): BaseResult

    @GET("admin/org/tree")
    suspend fun getOrgTree(): OrgTreeResult

    @GET("admin/dict/type/anti_bullying_device_model")
    suspend fun getDevType(): DevTypeResult

    /**
     * 获取最新版本的app
     */
    @GET("anti-bullying/version/latest")
    suspend fun getNewApp(
        @Query("type") type: String = "mobile",
        @Query("model") model: String = "android"
    ): UpdateAppResult
}