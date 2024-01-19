package com.mj.preventbullying.client.ui.activity

import android.text.Editable
import android.text.TextWatcher
import com.mj.preventbullying.client.app.AppMvActivity
import com.mj.preventbullying.client.app.MyApp
import com.mj.preventbullying.client.databinding.ActivityKeywordAddBinding
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseViewModel

/**
 * Create by MJ on 2024/1/19.
 * Describe :
 */

class AddKeywordActivity : AppMvActivity<ActivityKeywordAddBinding, BaseViewModel>() {
    override fun getViewBinding(): ActivityKeywordAddBinding {
        return ActivityKeywordAddBinding.inflate(layoutInflater)
    }

    override fun initParam() {
        binding.titleLl.titleTv.text = "新增关键词"
        binding.orgListTv.text = MyApp.globalEventViewModel.getSchoolName()
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