package com.mj.preventbullying.client.ui.viewmodel

import android.util.ArrayMap
import com.blackview.base.http.requestNoCheck
import com.hjq.toast.ToastUtils
import com.kunminx.architecture.ui.callback.UnPeekLiveData
import com.mj.preventbullying.client.app.MyApp
import com.mj.preventbullying.client.http.apiService
import com.mj.preventbullying.client.http.result.KRecord
import com.mj.preventbullying.client.http.result.KeywordResult
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseViewModel

/**
 * Create by MJ on 2024/1/17.
 * Describe : 关键词
 */

class KeywordViewModel : BaseViewModel() {
    var keywordEvent = UnPeekLiveData<KeywordResult>()
    var enableKeywordEvent = UnPeekLiveData<Boolean>()
    var deleteKeywordEvent = UnPeekLiveData<Boolean>()

    fun getKeywords() {
        requestNoCheck({
            apiService.getKeywordList()
        }, {
            Logger.i("获取关键词列表：$it")
            if (it.success) {
                keywordEvent.postValue(it)
            } else {
                Logger.e("关键字列表获取失败")
            }
        })
    }

    fun enableKeyword(kRecord: KRecord, enable: Boolean) {
        val params = ArrayMap<Any, Any>()
        params["keywordId"] = kRecord.keywordId
        params["keyword"] = kRecord.keyword
        params["enabled"] = enable
        params["matchType"] = kRecord.matchType
        params["credibility"] = kRecord.credibility
        params["voiceId"] = kRecord.voice.id
        params["orgId"] = MyApp.globalEventViewModel.getSchoolId() ?: 0
        requestNoCheck({
            // apiService.enableKeyword(keywordId, enable)
            apiService.editKeyword(params)
        }, {
            enableKeywordEvent.postValue(it.success)
        }, {
            enableKeywordEvent.postValue(false)
        }, true)
    }

    fun deleteKeyword(keywordId: Long) {
        requestNoCheck({
            apiService.deleteKeyword(keywordId)
        }, {
            deleteKeywordEvent.postValue(it.success)
        }, {
            deleteKeywordEvent.postValue(false)
        }, true)
    }
}