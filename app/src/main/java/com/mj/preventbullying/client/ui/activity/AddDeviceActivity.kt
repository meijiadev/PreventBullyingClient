package com.mj.preventbullying.client.ui.activity

import android.view.View
import com.google.gson.Gson
import com.lxj.xpopup.XPopup
import com.mj.preventbullying.client.R
import com.mj.preventbullying.client.app.AppMvActivity
import com.mj.preventbullying.client.app.MyApp
import com.mj.preventbullying.client.bletooth.BleData
import com.mj.preventbullying.client.bletooth.ChatService
import com.mj.preventbullying.client.databinding.ActivityAddActivityBinding
import com.mj.preventbullying.client.foldtree.TreeModel
import com.mj.preventbullying.client.http.result.DevType
import com.mj.preventbullying.client.http.service.ApiService
import com.mj.preventbullying.client.tool.dismissLoadingExt
import com.mj.preventbullying.client.tool.showLoadingExt
import com.mj.preventbullying.client.ui.dialog.ItemListDialog
import com.mj.preventbullying.client.ui.viewmodel.AddDevViewModel
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseViewModel

/**
 * Create by MJ on 2024/1/24.
 * Describe : 添加设备 or 修改设备信息
 */

class AddDeviceActivity : AppMvActivity<ActivityAddActivityBinding, AddDevViewModel>() {

    // 设备注册相关页面
    private var treeList: MutableList<TreeModel>? = mutableListOf()
    private var typeList: MutableList<DevType>? = null
    private var deviceSn: String? = null
    private var curOrgId: Long = 0
    private var isAddDev = false
    private var deviceId: String? = null

    override fun getViewBinding(): ActivityAddActivityBinding {
        return ActivityAddActivityBinding.inflate(layoutInflater)
    }

    override fun initParam() {
        isAddDev = intent.getBooleanExtra("isAddDevice", false)
        deviceSn = intent.getStringExtra("snCode")
        binding.apply {
            snEt.setText(deviceSn)
            if (!isAddDev) {
                deviceId = intent.getStringExtra("deviceId")
                val devType = intent.getStringExtra("deviceType")
                val location = intent.getStringExtra("location")
                val desc = intent.getStringExtra("desc")
                // 编辑页面
                titleLy.titleTv.text = "设备编辑"
                if (devType != null)
                    devTypeTv.text = devType
                if (location != null)
                    locationEt.setText(location)
                if (desc != null)
                    desEt.setText(desc)
                orgListTv.text = MyApp.globalEventViewModel.getSchoolName()
                curOrgId = MyApp.globalEventViewModel.getSchoolId()?.toLong() ?: 0
            } else {
                titleLy.titleTv.text = "设备添加"
            }
        }
    }

    override fun initData() {
        getDevInfoList()
    }

    /**
     * 获取组织列表和设备类型
     */
    private fun getDevInfoList() {
        //viewModel.getOrgList()
        viewModel.getDevType()
    }

    override fun initViewObservable() {
        binding.run {
            orgLl.setOnClickListener {
                showOrgDialog(it)
            }

            devTypeLl.setOnClickListener {
                showTypeDialog(it)
            }

            confirmTv.setOnClickListener {
                val sn = snEt.text.toString()
                // val name = nameEt.text.toString()
                val orgMsg = orgListTv.text.toString()
                val devType = devTypeTv.text.toString()
                val location = locationEt.text.toString()
                val desc = desEt.text.toString()
                if (sn.isEmpty() || orgMsg.isEmpty() || devType.isEmpty() || location.isEmpty()) {
                    toast("请按照指示填写必填信息")
                } else {
                    if (isAddDev) {
                        viewModel.addDev(sn, curOrgId, devType, location, desc)
                        showLoadingExt(R.string.add_device_ing)
                    } else {
                        deviceId?.let {
                            viewModel.amendDev(it, sn, curOrgId, devType, location, desc)
                        }
                    }
                }
            }
        }

    }

    /**
     * 显示组织列表弹窗
     */
    private fun showOrgDialog(v: View) {
        val location = IntArray(2)
        v.getLocationOnScreen(location)
        val y = location[1]
        val itemListDialog = ItemListDialog(this).setOrgData(treeList).onOrgListener {
            Logger.i("选择的组织")
            binding.orgListTv.text = it.name
            curOrgId = it.id.toLong()
        }
        XPopup.Builder(this).isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
            .hasShadowBg(false).hasBlurBg(false).offsetY(y + v.height).asCustom(itemListDialog)
            .show()
    }

    /**
     * 显示设备的类型列表
     */
    private fun showTypeDialog(v: View) {
        val location = IntArray(2)
        v.getLocationOnScreen(location)
        val y = location[1]
        val itemListDialog = ItemListDialog(this).setDevType(typeList).onTypeListener {
            Logger.i("选择的类型")
            binding.devTypeTv.text = it
        }
        XPopup.Builder(this).isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
            .hasShadowBg(false).hasBlurBg(false).offsetY(y + v.height).asCustom(itemListDialog)
            .show()
    }

    override fun initView() {

    }

    override fun initListener() {
        viewModel.devTypeEvent.observe(this) {
            treeList?.clear()
            MyApp.globalEventViewModel.treeList?.let { it1 -> treeList?.addAll(it1) }
            typeList = it?.data as MutableList<DevType>?
        }
        viewModel.addDevEvent.observe(this) {
            if (it) {
                toast("设备添加成功！")
                val data = BleData(2, ApiService.getHostUrl(), deviceSn)
                val jsonStr = Gson().toJson(data)
                ChatService.instance.write(jsonStr.toByteArray())
            } else {
                toast("设备添加失败")
                dismissLoadingExt()
            }
        }
        viewModel.amendDevEvent.observe(this) {
            if (it) {
                //viewModel.getAllDevices()
                finish()
            } else {
                toast("设备修改失败，请重试")
            }
        }

        ChatService.instance.onDevRegister {
            toast("设备已注册成功！")
            dismissLoadingExt()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ChatService.instance.stop()
    }
}