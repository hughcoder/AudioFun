package com.hugh.common.recorder;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.util.Log;


import com.hugh.common.Interface.VoiceRecorderOperateInterface;
import com.hugh.common.global.Constant;
import com.hugh.common.recorder.mp3.MP3Recorder;
import com.hugh.common.util.CommonFunction;
import com.hugh.common.util.FileFunction;

import java.io.File;
import java.io.IOException;


public class RecorderEngine {
    private boolean recording;

    private final int sampleDuration = 500;// 间隔取样时间

    private long recordStartTime;
    private long recordDuration;

    private String recordFileUrl;

    private VoiceRecorderOperateInterface voiceRecorderInterface;

    private AudioManager audioManager;

    private Handler handler;

    private static MP3Recorder recorder;

    //    private static NativeRecorder recorder;

    private static RecorderEngine instance;

    private RecorderEngine(Activity activity) {
        audioManager = (AudioManager) activity
                .getSystemService(Context.AUDIO_SERVICE);


        handler = new Handler();

        //        recorder = new NativeRecorder();

        recorder = new MP3Recorder();
    }

    public static RecorderEngine getInstance(Activity activity) {
        if (instance == null) {
            synchronized (RecorderEngine.class) {
                if (instance == null) {
                    instance = new RecorderEngine(activity);
                }
            }
        }

        return instance;
    }

    public synchronized static void Destroy() {
        if (instance != null) {
            recorder.release();
        }

        instance = null;
    }

    public boolean IsRecording() {
        return recording;
    }

    private boolean startRecordVoice(String recordFileUrl) {
        if (CommonFunction.isEmpty(recordFileUrl)) {
            return false;
        }

        File recordFile = new File(recordFileUrl);

        if (!recordFile.exists()) {
            try {
                recordFile.createNewFile();
            } catch (IOException e) {
                Log.e("record", "创建文件失败");
                return false;
            }
        }

        return recorder.startRecordVoice(recordFileUrl);
    }

    public void startRecordVoice(String recordFileUrl,
                                 VoiceRecorderOperateInterface voiceRecorderOperateInterface) {
        stopRecordVoice();

        recordDuration = 0;

        recording = startRecordVoice(recordFileUrl);

        if (recording) {
            this.recordFileUrl = recordFileUrl;
            this.voiceRecorderInterface = voiceRecorderOperateInterface;

            recordStartTime = System.currentTimeMillis();

            updateMicStatus();

            if (voiceRecorderOperateInterface != null) {
                voiceRecorderOperateInterface.recordVoiceBegin();
            }
        } else {

            if (voiceRecorderOperateInterface != null) {
                voiceRecorderOperateInterface.recordVoiceFail();
            }
        }
    }

    public void stopRecordVoice() {
        if (recording) {
            boolean recordVoiceSuccess = recorder.stopRecordVoice();
            long recordDuration = System.currentTimeMillis() - recordStartTime;

            recording = false;

            if (recordDuration < Constant.OneSecond) {
                recordVoiceSuccess = false;
            }

            if (!recordVoiceSuccess) {

                if (voiceRecorderInterface != null) {
                    voiceRecorderInterface.recordVoiceFail();
                }

                FileFunction.DeleteFile(recordFileUrl);
                return;
            }

            if (voiceRecorderInterface != null) {
                voiceRecorderInterface.recordVoiceFinish();
            }
        }
    }

    public void giveUpRecordVoice(boolean fromHand) {
        if (recording) {
            boolean stopRecordSuccess = recorder.stopRecordVoice();

            recording = false;

            if (stopRecordSuccess) {
                if (voiceRecorderInterface != null) {
                    voiceRecorderInterface.recordVoiceFinish();
                }
            } else {
                if (voiceRecorderInterface != null) {
                    voiceRecorderInterface.recordVoiceFail();
                }
            }

            FileFunction.DeleteFile(recordFileUrl);

            if (voiceRecorderInterface != null) {
                voiceRecorderInterface.giveUpRecordVoice();
            }
        }
    }

    public void prepareGiveUpRecordVoice(boolean fromHand) {
        if (voiceRecorderInterface != null) {
            voiceRecorderInterface.prepareGiveUpRecordVoice();
        }
    }

    public void recoverRecordVoice(boolean fromHand) {
        if (voiceRecorderInterface != null) {
            voiceRecorderInterface.recoverRecordVoice();
        }
    }

    public void recordVoiceStateChanged(int volume) {
        if (voiceRecorderInterface != null) {
            voiceRecorderInterface.recordVoiceStateChanged(volume, recordDuration);
        }
    }

    private void updateMicStatus() {
        int volume = recorder.getVolume();

        recordVoiceStateChanged(volume);

        handler.postDelayed(updateMicStatusThread, sampleDuration);
    }

    private Runnable updateMicStatusThread = new Runnable() {
        public void run() {
            if (recording) {
                // 判断是否超时
                recordDuration = System.currentTimeMillis() - recordStartTime;

                updateMicStatus();
            }
        }
    };
}
