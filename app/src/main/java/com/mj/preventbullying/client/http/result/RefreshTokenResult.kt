package com.mj.preventbullying.client.http.result

/**
 * 刷新token返回值
 */
data class RefreshTokenResult(
    val access_token: String?,
    val expires_in: String?,
    val refresh_token: String?,
    val scope: String?,
    val token_type: String?
)