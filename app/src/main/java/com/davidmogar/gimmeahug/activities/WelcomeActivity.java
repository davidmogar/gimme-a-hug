package com.davidmogar.gimmeahug.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.VideoView;

import com.davidmogar.gimmeahug.R;

import java.util.regex.Pattern;


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
                Intent intent = new Intent(this, SignupActivity.class);
                intent.putExtra("displayName", getUserDisplayName());
                intent.putExtra("email", getUserEmail());
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        backgroundVideo.start();
    }

    private String getUserDisplayName() {
        String displayName;

        Cursor cursor = getApplication().getContentResolver().query(ContactsContract.Profile.CONTENT_URI, null, null, null, null);
        cursor.moveToFirst();
        displayName = cursor.getString(cursor.getColumnIndex("display_name"));
        cursor.close();

        return displayName;
    }

    private String getUserEmail() {
        String email = "";

        Pattern emailPattern = Patterns.EMAIL_ADDRESS;
        Account[] accounts = AccountManager.get(getApplicationContext()).getAccounts();

        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                email = account.name;
                break;
            }
        }

        return email;
    }
}
