package com.mj.preventbullying.client.http.result

data class DevTypeResult(
    val code: Int,
    val `data`: List<Type>,
    val msg: Any,
    val success: Boolean
)

data class Type(
    val createBy: String,
    val createTime: String,
    val delFlag: String,
    val description: String,
    val dictId: String,
    val dictType: String,
    val id: String,
    val label: String,
    val remarks: String,
    val sortOrder: Int,
    val updateBy: String,
    val updateTime: Any,
    val value: String
)