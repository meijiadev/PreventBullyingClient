package com.mj.preventbullying.client.ui.dialog

import android.content.Context
import androidx.appcompat.widget.AppCompatTextView
import com.hjq.shape.view.ShapeEditText
import com.hjq.shape.view.ShapeTextView
import com.hjq.toast.ToastUtils
import com.lxj.xpopup.core.CenterPopupView
import com.mj.preventbullying.client.R
import com.mj.preventbullying.client.ui.adapter.PROCESSED_IGNORE
import com.mj.preventbullying.client.ui.adapter.PROCESSED_STATUS

/**
 * Create by MJ on 2024/1/2.
 * Describe : 输入框，处理手段
 */

class InputMsgDialog(context: Context) : CenterPopupView(context) {
    private val cancelTv: ShapeTextView by lazy { findViewById(R.id.cancel_tv) }
    private val ignoreTV: ShapeTextView by lazy { findViewById(R.id.ignore_tv) }
    private val processTv: ShapeTextView by lazy { findViewById(R.id.process_tv) }
    private val remarkTv: ShapeEditText by lazy { findViewById(R.id.remark_tv) }

    override fun getImplLayoutId(): Int = R.layout.dialog_input

    override fun onCreate() {
        super.onCreate()
        cancelTv.setOnClickListener {
            dismiss()
        }

        processTv.setOnClickListener {
            val msg = remarkTv.text.toString()
            if (msg.isEmpty()) {
                ToastUtils.show("请填写处理方式！")
                return@setOnClickListener
            }
            confirmListener?.invoke(PROCESSED_STATUS, msg)
            dismiss()
        }

        ignoreTV.setOnClickListener {
            val msg = "直接忽略"
            confirmListener?.invoke(PROCESSED_IGNORE, msg)
            dismiss()

        }

    }

    private var confirmListener: ((model: String, msg: String) -> Unit)? = null
    fun setConfirmListener(listener: ((model: String, msg: String) -> Unit)): InputMsgDialog =
        apply {
            this.confirmListener = listener
        }

}