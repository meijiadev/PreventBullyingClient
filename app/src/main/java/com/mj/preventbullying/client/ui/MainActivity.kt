package com.mj.preventbullying.client.ui

import android.annotation.SuppressLint
import android.view.View
import androidx.fragment.app.Fragment
import cn.jpush.android.api.JPushInterface
import cn.jpush.android.ups.JPushUPSManager
import cn.jpush.android.ups.TokenResult
import cn.jpush.android.ups.UPSTurnCallBack
import com.google.gson.Gson
import com.gyf.immersionbar.ktx.immersionBar
import com.mj.preventbullying.client.Constant
import com.mj.preventbullying.client.Constant.USER_ID_KEY
import com.mj.preventbullying.client.MyApp
import com.mj.preventbullying.client.R
import com.mj.preventbullying.client.SpManager
import com.mj.preventbullying.client.databinding.ActivityMainBinding
import com.mj.preventbullying.client.jpush.receive.JPushExtraMessage
import com.mj.preventbullying.client.ui.fragment.DeviceFragment
import com.mj.preventbullying.client.ui.fragment.MessageFragment
import com.mj.preventbullying.client.ui.login.LoginActivity
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseMvActivity


/**
 * Create by MJ on 2023/12/9.
 * Describe : 主页
 */
class MainActivity : BaseMvActivity<ActivityMainBinding, MainViewModel>() {
    private val messageFragment by lazy { MessageFragment.newInstance() }
    private val deviceFragment by lazy { DeviceFragment.newInstance() }

    private var jPushExtraMessage: JPushExtraMessage? = null


    override fun getViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    @SuppressLint("ResourceType")
    override fun initParam() {
        val extras = intent.extras?.getString(JPushInterface.EXTRA_NOTIFICATION_ACTION_EXTRA)
        Logger.e("收到极光推送的报警recordId:$extras")
        if (extras!=null){
            jPushExtraMessage = Gson().fromJson(extras, JPushExtraMessage::class.java)
        }
        immersionBar {
            //深色字体
            statusBarDarkFont(true)
        }
    }

    override fun initData() {
        val userId = SpManager.getString(USER_ID_KEY)
        val registerId = SpManager.getString(Constant.REGISTER_ID_KEY)
        if (userId != null && registerId != null) {
            MyApp.socketEventViewModel.initSocket(userId, registerId)
            // viewModel.getAllDeviceRecords()
        }

    }

    override fun initViewObservable() {

    }

    override fun initView() {
        switchFragment(messageFragment)

    }

    override fun initListener() {
        binding.addDeviceLl.setOnClickListener {
            Logger.i("点击添加设备")

        }
    }


    fun onExit(v: View) {
        SpManager.putString(Constant.ACCESS_TOKEN_KEY, null)
        SpManager.putString(Constant.FRESH_TOKEN_KEY, null)
        SpManager.putString(Constant.USER_ID_KEY, null)
        JPushUPSManager.turnOffPush(this) {
            Logger.i("关闭极光推送服务：$it")
        }
        startActivity(LoginActivity::class.java)
        finish()
    }

    fun onMessage(v: View) {
        binding.titleTv.text = "消息通知"
        switchFragment(messageFragment)
    }

    fun onDevice(v: View) {
        binding.titleTv.text = "设备管理"
        switchFragment(deviceFragment)
    }

    /**
     * 当前的fragment
     */
    private var mFragment = Fragment()

    /**
     * 切换fragment
     */
    private fun switchFragment(target: Fragment) {
        if (target != null && target != mFragment) {
            val transaction = supportFragmentManager.beginTransaction()
            if (target is MessageFragment) {
                transaction.setCustomAnimations(
                    R.anim.action_left_enter,
                    R.anim.action_left_exit
                )
            } else {
                transaction.setCustomAnimations(
                    R.anim.action_rigth_enter,
                    R.anim.action_rigth_exit
                )
            }
            // 先判断该fragment 是否已经被添加到管理器
            if (!target.isAdded) {
                transaction.hide(mFragment).add(R.id.fragment_container, target)
                    .commitAllowingStateLoss()
            } else {
                // 添加的fragment 直接显示
                transaction.hide(mFragment).show(target).commitAllowingStateLoss()
            }
            mFragment = target

        }

    }

}