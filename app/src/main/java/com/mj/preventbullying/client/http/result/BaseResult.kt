package com.mj.preventbullying.client.http.result

data class BaseResult(
    val code: Int,
    val msg: String,
    val success: Boolean
)