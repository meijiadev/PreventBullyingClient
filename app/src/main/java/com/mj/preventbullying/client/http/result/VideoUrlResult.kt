package com.mj.preventbullying.client.http.result

data class VideoUrlResult(
    val code: Int,
    val `data`: VideoUrl,
    val msg: Any,
    val success: Boolean
)
data class VideoUrl(
    val flvUrl: Any,
    val hlsUrl: Any,
    val snCode: String,
    val webrtcUrl: String
)