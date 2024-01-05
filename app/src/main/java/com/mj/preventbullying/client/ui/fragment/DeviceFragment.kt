package com.mj.preventbullying.client.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import cn.jpush.android.api.JPushInterface
import com.chad.library.adapter4.BaseQuickAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.enums.PopupAnimation
import com.mj.preventbullying.client.R
import com.mj.preventbullying.client.databinding.FragmentDeviceBinding
import com.mj.preventbullying.client.foldtree.TreeModel
import com.mj.preventbullying.client.http.result.DevType
import com.mj.preventbullying.client.http.result.DeviceRecord
import com.mj.preventbullying.client.ui.viewmodel.MainViewModel
import com.mj.preventbullying.client.ui.adapter.DeviceListAdapter
import com.mj.preventbullying.client.ui.dialog.DevInfoDialog
import com.mj.preventbullying.client.ui.dialog.MessageTipsDialog
import com.mj.preventbullying.client.ui.viewmodel.DeviceViewModel
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseMvFragment

/**
 * Create by MJ on 2023/12/11.
 * Describe :
 */

class DeviceFragment : BaseMvFragment<FragmentDeviceBinding, DeviceViewModel>() {
    private var deviceListAdapter: DeviceListAdapter? = null
    private var deviceList: MutableList<DeviceRecord>? = null

    private var mainViewModel: MainViewModel? = null
    private var treeList: MutableList<TreeModel>? = null
    private var typeList: MutableList<DevType>? = null

    companion object {
        fun newInstance(): DeviceFragment {
            val args = Bundle()
            val fragment = DeviceFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): FragmentDeviceBinding {
        return FragmentDeviceBinding.inflate(inflater, parent, false)
    }

    override fun initParam() {
        viewModel.getAllDevices()
        mainViewModel = getActivityViewModel(MainViewModel::class.java)

    }

    override fun initData() {
        deviceListAdapter = DeviceListAdapter()
        deviceListAdapter?.setItemAnimation(BaseQuickAdapter.AnimationType.ScaleIn)
        //deviceListAdapter?.addAll(deviceList)
        val layoutManager = GridLayoutManager(requireContext(), 2)
        binding.deviceList.layoutManager = layoutManager
        binding.deviceList.adapter = deviceListAdapter
    }

    override fun initViewObservable() {
        deviceListAdapter?.addOnItemChildClickListener(R.id.delete_iv) { adapter, view, position ->
            val tipsDialog = MessageTipsDialog(requireContext()).setTitle("是否确定删除该设备？")
                .setListener(object : MessageTipsDialog.OnListener {
                    override fun onCancel() {

                    }

                    override fun onConfirm() {
                        runCatching {
                            val deviceId = deviceList?.get(position)?.deviceId?.toLong()
                            if (deviceId != null) {
                                viewModel.deleteDev(deviceId)
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
        deviceListAdapter?.addOnItemChildClickListener(R.id.amend_iv) { adapter, view, position ->
            val devMsg = deviceList?.get(position)
            showDialogInfo(devMsg)
        }

//        deviceListAdapter?.setOnItemLongClickListener { adapter, view, position ->
//
//            true
//        }

    }

    private fun getDevInfoList() {
        mainViewModel?.getOrgList()
        mainViewModel?.getDevType()
    }

    private var amendDevDialog: DevInfoDialog? = null
    private fun showDialogInfo(dev: DeviceRecord?) {
        getDevInfoList()
        amendDevDialog =
            DevInfoDialog(requireContext()).setOnListener(object : DevInfoDialog.AddDevListener {

                override fun onCancel() {

                }

                override fun onConfirm(
                    sn: String,
                    //name: String,
                    orgId: Long,
                    orgName: String,
                    location: String,
                    modelCode: String,
                    desc: String?
                ) {
                    dev?.let {
                        viewModel.amendDev(dev.deviceId, sn, orgId, modelCode, location, desc)
                    }
                }


            })
                .setOrgData(treeList)
                .setTypeData(typeList)
                .setTitleMsg("修改设备信息")
                .setAmendData(dev)
        XPopup.Builder(requireContext())
            .isViewMode(true)
            .dismissOnTouchOutside(false)
            .dismissOnBackPressed(false)
            .isDestroyOnDismiss(true)
            .popupAnimation(PopupAnimation.TranslateFromBottom)
            .asCustom(amendDevDialog)
            .show()

    }


    override fun initView() {
        binding.smartRefreshLayout.let {
            it.setReboundDuration(300)
            it.setOnRefreshListener {
                Logger.i("下拉刷新")
                viewModel.getAllDevices()
            }
        }

    }

    override fun initListener() {
        viewModel.deviceResultEvent.observe(this) {
            deviceList = it.data.records
            binding.smartRefreshLayout.finishRefresh(1000)
            deviceListAdapter?.submitList(deviceList)
        }

        viewModel.deleteDevEvent.observe(this) {
            if (it) {
                viewModel.getAllDevices()
            }
        }

        viewModel.amendDevEvent.observe(this) {
            if (it) {
                viewModel.getAllDevices()
            } else {
                toast("设备修改失败，请重试")
            }
        }
        mainViewModel?.addDevEvent?.observe(this) {
            if (it) {
                viewModel.getAllDevices()
            }

        }


        mainViewModel?.orgTreeEvent?.observe(this) {
            // 接收到组织树列表
            val tree = it?.data
            val gson = Gson()
            val jsonStr = gson.toJson(tree)
            treeList = gson.fromJson(jsonStr, object : TypeToken<List<TreeModel?>?>() {}.type)
            Logger.i("转化之后的组织树：${treeList?.size}")
            amendDevDialog?.setOrgData(treeList)
        }
        mainViewModel?.devTypeEvent?.observe(this) {
            typeList = it?.data as MutableList<DevType>?
            amendDevDialog?.setTypeData(typeList)
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        Logger.i("是否隐藏：$hidden")
        if (!hidden) {
            viewModel.getAllDevices()
        }
    }
}