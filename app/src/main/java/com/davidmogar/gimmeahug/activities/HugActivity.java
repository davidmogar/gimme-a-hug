package com.davidmogar.gimmeahug.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.davidmogar.gimmeahug.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;

public class HugActivity extends ActionBarActivity {

    private String userId;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hug);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        username = intent.getStringExtra("username");

        ((TextView) findViewById(R.id.message)).setText(username + " " + getString(R.string.user_wants_hug));

        CircleImageView imageView = (CircleImageView) findViewById(R.id.profile_image);
        new ImageLoadTask("http://156.35.95.69/gimmeahug/" + userId, imageView).execute();
    }

    public void acceptHug(View view) {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.shared_preferences_name), Context.MODE_PRIVATE);
        String currentUserId = sharedPreferences.getString(getString(R.string.user_id), "");

        if (currentUserId.trim().length() > 0) {
            new AcceptHugTask().execute(currentUserId, userId);
        }
    }

    public void ignoreHug(View view) {
        finish();
    }

    public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {

        private String url;
        private ImageView imageView;

        public ImageLoadTask(String url, ImageView imageView) {
            this.url = url;
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                URL urlConnection = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlConnection
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            imageView.setImageBitmap(result);
        }

    }

    public class AcceptHugTask extends AsyncTask<String, String, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://156.35.95.69:8002/users/" + params[0] + "/accept/" + params[1]);
            post.setHeader("content-type", "application/json");

            try {
                HttpResponse httpResponse = httpClient.execute(post);
                JSONObject response = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
                if (response.getBoolean("type")) {
                    return true;
                }
            } catch (Exception e) {
                Log.v("RestService", "AcceptHugTask error: " + e);
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Intent intent = new Intent(getApplicationContext(), HugInfoActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("username", username);
            bundle.putString("userId", userId);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }
    }

}
