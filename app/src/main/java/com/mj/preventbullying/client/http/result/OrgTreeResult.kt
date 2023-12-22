package com.mj.preventbullying.client.http.result

/**
 * 组织树返回值
 */
data class OrgTreeResult(
    val code: Int,
    val `data`: List<Children>,
    val msg: Any,
    val success: Boolean
)

data class Children(
    val children: List<Children>?,
    val id: String,
    val localFlag: Boolean,
    val name: String,
    val parentId: String,
    val type: String,
    val weight: Int
)

