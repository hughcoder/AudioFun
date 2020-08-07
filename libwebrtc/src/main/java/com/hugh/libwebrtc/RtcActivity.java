package com.hugh.libwebrtc;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

/**
 * Created by chenyw on 2020/7/24.
 */
public class RtcActivity extends Activity implements View.OnClickListener {

    private static final int SAMPLERATE_32K = 32000;
    private static final int SAMPLERATE_16K = 16000;
    private static final int SAMPLERATE_8K = 8000;
    private static final int SAMPLERATE_44K = 44100;

    private static final String AUDIO_FILE_AST_8K = "record/recorded_audio.pcm";
    private static final String AUDIO_FILE_AST_16k = "record/recorded_audio_16k.pcm";
    private static final String AUDIO_FILE_AST_32k = "record/recorded_audio_32k.pcm";

    /**
     * 原始音频文件路径
     */
    private static final String AUDIO_FILE_PATH_8k = Environment.getExternalStorageDirectory().getPath() +
            "/recorded_audio.pcm";
    private static final String AUDIO_FILE_PATH_16k = Environment.getExternalStorageDirectory().getPath() +
            "/recorded_audio_16k.pcm";
    private static final String AUDIO_FILE_PATH_32K = Environment.getExternalStorageDirectory().getPath() +
            "/recorded_audio_32k.pcm";

    /**
     * 处理过的音频文件路径
     */
    private static final String AUDIO_PROCESS_FILE_PATH_8k = Environment.getExternalStorageDirectory().getPath() +
            "/recorded_audio_process.pcm";
    private static final String AUDIO_PROCESS_FILE_PATH_16k = Environment.getExternalStorageDirectory().getPath() +
            "/recorded_audio_process_16k.pcm";
    private static final String AUDIO_PROCESS_FILE_PATH_32k = Environment.getExternalStorageDirectory().getPath() +
            "/recorded_audio_process_32k.pcm";


