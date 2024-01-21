package com.mj.preventbullying.client.ui.viewmodel

import android.util.ArrayMap
import com.blackview.base.http.requestNoCheck
import com.kunminx.architecture.ui.callback.UnPeekLiveData
import com.mj.preventbullying.client.app.MyApp
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
    var addKeywordEvent = UnPeekLiveData<Boolean>()
    var addVoiceEvent = UnPeekLiveData<Boolean>()

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

    fun addKeyword(
        keyword: String,
        enabled: Boolean,
        credibility: Int,
        voiceId: Long?,
        level: Int
    ) {
        val params = ArrayMap<Any, Any>()
        params["keyword"] = keyword
        params["enabled"] = enabled
        params["credibility"] = credibility
        params["voiceId"] = voiceId
        params["matchType"] = "contains"
        params["orgId"] = MyApp.globalEventViewModel.getSchoolId()
        params["level"] = level
        requestNoCheck({
            apiService.addKeyword(params)
        }, {
            addKeywordEvent.postValue(it.success)

        })
    }

    fun addVoice(
        msg: String,
        times: Int,
    ) {
        val params = ArrayMap<Any, Any>()
        params["text"] = msg
        params["times"] = times
        params["orgId"] = MyApp.globalEventViewModel.getSchoolId()
        params["defaultFlag"] = false
        requestNoCheck({
            apiService.addVoice(params)
        }, {
            addVoiceEvent.postValue(it.success)
        }, {
            addVoiceEvent.postValue(false)
        }, true, "添加中...")
    }
}