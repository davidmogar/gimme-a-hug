package com.davidmogar.gimmeahug.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.davidmogar.gimmeahug.R;
import com.soundcloud.android.crop.Crop;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;


public class SignupActivity extends ActionBarActivity {

    private static final int RESULT_PICK_IMAGE = 2000;
    private static final int RESULT_CROP_IMAGE = 3000;

    private CircleImageView ivProfileImage;
    private EditText etUsername;
    private EditText etEmail;
    private EditText etPassword;

    private Toolbar toolbar;

    private Bitmap profileImage;

    private boolean waitForUpload = false;
    private byte[] imageData;

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
                    profileImage = BitmapFactory.decodeFile(croppedImageFile.getAbsolutePath());
                    ivProfileImage.setImageBitmap(profileImage);

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

        initializeForm();
    }

    public void handleSignUpData() {
        String username = etUsername.getText().toString();
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        if (username.trim().length() == 0 || email.trim().length() == 0 || password.trim().length() == 0) {
            Toast.makeText(getApplicationContext(), getText(R.string.empty_form_error), Toast.LENGTH_SHORT).show();
        } else if (password.length() < 5) {
            Toast.makeText(getApplicationContext(), getText(R.string.password_not_secure_error), Toast.LENGTH_SHORT).show();
        } else {
            signUp(username, email, password);
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

    public void signUp(String username, String email, String password) {
        CreateUserTask task = new CreateUserTask();
        task.execute(username, email, password);
    }

    public void uploadImage(JSONObject object) {
        if (imageData != null) {
            waitForUpload = true;

            try {
                String id = object.getJSONObject("data").getString("_id");
                UploadImageTask task = new UploadImageTask();
                task.execute(id);
            } catch(JSONException e) {
                Log.v("RestService", "No user id found");
            }
        }
    }

    public void selectProfileImage(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select File"), RESULT_PICK_IMAGE);
    }

    private void cropImage(Uri sourceImage, Uri croppedImage) {
        new Crop(sourceImage).output(croppedImage).asSquare().withMaxSize(200, 200).start(this);
    }

    private void initializeForm() {
        ivProfileImage = (CircleImageView) findViewById(R.id.profile_image);
        etUsername = (EditText) findViewById(R.id.username);
        etEmail = (EditText) findViewById(R.id.email);
        etPassword = (EditText) findViewById(R.id.password);

        Intent intent = getIntent();

        if (intent.getExtras() != null) {
            String displayName = getIntent().getStringExtra("displayName");
            String email = getIntent().getStringExtra("email");

            etUsername.setText(displayName);
            etEmail.setText(email);
        }
    }

    private void startNextActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("username", etUsername.getText().toString());
        intent.putExtra("email", etEmail.getText().toString());

        if (imageData != null) {
            intent.putExtra("profileImage", imageData);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private class CreateUserTask extends AsyncTask<String, String, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            boolean result = false;

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://156.35.95.69:8002/users");
            post.setHeader("content-type", "application/json");

            try {
                JSONObject user = new JSONObject();
                user.put("username", params[0]);
                user.put("email", params[1]);
                user.put("password", params[2]);

                post.setEntity(new StringEntity(user.toString()));

                HttpResponse httpResponse = httpClient.execute(post);
                JSONObject response = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
                if (response.getBoolean("type")) {
                    uploadImage(response);
                    result = true;
                }
            } catch (Exception e) {
                Log.v("RestService", "CreateUserTask error: " + e);
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                if (!waitForUpload) {
                    startNextActivity();
                }
            } else {
                Toast.makeText(getApplicationContext(), getText(R.string.cant_create_error), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class UploadImageTask extends AsyncTask<String, String, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            Toast.makeText(getApplicationContext(), getText(R.string.uploading_image), Toast.LENGTH_SHORT).show();

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://156.35.95.69:8002/users/" + params[0] + "/image");

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            profileImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
            imageData = stream.toByteArray();

            InputStreamBody inputStreamBody = new InputStreamBody(new ByteArrayInputStream(imageData), params[0]);

            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
            multipartEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            multipartEntityBuilder.addPart("source", inputStreamBody);

            post.setEntity(multipartEntityBuilder.build());

            try {
                HttpResponse httpResponse = httpClient.execute(post);
                JSONObject response = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
                Log.v("FUCK", response.toString());
            } catch (IOException e) {
                Log.v("RestService", "Can't upload image");
            } catch (JSONException e) {
                Log.v("RestService", "Can't upload image");
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            startNextActivity();
        }
    }
}
