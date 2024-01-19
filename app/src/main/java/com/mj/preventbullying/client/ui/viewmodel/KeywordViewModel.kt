package com.mj.preventbullying.client.ui.viewmodel

import com.blackview.base.http.requestNoCheck
import com.kunminx.architecture.ui.callback.UnPeekLiveData
import com.mj.preventbullying.client.http.apiService
import com.mj.preventbullying.client.http.result.KeywordResult
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseViewModel

/**
 * Create by MJ on 2024/1/17.
 * Describe : 关键词
 */

class KeywordViewModel : BaseViewModel() {
    var keywordEvent = UnPeekLiveData<KeywordResult>()

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
}