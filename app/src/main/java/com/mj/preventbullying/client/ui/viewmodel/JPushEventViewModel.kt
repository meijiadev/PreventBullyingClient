package com.mj.preventbullying.client.ui.viewmodel

import cn.jpush.android.api.NotificationMessage
import com.kunminx.architecture.ui.callback.UnPeekLiveData
import com.sjb.base.base.BaseViewModel

/**
 * Create by MJ on 2023/12/27.
 * Describe : 推送消息
 */

class JPushEventViewModel : BaseViewModel() {

    var notifyMsgEvent = UnPeekLiveData<NotificationMessage>()
}