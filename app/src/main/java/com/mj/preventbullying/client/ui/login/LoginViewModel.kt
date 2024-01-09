package com.mj.preventbullying.client.ui.login


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.blackview.base.http.requestNoCheck
import com.mj.preventbullying.client.R
import com.mj.preventbullying.client.http.apiService

import com.mj.preventbullying.client.http.result.LoginResult
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseViewModel

class LoginViewModel() : BaseViewModel() {
    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(username: String, randomStr: String, code: String, password: String) {
        Logger.i("登录：$username,$randomStr,$code,$password")
        requestNoCheck({ apiService.login(username, randomStr, code, ps = password) }, {
            if (it.refresh_token != null){
                _loginResult.value = it
            }
            Logger.i("登录返回结果：$it")
        }, {
            Logger.i("报错：${it}")
        }
        )

    }

}