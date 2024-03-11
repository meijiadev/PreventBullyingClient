package com.mj.preventbullying.client.ui.dialog

import android.content.Context
import androidx.appcompat.widget.AppCompatTextView
import com.lxj.xpopup.core.CenterPopupView
import com.mj.preventbullying.client.R

/**
 * Create by MJ on 2023/12/22.
 * Describe :有其他人登录进来，或者被强制人强制退出
 */

class MessageTipsDialog(context: Context) : CenterPopupView(context) {
    private val titleTv: AppCompatTextView by lazy { findViewById(R.id.title_tv) }
    private val cancelTv: AppCompatTextView by lazy { findViewById(R.id.cancel_tv) }
    private val confirmTv: AppCompatTextView by lazy { findViewById(R.id.confirm_tv) }
    private var listener: OnListener? = null
    private var titleMsg: String? = null

    override fun getImplLayoutId(): Int = R.layout.dialog_login_status

    override fun onCreate() {
        super.onCreate()
        titleTv.text = titleMsg
        // 取消点击事件
        cancelTv.setOnClickListener {
            dismiss()
            listener?.onCancel()
        }
        // 确定点击事件
        confirmTv.setOnClickListener {
            dismiss()
            listener?.onConfirm()
        }
    }

    fun setListener(listener: OnListener): MessageTipsDialog = apply {
        this.listener = listener
    }

    fun setTitle(msg: String): MessageTipsDialog = apply {
        this.titleMsg = msg
    }

    interface OnListener {
        // 取消
        fun onCancel()

        // 确定
        fun onConfirm()
    }
}