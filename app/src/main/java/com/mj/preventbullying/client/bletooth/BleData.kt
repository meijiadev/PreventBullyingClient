package com.mj.preventbullying.client.bletooth

/**
 * Create by MJ on 2024/1/15.
 * Describe :
 */

data class BleData(
    val status: Int,
    val url: String,
    val snCode: String? = null
)                    // status:1 配网中   status:2 配网结束
