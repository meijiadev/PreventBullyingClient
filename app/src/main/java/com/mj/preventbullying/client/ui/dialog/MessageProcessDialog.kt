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
import com.mj.preventbullying.client.MyApp
import com.mj.preventbullying.client.R
import com.mj.preventbullying.client.webrtc.CALLED_STATUS
import com.mj.preventbullying.client.webrtc.CALL_FAILURE
import com.mj.preventbullying.client.webrtc.CALL_HANG_UP
import com.orhanobut.logger.Logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

/**
 * Create by MJ on 2023/12/11.
 * Describe :
 */

class MessageProcessDialog(context: Context) : CenterPopupView(context) {
    private val call: AppCompatTextView by lazy { findViewById(R.id.go_call_tv) }
    private val play: AppCompatTextView by lazy { findViewById(R.id.play_waring_tv) }
    private val ignore: AppCompatTextView by lazy { findViewById(R.id.ignore_tv) }
    private val cancel: AppCompatTextView by lazy { findViewById(R.id.cancel_tv) }
    private val callLayout: LinearLayout by lazy { findViewById(R.id.call_ll) }          // 拨打语言的布局
    private val actionLayout: LinearLayout by lazy { findViewById(R.id.action_ll) }      // 功能列表布局

    private val hangUpIv: AppCompatImageView by lazy { findViewById(R.id.hang_up_iv) }

    private val speakPhoneOnIv: AppCompatImageView by lazy { findViewById(R.id.speak_phone_on_iv) }

    private val loadingTv: AppCompatTextView by lazy { findViewById(R.id.loading_tv) }
    private val closeIv: AppCompatImageView by lazy { findViewById(R.id.close_iv) }

    private val callTimeTv: AppCompatTextView by lazy { findViewById(R.id.call_time_tv) }


    private var messageDialogClick: MessageDialogClick? = null

    private var audioManager: AudioManager? = null

    override fun getImplLayoutId(): Int = R.layout.dialog_message_process

    override
    fun onCreate() {
        super.onCreate()
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        call.setOnClickListener {
            MyApp.timerViewModel.stopTimer()
            Logger.i("拨打设备语音")
            messageDialogClick?.toCall()
            loadingTv.visibility = View.VISIBLE
            closeIv.visibility = View.VISIBLE
            actionLayout.visibility = View.GONE
        }

        play.setOnClickListener {
            Logger.i("播放报警的语音")
            messageDialogClick?.playWarnAudio()
            // ToastUtils.show("暂未开放此功能")
            dismiss()

        }

        ignore.setOnClickListener {
            messageDialogClick?.ignore()
            dismiss()
        }

        cancel.setOnClickListener {
            dismiss()
        }


        hangUpIv.setOnClickListener {
            closeSpeaker()
            MyApp.timerViewModel.stopTimer()
            MyApp.socketEventViewModel.sendHangUp()
            dismiss()
            messageDialogClick?.callFinish()
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
            MyApp.socketEventViewModel.sendHangUp()
            dismiss()

        }



        MyApp.socketEventViewModel.voiceCallEvent.observe(this) {
            when (it) {
                CALLED_STATUS -> {
                    lifecycleScope.launch {
                        openSpeaker()
                        callLayout.visibility = View.VISIBLE
                        actionLayout.visibility = View.GONE
                        loadingTv.visibility = View.GONE
                        closeIv.visibility = View.GONE
                        MyApp.timerViewModel.startTimer()
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

            }
        }
        MyApp.timerViewModel.getCurrentTime().observe(this) {
            callTimeTv.text = it
        }
    }

    fun setClickListener(listener: MessageDialogClick): MessageProcessDialog = apply {
        messageDialogClick = listener

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

    interface MessageDialogClick {
        fun toCall()
        fun playWarnAudio()
        fun ignore()

        fun callFinish()

    }
}