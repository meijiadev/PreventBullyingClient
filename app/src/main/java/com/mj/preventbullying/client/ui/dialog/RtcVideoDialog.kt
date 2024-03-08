package com.mj.preventbullying.client.ui.dialog

import android.content.Context
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.lifecycleScope
import com.hjq.shape.view.ShapeTextView
import com.lxj.xpopup.core.CenterPopupView
import com.mj.preventbullying.client.Constant
import com.mj.preventbullying.client.R
import com.mj.preventbullying.client.app.MyApp
import com.orhanobut.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.webrtc.DataChannel
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.MediaStreamTrack
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RTCStatsCollectorCallback
import org.webrtc.RTCStatsReport
import org.webrtc.RendererCommon
import org.webrtc.RtpReceiver
import org.webrtc.RtpTransceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack
import java.io.IOException
import kotlin.concurrent.thread

/**
 * Create by MJ on 2024/2/27.
 * Describe :
 */

class RtcVideoDialog(context: Context) : CenterPopupView(context) {
    private val closeTv: ShapeTextView by lazy { findViewById(R.id.close_tv) }
    private val fullTv: ShapeTextView by lazy { findViewById(R.id.full_tv) }
    private val rtcVideo: SurfaceViewRenderer by lazy { findViewById(R.id.rtc_video) }
    private val restartPlayTv: AppCompatTextView by lazy { findViewById(R.id.restart_play_tv) }

    private var peerConnection: PeerConnection? = null
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var eglBaseContext: EglBase.Context? = null
    private var videoTrack: VideoTrack? = null
    private var videoUrl: String? = null
    private var isRunning = true
    private var videoLength = 0L
    private var job: Job? = null

    override fun getImplLayoutId(): Int {
        return R.layout.dialog_rtc_video
    }


    override fun onCreate() {
        super.onCreate()
        closeTv.setOnClickListener {
            dismiss()
        }
        fullTv.setOnClickListener {
            dismiss()
            onFull?.invoke()
        }
        initPeer()
        restartPlayTv.setOnClickListener {
            if (restartPlayTv.text == "正在加载中...")
                initPeer()
        }
    }

    private var onFull: (() -> Unit)? = null

    fun onFullListener(listener: (() -> Unit)): RtcVideoDialog = apply {
        this.onFull = listener
    }


    fun setVideoUrl(url: String): RtcVideoDialog = apply {
        this.videoUrl = url
    }


    private fun initPeer() {
        eglBaseContext = EglBase.create().eglBaseContext
        // 初始化 surfaceViewRenderer
        rtcVideo.init(eglBaseContext, null)
        rtcVideo.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
        rtcVideo.setEnableHardwareScaler(true)
        rtcVideo.setZOrderMediaOverlay(true)
        createPeer()
    }

    private fun createPeer() {
        // 初始化PeerConnection
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions
                .builder(MyApp.context)
                .createInitializationOptions()
        )
        val options = PeerConnectionFactory.Options()
        val encoderFactory = DefaultVideoEncoderFactory(eglBaseContext, true, true)
        val decoderFactory = DefaultVideoDecoderFactory(eglBaseContext)
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(options)
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
            .createPeerConnectionFactory()
        val rtcConfig = PeerConnection.RTCConfiguration(arrayListOf())
        // 修改模式 planB无法使用仅接收音频配置
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        peerConnection =
            peerConnectionFactory?.createPeerConnection(
                rtcConfig,
                object : PeerConnection.Observer {
                    override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
                        Logger.i("onSignalingChange")
                    }

                    override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
                        Logger.i("onIceConnectionChange")
                    }

                    override fun onIceConnectionReceivingChange(p0: Boolean) {
                        Logger.i("onIceConnectionReceivingChange")
                    }

