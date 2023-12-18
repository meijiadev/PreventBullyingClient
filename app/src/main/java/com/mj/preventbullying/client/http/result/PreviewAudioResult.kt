package com.mj.preventbullying.client.http.result

/**
 * 预览pcm音频返回结果
 */
data class PreviewAudioResult(
    val code: Int,
    val `data`: Data?,
    val msg: Any,
    val success: Boolean
)

data class Data(
    val fileName: String,
    val url: String
)