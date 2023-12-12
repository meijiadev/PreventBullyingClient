package com.mj.preventbullying.client.webrtc

import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.kunminx.architecture.ui.callback.UnPeekLiveData
import com.mj.preventbullying.client.MyApp
import com.orhanobut.logger.Logger
import com.sjb.base.action.HandlerAction
import io.socket.client.IO
import io.socket.client.Socket
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

//正在拨打中
const val CALLING_STATUS = 1

// 正在通话中
const val CALLED_STATUS = 2

// 拨打失败
const val CALL_FAILURE = 3

// 已挂断
const val CALL_HANG_UP = 4

class SocketEventViewModel : ViewModel(), HandlerAction {


    // 被呼叫或者呼叫别人
    var callEvent = UnPeekLiveData<Boolean>()

    /**
     * 语音电话当前状态
     */
    var voiceCallEvent = UnPeekLiveData<Int>()

    var toId: String = "SN012345678901"
    private var mSocket: Socket? = null
    private var webRtcManager: WebRtcManager? = null

    private var userId: String? = null

    // private var userId: String? = null

    private var registerId: String? = null


    private var isConnected = false

    fun initSocket(sn: String, registerId: String) {
        if (!isConnected) {
            userId = sn
            this.registerId = registerId
            kotlin.runCatching {
                mSocket = IO.socket(
                    "http://192.168.1.6:7099/spad-cloud?token=1231&clientType=anti_bullying_device&clientId=$sn"
                )
            }.onFailure {
                Logger.e("${it.message}")
            }
            mSocket?.connect()
            receiveMessage()
            webRtcManager = WebRtcManager(MyApp.context)
            // this.from = sn
            Logger.i("初始化socket:${mSocket?.isActive}")
        }else{
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

    fun call(toId: String?) {
        Logger.i("call ,toId:$toId")
        toId?.let {
            this.toId = toId
            val message = Message("call", userId, toId, null)
            mSocket?.emit("message", Gson().toJson(message))
            voiceCallEvent.postValue(CALLING_STATUS)
        }
    }


    /**
     * 收到对方语音请求，同意语音通话发送called
     */
    fun sendCalled() {
        val message = Message("called", userId, toId, null)
        mSocket?.emit("message", Gson().toJson(message))
    }


    fun sendHangUp() {
        val message = Message("hangUp", userId, toId, null)
        mSocket?.emit("message", Gson().toJson(message))
        webRtcManager?.release()
        voiceCallEvent.postValue(CALL_HANG_UP)
    }

    fun icecandidate(iceCandidate: IceCandidate) {
        val message = Message("icecandidate", userId, toId, Gson().toJson(iceCandidate))
        mSocket?.emit("message", Gson().toJson(message))
    }

    fun sendOffer(offer: SessionDescription) {
        val message = Message("offer", userId, toId, offer.description)
        mSocket?.emit("message", Gson().toJson(message))
    }

    fun sendAnswer(answer: SessionDescription) {
        val message = Message("answer", userId, toId, answer.description)
        mSocket?.emit("message", Gson().toJson(message))
    }

    private fun receiveMessage() {
        mSocket?.on("message") {
            val message = Gson().fromJson(it[0].toString(), Message::class.java)
            Logger.e(message.msgType)
            when (message.msgType) {
                "call" -> {
                    Logger.i("on call ${it[0]}")
                    // CallSingleActivity.openActivity(App.instance?.applicationContext, toId, false, false)
                    // 对方发送的参数 snCodeId toId
                    toId = message?.sendFrom.toString()
//                    Logger.i("来电了：snCode:${it[0]},to:${it[1]}")
                    postDelayed({
                        sendCalled()
                    }, 100)
                }

                "called" -> {
                    Logger.i("on called ${it[0]}")
                    //此处发送offer
                    webRtcManager?.createPeerConnect()
                    webRtcManager?.isOffer = true
                    webRtcManager?.createLocalStream()
                    webRtcManager?.addLocalStream()
                    webRtcManager?.createOffer()
                }

                "offer" -> {
                    val sdp =
                        SessionDescription(SessionDescription.Type.OFFER, message.data.toString())
                    Logger.i("接收到的sdp:${sdp.description}")
                    //  发送answer 并设置remote sdp
                    webRtcManager?.isOffer = false
                    webRtcManager?.createPeerConnect()
                    webRtcManager?.createLocalStream()
                    webRtcManager?.addLocalStream()
                    webRtcManager?.setRemoteDescription(sdp)
//                    sendAnswer(sdp)
                    webRtcManager?.createAnswer()
                    voiceCallEvent.postValue(CALLED_STATUS)
                }

                "answer" -> {
                    // 设置 remote sdp
                    Logger.i("receive answer:${message.data}")
                    val sdp =
                        SessionDescription(SessionDescription.Type.ANSWER, message.data.toString())
                    //  发送answer 并设置remote sdp
                    webRtcManager?.setRemoteDescription(sdp)
                    voiceCallEvent.postValue(CALLED_STATUS)
                }

                "hangUp" -> {
                    webRtcManager?.release()
                }

                "icecandidate" -> {
                    Logger.d("ice:${message.data}")
                    val ice = Gson().fromJson(message.data.toString(), IceCandidate::class.java)
                    webRtcManager?.addIce(ice)
                }
            }
        }
        // ice:{"sdpMid":"0","sdpMLineIndex":0,"sdp":"candidate:4160270536 1 udp 2122260223 192.168.1.19 61451 typ host generation 0 ufrag sWgO network-id 1 network-cost 10"}
// ice:{adapterType=UNKNOWN, sdp=candidate:842163049 1 udp 1686052607 116.17.147.180 13175 typ srflx raddr 192.168.1.29 rport 54022 generation 0 ufrag wlhQ network-id 1, sdpMLineIndex=0.0, sdpMid=audio, serverUrl=stun:39.108.177.117:3478}

        mSocket?.on("connect") {
            Logger.i("socket.io 正在连接")
            isConnected = true
            login()
        }

        mSocket?.on("disconnected") {
            Logger.i("socket.io 断开连接")
            isConnected = false
        }

    }


}