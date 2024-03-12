package com.mj.preventbullying.client.ui.activity

import android.webkit.WebSettings
import androidx.navigation.fragment.findNavController
import com.mj.preventbullying.client.app.AppMvActivity
import com.mj.preventbullying.client.databinding.ActivityPrivacyBinding
import com.mj.preventbullying.client.http.service.ApiService
import com.sjb.base.base.BaseViewModel

/**
 * Create by MJ on 2024/3/12.
 * Describe :
 */

class PrivacyActivity:AppMvActivity<ActivityPrivacyBinding,BaseViewModel>() {
    override fun getViewBinding(): ActivityPrivacyBinding {
        return ActivityPrivacyBinding.inflate(layoutInflater)
    }

    override fun initParam() {
        binding.titleLayout.titleTv.text="隐私政策"
    }

    override fun initData() {
        binding.run {
            webView.loadUrl(ApiService.policy_file_url)
            // 支持App内部javascript交互
            webView.settings.javaScriptEnabled = true
            // 自适应屏幕
            webView.settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
            webView.settings.loadWithOverviewMode = true
            // 设置可以支持缩放
            webView.settings.setSupportZoom(true)
            // 扩大比例的缩放
            webView.settings.useWideViewPort = true
            // 设置是否出现缩放工具
            webView.settings.builtInZoomControls = true
            webView.settings.displayZoomControls = false
        }

    }

    override fun initViewObservable() {
        binding.titleLayout.backIv.setOnClickListener {
          finish()
        }

    }

    override fun initView() {

    }

    override fun initListener() {
    }
}