package com.mj.preventbullying.client.ui.fragment

import com.blackview.base.http.requestNoCheck
import com.kunminx.architecture.ui.callback.UnPeekLiveData
import com.mj.preventbullying.client.http.apiService
import com.mj.preventbullying.client.http.result.DeviceRecordResult
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseViewModel

/**
 * Create by MJ on 2023/12/11.
 * Describe :
 */

class MessageViewModel : BaseViewModel() {
    val messageEvent = UnPeekLiveData<DeviceRecordResult>()

    /**
     * 获取所有设备的报警记录
     */
    fun getAllDeviceRecords() {
        requestNoCheck({
            apiService.getAllRecords()
        }, {
            Logger.i("获取设备报警记录：$it")
            messageEvent.postValue(it)
        })
    }
}