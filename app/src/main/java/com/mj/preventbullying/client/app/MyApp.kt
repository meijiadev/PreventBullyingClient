package com.mj.preventbullying.client.app

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Process
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import cn.jpush.android.api.JPushInterface
import cn.jpush.android.ups.JPushUPSManager
import com.clj.fastble.BleManager
import com.hjq.toast.ToastLogInterceptor
import com.hjq.toast.ToastUtils
import com.hjq.toast.style.WhiteToastStyle
import com.mj.preventbullying.client.Constant
import com.mj.preventbullying.client.tool.SpManager
import com.mj.preventbullying.client.ui.viewmodel.GlobalEventViewModel
import com.mj.preventbullying.client.ui.viewmodel.TimerViewModel
import com.mj.preventbullying.client.webrtc.SocketEventViewModel
import com.mj.preventbullying.client.webrtc.WebrtcSocketManager

import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy


class MyApp : Application(), ViewModelStoreOwner {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        lateinit var socketEventViewModel: SocketEventViewModel
        lateinit var timerViewModel: TimerViewModel
        lateinit var webrtcSocketManager: WebrtcSocketManager
        lateinit var globalEventViewModel: GlobalEventViewModel

    }

    private lateinit var mAppViewModelStore: ViewModelStore

    /**
     * 作用域为Application的ViewModel，整个应用程序周期（用于在不同的Activity之间传递数据）
     */
    private lateinit var mApplicationProvider: ViewModelProvider
    override fun onCreate() {
        super.onCreate()
        //在Application中的onCreate方法中
        //规避app启动后执行俩次
        val processName = getProcessName(this, Process.myPid())
        if (processName != null) {
            val defaultProcess = processName == "com.mj.preventbullying.client"
            if (defaultProcess) {
                //当前应用的初始化
                context = this
                init()
            }
        }
    }

    private fun init() {
        val formatStrategy = PrettyFormatStrategy
            .newBuilder()
            .showThreadInfo(false)
            .methodCount(2)
            .tag("MJ-prevent")
            .build()
        Logger.addLogAdapter(AndroidLogAdapter(formatStrategy))
//        Logger.i("启动-start-1")
        SpManager.init(this)
        // 初始化吐司
        ToastUtils.init(this, WhiteToastStyle())
        com.mj.preventbullying.client.tool.ActivityManager.getInstance().init(this)
        // 设置调试模式
        // 设置 Toast 拦截器
        ToastUtils.setInterceptor(ToastLogInterceptor())
        //Logger.i("启动-start-2")
        initJGPush()
        //Logger.i("启动-start-3")
        BleManager.getInstance().init(this)
        BleManager.getInstance()
            .enableLog(false)
            .setReConnectCount(2, 5000).operateTimeout = 5000
        //Logger.i("启动-start-4")
        mAppViewModelStore = ViewModelStore()
        mApplicationProvider = ViewModelProvider(this)
        timerViewModel = getApplicationViewModel(TimerViewModel::class.java)
        socketEventViewModel = getApplicationViewModel(SocketEventViewModel::class.java)
        webrtcSocketManager = getApplicationViewModel(WebrtcSocketManager::class.java)
        globalEventViewModel = getApplicationViewModel(GlobalEventViewModel::class.java)
        //Logger.i("启动-start-5")
    }

    /**
     * 获取作用域在Application的ViewModel对象
     */
    fun <T : ViewModel> getApplicationViewModel(modelClass: Class<T>): T {
        return mApplicationProvider.get(modelClass)
    }

    override val viewModelStore: ViewModelStore
        get() = mAppViewModelStore


    private fun initJGPush() {
        Logger.e("初始化极光推送")
        JPushInterface.setDebugMode(true)
        JPushInterface.init(this)
        JPushUPSManager.registerToken(
            this, "1f56ed865ec03bb22a91c9ed", null, ""
        ) {
            SpManager.putString(Constant.REGISTER_ID_KEY, JPushInterface.getRegistrationID(this))
            Logger.e("initJGPush: ${it.token},registerID:${JPushInterface.getRegistrationID(this)}")
        }
    }

    /**
     * @param cxt
     * @param pid
     * @return 获取进程名称
     */
    @SuppressLint("ServiceCast")
    fun getProcessName(cxt: Context, pid: Int): String? {
        val am = cxt.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val runningApps: List<android.app.ActivityManager.RunningAppProcessInfo> =
            am.runningAppProcesses
                ?: return null
        for (procInfo in runningApps) {
            if (procInfo.pid === pid) {
                return procInfo.processName
            }
        }
        return null
    }


}