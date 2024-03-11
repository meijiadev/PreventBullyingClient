package com.mj.preventbullying.client.ui.viewmodel

import android.util.ArrayMap
import com.blackview.base.http.requestNoCheck
import com.kunminx.architecture.ui.callback.UnPeekLiveData
import com.mj.preventbullying.client.Constant
import com.mj.preventbullying.client.http.apiService
import com.mj.preventbullying.client.http.result.DeviceRecordResult
import com.mj.preventbullying.client.http.result.PreviewAudioResult
import com.mj.preventbullying.client.http.result.RecordProcessResult
import com.mj.preventbullying.client.ui.adapter.PENDING_STATUS
import com.mj.preventbullying.client.ui.dialog.MessageProcessDialog
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseViewModel

/**
 * Create by MJ on 2023/12/11.
 * Describe :
 */

class MessageViewModel : BaseViewModel() {
    val messageEvent = UnPeekLiveData<DeviceRecordResult>()
    val recordProcessedEvent = UnPeekLiveData<RecordProcessResult>()
    val getPreVieUrlEvent = UnPeekLiveData<PreviewAudioResult>()
    val rtcVideoUrlEvent = UnPeekLiveData<String>()

//    /**
//     * 获取所有设备的报警记录
//     */
//    fun getAllDeviceRecords(curPage: Int? = null) {
//        Logger.i("去获取所有设备的报警记录")
//        requestNoCheck({
//            apiService.getAllRecords(state = null)
//        }, {
//            Logger.d("获取设备报警记录：$it")
//            messageEvent.postValue(it)
//        })
//    }

    fun getAllDeviceRecords(curPage: Int, state: String? = null) {
        Logger.i("去获取设备的报警记录$curPage,state:$state")
        requestNoCheck({
            apiService.getAllRecords(current = curPage, state = state)
        }, {
            Logger.d("获取设备报警记录：$it")
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
            if (it.success) {
                getAllDeviceRecords(1, PENDING_STATUS)
            }
        })
    }


    /**
     * 获取播放的音频url
     */
    fun getAudioPreUrl(recordId: String) {
        requestNoCheck({
            apiService.getPreviewPcm(recordId = recordId)
        }, {
            Logger.i("获取预览得 音频地址：$it")
            getPreVieUrlEvent.postValue(it)
        },{

        },true,"加载中...")
    }

    fun getRtcVideoUrl(recordId: String) {
        requestNoCheck({
            apiService.getRtcVideoUrl(recordId)
        }, {
            Logger.i("获取绑定摄像头返回结果")
            if (it.success){
                val url = it.data.webrtcUrl
                rtcVideoUrlEvent.postValue(url)
                Constant.videoSnCOde = it.data.snCode
            }
        })
    }


}