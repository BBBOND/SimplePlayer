package com.kim.simpleplayerexample;

import android.app.Application;

import com.kim.simpleplayer.helper.LogHelper;

/**
 * Created by bbbond on 2017/4/3.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LogHelper.init(true);
    }
}
