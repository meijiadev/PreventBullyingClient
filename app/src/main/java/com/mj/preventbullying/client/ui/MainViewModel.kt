package com.mj.preventbullying.client.ui

import cn.jpush.android.cache.Sp
import com.blackview.base.http.requestNoCheck
import com.kunminx.architecture.ui.callback.UnPeekLiveData
import com.mj.preventbullying.client.Constant
import com.mj.preventbullying.client.SpManager
import com.mj.preventbullying.client.http.apiService
import com.mj.preventbullying.client.http.result.DeviceResult
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseViewModel

/**
 * Create by MJ on 2023/12/9.
 * Describe : 主页的viewModel
 */
class MainViewModel : BaseViewModel() {


    fun refreshToken() {
        requestNoCheck({
            apiService.refreshToken()
        }, {
            Logger.e("refresh token:$it")
            if (it.access_token != null) {
                SpManager.putString(Constant.ACCESS_TOKEN_KEY, it.access_token)
                SpManager.putString(Constant.FRESH_TOKEN_KEY, it.refresh_token)
                SpManager.putString(Constant.EXPIRES_TIME_KEY, it.expires_in)
                SpManager.putLong(Constant.LOGIN_OR_REFRESH_TIME_KEY, System.currentTimeMillis())
            }
        }, {
            Logger.e("refresh token error:${it.message}")
        })
    }

    /**
     * 获取组织列表
     */
    fun getOrgList() {
        requestNoCheck({
            apiService.getOrgTree()
        }, {
            Logger.i("组织树查询结果：$it")
        })
    }

    /**
     * 获取设备型号列表
     */
    fun getDevType() {
        requestNoCheck({
            apiService.getDevType()
        }, {
            Logger.i("获取设备型号列表：$it")
        })
    }

}