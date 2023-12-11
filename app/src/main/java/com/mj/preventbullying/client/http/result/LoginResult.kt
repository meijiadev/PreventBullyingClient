package com.mj.preventbullying.client.http.result

data class LoginResult(
    val access_token: String?,
    val aud: List<String>,
    val clientId: String,
    val exp: String,
    val expires_in: String,
    val iat: String,
    val iss: String,
    val jti: String,
    val license: String,
    val nbf: String,
    val refresh_token: String?,
    val scope: List<String>,
    val sub: String,
    val token_type: String,
    val user_id: String,
    val user_info: UserInfo,
    val username: String,

    val code: Int,
    val msg: String,
    val data: Any,
    val success: String

)


class Attributes

data class Authority(
    val authority: String
)

data class UserInfo(
    val accountNonExpired: Boolean,
    val accountNonLocked: Boolean,
    val attributes: Attributes,
    val authorities: List<Authority>,
    val credentialsNonExpired: Boolean,
    val enabled: Boolean,
    val id: String,
    val name: String,
    val orgId: String,
    val password: Any,
    val phone: String,
    val username: String
)