package com.mj.preventbullying.client.ui.login

/**
 * 登录表单的数据验证状态。
 */
data class LoginFormState(
    val usernameError: Int? = null,
    val passwordError: Int? = null,
    val codeError: Int? = null,
    val isDataValid: Boolean = false
)