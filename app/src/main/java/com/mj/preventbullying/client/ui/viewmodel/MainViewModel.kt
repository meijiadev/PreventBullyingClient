package com.mj.preventbullying.client.ui.viewmodel

import android.util.ArrayMap
import com.blackview.base.http.requestNoCheck
import com.kunminx.architecture.ui.callback.UnPeekLiveData
import com.mj.preventbullying.client.Constant
import com.mj.preventbullying.client.tool.SpManager
import com.mj.preventbullying.client.http.apiService
import com.mj.preventbullying.client.http.result.DevTypeResult
import com.mj.preventbullying.client.http.result.OrgTreeResult
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseViewModel

/**
 * Create by MJ on 2023/12/9.
 * Describe : 主页的viewModel
 */
class MainViewModel : BaseViewModel() {
    var orgTreeEvent = UnPeekLiveData<OrgTreeResult>()
    var devTypeEvent = UnPeekLiveData<DevTypeResult>()
    val addDevEvent = UnPeekLiveData<Boolean>()
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
            orgTreeEvent.postValue(it)
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
            devTypeEvent.postValue(it)
        })
    }

    fun addDev(
        sn: String,
        name: String,
        orgID: Long,
        devType: String,
        location: String,
        des: String?
    ) {
        val params = ArrayMap<Any, Any>()
        params["name"] = name
        params["snCode"] = sn
        params["orgId"] = orgID
        params["location"] = location
        params["modelCode"] = devType
        params["description"] = des
        requestNoCheck({
            apiService.addDevice(params)
        }, {
            if (it.success) {
                Logger.i("添加设备成功！")
                addDevEvent.postValue(true)
            }
        })
    }

    fun getAppVersion(){
        requestNoCheck({
            apiService.getNewApp()
        },{

        })
    }

}