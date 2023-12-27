package com.mj.preventbullying.client.ui.dialog

import android.content.Context
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import com.hjq.toast.ToastUtils
import com.lxj.xpopup.core.CenterPopupView
import com.mj.preventbullying.client.R
import com.sjb.base.view.PasswordEditText

/**
 * Create by MJ on 2023/12/26.
 * Describe :修改密码的弹窗
 */

class AmendPasswordDialog(context: Context) : CenterPopupView(context) {
    private val oldPasswordEt: PasswordEditText by lazy { findViewById(R.id.old_password) }
    private val newPasswordEt: AppCompatEditText by lazy { findViewById(R.id.new_password) }
    private val confirmPasswordEt: AppCompatEditText by lazy { findViewById(R.id.confirm_password) }
    private val cancelTV: AppCompatTextView by lazy { findViewById(R.id.cancel_tv) }
    private val confirmTv: AppCompatTextView by lazy { findViewById(R.id.confirm_tv) }

    override fun getImplLayoutId(): Int = R.layout.dialog_amend_password


    override fun onCreate() {
        super.onCreate()
        // 确定按钮
        confirmTv.setOnClickListener {
            val oldPs = oldPasswordEt.text.toString()
            val newPs = newPasswordEt.text.toString()
            val confirmPs = confirmPasswordEt.text.toString()
            // 请确认都填写正确
            if (oldPs.isEmpty() || newPs.isEmpty() || confirmPs.isEmpty()) {
                ToastUtils.show("请完整填写相关内容！")
            } else {
                if (newPs != confirmPs) {
                    ToastUtils.show("请保证新密码和确认密码一致！")
                } else {
                    onConfirmListener?.invoke(oldPs, newPs)
                }
            }

        }
        // 取消按钮
        cancelTV.setOnClickListener {
            dismiss()
        }

    }

    private var onConfirmListener: ((oldPs: String, newPs: String) -> Unit)? = null

    fun setOnConfirmListener(listener: ((oldPs: String, newPs: String) -> Unit)) {
        this.onConfirmListener = listener


    }


}