                    override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
                        Logger.i("onIceGatheringChange")
                    }

                    override fun onIceCandidate(p0: IceCandidate?) {
                        Logger.i("onIceCandidate")
                    }

                    override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
                        Logger.i("onIceCandidatesRemoved")
                    }

                    override fun onAddStream(p0: MediaStream?) {
                        videoTrack = p0?.videoTracks?.get(0)
                        videoTrack?.addSink(rtcVideo)
                        lifecycleScope.launch(Dispatchers.Main) {
                            // restartPlayTv.visibility = GONE
                            MyApp.heartbeatViewModel.sendHeartbeat(Constant.videoSnCOde)
                        }
                        Logger.i("onAddStream")
                    }

                    override fun onRemoveStream(p0: MediaStream?) {
                        Logger.i("onRemoveStream")
                    }

                    override fun onDataChannel(p0: DataChannel?) {
                        Logger.i("onDataChannel:${p0?.bufferedAmount()},${p0?.state()}")
                    }

                    override fun onRenegotiationNeeded() {
                        Logger.i("onRenegotiationNeeded")
                    }

                    override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
                        val track = p0?.track()
                        if (track is VideoTrack) {
                            val remoteVideoTrack = track
                            remoteVideoTrack.setEnabled(true)
                        }
                        Logger.i("onAddTrack")
                    }
                })
        peerConnection?.addTransceiver(
            MediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO,
            RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.RECV_ONLY)
        )
        peerConnection?.addTransceiver(
            MediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO,
            RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.RECV_ONLY)
        )
        peerConnection?.createOffer(sdpObserver, MediaConstraints())
        job = lifecycleScope.launch {
            while (isRunning) {
                peerConnection?.getStats {
                    val rtc = it.statsMap
                    for (value in rtc) {
                        if (value.value?.type == "inbound-rtp") {
                            if (value.value.members.get("mediaType") == "video") {
                                Logger.i("监听：${value.value}")
                                val curLength =
                                    value.value.members["bytesReceived"].toString().toLong()
                                if (videoLength == 0L) {
                                    // 画面正式加载出来的时候
                                    lifecycleScope.launch(Dispatchers.Main) {
                                        restartPlayTv.visibility = GONE
                                        MyApp.heartbeatViewModel.sendHeartbeat(Constant.videoSnCOde)
                                    }
                                }
                                if (curLength > videoLength) {
                                    videoLength = curLength
                                } else if (curLength == videoLength) {
                                    Logger.i("视频流已暂停")
                                    restartPlayTv.text = "当前视频已断开，点击重新加载！"
                                    restartPlayTv.visibility = VISIBLE
                                    release()
                                }
                            }
                        }
                    }
                }
                delay(2000)
            }
        }
    }

    private val sdpObserver = object : SdpObserver {
        override fun onCreateSuccess(p0: SessionDescription?) {

            if (p0?.type == SessionDescription.Type.OFFER) {
                peerConnection?.setLocalDescription(this, p0)
                val sdpJson = p0.description
                getSrs(sdpJson, videoUrl)
            }
        }

        override fun onSetSuccess() {

        }

        override fun onCreateFailure(p0: String?) {

        }

        override fun onSetFailure(p0: String?) {

        }

    }


    fun setRemoteDescription(sdp: String) {
        val remoteSdp = SessionDescription(SessionDescription.Type.ANSWER, sdp)
        peerConnection?.setRemoteDescription(sdpObserver, remoteSdp)
    }

    fun getSrs(sdpJson: String, url: String?) {
        if (url == null) {
            return
        }
        thread {
            try {
                Logger.i("sdp:$sdpJson")
                val mediaType = "application/json".toMediaTypeOrNull()
                val requestBody = RequestBody.create(mediaType, sdpJson)
                val client = OkHttpClient().newBuilder().build()
                val request = Request.Builder()
                    .url(url)
                    .method("POST", requestBody)
                    .build()
                val call = client.newCall(request)
                // Logger.i("请求成功返回：${response.body}")
                call.enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Logger.e("请求失败：$e")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val sdp = response.body?.string()
                        Logger.i("请求成功返回：$sdp")
                        lifecycleScope.launch(Dispatchers.Main) {
                            if (sdp != null) {
                                setRemoteDescription(sdp)
                            }
                        }

                    }
                })


            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDismiss() {
        super.onDismiss()
        release()
        Logger.i("dialog is destroy")
    }

    private fun release() {
        if (peerConnection != null) {
            peerConnection?.dispose()
            peerConnection = null
        }
        if (peerConnectionFactory != null) {
            peerConnectionFactory?.dispose()
            peerConnectionFactory = null
        }
        rtcVideo.clearImage()
        MyApp.heartbeatViewModel.stopSend()
        isRunning = false
        job?.cancel()
    }


}