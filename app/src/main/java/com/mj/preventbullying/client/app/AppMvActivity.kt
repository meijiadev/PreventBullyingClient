package com.mj.preventbullying.client.app

import androidx.viewbinding.ViewBinding
import com.jeremyliao.liveeventbus.LiveEventBus
import com.mj.preventbullying.client.Constant
import com.mj.preventbullying.client.jpush.NotifyMsgEvent
import com.mj.preventbullying.client.tool.AudioPlayer
import com.mj.preventbullying.client.tool.SpManager
import com.sjb.base.base.BaseMvActivity
import com.sjb.base.base.BaseViewModel

/**
 * Create by MJ on 2024/1/3.
 * Describe : 中间层基类
 */

abstract class AppMvActivity<V : ViewBinding, VM : BaseViewModel> : BaseMvActivity<V, VM>() {

    override fun onViewCreated() {
        super.onViewCreated()
        LiveEventBus.get<NotifyMsgEvent>(Constant.NOTIFY_MSG_EVENT_KEY).observe(this){
            //val alarm = SpManager.getString(Constant.ALARM_PLAY_NAME_KEY)
           // Constant.alarmAudioName = if (alarm.isNullOrEmpty()) Constant.alarmAudioName else alarm
            if (AudioPlayer.instance.isPlaying()) {
                AudioPlayer.instance.stop()
            }

            AudioPlayer.instance.playAssets(Constant.alarmAudioName)
        }
    }
}