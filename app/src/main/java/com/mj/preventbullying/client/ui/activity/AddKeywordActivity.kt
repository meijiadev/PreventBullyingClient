package com.mj.preventbullying.client.ui.activity

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.lxj.xpopup.XPopup
import com.mj.preventbullying.client.app.AppMvActivity
import com.mj.preventbullying.client.app.MyApp
import com.mj.preventbullying.client.databinding.ActivityKeywordAddBinding
import com.mj.preventbullying.client.http.result.VRecord
import com.mj.preventbullying.client.ui.dialog.ItemListDialog
import com.mj.preventbullying.client.ui.viewmodel.AddViewModel
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseViewModel

/**
 * Create by MJ on 2024/1/19.
 * Describe :
 */

class AddKeywordActivity : AppMvActivity<ActivityKeywordAddBinding, AddViewModel>() {
    private var voiceList: List<VRecord>? = null
    override fun getViewBinding(): ActivityKeywordAddBinding {
        return ActivityKeywordAddBinding.inflate(layoutInflater)
    }

    override fun initParam() {
        binding.titleLl.titleTv.text = "新增关键词"
        binding.orgListTv.text = MyApp.globalEventViewModel.getSchoolName()
        viewModel.getVoiceList()
    }

    override fun initData() {


    }

    override fun initViewObservable() {
        binding.officialLl.setOnClickListener {
            showVoiceDialog(it)
        }
    }

    private fun showVoiceDialog(v: View) {
        val location = IntArray(2)
        v.getLocationOnScreen(location)
        val y = location[1]
        val itemListDialog = ItemListDialog(this).setVoiceList(voiceList).onOrgListener {

        }
        XPopup.Builder(this)
            .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
            .hasShadowBg(false)
            .hasBlurBg(false)
            .isViewMode(true)
            .offsetY(y + v.height)
            .asCustom(itemListDialog)
            .show()
    }

    override fun initView() {

    }

    override fun initListener() {
        viewModel.voiceListEvent.observe(this) {
            voiceList = it
        }
    }

}