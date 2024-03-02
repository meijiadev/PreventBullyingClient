package com.mj.preventbullying.client.ui.activity


import com.mj.preventbullying.client.app.AppMvActivity
import com.mj.preventbullying.client.app.MyApp
import com.mj.preventbullying.client.databinding.ActivityRtcVideoBinding
import com.orhanobut.logger.Logger
import com.sjb.base.base.BaseViewModel
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
import org.webrtc.RendererCommon
import org.webrtc.RtpReceiver
import org.webrtc.RtpTransceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.VideoTrack
import java.io.IOException
import kotlin.concurrent.thread


/**
 * Create by MJ on 2024/2/20.
 * Describe : webrtc 视频流 播放
 */

class RtcVideoActivity : AppMvActivity<ActivityRtcVideoBinding, BaseViewModel>() {

    private var peerConnection: PeerConnection? = null
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var eglBaseContext: EglBase.Context? = null
    private var videoTrack: VideoTrack? = null
    private var videoUrl: String? = null

    override fun getViewBinding(): ActivityRtcVideoBinding {
        return ActivityRtcVideoBinding.inflate(layoutInflater)
    }

    override fun initParam() {
        videoUrl = intent.getStringExtra("videoUrl")
    }

    override fun initData() {
        initPeer()
    }

    override fun initViewObservable() {

    }

    override fun initView() {

    }

    override fun initListener() {
        binding.backIv.setOnClickListener {
            Logger.i("退出rtc全屏")
            finish()
        }
    }

    private fun initPeer() {
        eglBaseContext = EglBase.create().eglBaseContext
        // 初始化 surfaceViewRenderer
        binding.rtcVideo.init(eglBaseContext, null)
        binding.rtcVideo.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
        binding.rtcVideo.setEnableHardwareScaler(true)
        binding.rtcVideo.setZOrderMediaOverlay(true)
        createPeer()
    }

    /**
     * 创建wrtc直播
     */
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

                    }

                    override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {

                    }

                    override fun onIceConnectionReceivingChange(p0: Boolean) {

                    }

                    override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {

                    }

                    override fun onIceCandidate(p0: IceCandidate?) {

                    }

                    override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {

                    }

                    override fun onAddStream(p0: MediaStream?) {
                        videoTrack = p0?.videoTracks?.get(0)
                        videoTrack?.addSink(binding.rtcVideo)
                        Logger.i("addStream")
                    }

                    override fun onRemoveStream(p0: MediaStream?) {
                        Logger.i("onRemoveStream")
                    }

                    override fun onDataChannel(p0: DataChannel?) {

                    }

                    override fun onRenegotiationNeeded() {

                    }

                    override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
                        val track = p0?.track()
                        if (track is VideoTrack) {
                            val remoteVideoTrack = track
                            remoteVideoTrack.setEnabled(true)

                        }

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
    }

    private val sdpObserver = object : SdpObserver {
        override fun onCreateSuccess(p0: SessionDescription?) {

            if (p0?.type == SessionDescription.Type.OFFER) {
                peerConnection?.setLocalDescription(this, p0)
                val sdpJson = p0.description
                getSrs(sdpJson)
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

    fun getSrs(sdpJson: String) {
        if (videoUrl == null) {
            return
        }
        thread {
            try {
                Logger.i("sdp:$sdpJson")
                val mediaType = "application/json".toMediaTypeOrNull()
                val requestBody = RequestBody.create(mediaType, sdpJson)
                val client = OkHttpClient().newBuilder().build()
                val request = Request.Builder()
                    .url(videoUrl!!)
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
                        runOnUiThread {
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


    override fun onPause() {
        super.onPause()
    }


    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
        release()
        // 暂停视频流
        Logger.i("onStop-暂停视频流")
    }

    override fun onRestart() {
        super.onRestart()
        Logger.i("onRestart")
        // 恢复视频流
        createPeer()
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
        binding.rtcVideo.clearImage()

    }
}