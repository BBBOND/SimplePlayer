package com.kim.simpleplayerexample;

import android.os.Bundle;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.kim.simpleplayer.SimplePlayer;
import com.kim.simpleplayer.model.MediaData;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mEditText = (EditText) findViewById(R.id.editText);

        SimplePlayer simplePlayer = SimplePlayer.getInstance();
    }

    public void play(View view) {
        try {
            String url = mEditText.getText().toString(); //适用于 文件路径或网络路径
            MediaData md = new MediaData();
            md.setMediaId("123");
            md.setTitle("哈哈");
            md.setMediaUri(url);
            md.setDisplayTitle("呵呵");
            md.setDisplaySubtitle("xixixi");
            List<MediaData> mediaDatas = new ArrayList<>();
            mediaDatas.add(md);
            SimplePlayer.getInstance().setMediaDataList(mediaDatas);
            SimplePlayer.getInstance().getTransportControls(this, new SimplePlayer.GetTransportControlsCallback() {
                @Override
                public void success(MediaControllerCompat.TransportControls transportControls) {
                    if (transportControls != null)
                        transportControls.playFromMediaId("123", null);
                }

                @Override
                public void error(String errorMsg) {
                    Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SimplePlayer.getInstance().getTransportControls(this, new SimplePlayer.GetTransportControlsCallback() {
            @Override
            public void success(MediaControllerCompat.TransportControls transportControls) {
                transportControls.stop();
            }

            @Override
            public void error(String errorMsg) {

            }
        });
    }
}