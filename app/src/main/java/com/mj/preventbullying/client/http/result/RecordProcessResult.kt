package com.mj.preventbullying.client.http.result

data class RecordProcessResult(
    val code: Int,
    val `data`: Boolean,
    val msg: Any,
    val success: Boolean
)