package com.mj.preventbullying.client.ui.login


import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import com.blackview.base.http.requestNoCheck
import com.mj.preventbullying.client.R

import com.mj.preventbullying.client.http.loginService
import com.mj.preventbullying.client.http.result.LoginResult
import com.mj.preventbullying.client.ui.login.LoginFormState
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseViewModel

class LoginViewModel() : BaseViewModel() {
    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(username: String, randomStr: String, code: String, password: String) {
        Logger.i("登录：$username,$randomStr,$code,$password")
        requestNoCheck({ loginService.login(username, randomStr, code, ps = password) }, {
            if (it.refresh_token != null){
                _loginResult.value = it
            }
            Logger.i("登录返回结果：$it")
        }, {
            Logger.i("报错：${it.message}")
        }
        )

    }


    fun loginDataChanged(username: String, password: String, code: String) {
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else if (code.isEmpty()) {
            _loginForm.value = LoginFormState(codeError = R.string.invalid_code)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return username.isNotEmpty()
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}