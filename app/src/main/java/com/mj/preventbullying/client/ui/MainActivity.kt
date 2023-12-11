package com.mj.preventbullying.client.ui

import android.annotation.SuppressLint
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.gyf.immersionbar.ImmersionBar
import com.gyf.immersionbar.ktx.immersionBar
import com.mj.preventbullying.client.Constant.registerId
import com.mj.preventbullying.client.Constant.userId
import com.mj.preventbullying.client.MyApp
import com.mj.preventbullying.client.R
import com.mj.preventbullying.client.databinding.ActivityMainBinding
import com.mj.preventbullying.client.ui.adapter.DeviceListAdapter
import com.mj.preventbullying.client.ui.fragment.DeviceFragment
import com.mj.preventbullying.client.ui.fragment.MessageFragment
import com.sjb.base.base.BaseMvActivity


/**
 * Create by MJ on 2023/12/9.
 * Describe : 主页
 */
class MainActivity : BaseMvActivity<ActivityMainBinding, MainViewModel>() {
    private val messageFragment by lazy { MessageFragment.newInstance() }
    private val deviceFragment by lazy { DeviceFragment.newInstance() }


    override fun getViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    @SuppressLint("ResourceType")
    override fun initParam() {
        immersionBar {
            //深色字体
            statusBarDarkFont(true)
        }
    }

    override fun initData() {

    }

    override fun initViewObservable() {

    }

    override fun initView() {
        switchFragment(messageFragment)

    }

    override fun initListener() {
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