package com.mj.preventbullying.client.ui.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.lxj.xpopup.XPopup
import com.mj.preventbullying.client.Constant
import com.mj.preventbullying.client.R
import com.mj.preventbullying.client.app.MyApp
import com.mj.preventbullying.client.databinding.FragmentDeviceBinding
import com.mj.preventbullying.client.databinding.FragmentHomeBinding
import com.mj.preventbullying.client.foldtree.TreeModel
import com.mj.preventbullying.client.tool.SpManager
import com.mj.preventbullying.client.ui.activity.MainActivity
import com.mj.preventbullying.client.ui.dialog.ItemListDialog
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseMvFragment
import com.sjb.base.base.BaseViewModel

/**
 * Create by MJ on 2024/3/25.
 * Describe :
 */

class HomeFragment : BaseMvFragment<FragmentHomeBinding, BaseViewModel>() {
    override fun getViewBinding(inflater: LayoutInflater, parent: ViewGroup?): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, parent, false)
    }

    override fun initParam() {

    }

    override fun initData() {

    }

    override fun initViewObservable() {
        binding.messageLl.setOnClickListener {
            Logger.i("点击去告警消息页面")
            findNavController().navigate(R.id.action_homeFragment_to_messageFragment)
        }
        binding.spreadIv.setOnClickListener {
            Logger.i("点击组织下拉框")
            showOrgDialog(binding.orgTv)
        }
        binding.devServerIv.setOnClickListener {
            if (MyApp.socketEventViewModel.isConnected) {
                toast("应用和设备服务器已连接成功")
            } else {
                toast("应用和设备服务器未连接，可能无法连接设备")
            }
        }
    }

    /**
     * 显示组织列表
     */
    private fun showOrgDialog(v: View) {
        val treeList = mutableListOf<TreeModel>()
        MyApp.globalEventViewModel.treeList?.let { treeList.addAll(it) }
        val location = IntArray(2)
        v.getLocationOnScreen(location)
        val y = location[1]
        val itemListDialog = ItemListDialog(requireContext()).setOrgData(treeList).onOrgListener {
            Logger.i("选择的组织:$it")
            binding.titleTv.text = it.name
            MyApp.globalEventViewModel.setSchoolName(it.name)
            MyApp.globalEventViewModel.setSchoolId(it.id)
            // SpManager.putString(Constant.ORG_ID_KEY, it.id)

        }
        XPopup.Builder(context)
            .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
            .hasShadowBg(false)
            .hasBlurBg(false)
            .isViewMode(true)
            .offsetY(y + v.height)
            .asCustom(itemListDialog)
            .show()
    }

    override fun initView() {
        val userId = SpManager.getString(Constant.USER_ID_KEY)
        val registerId = SpManager.getString(Constant.REGISTER_ID_KEY)
        if (userId != null && registerId != null) {
            MyApp.socketEventViewModel.initSocket(userId, registerId)
        }
        postDelayed({
            MyApp.globalEventViewModel.getAppVersion()
        }, 1500)


        binding.orgTv.text = MyApp.globalEventViewModel.getSchoolName()
    }

    override fun initListener() {

    }
}