package com.hugh.libwebrtc.other

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.hugh.libwebrtc.R
import com.hugh.libwebrtc.WebRtcAGCUtils
import com.hugh.libwebrtc.WebRtcAGCUtils.*
import com.hugh.libwebrtc.WebRtcNsUtils
import com.hugh.libwebrtc.WebRtcNsUtils.*
import kotlinx.android.synthetic.main.module_rtc_activity_file.*

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.concurrent.thread

class RtcFileActivity : AppCompatActivity() {
    private val tag = "MainActivity"
    var isStop = false
    private var smpleRate = 32000; //8000  ,  16000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.module_rtc_activity_file)
    }

    private val audioRes by lazy {
//        resources.openRawResource(R.raw.recorded_audio_16k)
        resources.openRawResource(R.raw.recorded_audio_32k)
//        resources.openRawResource(R.raw.recorded_audio_8k)
    }


    fun onClick(view: View) {
        when (view) {
            start_btn -> {
                isStop = false
                thread {
                    val enabledNsAgc = enable_ns_agc_switch.isChecked
                    val audioManager: AudioManager =
                            getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    val bufferSize: Int =
                            AudioTrack.getMinBufferSize(
                                    smpleRate,
                                    AudioFormat.CHANNEL_OUT_MONO,
                                    AudioFormat.ENCODING_PCM_16BIT
                            )
                    val audioAttributes: AudioAttributes = AudioAttributes.Builder()
                            .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                            .build()
                    val audioFormat: AudioFormat = AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(smpleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    val sessionId = audioManager.generateAudioSessionId()
                    val audioTrack =
                            AudioTrack(
                                    audioAttributes, audioFormat, bufferSize, AudioTrack.MODE_STREAM,
                                    sessionId
                            )
                    var nsUtils: WebRtcNsUtils? = null
                    var nsxId = 0L
                    var agcUtils: WebRtcAGCUtils? = null
                    var agcId = 0L
                    if (enabledNsAgc) {
                        nsUtils = WebRtcNsUtils()
                        nsxId = WebRtcNsx_Create()
                        val frequency = smpleRate;
                        val nsxInit = WebRtcNsx_Init(nsxId, frequency)
                        val nexSetPolicy = nsxSetPolicy(nsxId, 2)
                        Log.i(tag, "nsxId : $nsxId  nsxInit: $nsxInit nexSetPolicy: $nexSetPolicy")

                        agcUtils = WebRtcAGCUtils()
                        agcId = WebRtcAgc_Create()
                        val agcInitResult = WebRtcAgc_Init(agcId, 0, 255, 3, smpleRate)
                        val agcSetConfigResult = agcSetConfig(agcId, 3, 15, true)
                        Log.e(
                                tag,
                                "agcId : $agcId  agcInit: $agcInitResult agcSetConfig: $agcSetConfigResult"
                        )
                    }
                    kotlin.run {
                        audioTrack.play()
                        audioRes.reset()
                        val audioData = audioRes.readBytes()
                        if (isStop) {
                            return@run
                        }
                        audioData.asSequence().chunked(320).filter { it.size == 320 }.forEach {
                            val byteArray = it.toByteArray()
                            if (enabledNsAgc) {
                                val inputData = ShortArray(160)
                                val outNsData = ShortArray(160)
                                val outAgcData = ShortArray(160)
                                ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN)
                                        .asShortBuffer()
                                        .get(inputData)
                                WebRtcNsx_Process(nsxId, inputData, 1, outNsData)
                                var ret = agcProcess(
                                        agcId, outNsData, 1, 160, outAgcData,
                                        0, 0, 0, false
                                )
                                if(ret!=0){
                                    Log.e("aaa","处理失败")
                                }
                                if (isStop) {
                                    return@run
                                }
                                Log.e("aaa","数据处理------>"+outAgcData.size)
                                audioTrack.write(outAgcData, 0, 160)
                            } else {
                                audioTrack.write(byteArray, 0, byteArray.size)
                            }
                            if (isStop) {
                                return@run
                            }
                        }
                    }
                    WebRtcNsx_Free(nsxId)
                    agcFree(agcId)
                    audioTrack.stop()
                    audioTrack.release()
                }
            }
            stop_btn -> {
                isStop = true
            }
            change_32k ->{
                smpleRate = 32000

            }
            else -> {

            }
        }
    }

    override fun onStop() {
        isStop = true
        super.onStop()
    }

}
