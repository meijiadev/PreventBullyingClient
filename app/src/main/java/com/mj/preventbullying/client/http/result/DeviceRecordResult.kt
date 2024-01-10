package com.mj.preventbullying.client.http.result

import com.google.gson.annotations.SerializedName

data class DeviceRecordResult(
    val code: Int,
    @SerializedName("data")
    val `data`: RecordData?,
    val msg: Any,
    val success: Boolean
)


data class RecordData(
    val countId: Any,
    val current: Int,
    val maxLimit: Any,
    val optimizeCountSql: Boolean,
    val orders: List<Order>,
    val pages: Int,
    val records: List<Record>,
    val searchCount: Boolean,
    val size: Int,
    val total: Int
)


data class Record(
    val credibility: Int,
    val duration: String,
    val fileId: String,
    val keyword: String,
    val keywordId: String,
    val location: String,
    val org: Org,
    val recordId: String,
    val snCode: String,
    val state: String,
    val type: String,
    val waringTime: String,
    val volume: String?
)