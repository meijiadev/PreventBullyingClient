package com.mj.preventbullying.client.http.result

data class VoiceResult(
    val code: Int,
    val `data`: VData,
    val msg: Any,
    val success: Boolean
)
data class VData(
    val countId: Any,
    val current: Int,
    val maxLimit: Any,
    val optimizeCountSql: Boolean,
    val orders: List<Any>,
    val pages: Int,
    val records: List<VRecord>,
    val searchCount: Boolean,
    val size: Int,
    val total: Int
)

data class VRecord(
    val defaultFlag: Boolean,
    val lastUpdateTime: String,
    val org: Org,
    val text: String,
    val times: Int,
    val voiceId: String
)