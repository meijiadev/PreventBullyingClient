package com.mj.preventbullying.client.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter4.BaseQuickAdapter
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupAnimation
import com.mj.preventbullying.client.R
import com.mj.preventbullying.client.app.MyApp
import com.mj.preventbullying.client.databinding.FragmentKeywordManagerBinding
import com.mj.preventbullying.client.ui.adapter.KeywordAdapter
import com.mj.preventbullying.client.ui.dialog.MessageTipsDialog
import com.mj.preventbullying.client.ui.viewmodel.KeywordViewModel
import com.mj.preventbullying.client.ui.viewmodel.MainViewModel
import com.sjb.base.base.BaseMvFragment
import com.sjb.base.view.SwitchButton

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

    private var curPosition: Int = 0

    private var enableView: SwitchButton? = null

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
        // 是否启用语音
        keywordAdapter?.addOnItemChildClickListener(R.id.enable_bt) { adapter, view, position ->
            val record = keywordAdapter?.getItem(position)
            val enable: Boolean = if (view is SwitchButton) {
                enableView = view
                view.isChecked()
            } else {
                false
            }
            curPosition = position
            record?.let { viewModel.enableKeyword(it, enable) }
        }
        keywordAdapter?.addOnItemChildClickListener(R.id.delete_iv) { adapter, view, position ->
            val tipsDialog = MessageTipsDialog(requireContext()).setTitle("是否确定删除该关键词？")
                .setListener(object : MessageTipsDialog.OnListener {
                    override fun onCancel() {

                    }

                    override fun onConfirm() {
                        runCatching {
                            val keywordId = keywordAdapter?.getItem(position)?.keywordId?.toLong()
                            if (keywordId != null) {
                                curPosition = position
                                viewModel.deleteKeyword(keywordId)
                            } else {
                                toast("删除失败！")
                            }
                        }.onFailure {
                            toast("删除错误！")
                        }
                    }
                })
            XPopup.Builder(requireContext()).isViewMode(true)
                .popupAnimation(PopupAnimation.TranslateFromBottom).asCustom(tipsDialog).show()

        }

        keywordAdapter?.addOnItemChildClickListener(R.id.amend_iv) { adapter, view, position ->


        }
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
        viewModel.enableKeywordEvent.observe(this) {
            if (it == true) {
                toast("操作成功")
                viewModel.getKeywords()
            } else {
                toast("操作失败！")
                val enable = enableView?.isChecked() ?: false
                enableView?.setChecked(!enable)
            }
        }
        viewModel.deleteKeywordEvent.observe(this) {
            if (it == true) {
                keywordAdapter?.removeAt(curPosition)
                //  keywordAdapter?.notifyDataSetChanged()
            } else {
                toast("删除失败！")
            }
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