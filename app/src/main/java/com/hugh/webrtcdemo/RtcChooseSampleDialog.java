package com.hugh.webrtcdemo;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

import com.hugh.audiofun.R;

import androidx.annotation.NonNull;

/**
 * Created by chenyw on 2020/8/10.
 */
public class RtcChooseSampleDialog extends Dialog {

    private Button mBtnSure;
    private SampleDialogListener mSampleDialogListener;
    private int mSampleRate;
    private int selectId = -1;

    public RtcChooseSampleDialog(@NonNull Context context,SampleDialogListener sampleDialogListener) {
        super(context);
        mSampleDialogListener = sampleDialogListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_choose_sample);
        mBtnSure = findViewById(R.id.btn_sure);
        RadioGroup radioGroup = findViewById(R.id.rg);
        int checkedRadioButtonId = radioGroup.getCheckedRadioButtonId();
        switchDataSrc(checkedRadioButtonId);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switchDataSrc(checkedId);
            }
        });
        mBtnSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSampleDialogListener.selectSample(mSampleRate);
                dismiss();
            }
        });
    }

    private void switchDataSrc(int rbId) {
        selectId = rbId;
        if (rbId == R.id.rb_8k) {
            mSampleRate = 8000;
        } else if (rbId == R.id.rb_16k) {
            mSampleRate = 16000;
        } else if (rbId == R.id.rb_32k) {
            mSampleRate = 32000;
        }else if(rbId == R.id.rb_48k){
            mSampleRate = 48000;
        }else {
            mSampleRate =8000;
        }

    }

    public interface SampleDialogListener{
        void selectSample(int Sample);
    }
}
