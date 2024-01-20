package com.mj.preventbullying.client.ui.viewmodel

import com.blackview.base.http.requestNoCheck
import com.kunminx.architecture.ui.callback.UnPeekLiveData
import com.mj.preventbullying.client.http.apiService
import com.mj.preventbullying.client.http.result.VRecord
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseViewModel

/**
 * Create by MJ on 2024/1/20.
 * Describe :
 */

class AddViewModel : BaseViewModel() {
    var voiceListEvent = UnPeekLiveData<List<VRecord>>()

    /**
     * 获取语音文案的列表
     */
    fun getVoiceList() {
        requestNoCheck(
            {
                apiService.getVoiceList()
            }, {
                Logger.i("获取语音播报的列表：$it")
                voiceListEvent.postValue(it.data.records)
            }
        )
    }
}