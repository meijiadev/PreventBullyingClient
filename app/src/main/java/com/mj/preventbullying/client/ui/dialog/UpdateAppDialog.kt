package com.mj.preventbullying.client.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatTextView
import com.azhon.appupdate.manager.DownloadManager
import com.lxj.xpopup.core.CenterPopupView
import com.mj.preventbullying.client.BuildConfig
import com.mj.preventbullying.client.Constant
import com.mj.preventbullying.client.R
import com.mj.preventbullying.client.http.result.AppData
import com.mj.preventbullying.client.http.result.UpdateAppResult
import com.orhanobut.logger.Logger

/**
 * Create by MJ on 2023/12/28.
 * Describe :
 */

class UpdateAppDialog(context: Context) : CenterPopupView(context) {
    private val tvUpdateVersion: AppCompatTextView by lazy { findViewById(R.id.tv_update_name) }

    private val tvUpdateDetails: AppCompatTextView by lazy { findViewById(R.id.tv_update_details) }

    private val pbUpdateProgress: ProgressBar by lazy { findViewById(R.id.pb_update_progress) }
    private val progressTv: AppCompatTextView by lazy { findViewById(R.id.progress_tv) }

    private val tvNext: AppCompatTextView by lazy { findViewById(R.id.tv_update_close) }
    private val tvUpdate: AppCompatTextView by lazy { findViewById(R.id.tv_update_update) }

    private var appResult: AppData? = null
    override fun getImplLayoutId(): Int = R.layout.update_dialog


    @SuppressLint("SetTextI18n")
    override fun onCreate() {
        super.onCreate()
        pbUpdateProgress.max = 100
        tvUpdateDetails.text = "修复bug\n优化用户体验"
        val version = appResult?.versionNo
        val des = appResult?.releaseLog
        version?.let {
            tvUpdateVersion.text = it
        }
        des?.let {
            tvUpdateDetails.text = it
        }
        Logger.i("版本号：$version,更新内容：$des")

        tvNext.setOnClickListener {
            dismiss()
        }

        tvUpdate.setOnClickListener {
            val msg = tvUpdate.text.toString()
            if (msg == "立刻更新") {
                pbUpdateProgress.visibility = View.VISIBLE
                progressTv.visibility=View.VISIBLE
                tvNext.visibility = View.GONE
                tvUpdate.text = "正在下载"
                updateAppListener?.invoke()
            } else if (msg == "下载完成") {
                dismiss()
            }
        }
    }

    /**
     * 设置更新的版本号，更新内容
     */
    fun setUpdateMsg(appResult: AppData): UpdateAppDialog =
        apply {
            this.appResult = appResult
        }

    fun setProgress(curProgress: Int) {
        pbUpdateProgress.progress = curProgress
        progressTv.text = curProgress.toString()
        if (curProgress == 100) {
            tvUpdate.text = "下载完成"
        }
    }

    private var updateAppListener: (() -> Unit)? = null

    fun setUpdateAppListener(listener: (() -> Unit)): UpdateAppDialog = apply {
        this.updateAppListener = listener
    }


}