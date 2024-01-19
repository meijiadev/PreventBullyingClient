package com.mj.preventbullying.client.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter4.BaseQuickAdapter
import com.mj.preventbullying.client.Constant
import com.mj.preventbullying.client.app.MyApp
import com.mj.preventbullying.client.databinding.FragmentKeywordManagerBinding
import com.mj.preventbullying.client.tool.SpManager
import com.mj.preventbullying.client.ui.adapter.KeywordAdapter
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

    private var isHideFragment: Boolean = true
    private var mainViewModel: MainViewModel? = null

    // private var curOrgId: String? = null
    private var keywordAdapter: KeywordAdapter? = null

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
        keywordAdapter = KeywordAdapter()
        keywordAdapter?.setItemAnimation(BaseQuickAdapter.AnimationType.ScaleIn)
        val layoutManager = GridLayoutManager(requireContext(), 2)
        binding.keywordRecycler.layoutManager = layoutManager
        binding.keywordRecycler.adapter = keywordAdapter
    }

    override fun initViewObservable() {

    }

    override fun initView() {

    }

    override fun initListener() {
        MyApp.globalEventViewModel.orgEvent.observe(this) {
            if (!isHideFragment) {
                viewModel.getKeywords()
            }
        }
        viewModel.keywordEvent.observe(this) {
            val list = it.data.records
            keywordAdapter?.submitList(list)

        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        isHideFragment = hidden
        if (!hidden) {
            viewModel.getKeywords()
        }
    }
}