package com.hugh.sound;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by chenyw on 2020/7/15.
 */
public class SoundTouchActivity extends AppCompatActivity {
    private TextView textView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soundtouch);
        textView = findViewById(R.id.tv_text);
        textView.setText(SoundTouch.getVersionString());
        Log.e("aaa","version:"+SoundTouch.getVersionString());

        findViewById(R.id.btn_go).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              startActivity(new Intent(SoundTouchActivity.this,ExampleActivity.class));
            }
        });
    }
}
