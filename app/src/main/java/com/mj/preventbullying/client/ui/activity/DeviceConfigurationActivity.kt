package com.mj.preventbullying.client.ui.activity

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.View
import android.widget.RelativeLayout

import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gyf.immersionbar.ktx.immersionBar
import com.mj.preventbullying.client.R
import com.mj.preventbullying.client.app.AppMvActivity
import com.mj.preventbullying.client.bletooth.BleData
import com.mj.preventbullying.client.bletooth.BleTooth
import com.mj.preventbullying.client.bletooth.ChatService
import com.mj.preventbullying.client.databinding.ActivityDeviceConfigBinding
import com.mj.preventbullying.client.foldtree.TreeListAdapter
import com.mj.preventbullying.client.foldtree.TreeModel
import com.mj.preventbullying.client.http.result.DevType
import com.mj.preventbullying.client.http.service.ApiService
import com.mj.preventbullying.client.tool.dismissLoadingExt
import com.mj.preventbullying.client.tool.showLoadingExt
import com.mj.preventbullying.client.ui.adapter.BleToothAdapter
import com.mj.preventbullying.client.ui.adapter.DevTypeAdapter
import com.mj.preventbullying.client.ui.viewmodel.MainViewModel
import com.orhanobut.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Create by MJ on 2024/1/13.
 * Describe : 设备配网
 * 1、连接蓝牙
 * 2、发送服务器地址
 * 3、配置设备参数
 */

class DeviceConfigurationActivity : AppMvActivity<ActivityDeviceConfigBinding, MainViewModel>() {

    private var bleAdapter: BluetoothAdapter? = null

    private var filter = IntentFilter()

    private var isScanning = false
    private var bleDevices = mutableListOf<BleTooth>()

    private var bTAdapter: BleToothAdapter? = null
    private lateinit var chatService: ChatService
    private var curPosition: Int = 0

    // 设备注册相关页面
    private var treeList: MutableList<TreeModel>? = null
    private var typeList: MutableList<DevType>? = null
    private var treeAdapter: TreeListAdapter? = null
    private var devTypeAdapter: DevTypeAdapter? = null
    private var deviceSn: String? = null
    private var curOrgId: Long = 0


    override fun getViewBinding(): ActivityDeviceConfigBinding {
        return ActivityDeviceConfigBinding.inflate(layoutInflater)
    }

    override fun initParam() {
        binding.titleLy.titleTv.text = "设备配网"
        immersionBar {
            //深色字体
            statusBarDarkFont(true)
        }
    }

    override fun initData() {
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(mReceiver, filter)
        bleAdapter = BluetoothAdapter.getDefaultAdapter()
        chatService = ChatService()
        getDevInfoList()

    }

    /**
     * 获取组织列表和设备类型
     */
    private fun getDevInfoList() {
        viewModel.getOrgList()
        viewModel.getDevType()
    }

    override fun initView() {
        lifecycleScope.launch {
            delay(500)
            doScan()
        }
        bTAdapter = BleToothAdapter()
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.bleRecycler.layoutManager = layoutManager
        binding.bleRecycler.adapter = bTAdapter
        // 设备注册相关页面业务
        treeAdapter = TreeListAdapter()
        devTypeAdapter = DevTypeAdapter()
        val lyManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.listItemRecycler.layoutManager = lyManager
    }

