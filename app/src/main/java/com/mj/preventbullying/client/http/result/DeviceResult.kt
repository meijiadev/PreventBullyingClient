package com.mj.preventbullying.client.http.result

import com.google.gson.annotations.SerializedName

data class DeviceResult(
    val code: Int,
    @SerializedName("data")
    val `data`: DeviceData,
    val msg: Any,
    val success: Boolean
)


data class DeviceData(
    val countId: Any,
    val current: Int,
    val maxLimit: Any,
    val optimizeCountSql: Boolean,
    val orders: List<Order>,
    val pages: Int,
    val records: List<DeviceRecord>,
    val searchCount: Boolean,
    val size: Int,
    val total: Int
)

data class Order(
    val asc: Boolean,
    val column: String
)

data class Org(
    val id: String,
    val name: String
)

data class DeviceRecord(
    val description: String,
    val deviceId: String,
    val ip: Any,
    val lastUpdateTime: String,
    val location: String,
    val modelCode: String,
    val name: String,
    val org: Org,
    val snCode: String,
    val state: String
)