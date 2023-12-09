package com.mj.preventbullying.client.http.result

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