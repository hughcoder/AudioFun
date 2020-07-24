package com.hugh.audiofun;

import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.hugh.libwebrtc.RtcActivity;
import com.hugh.sound.SoundTouch;
import com.hugh.sound.SoundTouchActivity;
import com.zhl.commonadapter.BaseViewHolder;
import com.zhl.commonadapter.CommonAdapter;

import org.fmod.FMOD;

import java.util.Arrays;

import static com.hugh.audiofun.FmodSound.TYPE_ETHEREAL;
import static com.hugh.audiofun.FmodSound.TYPE_LOLITA;
import static com.hugh.audiofun.FmodSound.TYPE_TREMOLO;
import static com.hugh.audiofun.FmodSound.TYPE_UNCLE;
import static com.hugh.audiofun.FmodSound.getVersion;
import static com.hugh.audiofun.FmodSound.playSound;

public class MainActivity extends AppCompatActivity {
    private CommonAdapter<Item> mAdapter;
    String path = "file:///android_asset/lightlesson_excellent.mp3";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!FMOD.checkInit()) {
            FMOD.init(this);
        }
        ListView listView = findViewById(R.id.lv_main);

        Log.e("aaa", SoundTouch.getVersionString());

        mAdapter = new CommonAdapter<Item>(Arrays.asList(Item.values())) {
            @Override
            public BaseViewHolder<Item> createViewHolder(int type) {
                return new ItemVH();
            }
        };
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Item item = mAdapter.getItem(position);
            if (item != null) {
                switch (item) {
                    case TYPE_PLAY_1:
                        playSound(path, TYPE_LOLITA);
                        break;
                    case TYPE_PLAY_2:
                        playSound(path, TYPE_ETHEREAL);
                        break;
                    case TYPE_PLAY_3:
                        playSound(path, TYPE_TREMOLO);
                        break;
                    case TYPE_PLAY_4:
                        play3D(path);
                        break;
                    case TYPE_GO:
                        Intent intent = new Intent(MainActivity.this, SoundTouchActivity.class);
                        startActivity(intent);
                        break;
                    case TYPE_GO_WEBRTC:
                        startActivity(new Intent(MainActivity.this, RtcActivity.class));
                        break;
                    default:
                        break;
                }
            }
        });

//        goToSoundTouch();
    }

    private void goToSoundTouch() {
        Intent intent = new Intent(MainActivity.this, SoundTouchActivity.class);
        startActivity(intent);
    }

    private void play3D(String path) {
        FmodSound.play3DSound(path);
    }

    enum Item {
        TYPE_GO("进入soundTouch"),
        TYPE_PLAY_1("播放萝莉"),
        TYPE_PLAY_2("播放空灵"),
        TYPE_PLAY_3("播放颤音"),
        TYPE_PLAY_4("混响功能使用"),
        TYPE_GO_WEBRTC("进入webRtc");


        private String title;

        Item(String title) {
            this.title = title;
        }
    }

    class ItemVH extends BaseViewHolder<Item> {

        private TextView mTvItem;

        @Override
        public void findView(View view) {
            mTvItem = view.findViewById(R.id.tv_item);
        }

        @Override
        public void updateView(Item data, int position) {
            mTvItem.setText(data.title);
        }

        @Override
        public int getLayoutResId() {
            return R.layout.item_main;
        }
    }

}
