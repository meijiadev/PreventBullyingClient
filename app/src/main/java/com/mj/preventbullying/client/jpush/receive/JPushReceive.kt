package com.mj.preventbullying.client.jpush.receive

import android.content.Context
import android.content.Intent
import android.os.Bundle
import cn.jpush.android.api.CustomMessage
import cn.jpush.android.api.JPushInterface
import cn.jpush.android.api.NotificationMessage

import cn.jpush.android.service.JPushMessageService
import com.mj.preventbullying.client.ui.MainActivity
import com.orhanobut.logger.Logger

/**
 * Create by MJ on 2023/12/6.
 * Describe :推送消息广播接收
 */

class JPushReceive : JPushMessageService() {
    override fun onMessage(p0: Context?, p1: CustomMessage?) {
        Logger.i("message：$p1")
    }

    override fun onNotifyMessageOpened(p0: Context?, p1: NotificationMessage?) {
        Logger.i("点击通知----onNotifyMessageOpened:${p1}")
        kotlin.runCatching {
            val intent = Intent(p0, MainActivity::class.java)
            val bundle = Bundle()
            bundle.putString(JPushInterface.EXTRA_NOTIFICATION_TITLE, p1?.notificationTitle)
            bundle.putString(JPushInterface.EXTRA_ALERT, p1?.notificationContent)
            bundle.putString(JPushInterface.EXTRA_NOTIFICATION_ACTION_EXTRA,p1?.notificationExtras)
            intent.putExtras(bundle)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            p0?.startActivity(intent)
        }.onFailure {
            Logger.e("错误：${it.message}")
        }
    }

    override fun onMultiActionClicked(p0: Context?, p1: Intent?) {
        Logger.i("用户点击了通知栏按钮")
        val nActionExtra =
            p1?.getExtras()?.getString(JPushInterface.EXTRA_NOTIFICATION_ACTION_EXTRA)
        Logger.i("action:$nActionExtra")

    }

    override fun onNotifyMessageArrived(p0: Context?, p1: NotificationMessage?) {
        Logger.i("收到通知---onNotifyMessageArrived:$p1")
    }

    override fun onNotifyMessageDismiss(p0: Context?, p1: NotificationMessage?) {
        Logger.i("onNotifyMessageDismiss:$p1")

    }
}