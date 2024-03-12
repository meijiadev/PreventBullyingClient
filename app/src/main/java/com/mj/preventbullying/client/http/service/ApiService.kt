package com.mj.preventbullying.client.http.service

import android.util.ArrayMap
import com.mj.preventbullying.client.Constant
import com.mj.preventbullying.client.app.MyApp
import com.mj.preventbullying.client.tool.SpManager
import com.mj.preventbullying.client.http.result.BaseResult
import com.mj.preventbullying.client.http.result.DevTypeResult
import com.mj.preventbullying.client.http.result.DeviceRecordResult
import com.mj.preventbullying.client.http.result.DeviceResult
import com.mj.preventbullying.client.http.result.KeywordResult
import com.mj.preventbullying.client.http.result.LoginResult
import com.mj.preventbullying.client.http.result.OrgTreeResult
import com.mj.preventbullying.client.http.result.PreviewAudioResult
import com.mj.preventbullying.client.http.result.RecordProcessResult
import com.mj.preventbullying.client.http.result.RefreshTokenResult
import com.mj.preventbullying.client.http.result.UpdateAppResult
import com.mj.preventbullying.client.http.result.VideoUrlResult
import com.mj.preventbullying.client.http.result.VoiceResult
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
        private const val BASE_TEST_HTTP_URL = "https://cloud.hdvsiot.com:2443/"       // 测试环境

        //private const val BASE_HTTP_URL = "https://spad-cloud.hdvsiot.com/"             // 演示环境
        private const val Base_COBWEB_URL = "https://spad-cloud.cobwebiot.com/"
        private const val BASE_HTTP_URL_ZYQ = "http://cloud.zyq0407.com:8080/"       // 开发环境
        private const val PRE_BASE_HTTP_URL_ZYQ = "http://192.168.1.6:9999/"       // 开发环境

        const val API = "api/"
        const val policy_file_url = "https://cloud.hdvsiot.com:2443/app/files/privacy_policy.html"

        //const val API = ""
        private var isDevVersion = true
        private var isTestVersion = true   // 是否是测试环境
        fun getHostUrl(): String {
            //  isDevVersion = SpManager.getBoolean(Constant.SERVICE_URL_KEY, true)
            // return PRE_BASE_HTTP_URL_ZYQ
            return if (isTestVersion) {
                BASE_TEST_HTTP_URL
            } else {
                Base_COBWEB_URL
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
        @Query("size") size: Int = 20,
        @Query("state") state: String?,
        @Query("orgId") orgId: Long? = MyApp.globalEventViewModel.getSchoolId()?.toLong()
    ): DeviceRecordResult

    @GET("anti-bullying/keyword/page")
    suspend fun getKeywordList(
        @Query("current") current: Int = 1,
        @Query("size") size: Int = 200,
        @Query("orgId") orgId: Long? = MyApp.globalEventViewModel.getSchoolId()?.toLong()
    ): KeywordResult

    /**
     * 获取设备信息
     * @param current 获取的当前页数
     * @param size 每页的数据条数
     */
    @GET("anti-bullying/device/page")
    suspend fun getAllDevices(
        @Query("current") current: Int = 1,
        @Query("size") size: Int = 100,
        @Query("orgId") orgId: Long? = MyApp.globalEventViewModel.getSchoolId()?.toLong()
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


    @POST("anti-bullying//record/{recordId}/voice/preview")
    suspend fun getPreviewPcm(
        @Path("recordId") recordId: String,
        @Query("download") download: Boolean = false
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


    @PUT("anti-bullying/device/upgrades/{deviceId}")
    suspend fun upgradeDevice(@Path("deviceId") deviceId: Long): BaseResult

    /**
     * 获取语言播报文案的列表
     */
    @GET("anti-bullying/voice/page")
    suspend fun getVoiceList(
        @Query("current") current: Int = 1,
        @Query("size") size: Int = 200,
        @Query("orgId") orgId: Long? = MyApp.globalEventViewModel.getSchoolId()?.toLong()
    ): VoiceResult

    /**
     * 新增关键字
     */
    @POST("anti-bullying/keyword")
    suspend fun addKeyword(
        @Body params: ArrayMap<Any, Any>
    ): BaseResult

    @POST("anti-bullying/voice")
    suspend fun addVoice(
        @Body params: ArrayMap<Any, Any>
    ): BaseResult

    @PUT("anti-bullying/keyword/{keywordId}/")
    suspend fun enableKeyword(
        @Query("enabled") enabled: Boolean,
        @Path("keywordId") keywordId: Long
    ): BaseResult


    @PUT("anti-bullying/keyword")
    suspend fun editKeyword(
        @Body params: ArrayMap<Any, Any>
    ): BaseResult

    @DELETE("anti-bullying/keyword/{keywordId}")
    suspend fun deleteKeyword(
        @Path("keywordId") keywordId: Long
    ): BaseResult

    /**
     * 拨打设备
     */
    @POST("anti-bullying/record/talkback")
    suspend fun callDevice(
        @Body params: ArrayMap<Any, Any>
    ): BaseResult


    @GET("anti-bullying/record/{recordId}/streamUrl")
    suspend fun getRtcVideoUrl(@Path("recordId") recordId: String): VideoUrlResult

    @GET("anti-bullying/camera/{sn}/streamHeartbeat")
    suspend fun streamHeartbeat(@Path("sn") sn: String)
}