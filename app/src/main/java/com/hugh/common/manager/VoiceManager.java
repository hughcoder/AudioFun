package com.hugh.common.manager;


import android.app.Activity;

import com.hugh.common.Interface.VoicePlayerInterface;
import com.hugh.common.Interface.VoiceRecorderOperateInterface;
import com.hugh.common.player.VoicePlayerEngine;
import com.hugh.common.recorder.RecorderEngine;
import com.hugh.common.util.CommonFunction;

public class VoiceManager {
    public static boolean IsRecordingVoice(Activity activity) {
        return RecorderEngine.getInstance(activity).IsRecording();
    }

    public synchronized static void StartRecordVoice(String recordFileUrl,
                                                     VoiceRecorderOperateInterface voiceRecorderOperateInterface,Activity activity) {
        RecorderEngine.getInstance(activity)
                .startRecordVoice(recordFileUrl, voiceRecorderOperateInterface);
    }

    public static void StopRecordVoice(Activity activity) {
        RecorderEngine.getInstance(activity).stopRecordVoice();
    }

//    public synchronized static void PrepareGiveUpRecordVoice(boolean fromHand) {
//        RecorderEngine.getInstance().prepareGiveUpRecordVoice(fromHand);
//    }
//
//    public synchronized static void RecoverRecordVoice(boolean fromHand) {
//        RecorderEngine.getInstance().recoverRecordVoice(fromHand);
//    }
//
//    public synchronized static void GiveUpRecordVoice(boolean fromHand) {
//        RecorderEngine.getInstance().giveUpRecordVoice(fromHand);
//    }

    public synchronized static String getPlayingUrl() {
        return VoicePlayerEngine.getInstance().getPlayingUrl();
    }

    public synchronized static boolean IsPlaying() {
        return VoicePlayerEngine.getInstance().isPlaying();
    }

    public synchronized static boolean IsPlayVoice(String fileUrl) {
        if (CommonFunction.isEmpty(fileUrl)) {
            return false;
        }

        return getPlayingUrl().equals(fileUrl);
    }

    public synchronized static boolean IsPlayingVoice(String fileUrl) {
        if (IsPlayVoice(fileUrl)) {
            return VoicePlayerEngine.getInstance().isPlaying();
        } else {
            return false;
        }
    }

    public synchronized static void PlayToggleVoice(String fileUrl,
                                                    VoicePlayerInterface voicePlayerInterface) {
        if (IsPlayVoice(fileUrl)) {
            VoicePlayerEngine.getInstance().stopVoice();
        } else {
            VoicePlayerEngine.getInstance()
                    .playVoice(fileUrl, voicePlayerInterface);
        }
    }

    public synchronized static void StopVoice() {
        VoicePlayerEngine.getInstance().stopVoice();
    }

    public synchronized static void StopVoice(String fileUrl) {
        if (getPlayingUrl().equals(fileUrl)) {
            VoicePlayerEngine.getInstance().stopVoice();
        }
    }
}