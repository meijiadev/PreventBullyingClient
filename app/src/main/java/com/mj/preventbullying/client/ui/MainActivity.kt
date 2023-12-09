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
 * Create by MJ on 2023/12/9.
 * Describe : 主页
 */
class MainActivity : BaseMvActivity<ActivityMainBinding, LoginViewModel>() {

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
    }

    override fun initListener() {

    }

}