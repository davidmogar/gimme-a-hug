package com.davidmogar.gimmeahug;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.VideoView;


public class WelcomeActivity extends ActionBarActivity {

    private VideoView backgroundVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.hugs);
        backgroundVideo = (VideoView) findViewById(R.id.video_view);
        backgroundVideo.setVideoURI(videoUri);
        backgroundVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });
    }

    public void onSignup(View view) {
        switch (view.getId()) {
            case R.id.signup_email:
                startActivity(new Intent(this, SignupActivity.class));
                break;
            case R.id.signup_plus:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        backgroundVideo.start();
    }
}
