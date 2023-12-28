package com.mj.preventbullying.client.ui.dialog

import android.content.Context
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatTextView
import com.lxj.xpopup.core.CenterPopupView
import com.mj.preventbullying.client.R
import com.orhanobut.logger.Logger

/**
 * Create by MJ on 2023/12/28.
 * Describe :
 */

class UpdateAppDialog(context: Context) : CenterPopupView(context) {
    private val tvUpdateVersion: AppCompatTextView by lazy { findViewById(R.id.tv_update_name) }

    private val tvUpdateDetails: AppCompatTextView by lazy { findViewById(R.id.tv_update_details) }

    private val pbUpdateProgress: ProgressBar by lazy { findViewById(R.id.pb_update_progress) }

    private val tvNext: AppCompatTextView by lazy { findViewById(R.id.tv_update_close) }
    private val tvUpdate: AppCompatTextView by lazy { findViewById(R.id.tv_update_update) }

    private var version: String? = null
    private var updateMsg: String? = null
    override fun getImplLayoutId(): Int = R.layout.update_dialog


    override fun onCreate() {
        super.onCreate()
        tvUpdateVersion.text = version
        updateMsg?.let {
            tvUpdateDetails.text = it
        }
        Logger.i("版本号：$version,更新内容：$updateMsg")

        tvNext.setOnClickListener {
            dismiss()
        }

        tvUpdate.setOnClickListener {

        }


    }

    /**
     * 设置更新的版本号，更新内容
     */
    fun setUpdateMsg(version: String, msg: String = "修复bug\n优化用户体验"): UpdateAppDialog =
        apply {
            this.version = version
            this.updateMsg = msg
        }


}