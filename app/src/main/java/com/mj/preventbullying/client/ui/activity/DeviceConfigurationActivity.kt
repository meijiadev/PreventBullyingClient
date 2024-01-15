package com.mj.preventbullying.client.ui.activity

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.View

import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.gyf.immersionbar.ktx.immersionBar
import com.mj.preventbullying.client.R
import com.mj.preventbullying.client.app.AppMvActivity
import com.mj.preventbullying.client.bletooth.BleData
import com.mj.preventbullying.client.bletooth.BleTooth
import com.mj.preventbullying.client.bletooth.ChatService
import com.mj.preventbullying.client.databinding.ActivityDeviceConfigBinding
import com.mj.preventbullying.client.http.service.ApiService
import com.mj.preventbullying.client.tool.dismissLoadingExt
import com.mj.preventbullying.client.tool.showLoadingExt
import com.mj.preventbullying.client.ui.adapter.BleToothAdapter
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Create by MJ on 2024/1/13.
 * Describe : 设备配网
 * 1、连接蓝牙
 * 2、发送服务器地址
 * 3、配置设备参数
 */

class DeviceConfigurationActivity : AppMvActivity<ActivityDeviceConfigBinding, BaseViewModel>() {
    private var bleAdapter: BluetoothAdapter? = null

    private var filter = IntentFilter()

    private var isScanning = false
    private var bleDevices = mutableListOf<BleTooth>()

    private var bTAdapter: BleToothAdapter? = null
    private lateinit var chatService: ChatService
    private var curPosition: Int = 0

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

    }

    override fun initViewObservable() {
        binding.titleLy.backIv.setOnClickListener {
            finish()
        }
        binding.restartScanTv.setOnClickListener {
            doScan()
        }

        bTAdapter?.addOnItemChildClickListener(R.id.ble_connect_bt) { adapter, view, position ->
            curPosition = position
            val mac = bTAdapter?.getItem(position)?.mac
            Logger.i("连接该设备：$mac")
            bleAdapter?.let {
                chatService.start()
                chatService.connect(it.getRemoteDevice(mac))
                showLoadingExt(R.string.connecting)
            }

        }
        binding.networkConfigTv.setOnClickListener {
            val data = BleData(1, ApiService.getHostUrl())
            val jsonStr = Gson().toJson(data)
            chatService.write(jsonStr.toByteArray())
        }
    }

    override fun initView() {
        lifecycleScope.launch {
            delay(1500)
            doScan()
        }
        bTAdapter = BleToothAdapter()
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.bleRecycler.layoutManager = layoutManager
        binding.bleRecycler.adapter = bTAdapter
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun initListener() {
        chatService.onConnected {
            Logger.i("连接成功")
            val bleTooth = bTAdapter?.getItem(curPosition)
            bleTooth?.let {
                bleTooth.connected = true
                bTAdapter?.add(curPosition, bleTooth)
                bTAdapter?.notifyDataSetChanged()
            }
            dismissLoadingExt()
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
                device.let {
                    binding.bleRecycler.visibility = View.VISIBLE
                    if (it.name == "rk3566") {
                        val bleTooth = BleTooth("AI防欺凌设备", it.address)
                        bleDevices.add(bleTooth)
                        bTAdapter?.submitList(bleDevices)
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