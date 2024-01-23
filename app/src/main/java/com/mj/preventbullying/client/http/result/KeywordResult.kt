package com.mj.preventbullying.client.http.result

data class KeywordResult(
    val code: Int,
    val `data`: KData,
    val msg: Any,
    val success: Boolean
)

data class KData(
    val countId: Any,
    val current: Int,
    val maxLimit: Any,
    val optimizeCountSql: Boolean,
    val orders: List<Any>,
    val pages: Int,
    val records: List<KRecord>,
    val searchCount: Boolean,
    val size: Int,
    val total: Int
)

data class KRecord(
    val credibility: Int,
    val enabled: Boolean,
    val keyword: String,
    val keywordId: String,
    val lastUpdateTime: String?,
    val level: Int,
    val matchType: String,
    val org: Org,
    val voice: Voice
)

data class Voice(
    val id: String,
    val name: String
)