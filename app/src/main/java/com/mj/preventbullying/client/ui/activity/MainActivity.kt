package com.mj.preventbullying.client.ui.activity

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.view.View
import androidx.fragment.app.Fragment
import cn.jpush.android.api.BasicPushNotificationBuilder
import cn.jpush.android.api.JPushInterface
import cn.jpush.android.ups.JPushUPSManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gyf.immersionbar.ktx.immersionBar
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupAnimation
import com.mj.preventbullying.client.BuildConfig
import com.mj.preventbullying.client.Constant
import com.mj.preventbullying.client.Constant.USER_ID_KEY
import com.mj.preventbullying.client.R
import com.mj.preventbullying.client.app.AppMvActivity
import com.mj.preventbullying.client.app.MyApp
import com.mj.preventbullying.client.databinding.ActivityMainBinding
import com.mj.preventbullying.client.foldtree.TreeModel
import com.mj.preventbullying.client.http.result.DevType
import com.mj.preventbullying.client.jpush.receive.JPushExtraMessage
import com.mj.preventbullying.client.tool.NetworkUtil
import com.mj.preventbullying.client.tool.SpManager
import com.mj.preventbullying.client.tool.requestLocationPermission
import com.mj.preventbullying.client.tool.requestPermission
import com.mj.preventbullying.client.ui.dialog.DevInfoDialog
import com.mj.preventbullying.client.ui.dialog.MessageTipsDialog
import com.mj.preventbullying.client.ui.fragment.DeviceFragment
import com.mj.preventbullying.client.ui.fragment.MessageFragment
import com.mj.preventbullying.client.ui.login.LoginActivity
import com.mj.preventbullying.client.ui.viewmodel.MainViewModel
import com.mj.preventbullying.client.webrtc.LOGIN_STATUS_ANTHER
import com.mj.preventbullying.client.webrtc.LOGIN_STATUS_FORCE_LOGOUT
import com.mj.preventbullying.client.webrtc.SOCKET_IO_CONNECT
import com.mj.preventbullying.client.webrtc.SOCKET_IO_DISCONNECTED
import com.orhanobut.logger.Logger


/**
 * Create by MJ on 2023/12/9.
 * Describe : 主页
 */
class MainActivity : AppMvActivity<ActivityMainBinding, MainViewModel>() {
    private val messageFragment by lazy { MessageFragment.newInstance() }
    private val deviceFragment by lazy { DeviceFragment.newInstance() }

    private var jPushExtraMessage: JPushExtraMessage? = null
    private var treeList: MutableList<TreeModel>? = null
    private var typeList: MutableList<DevType>? = null


