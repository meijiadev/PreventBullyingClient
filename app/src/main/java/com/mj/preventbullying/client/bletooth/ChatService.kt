package com.mj.preventbullying.client.bletooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.google.gson.Gson
import com.orhanobut.logger.Logger
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

/**
 * Created by Excalibur on 2017/5/30.
 */
class ChatService {
    private val mAdapter: BluetoothAdapter
    private var mConnectThread: ConnectThread? = null
    private var mConnectedThread: ConnectedThread? = null
    private var mState: Int

    init {
        mAdapter = BluetoothAdapter.getDefaultAdapter()
        mState = STATE_NONE
    }

    @get:Synchronized
    @set:Synchronized
    var state: Int
        get() = mState
        private set(state) {
            mState = state
        }

    @Synchronized
    fun start() {
        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }
        Logger.i("启动chatServer")
        state = STATE_LISTEN
    }

    // 取消 Connecting Connected状态下的相关线程，然后运行新的mConnectThread线程
    @Synchronized
    fun connect(device: BluetoothDevice) {
        if (mState == STATE_CONNECTED) {
            if (mConnectThread != null) {
                mConnectThread!!.cancel()
                mConnectThread = null
            }
        }
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }
        mConnectThread = ConnectThread(device)
        mConnectThread?.start()
        state = STATE_CONNECTING
        Logger.i("去连接蓝牙")
    }

    // 开启一个ConnectThread来管理对应的当前连接。之前取消任意现存的mConnectThread
    // mConnectThread，mAcceptThread线程，然后开启新的mConnectThread，传入当前
    // 刚刚接受的socket连接，最后通过Handler来通知UI连接
    @Synchronized
    fun connected(
        socket: BluetoothSocket?
    ) {
        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }
        mConnectedThread = ConnectedThread(socket)
        mConnectedThread!!.start()
        Logger.i("blue socket连接成功！")
        state = STATE_CONNECTED
        onConnected?.invoke(true)
    }

    // 停止所有相关线程，设当前状态为none
    @Synchronized
    fun stop() {
        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }
        state = STATE_NONE
    }

    // 在STATE_CONNECTED状态下，调用mConnectedThread里的write方法，写入byte
    fun write(out: ByteArray) {
        Logger.i("send data:${String(out)},${mConnectedThread == null}")
        mConnectedThread?.write(out)
    }

    // 连接失败的时候处理，通知UI，并设为STATE_LISTEN状态
    private fun connectionFailed() {
        state = STATE_LISTEN
        start()
    }

    // 当连接失去的时候，设为STATE_LISTEN
    private fun connectionLost() {
        state = STATE_LISTEN
        start()
    }


    // 连接线程，专门用来对外发出连接对方蓝牙的请求并进行处理
    // 构造函数里通过BluetoothDevice.createRfcommSocketToServiceRecord(),
    // 从待连接的device产生BluetoothSocket，然后在run方法中connect
    // 成功后调用 BluetoothChatService的connnected（）方法，定义cancel（）在关闭线程时能关闭socket
    @SuppressLint("MissingPermission")
    private inner class ConnectThread(private val mmDevice: BluetoothDevice) : Thread() {
        private val mmSocket: BluetoothSocket?

        init {
            var tmp: BluetoothSocket? = null
            try {
                tmp = mmDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID)
            } catch (e: IOException) {

            }
            mmSocket = tmp
        }

        override fun run() {
            mAdapter.cancelDiscovery()
            try {
                mmSocket!!.connect()
                Logger.i("连接成功！")
            } catch (e: IOException) {
                connectionFailed()
                Logger.e("连接失败：${e.message}")
                onConnected?.invoke(false)
                try {
                    mmSocket!!.close()
                } catch (e2: IOException) {
                }

                this@ChatService.start()
                return
            }
            //synchronized(this@ChatService) { mConnectedThread = null }
            connected(mmSocket)
        }

        fun cancel() {
            /* try{
                mmSocket.close();
            }catch (IOException e){}*/
        }
    }

    // 双方蓝牙连接后一直运行的线程。构造函数中设置输入输出流。
    // Run方法中使用阻塞模式的InputStream.read()循环读取输入流
    // 然后psot到UI线程中更新聊天信息。也提供了write()将聊天消息写入输出流传输至对方，
    // 传输成功后回写入UI线程。最后cancel()关闭连接的socket
    private inner class ConnectedThread(private val mmSocket: BluetoothSocket?) : Thread() {
        private val mmInStream: InputStream?
        private val mmOutStream: OutputStream?

        init {
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null
            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = mmSocket?.inputStream
                tmpOut = mmSocket?.outputStream
            } catch (e: IOException) {
                Logger.e("输入输出流 error:${e.message}")
            }
            mmInStream = tmpIn
            mmOutStream = tmpOut
            Logger.i("启动输入输出线程")
        }

        override fun run() {
            val buffer = ByteArray(1024)
            var bytes: Int
            Logger.i("connectedThread 已启动")
            while (true) {
                try {
                    bytes = mmInStream!!.read(buffer)
                    parseData(buffer, bytes)
                } catch (e: IOException) {
                    Logger.e("读取数据失败:${e.message}")
                    onConnected?.invoke(false)
                    connectionLost()
                    break
                }
            }
        }

        fun write(buffer: ByteArray) {
            try {
                Logger.i("send Msg:${String(buffer)}")
                mmOutStream!!.write(buffer)
            } catch (e: IOException) {
                Logger.i("Send Fail")
            }
            //  mHandler.obtainMessage(MainActivity.MESSAGE_WRITE,buffer).sendToTarget();
        }

        fun cancel() {
            try {
                mmSocket!!.close()
            } catch (e: IOException) {
            }
        }
    }


    fun parseData(buffer: ByteArray, length: Int) {
        Logger.i("接收的信息:${String(buffer, 0, length)}")
        val jsonStr = String(buffer, 0, length)
        val bleData = Gson().fromJson(jsonStr, BleData::class.java)
        val url = bleData.url
        val sn = bleData.snCode
        val status = bleData.status
        if (status == 2) {
            if (sn != null)
                onSnCodeListener?.invoke(sn)
            return
        }

        if (status == 3) {
            devRegisterListener?.invoke(true)
            Logger.i("设备注册已连接成功！")
            return
        }
        if (status == 4) {
            Logger.i("设备已经被注册到平台了")
            devHasRegister?.invoke()

        }
        Logger.e("参数配置错误")


    }

    companion object {
        private const val NAME = "PREVENT"

        // UUID-->通用唯一识别码，能唯一地辨识咨询
        private val MY_UUID = UUID.fromString(
            "00001101-0000-1000-8000-00805F9B34FB"
        )

        //串口
        const val STATE_NONE = 0
        const val STATE_LISTEN = 1
        const val STATE_CONNECTING = 2
        const val STATE_CONNECTED = 3
    }

    // 蓝牙通信建立成功
    private var onConnected: ((data: Boolean) -> Unit)? = null

    // 收到设备发送的sn
    private var onSnCodeListener: ((sn: String) -> Unit)? = null

    // 设备已经注册到平台上了
    private var devRegisterListener: ((data: Boolean) -> Unit)? = null

    private var devHasRegister: (() -> Unit)? = null

    fun onConnected(listener: ((data: Boolean) -> Unit)) {
        onConnected = listener
    }

    fun onDevRegister(listener: (data: Boolean) -> Unit) {
        devRegisterListener = listener
    }

    fun onSnListener(listener: (sn: String) -> Unit) {
        onSnCodeListener = listener
    }

    fun onDevHasRegister(listener: () -> Unit) {
        devHasRegister = listener
    }

}