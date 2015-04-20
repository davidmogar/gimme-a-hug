package com.davidmogar.gimmeahug.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.davidmogar.gimmeahug.R;
import com.davidmogar.gimmeahug.models.User;
import com.melnykov.fab.FloatingActionButton;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserInfoFragment extends Fragment {

    private TextView followersCount;
    private User user;
    private boolean alreadyFollowing = false;

    public static UserInfoFragment newInstance(User user) {
        UserInfoFragment fragment = new UserInfoFragment();
        fragment.user = user;
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_user_info, container, false);

        ((TextView) view.findViewById(R.id.username)).setText(user.getName());
        ((TextView) view.findViewById(R.id.email)).setText(user.getEmail());
        followersCount = (TextView) view.findViewById(R.id.followers_count);
        followersCount.setText(String.valueOf(user.getFollowers()));
        ((TextView) view.findViewById(R.id.hugs_count)).setText(String.valueOf(user.getHugs()));

        CircleImageView imageView = (CircleImageView) view.findViewById(R.id.profile_image);
        new ImageLoadTask("http://156.35.95.69/gimmeahug/" + user.getId(), imageView).execute();

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!alreadyFollowing) {
                    SharedPreferences sharedPreferences = view.getContext().getSharedPreferences(getString(R.string.shared_preferences_name), Context.MODE_PRIVATE);
                    String userId = sharedPreferences.getString(getString(R.string.user_id), "");

                    if (userId.trim().length() > 0) {
                        new FollowUserTask().execute(userId, user.getId());
                    }
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), getText(R.string.already_following), Toast.LENGTH_SHORT).show();
                }
            }
        });

        final Button sendHug = (Button) view.findViewById(R.id.send_hug);
        sendHug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendHugNotification();
            }
        });

        return view;
    }

    public void sendHugNotification() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.shared_preferences_name), Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString(getString(R.string.user_id), "");

        if (userId.trim().length() > 0) {
            new SendHugNotificationTask().execute(user.getId(), userId);
        }
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

    public class FollowUserTask extends AsyncTask<String, String, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://156.35.95.69:8002/users/" + params[0] + "/follow/" + params[1]);
            post.setHeader("content-type", "application/json");

            try {
                HttpResponse httpResponse = httpClient.execute(post);
                JSONObject response = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
                if (response.getBoolean("type")) {
                    return true;
                }
            } catch (Exception e) {
                Log.v("RestService", "FollowUserTask error: " + e);
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Toast.makeText(getActivity().getApplicationContext(), getText(R.string.following), Toast.LENGTH_SHORT).show();
                followersCount.setText(String.valueOf(Integer.parseInt(followersCount.getText().toString()) + 1));
                alreadyFollowing = true;
            }
        }
    }

    public class SendHugNotificationTask extends AsyncTask<String, String, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://156.35.95.69:8002/users/" + params[0] + "/notify/" + params[1]);
            post.setHeader("content-type", "application/json");

            try {
                HttpResponse httpResponse = httpClient.execute(post);
                JSONObject response = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
                if (response.getBoolean("type")) {
                    return true;
                }
            } catch (Exception e) {
                Log.v("RestService", "SendHugNotificationTask error: " + e);
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Toast.makeText(getActivity().getApplicationContext(), getText(R.string.notification_sent), Toast.LENGTH_SHORT).show();
            }
        }
    }

}
