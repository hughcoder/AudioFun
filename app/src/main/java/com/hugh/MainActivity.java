package com.hugh;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.hugh.audiofun.FmodActivity;
import com.hugh.audiofun.R;
import com.hugh.libwebrtc.other.RtcFileActivity;
import com.hugh.webrtcdemo.RtcActivity;
import com.hugh.sound.SoundTouchExActivity;

import androidx.annotation.Nullable;

/**
 * Created by chenyw on 2020/8/7.
 */
public class MainActivity extends Activity {



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_go_fmod).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, FmodActivity.class));
            }
        });

        findViewById(R.id.btn_go_soundtouch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SoundTouchExActivity.class));
            }
        });

        findViewById(R.id.btn_go_webrtc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RtcActivity.class));
            }
        });

        findViewById(R.id.btn_go_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