    override fun getViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    @SuppressLint("ResourceType")
    override fun initParam() {
        immersionBar {
            //深色字体
            statusBarDarkFont(true)
        }
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
                    Uri.parse("android.resource://com.mj.preventbullying.client/${R.raw.alarm_3}"),
                    null
                ) // 设置自定义铃声
                nm.createNotificationChannel(notificationChannel)
            }
        }
    }

    override fun initData() {
        val userId = SpManager.getString(USER_ID_KEY)
        val registerId = SpManager.getString(Constant.REGISTER_ID_KEY)
        if (userId != null && registerId != null) {
            MyApp.socketEventViewModel.initSocket(userId, registerId)
            // viewModel.getAllDeviceRecords()
        }

        MyApp.globalEventViewModel.getAppVersion()
        requestLocationPermission()

    }

    override fun initViewObservable() {

    }

    override fun initView() {
        switchFragment(messageFragment)
        if (MyApp.socketEventViewModel.isConnected) {
            binding.devServerIv.setImageResource(R.mipmap.dev_connect)
        } else {
            binding.devServerIv.setImageResource(R.mipmap.dev_disconnect)
        }
        binding.devServerIv.setOnClickListener {
            if (MyApp.socketEventViewModel.isConnected) {
                toast("应用和设备服务器已连接成功")
            } else {
                toast("应用和设备服务器未连接，可能无法连接设备")
            }
        }

    }

    private fun getDevInfoList() {
        viewModel.getOrgList()
        viewModel.getDevType()
    }

    override fun initListener() {
        binding.addDeviceLl.setOnClickListener {
            getDevInfoList()
            Logger.i("点击添加设备")
            showAddDialog()
        }
        // 登录状态
        MyApp.socketEventViewModel.loginStatusEvent.observe(this) {
            when (it) {
                LOGIN_STATUS_ANTHER -> {
                    Logger.i("有其他人登录，是否强制退出")
                    //MyApp.socketEventViewModel.confirmLogin(true)
                    val tipsDialog =
                        MessageTipsDialog(this).setTitle("发现账号有其他人登录，是否让其强制退出?")
                            .setListener(object : MessageTipsDialog.OnListener {
                                override fun onCancel() {
                                    MyApp.socketEventViewModel.confirmLogin(false)
                                    loginOut()
                                }

                                override fun onConfirm() {
                                    MyApp.socketEventViewModel.confirmLogin(true)
                                }
                            })
                    XPopup.Builder(this)
                        .isViewMode(true)
                        .isDestroyOnDismiss(true)
                        .dismissOnBackPressed(false)
                        .dismissOnTouchOutside(false)
                        .popupAnimation(PopupAnimation.TranslateFromBottom)
                        .asCustom(tipsDialog)
                        .show()
                }

                LOGIN_STATUS_FORCE_LOGOUT -> {
                    toast("账号已被其他人登录，你已被强制退出！")
                    // Logger.i("有其他人登录，你也被强制退出")
                    loginOut()
                }

                SOCKET_IO_CONNECT -> {
                    binding.devServerIv.setImageResource(R.mipmap.dev_connect)
                }

                SOCKET_IO_DISCONNECTED -> {
                    binding.devServerIv.setImageResource(R.mipmap.dev_disconnect)
                }
            }
        }

        viewModel.orgTreeEvent.observe(this) {
            // 接收到组织树列表
            val tree = it?.data
            val gson = Gson()
            val jsonStr = gson.toJson(tree)
            treeList = gson.fromJson(jsonStr, object : TypeToken<List<TreeModel?>?>() {}.type)
            Logger.d("转化之后的组织树：${treeList?.size}")
            addDevDialog?.setOrgData(treeList)
        }
        viewModel.devTypeEvent.observe(this) {
            typeList = it?.data as MutableList<DevType>?
            addDevDialog?.setTypeData(typeList)
        }
        viewModel.addDevEvent.observe(this) {
            toast("设备添加成功！")
        }

        MyApp.globalEventViewModel.updateAppEvent.observe(this) {
            Logger.i("收到更新信息:$it")
            val versionCode = it?.data?.versionCode
            if (versionCode != null) {
                Constant.isNewAppVersion = it.data.versionCode > BuildConfig.VERSION_CODE
            }
        }


    }

    var addDevDialog: DevInfoDialog? = null
    private fun showAddDialog() {
        addDevDialog = DevInfoDialog(this).setOnListener(object : DevInfoDialog.AddDevListener {

            override fun onCancel() {

            }

            override fun onConfirm(
                sn: String,
                //name: String,
                orgId: Long,
                orgName: String,
                location: String,
                modelCode: String,
                desc: String?
            ) {
                viewModel.addDev(sn, orgId, modelCode, location, desc)
            }


        }).setOrgData(treeList).setTypeData(typeList).setTitleMsg("添加设备")
        XPopup.Builder(this)
            .isViewMode(true)
            .dismissOnTouchOutside(false)
            .dismissOnBackPressed(false)
            .isDestroyOnDismiss(true)
            .popupAnimation(PopupAnimation.TranslateFromBottom)
            .asCustom(addDevDialog)
            .show()

    }

    override fun onDestroy() {
        super.onDestroy()
        MyApp.webrtcSocketManager.sendHangUp()
        MyApp.webrtcSocketManager.release()
        MyApp.socketEventViewModel.disconnect()
        Logger.i("onDestroy")
    }

    // c8789e02-e585-470d-a684-1b3991fd4b64


    override fun onResume() {
        super.onResume()
        val extras = intent.extras?.getString(JPushInterface.EXTRA_NOTIFICATION_ACTION_EXTRA)
        Logger.e("收到极光推送的报警recordId:$extras")
        if (extras != null) {
            jPushExtraMessage = Gson().fromJson(extras, JPushExtraMessage::class.java)
        }

    }

    override fun onRestart() {
        super.onRestart()
        Logger.i("连接的网络的IP:${NetworkUtil.getIPAddress(true)}")
    }

    fun onExit(v: View) {
        //loginOut()
        startActivity(SettingActivity::class.java)
    }

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
                binding.messageIv.setImageResource(R.mipmap.message_icon_select)
                binding.deviceManagerIv.setImageResource(R.mipmap.dev_manager)
                transaction.setCustomAnimations(
                    R.anim.action_left_enter,
                    R.anim.action_left_exit
                )
            } else {
                binding.messageIv.setImageResource(R.mipmap.message_icon)
                binding.deviceManagerIv.setImageResource(R.mipmap.dev_manager_select)
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