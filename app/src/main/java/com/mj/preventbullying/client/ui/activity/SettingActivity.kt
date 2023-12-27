package com.mj.preventbullying.client.ui.activity

import cn.jpush.android.ups.JPushUPSManager
import com.gyf.immersionbar.ktx.immersionBar
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupAnimation
import com.mj.preventbullying.client.Constant
import com.mj.preventbullying.client.MyApp
import com.mj.preventbullying.client.databinding.ActivitySettingBinding
import com.mj.preventbullying.client.tool.ActivityManager
import com.mj.preventbullying.client.tool.NetworkUtil
import com.mj.preventbullying.client.tool.SpManager
import com.mj.preventbullying.client.ui.dialog.AmendPasswordDialog
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

    }
}