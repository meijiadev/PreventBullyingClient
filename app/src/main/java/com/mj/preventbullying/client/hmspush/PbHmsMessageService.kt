package com.mj.preventbullying.client.hmspush

import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage
import com.orhanobut.logger.Logger

/**
 * Create by MJ on 2024/1/5.
 * Describe :
 */

class PbHmsMessageService : HmsMessageService() {


    override fun onMessageReceived(message: RemoteMessage?) {
        super.onMessageReceived(message)
        Logger.i("onMessageReceived is called")
        if (message == null) {
            Logger.i("收到的华为消息为空")
            return
        }
        Logger.i("华为消息：$message")
        // 获取消息内容
        Logger.i(
            """getData: ${message.data}        
        getFrom: ${message.from}        
        getTo: ${message.to}        
        getMessageId: ${message.messageId}
        getSentTime: ${message.sentTime}           
        getDataMap: ${message.dataOfMap}
        getMessageType: ${message.messageType}   
        getTtl: ${message.ttl}        
        getToken: ${message.token}""".trimIndent()
        )
    }
}