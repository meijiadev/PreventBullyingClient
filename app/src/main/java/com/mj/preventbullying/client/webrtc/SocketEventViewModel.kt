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


class SocketEventViewModel : ViewModel(), HandlerAction {


    // 被呼叫或者呼叫别人
    var callEvent = UnPeekLiveData<Boolean>()

    var toId: String = "SN012345678901"
    private var mSocket: Socket? = null
    private var webRtcManager: WebRtcManager? = null

    private var snCode: String? = null


    fun initSocket(sn: String) {
        snCode = sn
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
    }


    /**
     * 注册 snCode
     */
    fun login() {
        Logger.i("login:$snCode")
        mSocket?.emit("register", snCode, "1975-01-01 14:04:17")
    }

    fun call(toId: String) {
        Logger.i("call ,$toId")
        this.toId = toId
        val message = Message("call", snCode, toId, null)
        mSocket?.emit("message", Gson().toJson(message))
    }


    /**
     * 收到对方语音请求，同意语音通话发送called
     */
    fun sendCalled() {
        val message = Message("called", snCode, toId, null)
        mSocket?.emit("message", Gson().toJson(message))
    }


    fun sendHangUp() {
        val message = Message("hangUp", snCode, toId, null)
        mSocket?.emit("message", Gson().toJson(message))
        webRtcManager?.release()
    }

    fun icecandidate(iceCandidate: IceCandidate) {
        val message = Message("icecandidate", snCode, toId, iceCandidate)
        mSocket?.emit("message", Gson().toJson(message))
    }

    fun sendOffer(offer: SessionDescription) {
        val message = Message("offer", snCode, toId, offer.description)
        mSocket?.emit("message", Gson().toJson(message))
    }

    fun sendAnswer(answer: SessionDescription) {
        val message = Message("answer", snCode, toId, answer.description)
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
                    toId = it[0].toString()
                    Logger.i("来电了：snCode:${it[0]},to:${it[1]}")
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
                }

                "answer" -> {
                    // 设置 remote sdp
                    Logger.i("receive answer:${message.data}")
                    val sdp =
                        SessionDescription(SessionDescription.Type.ANSWER, message.data.toString())
                    //  发送answer 并设置remote sdp
                    webRtcManager?.setRemoteDescription(sdp)
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




        mSocket?.on("connect") {
            Logger.i("socket.io 正在连接")
            login()
        }

        mSocket?.on("disconnected") {
            Logger.i("socket.io 断开连接")
        }

    }


}