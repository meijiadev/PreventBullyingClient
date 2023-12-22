package com.mj.preventbullying.client.ui.fragment

import com.blackview.base.http.requestNoCheck
import com.hjq.toast.ToastUtils
import com.kunminx.architecture.ui.callback.UnPeekLiveData
import com.mj.preventbullying.client.http.apiService
import com.mj.preventbullying.client.http.result.DeviceResult
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseViewModel

/**
 * Create by MJ on 2023/12/11.
 * Describe :
 */

class DeviceViewModel : BaseViewModel() {
    val deviceResultEvent = UnPeekLiveData<DeviceResult>()
    val deleteDevEvent = UnPeekLiveData<Boolean>()

    fun getAllDevices() {
        requestNoCheck({
            apiService.getAllDevices()
        }, {
            Logger.i("获取1-10页设备列表：$it")
            deviceResultEvent.postValue(it)
        })
    }

    fun deleteDev(deviceId: Long) {
        requestNoCheck({
            apiService.deleteDev(deviceId)
        }, {
            Logger.e("删除结果：${it.success}")
            ToastUtils.show("删除成功！")
            if (it.success) {
                deleteDevEvent.postValue(true)
            }

        })
    }
}