    private Button mBtnNsOperate;
    private boolean isProcessing;
    private boolean isInitialized;
    private int mMinBufferSize;
    private AudioTrack mAudioTrack;
    private File mFile;
    private File mProcessFile;
    private String AUDIO_FILE_PATH;
    private String AUDIO_PROCESS_FILE_PATH;
    private String srcPath;
    private boolean process32KData;
    private int mSampleRate;
    private ExecutorService mThreadExecutor;
    private int selectId = -1;
    private boolean isPlaying;
    private Switch agc_switch;
    private boolean mIsOpenAgc;
    private long nsxId; //ns降噪id
    private long agcId; //agc增益id
    private int num_bands = 1;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.module_rtc_activity_main);
        agc_switch = findViewById(R.id.agc_switch);
        agc_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mIsOpenAgc = isChecked;
                if (isChecked) {
                    Toast.makeText(RtcActivity.this, "开启agc增益", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(RtcActivity.this, "开启agc增益", Toast.LENGTH_LONG).show();
                }
                Log.e("aaa", "mIsOpenAgc------>" + mIsOpenAgc);
            }
        });
        mBtnNsOperate = findViewById(R.id.ns_audio);
        mBtnNsOperate.setOnClickListener(this);
        selectId = R.id.rb_8k;
        RadioGroup radioGroup = findViewById(R.id.rg);
        int checkedRadioButtonId = radioGroup.getCheckedRadioButtonId();
        switchDataSrc(checkedRadioButtonId);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                switchDataSrc(checkedId);
            }
        });
        mThreadExecutor = Executors.newScheduledThreadPool(3);

        //初始化
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
        } else {
            initAudioFile();
        }
    }


    private void initAudioFile() {
        mSampleRate = SAMPLERATE_8K;
        AUDIO_FILE_PATH = AUDIO_FILE_PATH_8k;
        AUDIO_PROCESS_FILE_PATH = AUDIO_PROCESS_FILE_PATH_8k;
        srcPath = AUDIO_FILE_AST_8K;
        initAudio();
        initAudioRecord();
    }

    private void switchDataSrc(int rbId) {
        if (selectId == rbId) {
            return;
        }
        isInitialized = false;
        selectId = rbId;
        process32KData = false;
        if (rbId == R.id.rb_8k) {
            mSampleRate = SAMPLERATE_8K;
            AUDIO_FILE_PATH = AUDIO_FILE_PATH_8k;
            AUDIO_PROCESS_FILE_PATH = AUDIO_PROCESS_FILE_PATH_8k;
            srcPath = AUDIO_FILE_AST_8K;
        } else if (rbId == R.id.rb_16k) {
            mSampleRate = SAMPLERATE_16K;
            AUDIO_FILE_PATH = AUDIO_FILE_PATH_16k;
            AUDIO_PROCESS_FILE_PATH = AUDIO_PROCESS_FILE_PATH_16k;
            srcPath = AUDIO_FILE_AST_16k;
        } else if (rbId == R.id.rb_32k) {
            process32KData = true;
            mSampleRate = SAMPLERATE_32K;
            AUDIO_FILE_PATH = AUDIO_FILE_PATH_32K;
            AUDIO_PROCESS_FILE_PATH = AUDIO_PROCESS_FILE_PATH_32k;
            srcPath = AUDIO_FILE_AST_32k;
        }

        initAudio();
        initAudioRecord();
    }

    @Override
    public void onClick(View v) {
        if (v == mBtnNsOperate) {
            if (!isInitialized || !mFile.exists()) {
                Toast.makeText(this, "文件读写失败", Toast.LENGTH_SHORT).show();
                return;
            }

            process();
        }
    }

    private void initAudio() {
        if (TextUtils.isEmpty(srcPath) || TextUtils.isEmpty(AUDIO_FILE_PATH) || TextUtils.isEmpty(AUDIO_PROCESS_FILE_PATH)) {
            return;
        }
        Log.e("aaa", "srcPath==" + srcPath);
        Log.e("aaa", "AUDIO_PROCESS_FILE_PATH==" + AUDIO_PROCESS_FILE_PATH);
        Log.e("aaa", "AUDIO_FILE_PATH==" + AUDIO_FILE_PATH);

        mProcessFile = new File(AUDIO_PROCESS_FILE_PATH);

        mFile = new File(AUDIO_FILE_PATH);

        if (!mFile.exists() || mFile.length() <= 0) {
            Log.e("aaa", " init file-----------");
            mThreadExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    AssetManager assets = getAssets();
                    try {
                        InputStream inputStream = assets.open(srcPath);
                        FileOutputStream fileOutputStream = new FileOutputStream(mFile);
                        byte[] buf = new byte[1024 * 1024];
                        int len;
                        while ((len = inputStream.read(buf)) != -1) {
                            fileOutputStream.write(buf, 0, len);
                        }
                        inputStream.close();
                        fileOutputStream.close();
                        isInitialized = true;
                        Log.e("aaa", " init file end-----------");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Log.e("aaa", "-----------");
            isInitialized = true;
        }
    }

    private void initAudioRecord() {
        stopPlay();
        mMinBufferSize = AudioRecord.getMinBufferSize(mSampleRate, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        if (mAudioTrack == null) {
            mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mSampleRate, AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, mMinBufferSize, AudioTrack.MODE_STREAM);
        }
    }

    private void stopPlay() {
        isPlaying = false;
        if (mAudioTrack != null) {
            mAudioTrack.release();
            mAudioTrack = null;
        }
    }


    private void process() {
        if (isProcessing) {
            return;
        }
        isProcessing = true;
        //ns初始化
        //fs == 8000 || fs == 16000 || fs == 32000 || fs == 48000
        nsxId = WebRtcNsUtils.WebRtcNsx_Create();
        int nsxInit = WebRtcNsUtils.WebRtcNsx_Init(nsxId, 8000); //0代表成功
        int nexSetPolicy = WebRtcNsUtils.nsxSetPolicy(nsxId, 2);

        //agc初始化
        agcId = WebRtcAGCUtils.WebRtcAgc_Create();
        //agcMode 0,1,2,3
        int agcInit = WebRtcAGCUtils.WebRtcAgc_Init(agcId, 0, 255, 3, mSampleRate);
        int agcSetConfig = WebRtcAGCUtils.agcSetConfig(agcId, (short)3, (short) 20, true);
        Log.e("aaa", "nexId--" + nsxId + "-----nsxInit----" + nsxInit + "---nexSetPolicy---" + nexSetPolicy);
        Log.e("aaa", "agcId---->" + agcId + "-----agcInit--->" + agcInit + "----agcSetConfig--" + agcSetConfig);
        mThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                FileInputStream ins = null;
                FileOutputStream out = null;
                try {
                    File inFile = mFile;
                    ins = new FileInputStream(inFile);
                    File outFile = new File(AUDIO_PROCESS_FILE_PATH);
                    out = new FileOutputStream(outFile);

                    byte[] buf;
                    buf = new byte[320];
                    while (ins.read(buf) != -1) {
                        short[] inputData = new short[buf.length >> 1];
                        short[] nsProcessData = new short[buf.length >> 1];
                        short[] outAgcData = new short[buf.length >> 1];

                        ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(inputData);
                        WebRtcNsUtils.WebRtcNsx_Process(nsxId, inputData, num_bands, nsProcessData);
                        if (mIsOpenAgc) {
                            int ret = WebRtcAGCUtils.agcProcess(agcId, nsProcessData, num_bands, 80, outAgcData, 0, 0, 0, false);
                            Log.e("aaa", "agc--->ret" + ret);
                            out.write(shortsToBytes(outAgcData));
                        } else {
                            out.write(shortsToBytes(nsProcessData));
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "处理完成", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    isProcessing = false;
                    WebRtcNsUtils.WebRtcNsx_Free(nsxId);
                    WebRtcAGCUtils.agcFree(agcId);
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (ins != null) {
                        try {
                            ins.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

    }


    private byte[] shortsToBytes(short[] data) {
        byte[] buffer = new byte[data.length * 2];
        int shortIndex, byteIndex;
        shortIndex = byteIndex = 0;
        for (; shortIndex != data.length; ) {
            buffer[byteIndex] = (byte) (data[shortIndex] & 0x00FF);
            buffer[byteIndex + 1] = (byte) ((data[shortIndex] & 0xFF00) >> 8);
            ++shortIndex;
            byteIndex += 2;
        }
        return buffer;
    }

}
