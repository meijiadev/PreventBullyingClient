package com.mj.preventbullying.client.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.jpush.android.ups.JPushUPSManager
import com.azhon.appupdate.listener.OnDownloadListenerAdapter
import com.azhon.appupdate.manager.DownloadManager
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupAnimation
import com.mj.preventbullying.client.Constant
import com.mj.preventbullying.client.R
import com.mj.preventbullying.client.app.MyApp
import com.mj.preventbullying.client.databinding.FragmentMineBinding
import com.mj.preventbullying.client.foldtree.TreeModel
import com.mj.preventbullying.client.http.result.AppData
import com.mj.preventbullying.client.http.result.UpdateAppResult
import com.mj.preventbullying.client.http.service.ApiService
import com.mj.preventbullying.client.tool.ActivityManager
import com.mj.preventbullying.client.tool.NetworkUtil
import com.mj.preventbullying.client.tool.SpManager
import com.mj.preventbullying.client.ui.activity.AlarmAudioActivity
import com.mj.preventbullying.client.ui.dialog.AmendPasswordDialog
import com.mj.preventbullying.client.ui.dialog.ItemListDialog
import com.mj.preventbullying.client.ui.dialog.UpdateAppDialog
import com.mj.preventbullying.client.ui.login.LoginActivity
import com.mj.preventbullying.client.ui.viewmodel.MainViewModel
import com.mj.preventbullying.client.ui.viewmodel.SettingViewModel
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseMvFragment
import com.sjb.base.base.BaseViewModel
import com.sjb.base.view.SwitchButton

/**
 * Create by MJ on 2024/1/13.
 * Describe : 我的界面
 */

class MineFragment : BaseMvFragment<FragmentMineBinding, SettingViewModel>() {
    private var appUpdateResult: UpdateAppResult? = null


    // 设备注册相关页面
    private var treeList: MutableList<TreeModel>? = null

    companion object {
        fun newInstance(): MineFragment {
            val args = Bundle()
            val fragment = MineFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun getViewBinding(inflater: LayoutInflater, parent: ViewGroup?): FragmentMineBinding {
        return FragmentMineBinding.inflate(layoutInflater, parent, false)
    }

    override fun initParam() {
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
        binding.orgTv.text = MyApp.globalEventViewModel.schoolName
        Logger.i("当前的学校是：${MyApp.globalEventViewModel.schoolName}")
    }


    override fun initViewObservable() {

        binding.phoneNumberLy.setOnClickListener {
            toast("暂不支持修改手机号码")
        }
        binding.msgToneLy.setOnClickListener {
            startActivity(AlarmAudioActivity::class.java)
        }

        binding.passwordLy.setOnClickListener {
            val passwordDialog = AmendPasswordDialog(requireContext())
            XPopup.Builder(requireContext()).isViewMode(true)
                .popupAnimation(PopupAnimation.TranslateFromBottom)
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
                    showUpdateDialog(it)
                }
            } else {
                toast("暂无新版本")
            }
        }

        binding.curOrgLy.setOnClickListener {
            showOrgDialog(it)
        }
    }

    /**
     *
     */
    private fun showOrgDialog(v: View) {
        treeList = MyApp.globalEventViewModel.treeList
        val location = IntArray(2)
        v.getLocationOnScreen(location)
        val y = location[1]
        val itemListDialog = ItemListDialog(requireContext()).setOrgData(treeList).onOrgListener {
            Logger.i("选择的组织")
            binding.orgTv.text = it.name
            MyApp.globalEventViewModel.schoolName = it.name
            MyApp.globalEventViewModel.schoolId = it.id
            // SpManager.putString(Constant.ORG_ID_KEY, it.id)
        }
        XPopup.Builder(requireContext())
            .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
            .hasShadowBg(false)
            .hasBlurBg(false)
            .isViewMode(true)
            .offsetY(y + v.height)
            .asCustom(itemListDialog)
            .show()
    }


    private var updateAppDialog: UpdateAppDialog? = null

    /**
     * 显示dialog
     */
    private fun showUpdateDialog(app: AppData) {
        updateAppDialog = UpdateAppDialog(requireContext()).setUpdateMsg(app).setUpdateAppListener {
            val manager = DownloadManager.Builder(requireActivity()).run {
                apkUrl(app.fileUrl)
                apkName(app.fileName)
                smallIcon(R.mipmap.app_icon)
                onDownloadListener(listenerAdapter)
                build()
            }
            manager.download()
        }
        XPopup.Builder(requireContext())
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
        JPushUPSManager.turnOffPush(requireContext()) {
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

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden){
            binding.orgTv.text = MyApp.globalEventViewModel.schoolName
            Logger.i("当前的学校是：${MyApp.globalEventViewModel.schoolName}")
        }
    }
}