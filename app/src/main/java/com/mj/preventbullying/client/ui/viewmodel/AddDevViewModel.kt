package com.mj.preventbullying.client.ui.viewmodel

import android.util.ArrayMap
import com.blackview.base.http.requestNoCheck
import com.kunminx.architecture.ui.callback.UnPeekLiveData
import com.mj.preventbullying.client.http.apiService
import com.mj.preventbullying.client.http.result.DevTypeResult
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseViewModel

/**
 * Create by MJ on 2024/1/24.
 * Describe :
 */

class AddDevViewModel:BaseViewModel() {
    val addDevEvent = UnPeekLiveData<Boolean>()
    val amendDevEvent = UnPeekLiveData<Boolean>()
    var devTypeEvent = UnPeekLiveData<DevTypeResult>()
    fun addDev(
        sn: String,
        // name: String,
        orgID: Long,
        devType: String,
        location: String,
        des: String?
    ) {
        val params = ArrayMap<Any, Any>()
        params["name"] = "校园防欺凌设备"
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
            } else {
                Logger.i("设备添加失败！")
                addDevEvent.postValue(false)
            }
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
    fun amendDev(
        deviceId: String,
        sn: String,
        //  name: String,
        orgID: Long,
        devType: String,
        location: String,
        des: String?
    ) {
        val params = ArrayMap<Any, Any>()
        params["deviceId"] = deviceId
        params["snCode"] = sn
        params["name"] = "校园防欺凌设备"
        params["orgId"] = orgID
        params["location"] = location
        params["modelCode"] = devType
        params["description"] = des
        requestNoCheck({
            apiService.amendDev(params)
        }, {
            Logger.i("修改成功:${it.success}")
            if (it.success) {
                amendDevEvent.postValue(true)
            } else {
                amendDevEvent.postValue(false)
            }
        })
    }
}