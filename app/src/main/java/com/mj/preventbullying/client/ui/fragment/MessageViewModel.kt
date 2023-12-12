package com.mj.preventbullying.client.ui.fragment

import android.util.ArrayMap
import com.blackview.base.http.requestNoCheck
import com.kunminx.architecture.ui.callback.UnPeekLiveData
import com.mj.preventbullying.client.http.apiService
import com.mj.preventbullying.client.http.result.DeviceRecordResult
import com.mj.preventbullying.client.http.result.RecordProcessResult
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseViewModel

/**
 * Create by MJ on 2023/12/11.
 * Describe :
 */

class MessageViewModel : BaseViewModel() {
    val messageEvent = UnPeekLiveData<DeviceRecordResult>()
    val recordProcessedEvent = UnPeekLiveData<RecordProcessResult>()

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

    /**
     * 更改消息状态
     */
    fun recordProcess(recordId: String, remark: String, state: String) {
        val params = ArrayMap<Any, Any>()
        params["recordId"] = recordId
        params["remark"] = remark
        params["state"] = state
        requestNoCheck({
            apiService.recordProcess(params)
        }, {
            Logger.i("更改记录状态:$it")
            if (it.success){
                getAllDeviceRecords()
            }
        })
    }


}