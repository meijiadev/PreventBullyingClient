package com.mj.preventbullying.client.ui.activity

import android.view.View
import cn.jpush.android.ups.JPushUPSManager
import com.azhon.appupdate.listener.OnDownloadListenerAdapter
import com.azhon.appupdate.manager.DownloadManager
import com.gyf.immersionbar.ktx.immersionBar
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupAnimation
import com.mj.preventbullying.client.Constant
import com.mj.preventbullying.client.app.MyApp
import com.mj.preventbullying.client.R
import com.mj.preventbullying.client.app.AppMvActivity
import com.mj.preventbullying.client.databinding.ActivitySettingBinding
import com.mj.preventbullying.client.http.result.AppData
import com.mj.preventbullying.client.http.result.UpdateAppResult
import com.mj.preventbullying.client.http.service.ApiService
import com.mj.preventbullying.client.tool.ActivityManager
import com.mj.preventbullying.client.tool.NetworkUtil
import com.mj.preventbullying.client.tool.SpManager
import com.mj.preventbullying.client.ui.dialog.AmendPasswordDialog
import com.mj.preventbullying.client.ui.dialog.UpdateAppDialog
import com.mj.preventbullying.client.ui.login.LoginActivity
import com.mj.preventbullying.client.ui.viewmodel.SettingViewModel
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseMvActivity
import com.sjb.base.view.SwitchButton

/**
 * Create by MJ on 2023/12/26.
 * Describe :设置界面
 */

class SettingActivity : AppMvActivity<ActivitySettingBinding, SettingViewModel>() {
    private var appUpdateResult: UpdateAppResult? = null
    override fun getViewBinding(): ActivitySettingBinding {
        return ActivitySettingBinding.inflate(layoutInflater)
    }

    override fun initParam() {
        immersionBar {
            //深色字体
            statusBarDarkFont(true)
        }
    }

    override fun initData() {
        binding.ipTv.text = NetworkUtil.getIPAddress(true)
        val isAutoLogin = SpManager.getBoolean(Constant.AUTO_LOGIN_KEY, true)
        binding.autoLoginBt.setChecked(isAutoLogin)
        binding.tvNewVersion.visibility = if (Constant.isNewAppVersion) {
            View.VISIBLE
        } else {
            View.GONE
        }
        MyApp.globalEventViewModel.getAppVersion()
        binding.serviceTv.text = ApiService.getHostUrl()
        var phone = SpManager.getString(Constant.USER_PHONE_KEY)
        if (phone.isNullOrEmpty()) {
            phone = "未绑定手机号码"
        } else {
            phone = phone.substring(0, 3) + "****" + phone.substring(7, 11)
        }
        binding.phoneTv.text = phone
    }

    override fun initViewObservable() {
        binding.titleLayout.backIv.setOnClickListener {
            count = 0
            finish()

        }
        binding.phoneNumberLy.setOnClickListener {
            count = 0
            toast("暂不支持修改手机号码")
        }
        binding.msgToneLy.setOnClickListener {
            startActivity(AlarmAudioActivity::class.java)
            count = 0
        }

        binding.passwordLy.setOnClickListener {
            val passwordDialog = AmendPasswordDialog(this)
            XPopup.Builder(this).isViewMode(true).popupAnimation(PopupAnimation.TranslateFromBottom)
                .asCustom(passwordDialog).show()
            passwordDialog.setOnConfirmListener { oldPs, newPs ->
                // 确认修改密码
                viewModel.amendPassword(oldPs, newPs)
            }
            count = 0
        }
        // 退出登录
        binding.logoutLy.setOnClickListener {
            loginOut()
            count = 0
        }

        binding.autoLoginBt.setOnCheckedChangeListener(object :
            SwitchButton.OnCheckedChangeListener {
            override fun onCheckedChanged(button: SwitchButton, checked: Boolean) {
                Logger.i("点击自动登录：$checked")
                SpManager.putBoolean(Constant.AUTO_LOGIN_KEY, checked)
            }
        })

        binding.updateAppLy.setOnClickListener {
            if (Constant.isNewAppVersion) {
                appUpdateResult?.data?.let {
                    showUpdateDialog(it)
                }
            } else {
                toast("暂无新版本")
            }
            count = 0
        }

    }

    private var count = 0


    private var updateAppDialog: UpdateAppDialog? = null

    /**
     * 显示dialog
     */
    private fun showUpdateDialog(app: AppData) {
        updateAppDialog = UpdateAppDialog(this).setUpdateMsg(app).setUpdateAppListener {
            val manager = DownloadManager.Builder(this).run {
                apkUrl(app.fileUrl)
                apkName(app.fileName)
                smallIcon(R.mipmap.app_icon)
                onDownloadListener(listenerAdapter)
                build()
            }
            manager.download()
        }
        XPopup.Builder(this)
            .isViewMode(true)
            .popupAnimation(PopupAnimation.TranslateFromBottom)
            .dismissOnTouchOutside(false)
            .dismissOnBackPressed(false)
            .asCustom(updateAppDialog)
            .show()
    }

    private val listenerAdapter: OnDownloadListenerAdapter = object : OnDownloadListenerAdapter() {

        override fun downloading(max: Int, progress: Int) {
            val curr = (progress / max.toDouble() * 100.0).toInt()
            Logger.i("当前下载进度：$curr")
            updateAppDialog?.setProgress(curr)
        }
    }


    /**
     * 退出登录
     */
    private fun loginOut() {
        MyApp.socketEventViewModel.logout()
        MyApp.webrtcSocketManager.sendHangUp()
        MyApp.webrtcSocketManager.disconnect()
        MyApp.socketEventViewModel.disconnect()
        SpManager.putString(Constant.ACCESS_TOKEN_KEY, null)
        SpManager.putString(Constant.FRESH_TOKEN_KEY, null)
        SpManager.putString(Constant.USER_ID_KEY, null)
        JPushUPSManager.turnOffPush(this) {
            Logger.i("关闭极光推送服务：$it")
        }
        startActivity(LoginActivity::class.java)
        ActivityManager.getInstance().finishAllActivities(LoginActivity::class.java)
    }

    override fun initView() {

    }

    override fun initListener() {
        MyApp.globalEventViewModel.updateAppEvent.observe(this) {
            appUpdateResult = it
            Logger.i("获取更新信息：$it")
        }
    }
}