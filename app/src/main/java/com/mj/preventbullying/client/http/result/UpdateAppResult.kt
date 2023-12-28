package com.mj.preventbullying.client.http.result

data class UpdateAppResult(
    val code: Int,
    val `data`: AppData,
    val msg: Any,
    val success: Boolean

)

data class AppData(
    val fileId: String,
    val fileName: String,
    val fileUrl: String,
    val model: String,
    val releaseTime: String,
    val type: String,
    val versionId: String,
    val versionNo: String,
    val versionCode: Int,
    val fileSize: String,
    val releaseLog:String?
)