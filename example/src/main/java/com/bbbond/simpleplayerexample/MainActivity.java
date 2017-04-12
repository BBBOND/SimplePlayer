package com.bbbond.simpleplayerexample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import com.bbbond.simpleplayer.SimplePlayer;
import com.bbbond.simpleplayer.helper.LogHelper;
import com.bbbond.simpleplayer.model.MediaData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText mEditText;
    private Button pre, next, play;
    private SeekBar seekBar;

    private int playState = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mEditText = (EditText) findViewById(R.id.editText);
        pre = (Button) findViewById(R.id.button_pre);
        next = (Button) findViewById(R.id.button_next);
        play = (Button) findViewById(R.id.button_play);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setEnabled(false);
        seekBar.setOnSeekBarChangeListener(new SeekBarChangeEvent());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            play.setEnabled(true);
        }

        SimplePlayer.getInstance().setPlayingActivity(MainActivity.class);
        SimplePlayer.getInstance().setPlayContinuously(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String path = "http://192.168.31.137:8080/media/%E5%9B%A7%E8%8F%8C%20-%20%E4%B8%9C%E4%BA%AC%E4%B8%8D%E5%A4%AA%E7%83%AD.mp3";
        mEditText.setText(SimplePlayer.getInstance().getMediaUri() == null ? path : SimplePlayer.getInstance().getMediaUri());
        initState();
        SimplePlayer.getInstance().registerMediaControllerCallback(new MediaControllerCompat.Callback() {
            @Override
            public void onSessionDestroyed() {
                super.onSessionDestroyed();
                SimplePlayer.getInstance().release();
            }

            @Override
            public void onPlaybackStateChanged(PlaybackStateCompat state) {
                switch (state.getState()) {
                    case PlaybackStateCompat.STATE_PAUSED:
                        playState = 1;
                        play.setEnabled(true);
                        play.setText("播放");
                        pre.setEnabled(true);
                        next.setEnabled(true);
                        seekBar.setEnabled(true);
                        break;
                    case PlaybackStateCompat.STATE_PLAYING:
                        playState = 2;
                        play.setEnabled(true);
                        play.setText("暂停");
                        pre.setEnabled(true);
                        next.setEnabled(true);
                        seekBar.setEnabled(true);
                        break;
                    case PlaybackStateCompat.STATE_STOPPED:
                        playState = 0;
                        play.setEnabled(true);
                        play.setText("播放");
                        pre.setEnabled(true);
                        next.setEnabled(true);
                        seekBar.setEnabled(false);
                        break;
                    case PlaybackStateCompat.STATE_BUFFERING:
                        playState = 3;
                        play.setEnabled(false);
                        play.setText("加载中...");
                        pre.setEnabled(true);
                        next.setEnabled(true);
                        seekBar.setEnabled(false);
                        break;
                }
            }

            @Override
            public void onMetadataChanged(MediaMetadataCompat metadata) {
                super.onMetadataChanged(metadata);
                if (metadata != null && metadata.getDescription() != null)
                    LogHelper.d("------", "onMetadataChanged: " + metadata.getDescription().getMediaUri());
            }
        });
        registerProgressListener();
    }

    private void registerProgressListener() {
        SimplePlayer.getInstance().setOnProgressChangeListener(new SimplePlayer.OnProgressChangeListener() {
            @Override
            public void progressChanged(int progress) {
                if (seekBar != null)
                    seekBar.setProgress(progress);
            }

            @Override
            public void secondaryProgressChanged(int progress) {
                if (seekBar != null) {
                    progress = (int) ((progress / 100.0) * seekBar.getMax());
                    seekBar.setSecondaryProgress(progress);
                }
            }

            @Override
            public void completion() {
//                stopMusic();
                Toast.makeText(MainActivity.this, "播放完成", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unregisterProgressListener() {
        SimplePlayer.getInstance().setOnProgressChangeListener(null);
    }

    @Override
    protected void onStop() {
        SimplePlayer.getInstance().unregisterMediaControllerCallback();
        unregisterProgressListener();
        super.onStop();
    }

    private void initState() {
        switch (SimplePlayer.getInstance().getState()) {
            case PlaybackStateCompat.STATE_PAUSED:
                playState = 1;
                play.setEnabled(true);
                play.setText("播放");
                pre.setEnabled(true);
                next.setEnabled(true);
                seekBar.setEnabled(true);
                break;
            case PlaybackStateCompat.STATE_PLAYING:
                playState = 2;
                play.setEnabled(true);
                play.setText("暂停");
                pre.setEnabled(true);
                next.setEnabled(true);
                seekBar.setEnabled(true);
                break;
            case PlaybackStateCompat.STATE_STOPPED:
                playState = 0;
                play.setEnabled(true);
                play.setText("播放");
                pre.setEnabled(true);
                next.setEnabled(true);
                seekBar.setEnabled(false);
                break;
            case PlaybackStateCompat.STATE_BUFFERING:
                playState = 3;
                play.setEnabled(false);
                play.setText("加载中...");
                pre.setEnabled(true);
                next.setEnabled(true);
                seekBar.setEnabled(false);
                break;
        }
    }

    public void play(View view) {
        switch (playState) {
            case 0:
                playMusicFirst();
                play.setText("加载中...");
                play.setEnabled(false);
                break;
            case 1:
                playMusic();
                break;
            case 2:
                pauseMusic();
                break;
            default:
                Toast.makeText(this, "未知状态", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public void pre(View view) {
        SimplePlayer.getInstance().getTransportControls(this, new SimplePlayer.GetTransportControlsCallback() {
            @Override
            public void success(MediaControllerCompat.TransportControls transportControls) {
                if (transportControls != null)
                    transportControls.skipToPrevious();
            }

            @Override
            public void error(String errorMsg) {
                Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void next(View view) {
        SimplePlayer.getInstance().getTransportControls(this, new SimplePlayer.GetTransportControlsCallback() {
            @Override
            public void success(MediaControllerCompat.TransportControls transportControls) {
                if (transportControls != null)
                    transportControls.skipToNext();
            }

            @Override
            public void error(String errorMsg) {
                Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void playMusicFirst() {
        try {
            String url = mEditText.getText().toString();
            Toast.makeText(this, url, Toast.LENGTH_SHORT).show();
            MediaData md = new MediaData();
            md.setMediaId("123");
            md.setTitle("哈哈");
            md.setDisplayTitle("呵呵");
            md.setDisplaySubtitle("呵呵哒");
            md.setMediaUri(url);
            md.setArtUri("http://g.hiphotos.baidu.com/baike/w%3D268%3Bg%3D0/sign=010ff86baa345982c58ae29434cf5690/faedab64034f78f061e4f6ec7e310a55b2191cc5.jpg");
            md.setAlbumArtUri("http://g.hiphotos.baidu.com/baike/w%3D268%3Bg%3D0/sign=010ff86baa345982c58ae29434cf5690/faedab64034f78f061e4f6ec7e310a55b2191cc5.jpg");
            md.setArtist("鹏泊");
            md.setAuthor("鹏泊");
            List<MediaData> mediaDatas = new ArrayList<>();
            mediaDatas.add(md);
            SimplePlayer.getInstance().setMediaDataList("music", mediaDatas, null);
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

    public void playMusic() {
        SimplePlayer.getInstance().getTransportControls(this, new SimplePlayer.GetTransportControlsCallback() {
            @Override
            public void success(MediaControllerCompat.TransportControls transportControls) {
                if (transportControls != null)
                    transportControls.play();
            }

            @Override
            public void error(String errorMsg) {
                Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void pauseMusic() {
        SimplePlayer.getInstance().getTransportControls(this, new SimplePlayer.GetTransportControlsCallback() {
            @Override
            public void success(MediaControllerCompat.TransportControls transportControls) {
                if (transportControls != null)
                    transportControls.pause();
            }

            @Override
            public void error(String errorMsg) {
                Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void stopMusic() {
        SimplePlayer.getInstance().getTransportControls(this, new SimplePlayer.GetTransportControlsCallback() {
            @Override
            public void success(MediaControllerCompat.TransportControls transportControls) {
                if (transportControls != null)
                    transportControls.stop();
            }

            @Override
            public void error(String errorMsg) {
                Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void addMusic(View view) {
        MediaData mediaData = new MediaData()
                .setMediaId(new Date().getTime() + "")
                .setMediaUri(mEditText.getText().toString())
                .setTitle("1")
                .setDisplayTitle("2")
                .setDisplaySubtitle("3")
                .setArtUri(null)
                .setAlbumArtUri(null)
                .setArtist("4")
                .setAuthor("5");
        boolean result = SimplePlayer.getInstance().addMediaData(mediaData);
        Toast.makeText(this, result ? "添加成功" : "添加失败", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (permissions.length != 1 || grantResults.length != 1 ||
                    grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "已拒绝播放！", Toast.LENGTH_SHORT).show();
            } else {
                play.setEnabled(true);
            }
        } else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private class SeekBarChangeEvent implements SeekBar.OnSeekBarChangeListener {
        int progress;

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            this.progress = progress * SimplePlayer.getInstance().getDuration() / seekBar.getMax();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            unregisterProgressListener();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            SimplePlayer.getInstance().getTransportControls(MainActivity.this, new SimplePlayer.GetTransportControlsCallback() {
                @Override
                public void success(MediaControllerCompat.TransportControls transportControls) {
                    if (transportControls != null)
                        transportControls.seekTo(progress);
                    registerProgressListener();
                }

                @Override
                public void error(String errorMsg) {
                    Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    registerProgressListener();
                }
            });

        }
    }

}
