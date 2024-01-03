package com.mj.preventbullying.client.ui.viewmodel

import cn.jpush.android.api.NotificationMessage
import com.blackview.base.http.requestNoCheck
import com.kunminx.architecture.ui.callback.UnPeekLiveData
import com.mj.preventbullying.client.app.MyApp
import com.mj.preventbullying.client.http.apiService
import com.mj.preventbullying.client.http.result.UpdateAppResult
import com.sjb.base.base.BaseViewModel

/**
 * Create by MJ on 2023/12/27.
 * Describe : 推送消息
 */

class GlobalEventViewModel : BaseViewModel() {

    var notifyMsgEvent = UnPeekLiveData<NotificationMessage>()

    /**
     * App更新事件
     */
    var updateAppEvent = UnPeekLiveData<UpdateAppResult>()

    fun getAppVersion() {
        requestNoCheck({
            apiService.getNewApp()
        }, {
         //   Logger.i("获取的最新app版本信息：$it")
            MyApp.globalEventViewModel.updateAppEvent.postValue(it)
        })
    }
}