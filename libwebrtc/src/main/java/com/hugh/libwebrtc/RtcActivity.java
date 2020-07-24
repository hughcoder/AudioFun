package com.hugh.libwebrtc;

import android.os.Bundle;

import org.webrtc.PeerConnection;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by chenyw on 2020/7/24.
 */
public class RtcActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.module_rtc_activity_main);
    }
}
