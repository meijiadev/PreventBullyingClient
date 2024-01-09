package com.mj.preventbullying.client.ui.dialog

import android.content.Context
import android.media.AudioManager
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.lifecycleScope
import com.hjq.toast.ToastUtils
import com.lxj.xpopup.core.CenterPopupView
import com.mj.preventbullying.client.app.MyApp
import com.mj.preventbullying.client.R
import com.mj.preventbullying.client.webrtc.CALLED_STATUS
import com.mj.preventbullying.client.webrtc.CALL_FAILURE
import com.mj.preventbullying.client.webrtc.CALL_HANG_UP
import com.mj.preventbullying.client.webrtc.RESTART_CALL
import com.orhanobut.logger.Logger
import kotlinx.coroutines.launch

/**
 * Create by MJ on 2023/12/11.
 * Describe :
 */

class MessageProcessDialog(context: Context) : CenterPopupView(context) {

    private val callLayout: LinearLayout by lazy { findViewById(R.id.call_ll) }          // 拨打语言的布局

    private val hangUpIv: AppCompatImageView by lazy { findViewById(R.id.hang_up_iv) }

    private val speakPhoneOnIv: AppCompatImageView by lazy { findViewById(R.id.speak_phone_on_iv) }

    private val loadingTv: AppCompatTextView by lazy { findViewById(R.id.loading_tv) }
    private val closeIv: AppCompatImageView by lazy { findViewById(R.id.close_iv) }

    private val callTimeTv: AppCompatTextView by lazy { findViewById(R.id.call_time_tv) }

    private val callStatueTv: AppCompatTextView by lazy { findViewById(R.id.call_statue_tv) }


    //private var messageDialogClick: MessageDialogClick? = null

    private var audioManager: AudioManager? = null
    private var isRestartCall = false
    private var toId: String? = null

    override fun getImplLayoutId(): Int = R.layout.dialog_message_process

    override
    fun onCreate() {
        super.onCreate()
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        hangUpIv.setOnClickListener {
            closeSpeaker()
            MyApp.timerViewModel.stopTimer()
            MyApp.webrtcSocketManager.sendHangUp()
            dismiss()
            //messageDialogClick?.callFinish()
        }
        speakPhoneOnIv.setOnClickListener {
            if (audioManager?.isSpeakerphoneOn == true) {
                closeSpeaker()
                speakPhoneOnIv.setImageResource(R.mipmap.loud_disable)
            } else {
                openSpeaker()
                speakPhoneOnIv.setImageResource(R.mipmap.loud_able)
            }
        }

        closeIv.setOnClickListener {
            MyApp.timerViewModel.stopTimer()
            MyApp.webrtcSocketManager.sendHangUp()
            dismiss()

        }



        MyApp.webrtcSocketManager.voiceCallEvent.observe(this) {
            when (it) {
                CALLED_STATUS -> {
                    lifecycleScope.launch {
                        if (!isRestartCall) {
                            openSpeaker()
                            callLayout.visibility = View.VISIBLE
                            loadingTv.visibility = View.GONE
                            closeIv.visibility = View.GONE
                            MyApp.timerViewModel.startTimer()
                        }
                        callStatueTv.text = "正在通话中..."
                    }
                }

                CALL_FAILURE -> {
                    dismiss()
                    ToastUtils.show("设备语音拨打失败")
                    MyApp.timerViewModel.stopTimer()
                }

                CALL_HANG_UP -> {
                    closeSpeaker()
                    dismiss()
                    MyApp.timerViewModel.stopTimer()
                }
                // 重新连接
                RESTART_CALL -> {
                    isRestartCall = true
                    callStatueTv.text = "网络有波动，正在重接中..."
                }

            }
        }
        MyApp.timerViewModel.getCurrentTime().observe(this) {
            callTimeTv.text = it
        }
    }

//    fun setClickListener(listener: MessageDialogClick): MessageProcessDialog = apply {
//        messageDialogClick = listener
//
//    }

    fun setToId(id: String?): MessageProcessDialog = apply {
        this.toId = id
    }

    fun openSpeaker() {
        audioManager?.isSpeakerphoneOn = true
    }

    fun closeSpeaker() {
        audioManager?.isSpeakerphoneOn = false
    }


    override fun dismiss() {
        super.dismiss()
        Logger.i("退出")
    }
//
//    interface MessageDialogClick {
//        fun toCall()
//        fun playWarnAudio()
//        fun ignore()
//
//        fun callFinish()
//
//    }

}