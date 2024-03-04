package com.mj.preventbullying.client.webrtc

import android.util.ArrayMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blackview.base.http.requestNoCheck
import com.google.gson.Gson
import com.hjq.toast.ToastUtils
import com.kunminx.architecture.ui.callback.UnPeekLiveData
import com.mj.preventbullying.client.Constant
import com.mj.preventbullying.client.app.MyApp
import com.mj.preventbullying.client.http.apiService
import com.mj.preventbullying.client.tool.SpManager
import com.mj.preventbullying.client.http.service.ApiService
import com.orhanobut.logger.Logger
import com.sjb.base.action.HandlerAction
import com.sjb.base.base.BaseViewModel
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URI

//正在拨打中
const val CALLING_STATUS = 1

// 正在通话中
const val CALLED_STATUS = 2

// 拨打失败
const val CALL_FAILURE = 3

// 已挂断
const val CALL_HANG_UP = 4

// 重新拨打一次
const val RESTART_CALL = 5 // 重新发起通话

const val CALL_BUSY = 6    //占线中...

// 有其他人登录
const val LOGIN_STATUS_ANTHER = 1
const val LOGIN_STATUS_FORCE_LOGOUT = 2
const val SOCKET_IO_CONNECT = 3
const val SOCKET_IO_DISCONNECTED = 4
const val OPPOSITE_OFF_LINE = "offline"
const val OPPOSITE_BUSY = "busy"
var isAnswer: Boolean = false       // 是否有回复

class SocketEventViewModel : BaseViewModel(), HandlerAction {

    /**
     * 语音电话当前状态
     */
    var voiceCallEvent = UnPeekLiveData<Int>()

    // 是否有其他人登录
    var loginStatusEvent = UnPeekLiveData<Int>()

    private var mSocket: Socket? = null

    private var userId: String? = null

    // private var userId: String? = null

    private var registerId: String? = null


    var isConnected = false

    fun initSocket(sn: String, registerId: String) {
        if (!isConnected) {
            userId = sn
            this.registerId = registerId
            val token = SpManager.getString(Constant.ACCESS_TOKEN_KEY)
            val url =
                "${ApiService.getHostUrl()}app?token=$token&clientType=anti_bullying_device&clientId=$sn"
            val uri = URI.create(url)
            val websocket = arrayOf("websocket")
            val options =
                IO.Options.builder().setReconnectionDelay(3000).setTransports(websocket).build()
            kotlin.runCatching {
                mSocket = IO.socket(
                    uri, options
                )
            }.onFailure {
                Logger.e("${it.message}")
            }
            mSocket?.connect()
            receiveMessage()
            // this.from = sn
            Logger.i("初始化socket:${mSocket?.isActive},url:$url")
        } else {
            Logger.e("socket.io 已经连接")
        }
    }


    /**
     * 注册 snCode
     */
    fun login() {
        Logger.i("login:$userId,registerId:$registerId")
        mSocket?.emit("login", userId, registerId)
    }

    /**
     * 取消登录
     */
    fun logout() {
        Logger.i("退出登录：$userId,registerId:$registerId")
        mSocket?.emit("logout", userId, registerId)
    }

    fun disconnect() {
        mSocket?.disconnect()
        mSocket = null
        isConnected = false
        loginStatusEvent.postValue(SOCKET_IO_DISCONNECTED)
    }


    /**
     *  是否确认登录
     */
    fun confirmLogin(isLogin: Boolean) {
        mSocket?.emit("confirmLogin", userId, isLogin)
    }

