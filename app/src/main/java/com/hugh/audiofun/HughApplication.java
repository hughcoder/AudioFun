package com.hugh.audiofun;

import android.app.Application;
import android.os.Environment;

import com.hugh.common.global.Variable;
import com.hugh.common.util.FileFunction;

/**
 * Created by chenyw on 2020/7/31.
 */
public class HughApplication extends Application {
    private static HughApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        init();

    }

    private void init() {
        FileFunction.InitStorage(this);
    }

    public static HughApplication getInstance() {
        return instance;
    }


}
