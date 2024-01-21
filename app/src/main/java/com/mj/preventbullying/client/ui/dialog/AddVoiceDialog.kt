package com.mj.preventbullying.client.ui.dialog

import android.content.Context
import com.hjq.shape.view.ShapeEditText
import com.hjq.shape.view.ShapeTextView
import com.hjq.toast.ToastUtils
import com.lxj.xpopup.core.CenterPopupView
import com.mj.preventbullying.client.R

/**
 * Create by MJ on 2024/1/21.
 * Describe :
 */

class AddVoiceDialog(context: Context) : CenterPopupView(context) {
    private val msgEt: ShapeEditText by lazy { findViewById(R.id.voice_text_et) }
    private val timesEt: ShapeEditText by lazy { findViewById(R.id.play_times_et) }
    private val cancelTv: ShapeTextView by lazy { findViewById(R.id.cancel_tv) }
    private val confirmTv: ShapeTextView by lazy { findViewById(R.id.confirm_tv) }

    override fun getImplLayoutId(): Int {
        return R.layout.dialog_add_voice
    }

    override fun onCreate() {
        super.onCreate()
        cancelTv.setOnClickListener {
            dismiss()
        }

        confirmTv.setOnClickListener {
            val msg = msgEt.text.toString().trim()
            val times = timesEt.text.toString().trim().toInt()
            if (msg.isEmpty()){
                ToastUtils.show("请先填写语音文案！")
                return@setOnClickListener
            }
            confirmListener?.invoke(msg, times)
            dismiss()
        }
    }

    private var confirmListener: ((msg: String, times: Int) -> Unit)? = null

    fun onConfirm(listener: ((msg: String, times: Int) -> Unit)): AddVoiceDialog = apply {
        this.confirmListener = listener
    }
}