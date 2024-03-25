package com.mj.preventbullying.client.ui.activity

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import cn.jpush.android.api.BasicPushNotificationBuilder
import cn.jpush.android.api.JPushInterface
import com.mj.preventbullying.client.Constant
import com.mj.preventbullying.client.R
import com.mj.preventbullying.client.app.AppMvActivity
import com.mj.preventbullying.client.app.MyApp
import com.mj.preventbullying.client.databinding.ActivityHomeBinding
import com.mj.preventbullying.client.tool.SpManager
import com.mj.preventbullying.client.tool.requestPermission
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseViewModel


/**
 * Create by MJ on 2024/3/22.
 * Describe :
 */

class HomeActivity : AppMvActivity<ActivityHomeBinding, BaseViewModel>() {

    private lateinit var navHostFragment: NavHostFragment

    private lateinit var navController: NavController
    override fun getViewBinding(): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(layoutInflater)
    }

    override fun initParam() {
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHomeHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        // 获取组织列表
        MyApp.globalEventViewModel.getOrgList()
        requestPermission()
        // 设置通知的样式
        val builder = BasicPushNotificationBuilder(this)
        builder.statusBarDrawable = R.drawable.app_icon
        builder.notificationFlags = (Notification.FLAG_AUTO_CANCEL
                or Notification.FLAG_SHOW_LIGHTS) //设置为自动消失和呼吸灯闪烁

        builder.notificationDefaults = (Notification.DEFAULT_SOUND
                or Notification.DEFAULT_VIBRATE
                or Notification.DEFAULT_LIGHTS) // 设置为铃声、震动、呼吸灯闪烁都要

        JPushInterface.setPushNotificationBuilder(1, builder)
        JPushInterface.setDebugMode(true)
        JPushInterface.init(this)
        initChannel()

    }


    /**
     *  初始化channel通道
     */
    private fun initChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (nm != null) {
                val notificationChannelGroup =
                    NotificationChannelGroup("prevent_group_1", "极光推送铃声自定义")
                nm.createNotificationChannelGroup(notificationChannelGroup)
                val notificationChannel = NotificationChannel(
                    "prevent_channel1",
                    "极光消息通道",
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationChannel.group = "prevent_group_1"
                notificationChannel.enableLights(true)
                notificationChannel.enableVibration(true)
                notificationChannel.setSound(
                    Uri.parse("android.resource://com.mj.preventbullying.client/${R.raw.alarm_4}"),
                    null
                ) // 设置自定义铃声
                nm.createNotificationChannel(notificationChannel)
            }
        }
    }

    override fun initData() {
    }

    override fun initViewObservable() {

    }

    override fun initView() {

    }

    override fun initListener() {

    }
}