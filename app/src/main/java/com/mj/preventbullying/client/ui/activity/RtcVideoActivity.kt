package com.mj.preventbullying.client.ui.activity

import com.mj.preventbullying.client.app.AppMvActivity
import com.mj.preventbullying.client.app.MyApp
import com.mj.preventbullying.client.databinding.ActivityRtcVideoBinding
import com.sjb.base.base.BaseViewModel
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
import org.webrtc.RtpReceiver
import org.webrtc.RtpTransceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

/**
 * Create by MJ on 2024/2/20.
 * Describe : webrtc 视频流 播放
 */

class RtcVideoActivity : AppMvActivity<ActivityRtcVideoBinding, BaseViewModel>() {

    private var peerConnection: PeerConnection? = null
    override fun getViewBinding(): ActivityRtcVideoBinding {
        return ActivityRtcVideoBinding.inflate(layoutInflater)
    }

    override fun initParam() {

    }

    override fun initData() {

    }

    override fun initViewObservable() {

    }

    override fun initView() {

    }

    override fun initListener() {

    }

    private fun initPeer() {
        val eglBaseContext = EglBase.create().eglBaseContext
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions
                .builder(MyApp.context)
                .createInitializationOptions()
        )
        // 初始化PeerConnection
        val options = PeerConnectionFactory.Options()
        val encoderFactory = DefaultVideoEncoderFactory(eglBaseContext, true, true)
        val decoderFactory = DefaultVideoDecoderFactory(eglBaseContext)
        val peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(options)
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
            .createPeerConnectionFactory()
        // 初始化 surfaceViewRenderer
        binding.rtcVideo.init(eglBaseContext, null)
        val rtcConfig = PeerConnection.RTCConfiguration(arrayListOf())
        // 修改模式 planB无法使用仅接收音频配置
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        peerConnection =
            peerConnectionFactory.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
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
                    p0?.videoTracks?.get(0)?.addSink(binding.rtcVideo)
                }

                override fun onRemoveStream(p0: MediaStream?) {

                }

                override fun onDataChannel(p0: DataChannel?) {

                }

                override fun onRenegotiationNeeded() {

                }

                override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {

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
            val sdp = SessionDescription(p0?.type, p0?.description)

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
}