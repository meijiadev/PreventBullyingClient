package com.mj.preventbullying.client.http.result

data class BaseResult(
    val code: Int,
    val `data`: Any,
    val msg: String,
    val success: Boolean
)