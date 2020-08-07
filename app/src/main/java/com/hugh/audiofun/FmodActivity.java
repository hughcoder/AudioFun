package com.hugh.audiofun;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.hugh.audiohome.AudioFunHomeActivity;
import com.hugh.libwebrtc.RtcActivity;
import com.hugh.libwebrtc.other.RtcFileActivity;
import com.hugh.sound.SoundTouch;
import com.hugh.sound.SoundTouchActivity;
import com.zhl.commonadapter.BaseViewHolder;
import com.zhl.commonadapter.CommonAdapter;

import org.fmod.FMOD;

import java.util.Arrays;

import static com.hugh.audiofun.FmodSound.TYPE_CHORUS;
import static com.hugh.audiofun.FmodSound.TYPE_ETHEREAL;
import static com.hugh.audiofun.FmodSound.TYPE_FUNNY;
import static com.hugh.audiofun.FmodSound.TYPE_LOLITA;
import static com.hugh.audiofun.FmodSound.TYPE_NORMAL;
import static com.hugh.audiofun.FmodSound.TYPE_THRILLER;
import static com.hugh.audiofun.FmodSound.TYPE_TREMOLO;
import static com.hugh.audiofun.FmodSound.TYPE_UNCLE;
import static com.hugh.audiofun.FmodSound.playSound;

public class FmodActivity extends Activity {
    private CommonAdapter<Item> mAdapter;
    String path = "file:///android_asset/lightlesson_excellent.mp3";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fmod);
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
                        playSound(path, TYPE_NORMAL);
                        break;
                    case TYPE_PLAY_2:
                        playSound(path, TYPE_LOLITA);
                        break;
                    case TYPE_PLAY_3:
                        playSound(path, TYPE_UNCLE);
                        break;
                    case TYPE_PLAY_4:
                        playSound(path, TYPE_THRILLER);
                        break;
                    case TYPE_PLAY_5:
                        playSound(path, TYPE_FUNNY);
                        break;
                    case TYPE_PLAY_6:
                        playSound(path, TYPE_ETHEREAL);
                        break;
                    case TYPE_PLAY_7:
                        playSound(path, TYPE_CHORUS);
                        break;
                    case TYPE_PLAY_8:
                        playSound(path, TYPE_TREMOLO);
                        break;
                    case TYPE_GO:
                        Intent intent = new Intent(FmodActivity.this, SoundTouchActivity.class);
                        startActivity(intent);
                        break;
                    case TYPE_GO_WEBRTC:
                        startActivity(new Intent(FmodActivity.this, RtcActivity.class));
                        break;
                    case TYPE_GO_FUN_AUDIO:
                        startActivity(new Intent(FmodActivity.this, AudioFunHomeActivity.class));
                        break;
                    case TYPE_GO_WEBRTC2:
                        startActivity(new Intent(FmodActivity.this, RtcFileActivity.class));
                        break;
                    default:
                        break;
                }
            }
        });

//        goToSoundTouch();
    }

    private void goToSoundTouch() {
        Intent intent = new Intent(FmodActivity.this, SoundTouchActivity.class);
        startActivity(intent);
    }

    private void play3D(String path) {
        FmodSound.play3DSound(path);
    }

    enum Item {
        TYPE_GO("进入soundTouch"),
        TYPE_PLAY_1("播放普通"),
        TYPE_PLAY_2("播放萝莉"),
        TYPE_PLAY_3("播放大叔"),
        TYPE_PLAY_4("播放惊悚"),
        TYPE_PLAY_5("播放搞怪"),
        TYPE_PLAY_6("播放空灵"),
        TYPE_PLAY_7("播放合唱团"),
        TYPE_PLAY_8("播放颤音"),
        TYPE_GO_WEBRTC("进入webRtc"),
        TYPE_GO_WEBRTC2("进入webRtc2"),
        TYPE_GO_FUN_AUDIO("进入录音调节页面");


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
