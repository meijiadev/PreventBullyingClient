package com.mj.preventbullying.client.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import com.mj.preventbullying.client.Constant
import com.mj.preventbullying.client.tool.SpManager
import com.mj.preventbullying.client.databinding.ActivitySplashBinding
import com.mj.preventbullying.client.ui.login.LoginActivity
import com.sjb.base.base.BaseMvActivity
import com.sjb.base.base.BaseViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Create by MJ on 2023/12/12.
 * Describe :
 */

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseMvActivity<ActivitySplashBinding, BaseViewModel>() {
    override fun getViewBinding(): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(layoutInflater)
    }

    override fun initParam() {
        // 问题及方案：https://www.cnblogs.com/net168/p/5722752.html
        // 如果当前 Activity 不是任务栈中的第一个 Activity
        if (!isTaskRoot) {
            val intent: Intent? = intent
            // 如果当前 Activity 是通过桌面图标启动进入的
            if (((intent != null) && intent.hasCategory(Intent.CATEGORY_LAUNCHER)
                        && (Intent.ACTION_MAIN == intent.action))
            ) {
                // 对当前 Activity 执行销毁操作，避免重复实例化入口
                finish()
                return
            }
        }
    }

    override fun initData() {
        MainScope().launch {
            delay(1000)
            // 如果不为空
            if (SpManager.getString(Constant.ACCESS_TOKEN_KEY).isNullOrEmpty()) {
                startActivity(LoginActivity::class.java)
            } else {
                startActivity(MainActivity::class.java)
            }
            finish()
        }
    }

    override fun initViewObservable() {

    }

    override fun initView() {

    }

    override fun initListener() {

    }


    /**
     * 屏蔽返回键
     *
     */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //super.onBackPressed()
    }

}