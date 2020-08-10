package com.hugh.webrtcdemo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;


import com.hugh.audiofun.R;
import com.hugh.component.audio.AudioTrackManager;
import com.hugh.libwebrtc.WebRtcAGCUtils;
import com.hugh.libwebrtc.WebRtcNsUtils;
import com.hugh.sound.util.ContentUtil;


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
 * 目前此页面只处理pcm文件
 */
public class RtcActivity extends Activity implements View.OnClickListener {

    private static final String TAG = RtcActivity.class.getSimpleName();

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
    private String mProcessFilePath;
    private String AUDIO_FILE_PATH;
    private String AUDIO_PROCESS_FILE_PATH;
    private String srcPath;
    private boolean process32KData;
    private int mSampleRate;
    private ExecutorService mThreadExecutor;
    private int selectId = -1;
    private boolean isPlaying;
    private Switch agc_switch;
    private TextView mTvHandleFile;
    private TextView mTvHandleSampleRate;
    private TextView mTvTitle;
    private Button mBtnChooseFile;
    private Button mBtnChooseSample;
    private TextView mTvCurrentProcessFile;
    private boolean mIsOpenAgc;
    private long nsxId; //ns降噪id
    private long agcId; //agc增益id
    private int num_bands = 1;
    private Button mPlayBtn;
    private Button mPlayOriginBtn;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.module_rtc_activity_main);
        agc_switch = findViewById(R.id.agc_switch);
        mTvHandleFile = findViewById(R.id.tv_current_handle_file);
        mTvHandleSampleRate = findViewById(R.id.tv_current_handle_sample);
        mBtnChooseFile = findViewById(R.id.btn_choose_file);
        mTvTitle = findViewById(R.id.tv_title);
        mBtnChooseSample = findViewById(R.id.btn_choose_sample);
        mPlayBtn = findViewById(R.id.btn_audio_play);
        mTvCurrentProcessFile = findViewById(R.id.tv_current_processfile);
        mPlayOriginBtn = findViewById(R.id.btn_play_origin_file);
        mBtnNsOperate = findViewById(R.id.ns_audio);
        mBtnNsOperate.setOnClickListener(this);
        mPlayBtn.setOnClickListener(this);
        mBtnChooseFile.setOnClickListener(this);
        mPlayOriginBtn.setOnClickListener(this);
        mTvTitle.setText("webrtc测试");
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

        mBtnChooseSample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RtcChooseSampleDialog dialog = new RtcChooseSampleDialog(RtcActivity.this, new RtcChooseSampleDialog.SampleDialogListener() {
                    @Override
                    public void selectSample(int Sample) {
                        mSampleRate = Sample;
                        mTvHandleSampleRate.setText(String.valueOf(mSampleRate));
                    }
                });
                dialog.show();
            }
        });
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


    protected void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        this.startActivityForResult(intent, 123);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && data.getData() != null) {
            String filePath = ContentUtil.getPath(this, data.getData());
            Log.i(TAG, "filePath=" + filePath);
            onSelectedFile(filePath);
        }
    }

    void onSelectedFile(String filePath) {
        mTvHandleFile.setText(filePath);
    }

    private void initAudioFile() {
        //初始化默认值
        mSampleRate = SAMPLERATE_8K;
        AUDIO_FILE_PATH = AUDIO_FILE_PATH_8k;
        AUDIO_PROCESS_FILE_PATH = AUDIO_PROCESS_FILE_PATH_8k;
        srcPath = AUDIO_FILE_AST_8K;
        mTvHandleFile.setText(AUDIO_FILE_PATH);
        mTvHandleSampleRate.setText(String.valueOf(mSampleRate));
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
        mTvHandleFile.setText(AUDIO_FILE_PATH);
        mTvHandleSampleRate.setText(String.valueOf(mSampleRate));
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
        } else if (v == mPlayBtn) {
            Log.e("aaa","当前播放新文件采样率----->"+mSampleRate);
            AudioTrackManager.getInstance().initConfig(mSampleRate);
            AudioTrackManager.getInstance().play(mProcessFilePath);
        }else if(v == mBtnChooseFile){
            selectFile();
        }else if(v == mPlayOriginBtn){
            Log.e("aaa","当前播放原文件采样率----->"+mSampleRate);
            Log.e("aaa","当前播放原文件 ----->"+AUDIO_FILE_PATH);
            AudioTrackManager.getInstance().initConfig(mSampleRate);
            AudioTrackManager.getInstance().play(AUDIO_FILE_PATH);
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
        mProcessFilePath = AUDIO_PROCESS_FILE_PATH;
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
        int nsxInit = WebRtcNsUtils.WebRtcNsx_Init(nsxId, mSampleRate); //0代表成功
        int nexSetPolicy = WebRtcNsUtils.nsxSetPolicy(nsxId, 2);

        //agc初始化
        agcId = WebRtcAGCUtils.WebRtcAgc_Create();
        //agcMode 0,1,2,3
        int agcInit = WebRtcAGCUtils.WebRtcAgc_Init(agcId, 0, 255, 3, mSampleRate);
        int agcSetConfig = WebRtcAGCUtils.agcSetConfig(agcId, (short) 3, (short) 20, true);
        Log.e("aaa", "nexId--" + nsxId + "-----nsxInit----" + nsxInit + "---nexSetPolicy---" + nexSetPolicy);
        Log.e("aaa", "agcId---->" + agcId + "-----agcInit--->" + agcInit + "----agcSetConfig--" + agcSetConfig);
        int sample = 80;
        if(mSampleRate == 8000){
            sample =80;
        }else if(mSampleRate == 16000){
            sample = 160;
        }else if(mSampleRate == 32000){
            sample = 320;
        }else if(mSampleRate == 48000){
            sample = 480;
        }
        final int finalSample = sample;
        mProcessFilePath = AUDIO_PROCESS_FILE_PATH;
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

                    Log.e("aaa","操作文件:"+inFile+"----mSampleRate:"+mSampleRate+"----->fq:"+finalSample);

                    byte[] buf;
                    buf = new byte[320];
                    while (ins.read(buf) != -1) {
                        short[] inputData = new short[buf.length >> 1];
                        short[] nsProcessData = new short[buf.length >> 1];
                        short[] outAgcData = new short[buf.length >> 1];

                        ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(inputData);
                        WebRtcNsUtils.WebRtcNsx_Process(nsxId, inputData, num_bands, nsProcessData);
                        if (mIsOpenAgc) {
                            int ret = WebRtcAGCUtils.agcProcess(agcId, nsProcessData, num_bands, finalSample, outAgcData, 0, 0, 0, false);
                            if(ret !=0){
                                Log.e("aaa","agcProcess 出问题");
                            }
                            out.write(shortsToBytes(outAgcData));
                        } else {
                            out.write(shortsToBytes(nsProcessData));
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTvCurrentProcessFile.setText(mProcessFilePath);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AudioTrackManager.getInstance().stop();
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
