package com.hugh.audiofun;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.fmod.FMOD;

import static com.hugh.audiofun.FmodSound.playSound;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    String path = "file:///android_asset/lightlesson_excellent.mp3";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!FMOD.checkInit()) {
            FMOD.init(this);
        }

        // Example of a call to a native method
        TextView tv = findViewById(R.id.sample_text);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound(path, FmodSound.TYPE_UNCLE);

            }
        });
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
}
