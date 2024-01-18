package com.mj.preventbullying.client.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.mj.preventbullying.client.Constant
import com.mj.preventbullying.client.databinding.FragmentKeywordManagerBinding
import com.mj.preventbullying.client.tool.SpManager
import com.mj.preventbullying.client.ui.viewmodel.KeywordViewModel
import com.mj.preventbullying.client.ui.viewmodel.MainViewModel
import com.sjb.base.base.BaseMvFragment
import com.sjb.base.base.BaseViewModel

/**
 * Create by MJ on 2024/1/17.
 * Describe :关键词管理界面
 */

class KeywordManagerFragment : BaseMvFragment<FragmentKeywordManagerBinding, KeywordViewModel>() {

    companion object {
        fun newInstance(): KeywordManagerFragment {
            val args = Bundle()
            val fragment = KeywordManagerFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private var mainViewModel: MainViewModel? = null
   // private var curOrgId: String? = null

    override fun getViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): FragmentKeywordManagerBinding {
        return FragmentKeywordManagerBinding.inflate(layoutInflater, parent, false)
    }

    override fun initParam() {
        mainViewModel = getActivityViewModel(MainViewModel::class.java)
        //curOrgId = SpManager.getString(Constant.ORG_ID_KEY)
        viewModel.getKeywords()
    }

    override fun initData() {

    }

    override fun initViewObservable() {

    }

    override fun initView() {

    }

    override fun initListener() {

    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            viewModel.getKeywords()
        }
    }
}