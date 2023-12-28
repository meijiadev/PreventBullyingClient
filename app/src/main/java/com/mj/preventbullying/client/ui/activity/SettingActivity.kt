package com.mj.preventbullying.client.ui.activity

import android.graphics.Color
import android.view.View
import cn.jpush.android.ups.JPushUPSManager
import com.azhon.appupdate.manager.DownloadManager
import com.gyf.immersionbar.ktx.immersionBar
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupAnimation
import com.mj.preventbullying.client.BuildConfig
import com.mj.preventbullying.client.Constant
import com.mj.preventbullying.client.MyApp
import com.mj.preventbullying.client.R
import com.mj.preventbullying.client.databinding.ActivitySettingBinding
import com.mj.preventbullying.client.http.result.UpdateAppResult
import com.mj.preventbullying.client.tool.ActivityManager
import com.mj.preventbullying.client.tool.NetworkUtil
import com.mj.preventbullying.client.tool.SpManager
import com.mj.preventbullying.client.ui.dialog.AmendPasswordDialog
import com.mj.preventbullying.client.ui.dialog.UpdateAppDialog
import com.mj.preventbullying.client.ui.login.LoginActivity
import com.mj.preventbullying.client.ui.viewmodel.SettingViewModel
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseMvActivity
import com.sjb.base.base.BaseViewModel
import com.sjb.base.view.SwitchButton

/**
 * Create by MJ on 2023/12/26.
 * Describe :设置界面
 */

class SettingActivity : BaseMvActivity<ActivitySettingBinding, SettingViewModel>() {
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

        viewModel.getAppVersion()
    }

    override fun initViewObservable() {
        binding.backIv.setOnClickListener {
            finish()
        }
        binding.passwordLy.setOnClickListener {
            val passwordDialog = AmendPasswordDialog(this)
            XPopup.Builder(this).isViewMode(true).popupAnimation(PopupAnimation.TranslateFromBottom)
                .asCustom(passwordDialog).show()
            passwordDialog.setOnConfirmListener { oldPs, newPs ->
                // 确认修改密码
                viewModel.amendPassword(oldPs, newPs)
            }
        }
        // 退出登录
        binding.logoutLy.setOnClickListener {
            loginOut()
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
                    val version = it.versionNo
                    val versionNo = version.replace("v", "")
                    Constant.newAppVersion = versionNo.replace(".", "").toInt()
                    val curVersion = BuildConfig.VERSION_NAME.replace(".", "").toInt()
                    if (curVersion < Constant.newAppVersion) {
                        val apkSize = "${it.fileSize.toLong() / 1024 / 1024}MB"
                        val manager = DownloadManager.Builder(this).run {
                            apkUrl(it.fileUrl)
                            apkName(it.fileName)
                            apkVersionCode(2)
                            smallIcon(R.mipmap.app_icon)
                            showNewerToast(true)
                            apkVersionName(version)
                            apkSize(apkSize)
                            apkDescription("修复bug\n优化用户体验")
                            enableLog(true)
                            jumpInstallPage(true)
                            dialogButtonTextColor(Color.WHITE)
                            showNotification(true)
                            showBgdToast(false)
                            forcedUpgrade(false)
                            build()
                        }
                        manager.download()
                    }
                }
            } else {
                toast("暂无新版本")
            }
        }

    }

    /**
     * 显示dialog
     */
    private fun showUpdateDialog(version: String) {
        val updateAppDialog = UpdateAppDialog(this).setUpdateMsg(version)
        XPopup.Builder(this)
            .isViewMode(true)
            .popupAnimation(PopupAnimation.TranslateFromBottom)
            .dismissOnTouchOutside(false)
            .dismissOnBackPressed(false)
            .asCustom(updateAppDialog)
            .show()
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
        }
    }
}