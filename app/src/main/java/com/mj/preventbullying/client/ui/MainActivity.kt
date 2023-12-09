package com.mj.preventbullying.client.ui

import coil.load
import com.mj.preventbullying.client.NetworkUtil
import com.mj.preventbullying.client.R
import com.mj.preventbullying.client.databinding.ActivityMainBinding
import com.mj.preventbullying.client.ui.login.LoginViewModel
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseMvActivity
import java.util.UUID

/**
 * 主页
 */
class MainActivity : BaseMvActivity<ActivityMainBinding, LoginViewModel>() {
    private var randomStr: String? = null
    override fun getViewBinding(): ActivityMainBinding {
       return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun initParam() {

    }

    override fun initData() {

    }

    override fun initViewObservable() {

    }

    override fun initView() {
        postDelayed({
            Logger.i("网络是否可以：${NetworkUtil.isAvailable()},IP:${NetworkUtil.getIPAddress(true)}")
            randomStr = UUID.randomUUID().toString()
            val url = "http://192.168.1.6:9999/code?randomStr=123456"
            //val url="https://login.sina.com.cn/cgi/pin.php?r=9967937&s=0&p=gz-d0dc363f6a4523cbd602a5a10f00c59b"
            binding.codeImage.load(url){
                error(R.drawable.ic_launcher_background)
            }
//            Glide.with(this)
//                .load(url)
//                .into(binding.codeImage!!)
            Logger.i("加载验证码：$url")
        }, 1500)
    }

    override fun initListener() {

    }

}