    override fun initViewObservable() {
        binding.titleLy.backIv.setOnClickListener {
            finish()
        }

        /************************************* 设备配网UI*****************************/
        binding.restartScanTv.setOnClickListener {
            doScan()
        }

        bTAdapter?.addOnItemChildClickListener(R.id.ble_connect_bt) { adapter, view, position ->
            curPosition = position
            if (bTAdapter?.getItem(position)?.connected == false) {
                val mac = bTAdapter?.getItem(position)?.mac
                Logger.i("连接该设备：$mac")
                bleAdapter?.let {
                    chatService.start()
                    chatService.connect(it.getRemoteDevice(mac))
                    showLoadingExt(R.string.connecting)
                }
            } else {
                toast("设备已连接！")
            }
        }
        binding.networkConfigTv.setOnClickListener {
            val data = BleData(1, ApiService.getHostUrl())
            val jsonStr = Gson().toJson(data)
            chatService.write(jsonStr.toByteArray())
            showLoadingExt(R.string.network_config_ing)
        }

        /**********************************设备注册页面业务***************************/
        binding.run {
            orgLl.setOnClickListener {
                Logger.i("点击组织选择")
                if (orgListLl.visibility == View.GONE) {
                    refreshOrgList()
                    val layoutParams = orgListLl.layoutParams as RelativeLayout.LayoutParams
                    layoutParams.addRule(RelativeLayout.BELOW, R.id.org_ll)
                    orgListLl.layoutParams = layoutParams
                    orgEnterIv.rotation = 90f
                    orgListLl.visibility = View.VISIBLE
                } else {
                    orgEnterIv.rotation = 0f
                    orgListLl.visibility = View.GONE
                }
            }

            devTypeLl.setOnClickListener {
                if (orgListLl.visibility == View.GONE) {
                    refreshDevList()
                    val layoutParams = orgListLl.layoutParams as RelativeLayout.LayoutParams
                    layoutParams.addRule(RelativeLayout.BELOW, R.id.dev_type_ll)
                    orgListLl.layoutParams = layoutParams

                    typeEnterIv.rotation = 90f
                    orgListLl.visibility = View.VISIBLE
                } else {
                    typeEnterIv.rotation = 0f
                    orgListLl.visibility = View.GONE
                }

            }
            treeAdapter?.setOnItemClickListener { adapter, view, position ->
                val mode = treeAdapter?.getItem(position)
                if (mode?.children != null) {
                    treeAdapter?.setOpenOrClose(treeList, position)
                    treeAdapter?.notifyDataSetChanged()
                } else {
                    val org = treeList?.get(position)
                    Logger.i("点击的项：${org.toString()}")
                    orgListTv.text = org?.name

                    runCatching {
                        curOrgId = org?.id?.toLong() ?: 0
                    }.onFailure {
                        Logger.e("error:${it.message}")
                        curOrgId = 0
                    }
                    orgEnterIv.rotation = 0f
                    orgListLl.visibility = View.GONE

                }
            }
            devTypeAdapter?.setOnItemClickListener { adapter, v, position ->
                val type = typeList?.get(position)
                Logger.i("点击的项：${type}")
                devTypeTv.text = type?.value
                orgEnterIv.rotation = 0f
                orgListLl.visibility = View.GONE
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
                    viewModel.addDev(sn, curOrgId, devType, location, desc)
                    showLoadingExt(R.string.add_device_ing)
                }
            }
        }
    }

    /**
     *  刷新组织列表
     */
    @SuppressLint("NotifyDataSetChanged")
    fun refreshOrgList() {
//        treeAdapter.setOpenOrClose()
        binding.listItemRecycler.adapter = treeAdapter
        treeAdapter?.submitList(treeList)
        treeAdapter?.notifyDataSetChanged()
    }

    /**
     * 刷新设备型号列表
     */
    @SuppressLint("NotifyDataSetChanged")
    fun refreshDevList() {
        binding.listItemRecycler.adapter = devTypeAdapter
        devTypeAdapter?.submitList(typeList)
        devTypeAdapter?.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun initListener() {
        chatService.onConnected {
            lifecycleScope.launch(Dispatchers.Main) {
                val bleTooth = bleDevices[curPosition]
                if (it) {
                    Logger.i("连接成功")
                    bleTooth.connected = true
                    binding.networkConfigTv.visibility = View.VISIBLE
                } else {
                    bleTooth.connected = false
                    Logger.i("连接断开")
                    binding.networkConfigTv.visibility = View.GONE
                }
                bTAdapter?.notifyDataSetChanged()
                dismissLoadingExt()
            }
        }
        // 设备sn码
        chatService.onSnListener {
            lifecycleScope.launch(Dispatchers.Main) {
                dismissLoadingExt()
                deviceSn = it
                binding.networkConfigLl.visibility = View.GONE
                binding.registerDevRl.visibility = View.VISIBLE
                binding.snEt.setText(deviceSn)
            }
        }

        chatService.onDevRegister {
            toast("设备已注册成功！")
            dismissLoadingExt()
            finish()
        }
        chatService.onDevHasRegister {
            toast("该设备已经配置过，无需再配置！")
            dismissLoadingExt()
        }

        viewModel.orgTreeEvent.observe(this) {
            // 接收到组织树列表
            val tree = it?.data
            val gson = Gson()
            val jsonStr = gson.toJson(tree)
            treeList = gson.fromJson(jsonStr, object : TypeToken<List<TreeModel?>?>() {}.type)
            Logger.d("转化之后的组织树：${treeList?.size}")
        }
        viewModel.devTypeEvent.observe(this) {
            typeList = it?.data as MutableList<DevType>?
        }
        viewModel.addDevEvent.observe(this) {
            if (it) {
                toast("设备添加成功！")
                val data = BleData(2, ApiService.getHostUrl(), deviceSn)
                val jsonStr = Gson().toJson(data)
                chatService.write(jsonStr.toByteArray())
            } else {
                dismissLoadingExt()
            }
            // dismissLoadingExt()
            //finish()
        }

    }

    @SuppressLint("MissingPermission")
    private fun doScan() {
        if (bleAdapter?.isDiscovering == true) {
            bleAdapter?.cancelDiscovery()
        }
        bleAdapter?.startDiscovery()
        bleDevices.clear()
        bTAdapter?.submitList(bleDevices)
        isScanning = true
        binding.bleRecycler.visibility = View.GONE
        binding.scanningTv.text = "正在扫描"
        binding.msgTipsTv.visibility = View.VISIBLE
        binding.restartScanTv.visibility = View.GONE
        binding.networkConfigTv.visibility = View.GONE
        Logger.i("开始搜索蓝牙设备")
        lifecycleScope.launch {
            while (isScanning) {
                delay(100)
                if (isScanning) {
                    binding.rateView.setStartAngle()
                }
            }
        }
    }


    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()
        if (bleAdapter != null) {
            bleAdapter?.cancelDiscovery()
        }
        chatService.stop()
        unregisterReceiver(mReceiver)
    }


    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                // if (device?.bondState != BluetoothDevice.BOND_BONDED) {
                if (device?.name == null) {
                    return
                }
                Logger.i("deviceName:${device.name},address:${device.address}")
                device.let { it ->
                    binding.bleRecycler.visibility = View.VISIBLE
                    if (it.name == "rk3566") {
                        val isExit = bleDevices.any { ble ->
                            ble.mac == it.address
                        }
                        if (!isExit) {
                            val bleTooth = BleTooth("AI防欺凌设备", it.address)
                            bleDevices.add(bleTooth)
                            bTAdapter?.submitList(bleDevices)
                        }
                    }
                }
                // }
            } else if ((BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action)
            ) {
                Logger.i("搜索完毕")
                isScanning = false
                if (bTAdapter?.items?.size == 0) {
                    binding.scanningTv.text = "未发现设备"
                    binding.msgTipsTv.visibility = View.GONE
                    binding.restartScanTv.visibility = View.VISIBLE
                } else {
                    binding.scanningTv.text = "扫描完成"
                    binding.msgTipsTv.visibility = View.GONE
                    binding.restartScanTv.visibility = View.VISIBLE
                }
            }
        }
    }


}