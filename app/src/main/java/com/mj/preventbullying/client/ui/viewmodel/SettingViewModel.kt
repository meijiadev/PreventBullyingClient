package com.mj.preventbullying.client.ui.viewmodel

import android.util.ArrayMap
import com.blackview.base.http.requestNoCheck
import com.mj.preventbullying.client.Constant
import com.mj.preventbullying.client.MyApp
import com.mj.preventbullying.client.http.apiService
import com.mj.preventbullying.client.tool.SpManager
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseViewModel

/**
 * Create by MJ on 2023/12/27.
 * Describe :
 */

class SettingViewModel : BaseViewModel() {

    /**
     * 修改密码
     */
    fun amendPassword(oldPassword: String, newPassword: String) {
        val username = SpManager.getString(Constant.ACCOUNT_KEY)
        val params = ArrayMap<Any, Any>()
        params["username"] = username
        params["oldPassword"] = oldPassword
        params["newPassword"] = newPassword
        Logger.i("username:$username,oldPs:$oldPassword,newPs:$newPassword")
        requestNoCheck({
            apiService.amendPs(params)
        }, {

        })
    }

}