    /**
     * 拨打设备语音
     */
    fun call(recordId: String?, uuid: String?) {
        val params = ArrayMap<Any, Any>()
        params["recordId"] = recordId
        params["tx"] = uuid
        requestNoCheck(
            {
                apiService.callDevice(params)
            }, {
                if (it.success) {
                    voiceCallEvent.postValue(CALLING_STATUS)
                    viewModelScope.launch(Dispatchers.IO) {
                        isAnswer = false
                        // 等待30s
                        delay(30 * 1000)
                        if (!isAnswer) {
                            MyApp.webrtcSocketManager.release()
                            ToastUtils.show("语音连接无响应，请重试！")
                        }
                    }
                } else {
                    isAnswer = false
                    MyApp.webrtcSocketManager.release()
                    ToastUtils.show(it.msg)
                }
            }, {
                isAnswer = false
                MyApp.webrtcSocketManager.release()
                ToastUtils.show("语音连接错误，请重试！")
            }
        )
        //Logger.i("call ,toId:$toId,uuid:$uuid")
//        toId?.let {
//            val message = Message("call", userId, toId, null, uuid)
//            mSocket?.emit("call", Gson().toJson(message), Ack { ack ->
//                if (ack?.isEmpty() == true) {
//                    Logger.i("ack为空")
//                    isAnswer = false
//                    MyApp.webrtcSocketManager.sendHangUp()
//                    ToastUtils.show("语音连接无响应，请重试！")
//                } else {
//                    Logger.i("接收ack:${ack[0].toString()}")
//                    val status = ack[0].toString()
//                    if (status == OPPOSITE_OFF_LINE) {
//                        Logger.i("设备已掉线无法拨通")
//                        MyApp.webrtcSocketManager.sendHangUp(false)
//                        ToastUtils.show("设备已掉线，无法拨通！")
//                    } else if (status == OPPOSITE_BUSY) {
//                        MyApp.webrtcSocketManager.sendHangUp(false)
//                        ToastUtils.show("设备已占线，其他用户正在连线！")
//                        Logger.i("设备已占线，其他用户在连线")
//                    } else {
//                        voiceCallEvent.postValue(CALLING_STATUS)
//                        viewModelScope.launch(Dispatchers.IO) {
//                            isAnswer = false
//                            // 等待30s
//                            delay(30 * 1000)
//                            if (!isAnswer) {
//                                MyApp.webrtcSocketManager.sendHangUp()
//                                ToastUtils.show("语音连接无响应，请重试！")
//                            }
//                        }
//                    }
//                }
//
//            })
//        }
    }


    private fun receiveMessage() {
        mSocket?.on("call") {
            // mSocket?.emit("ack", "success")
            val message = Gson().fromJson(it[0].toString(), Message::class.java)
            Logger.e(message.msgType)
            when (message.msgType) {
                "call" -> {
                    Logger.i("on call ${it[0]}")
                    // CallSingleActivity.openActivity(App.instance?.applicationContext, toId, false, false)
                    // 对方发送的参数 snCodeId toId
                    // toId = message?.sendFrom.toString()
//                    Logger.i("来电了：snCode:${it[1]},to:${it[1]}")
                }
            }
        }

        mSocket?.on(Socket.EVENT_CONNECT) {
            Logger.i("全局socket.io 连接成功")
            loginStatusEvent.postValue(SOCKET_IO_CONNECT)
            isConnected = true
            login()
        }

        mSocket?.on(Socket.EVENT_DISCONNECT) {
            Logger.i("全局socket.io 断开连接")
            isConnected = false
            loginStatusEvent.postValue(SOCKET_IO_DISCONNECTED)
        }

        mSocket?.on(Socket.EVENT_CONNECT_ERROR) {
            Logger.d("全局socket.io 连接错误！${it[0].toString()}")
            isConnected = false
            loginStatusEvent.postValue(SOCKET_IO_DISCONNECTED)
        }

        // 监听到这个参数，表示有其他人登录，弹出弹窗是否踢掉别人
        mSocket?.on("confirmLogin") {
            val userId = it[0]
            loginStatusEvent.postValue(LOGIN_STATUS_ANTHER)

        }
        // 强制退出
        mSocket?.on("forceOut") {
            val userId = it[0]
            loginStatusEvent.postValue(LOGIN_STATUS_FORCE_LOGOUT)
        }

    }


}