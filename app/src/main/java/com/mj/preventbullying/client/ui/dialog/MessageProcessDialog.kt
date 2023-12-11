package com.mj.preventbullying.client.ui.dialog

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.lifecycleScope
import com.lxj.xpopup.core.CenterPopupView
import com.mj.preventbullying.client.MyApp
import com.mj.preventbullying.client.R
import com.mj.preventbullying.client.webrtc.CALLED_STATUS
import com.orhanobut.logger.Logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    private val loadingTv: AppCompatTextView by lazy { findViewById(R.id.loading_tv) }


    private var messageDialogClick: MessageDialogClick? = null
    override fun getImplLayoutId(): Int = R.layout.dialog_message_process

    override
    fun onCreate() {
        super.onCreate()

        call.setOnClickListener {
            Logger.i("拨打设备语音")
            messageDialogClick?.toCall()
            loadingTv.visibility = View.VISIBLE
            actionLayout.visibility = View.GONE

        }

        play.setOnClickListener {
            Logger.i("播放报警的语音")
            messageDialogClick?.playWarnAudio()

        }

        ignore.setOnClickListener {
            messageDialogClick?.ignore()
        }

        cancel.setOnClickListener {
            dismiss()
        }

        hangUpIv.setOnClickListener {
            MyApp.socketEventViewModel.sendHangUp()
        }



        MyApp.socketEventViewModel.voiceCallEvent.observe(this) {
            when (it) {
                CALLED_STATUS -> {
                    lifecycleScope.launch {
                        delay(1000)
                        callLayout.visibility = View.VISIBLE
                        actionLayout.visibility = View.GONE
                        loadingTv.visibility = View.GONE
                    }
                }
            }
        }
    }

    fun setClickListener(listener: MessageDialogClick): MessageProcessDialog = apply {
        messageDialogClick = listener

    }


    interface MessageDialogClick {
        fun toCall()
        fun playWarnAudio()
        fun ignore()

    }
}