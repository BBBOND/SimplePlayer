package com.bbbond.simpleplayerexample;

import android.app.Application;

import com.bbbond.simpleplayer.helper.LogHelper;

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
