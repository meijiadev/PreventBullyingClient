package com.mj.preventbullying.client.ui.dialog

import android.content.Context
import androidx.appcompat.widget.AppCompatTextView
import com.lxj.xpopup.core.PositionPopupView
import com.lxj.xpopup.enums.DragOrientation
import com.mj.preventbullying.client.R

/**
 * Create by MJ on 2024/1/23.
 * Describe :
 */
class NotifyDialog(context: Context) : PositionPopupView(context) {
    private val msgTv: AppCompatTextView by lazy { findViewById(R.id.msg_tv) }

    private var msg: String? = null
    override fun getImplLayoutId(): Int {
        return R.layout.dialog_msg_layout
    }

    override fun onCreate() {
        super.onCreate()
        msgTv.text = msg

    }

    fun setMsg(msg: String): NotifyDialog = apply {
        this.msg = msg
        msgTv.text = msg
    }

    override fun getDragOrientation(): DragOrientation? {
        return DragOrientation.DragToLeft
    }
}