package com.davidmogar.gimmeahug.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;
import android.widget.VideoView;

import com.davidmogar.gimmeahug.R;


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

        TextView loginText = (TextView) findViewById(R.id.login);
        loginText.setText(Html.fromHtml(getText(R.string.login).toString()));
        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
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
