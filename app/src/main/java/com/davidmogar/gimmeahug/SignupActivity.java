package com.davidmogar.gimmeahug;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.soundcloud.android.crop.Crop;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;


public class SignupActivity extends ActionBarActivity {

    private static final int RESULT_PICK_IMAGE = 2000;
    private static final int RESULT_CROP_IMAGE = 3000;
    private Toolbar toolbar;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        File croppedImageFile = new File(getFilesDir(), "profile.jpg");

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case RESULT_PICK_IMAGE:
                    cropImage(data.getData(), Uri.fromFile(croppedImageFile));
                    break;
                case Crop.REQUEST_CROP:
                    CircleImageView imageView = (CircleImageView) findViewById(R.id.profile_image);
                    imageView.setImageBitmap(BitmapFactory.decodeFile(croppedImageFile.getAbsolutePath()));
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    public void handleSignUpData() {
        String username = ((EditText) findViewById(R.id.username)).getText().toString();
        String password = ((EditText) findViewById(R.id.password)).getText().toString();

        if (username.trim().length() == 0 || password.trim().length() == 0) {
            Toast.makeText(getApplicationContext(), getText(R.string.signup_emptyform_error), Toast.LENGTH_SHORT).show();
        } else if (password.length() < 5) {
            Toast.makeText(getApplicationContext(), getText(R.string.password_not_secure_error), Toast.LENGTH_SHORT).show();
        } else {
//            signUp(username, password);
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_signup, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_done) {
            handleSignUpData();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void signUp(String username, String password) {
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet("");

        try {
            HttpResponse response = client.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == 200) {

            } else {

            }
        } catch (ClientProtocolException e) {

        } catch (IOException e) {

        }
    }

    public void selectProfileImage(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select File"), RESULT_PICK_IMAGE);
    }

    private void cropImage(Uri sourceImage, Uri croppedImage) {
        new Crop(sourceImage).output(croppedImage).asSquare().start(this);
    }
}
