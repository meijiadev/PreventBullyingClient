package com.mj.preventbullying.client

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import cn.jpush.android.api.JPushInterface
import cn.jpush.android.ups.JPushUPSManager

import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger


class MyApp : Application(), ViewModelStoreOwner {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    private lateinit var mAppViewModelStore: ViewModelStore

    /**
     * 作用域为Application的ViewModel，整个应用程序周期（用于在不同的Activity之间传递数据）
     */
    private lateinit var mApplicationProvider: ViewModelProvider
    override fun onCreate() {
        super.onCreate()
        context = this
        Logger.addLogAdapter(AndroidLogAdapter())
        SpManager.init(this)
        mAppViewModelStore = ViewModelStore()
        mApplicationProvider = ViewModelProvider(this)

        initJGPush()
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
//        JPushInterface.setTags(this, mutableSetOf("HUAWEI"), object : TagAliasCallback {
//            override fun gotResult(p0: Int, p1: String?, p2: MutableSet<String>?) {
//
//            }
//
//        })

        JPushUPSManager.registerToken(
            this, "1f56ed865ec03bb22a91c9ed", null, ""
        ) {
            Logger.e("initJGPush: ${it.token},registerID:${JPushInterface.getRegistrationID(this)}")
        }


    }


}