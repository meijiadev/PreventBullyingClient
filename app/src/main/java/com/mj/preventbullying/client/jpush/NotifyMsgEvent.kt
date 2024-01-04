package com.mj.preventbullying.client.jpush

import com.jeremyliao.liveeventbus.core.LiveEvent

/**
 * Create by MJ on 2024/1/4.
 * Describe : 推送
 */

class NotifyMsgEvent : LiveEvent {
    var notificationId: Int? = null

    constructor(notificationId: Int?) {
        this.notificationId = notificationId
    }
}