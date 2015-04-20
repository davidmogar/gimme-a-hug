package com.davidmogar.gimmeahug.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.davidmogar.gimmeahug.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;

public class HugInfoActivity extends ActionBarActivity {

    private String userId;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hug_info);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        username = intent.getStringExtra("username");

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.shared_preferences_name), Context.MODE_PRIVATE);
        String currentUserId = sharedPreferences.getString(getString(R.string.user_id), "");

        if (currentUserId.trim().length() > 0) {
            CircleImageView imageView = (CircleImageView) findViewById(R.id.profile_image_user1);
            new ImageLoadTask("http://156.35.95.69/gimmeahug/" + currentUserId, imageView).execute();

            new CreateHugTask().execute(currentUserId, userId);
        }

        CircleImageView imageView = (CircleImageView) findViewById(R.id.profile_image_user2);
        new ImageLoadTask("http://156.35.95.69/gimmeahug/" + userId, imageView).execute();
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

    private class CreateHugTask extends AsyncTask<String, String, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            boolean result = false;

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://156.35.95.69:8002/hugs");
            post.setHeader("content-type", "application/json");

            try {
                JSONObject hug = new JSONObject();
                hug.put("user1", params[0]);
                hug.put("user2", params[1]);

                post.setEntity(new StringEntity(hug.toString()));

                HttpResponse httpResponse = httpClient.execute(post);
                JSONObject response = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
                if (response.getBoolean("type")) {
                    result = true;
                }
            } catch (Exception e) {
                Log.v("RestService", "CreateHugTask error: " + e);
            }

            return result;
        }
    }
}
