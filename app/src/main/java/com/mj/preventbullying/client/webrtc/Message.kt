package com.mj.preventbullying.client.webrtc


data class Message(
    val msgType: String,
    val sendFrom: String?,
    val sendTo: String?,
    val data: Any?,
    val tx: String?
)
