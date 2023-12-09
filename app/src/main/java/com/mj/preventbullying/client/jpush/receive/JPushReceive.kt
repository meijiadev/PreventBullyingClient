package com.mj.preventbullying.client.jpush.receive

import android.content.Context
import cn.jpush.android.api.CustomMessage
import cn.jpush.android.service.JPushMessageReceiver
import com.orhanobut.logger.Logger

/**
 * Create by MJ on 2023/12/6.
 * Describe :推送消息广播接收
 */

class JPushReceive : JPushMessageReceiver() {
    override fun onMessage(p0: Context?, p1: CustomMessage?) {
        super.onMessage(p0, p1)
        Logger.i("message：$p1")
    }
}