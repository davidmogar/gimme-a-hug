package com.davidmogar.gimmeahug.activities;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;

import com.davidmogar.gimmeahug.R;
import com.davidmogar.gimmeahug.fragments.MapFragment;
import com.davidmogar.gimmeahug.fragments.ProfileFragment;
import com.davidmogar.gimmeahug.fragments.SettingsFragment;
import com.davidmogar.gimmeahug.fragments.StarredFragment;
import com.davidmogar.gimmeahug.fragments.UsersFragment;
import com.davidmogar.gimmeahug.models.Comment;
import com.davidmogar.gimmeahug.models.User;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import br.liveo.interfaces.NavigationLiveoListener;
import br.liveo.navigationliveo.NavigationLiveo;

public class MainActivity extends NavigationLiveo implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, NavigationLiveoListener, LocationListener, StarredFragment.OnProfileRequestListener {

    private UsersFragment usersFragment;
    private StarredFragment starredFragment;
    private GoogleApiClient googleApiClient;
    private List<String> itemsNames;
    private LocationRequest locationRequest;
    private List<User> users;
    private Timer notificationTimer;
    private Timer acceptedTimer;

    @Override
    public void onClickFooterItemNavigation(View view) {
        this.setCheckedItemNavigation(getCurrentPosition(), false);
        this.getToolbar().setTitle(getString(R.string.settings));

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = new SettingsFragment();
        fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();
    }

    @Override
    public void onClickUserPhotoNavigation(View view) {

    }

    @Override
    public void onInt(Bundle bundle) {
        this.setNavigationListener(this);
        this.setDefaultStartPositionNavigation(0);

        itemsNames = new ArrayList<>();
        itemsNames.add(0, getString(R.string.active_users));
        itemsNames.add(1, getString(R.string.explore));
        itemsNames.add(2, getString(R.string.starred_users));
        itemsNames.add(3, getString(R.string.account));
        itemsNames.add(4, getString(R.string.log_out));


        List<Integer> itemsIcons = new ArrayList<>();
        itemsIcons.add(0, R.drawable.ic_group_black_24dp);
        itemsIcons.add(1, R.drawable.ic_explore_black_24dp);
        itemsIcons.add(2, R.drawable.ic_star_black_24dp);
        itemsIcons.add(3, 0);
        itemsIcons.add(4, R.drawable.ic_exit_to_app_black_24dp);

        List<Integer> headers = new ArrayList<>();
        headers.add(3);

        this.setFooterInformationDrawer("Settings", R.drawable.ic_settings_black_24dp);
        this.setNavigationAdapter(itemsNames, itemsIcons, headers, null);

        buildGoogleApiClient();
        createLocationRequest();
        setNotificationTimer();
        setAcceptedTimer();
    }

    private void setNotificationTimer() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.shared_preferences_name), Context.MODE_PRIVATE);
        final String userId = sharedPreferences.getString(getString(R.string.user_id), "");

        if (userId.trim().length() > 0) {
            notificationTimer = new Timer();
            notificationTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    new GetNotifications().execute(userId);
                }
            }, 0, 10000);
        }
    }

    private void setAcceptedTimer() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.shared_preferences_name), Context.MODE_PRIVATE);
        final String userId = sharedPreferences.getString(getString(R.string.user_id), "");

        if (userId.trim().length() > 0) {
            acceptedTimer = new Timer();
            acceptedTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    new GetAcceptedHugs().execute(userId);
                }
            }, 0, 10000);
        }
    }

    @Override
    public void onItemClickNavigation(int position, int layoutContainerId) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = null;

        switch (position) {
            case 0:
                this.getToolbar().setTitle(getString(R.string.active_users));
                fragment = new UsersFragment().newInstance();
                usersFragment = (UsersFragment) fragment;
                break;
            case 1:
                this.getToolbar().setTitle(getString(R.string.explore));
                fragment = new MapFragment().newInstance(false);
                break;
            case 2:
                this.getToolbar().setTitle(getString(R.string.starred_users));
                fragment = new StarredFragment().newInstance();
                starredFragment = (StarredFragment) fragment;
                break;
            case 4:
                startActivity(new Intent(this, WelcomeActivity.class));
                finish();
                break;
        }

        if (fragment != null) {
            fragmentManager.beginTransaction().replace(layoutContainerId, fragment).commit();
        }
    }

    @Override
    public void onPrepareOptionsMenuNavigation(Menu menu, int i, boolean b) {

    }

    @Override
    public void onUserInformation() {
        this.mUserBackground.setImageResource(R.drawable.drawer_background);

        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            Bundle extras = intent.getExtras();
            this.mUserName.setText(extras.getString("username"));
            this.mUserEmail.setText(extras.getString("email"));

            if (intent.hasExtra("profileImage")) {
                byte[] image = extras.getByteArray("profileImage");
                Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                this.mUserPhoto.setImageBitmap(bitmap);
            } else {
                new ImageLoadTask("http://156.35.95.69/gimmeahug/" + extras.getString("userId"), this.mUserPhoto).execute();
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (googleApiClient != null && googleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    protected void stopLocationUpdates() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    googleApiClient, this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.shared_preferences_name), Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString(getString(R.string.user_id), "");

        if (userId.trim().length() > 0) {
            new UpdateLocationTask().execute(userId, String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
        }

        if (usersFragment != null) {
            usersFragment.centerMapCamera(location);
        }
    }

    private void processUserList(JSONArray data) {
        users = new ArrayList<>();

        for (int i = 0; i < data.length(); i++) {
            try {
                JSONObject userObject = data.getJSONObject(i);
                User user = new User(userObject.getString("_id"), userObject.getString("username"), userObject.getString("email"));
                user.setFollowers(userObject.getJSONArray("followers").length());
                users.add(user);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        updateUI();
    }

    private void processNotifications(JSONArray data) {
        for (int i = 0; i < data.length(); i++) {
            try {
                JSONObject object = data.getJSONObject(i);
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(MainActivity.this)
                                .setAutoCancel(true)
                                .setSmallIcon(R.drawable.ic_person_white_24dp)
                                .setLargeIcon((((BitmapDrawable) getResources()
                                        .getDrawable(R.drawable.ic_person_white_24dp)).getBitmap()))
                                .setContentTitle(getString(R.string.request_notification_title))
                                .setContentText(getString(R.string.a_user_wants_hug))
                                .setContentTitle(getString(R.string.request_notification_title));
                Intent notIntent = new Intent(this, HugActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("username", object.getString("username"));
                bundle.putString("userId", object.getString("_id"));
                notIntent.putExtras(bundle);

                PendingIntent contIntent = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(contIntent);
                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                mNotificationManager.notify(4000, mBuilder.build());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void processAcceptedHugs(JSONArray data) {
        for (int i = 0; i < data.length(); i++) {
            try {
                JSONObject object = data.getJSONObject(i);
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(MainActivity.this)
                                .setAutoCancel(true)
                                .setSmallIcon(R.drawable.ic_person_white_24dp)
                                .setLargeIcon((((BitmapDrawable) getResources()
                                        .getDrawable(R.drawable.ic_person_white_24dp)).getBitmap()))
                                .setContentTitle(getString(R.string.accept_notification_title))
                                .setContentText(getString(R.string.a_user_accepted_hug))
                                .setContentTitle(getString(R.string.request_notification_title));
                Intent notIntent = new Intent(this, HugInfoActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("username", object.getString("username"));
                bundle.putString("userId", object.getString("_id"));
                notIntent.putExtras(bundle);

                PendingIntent contIntent = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(contIntent);
                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                mNotificationManager.notify(4000, mBuilder.build());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                starredFragment.updateUsersList(users);
            }
        });
    }

    @Override
    public void onProfileRequest(User user) {
        new GetUserCommentsTask(user).execute();
    }

    @Override
    public void onRequestUsers() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.shared_preferences_name), Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString(getString(R.string.user_id), "");

        if (userId.trim().length() > 0) {
            new GetStarredUsersTask().execute(userId);
        }
    }

    private void parseComments(User user, JSONArray data) {
        List<Comment> comments = new ArrayList<>();

        for (int i = 0; i < data.length(); i++) {
            try {
                Comment comment = new Comment();

                JSONObject commentObject = data.getJSONObject(i);
                comment.setComment(commentObject.getString("text"));
                comment.setUser(new User(commentObject.getString("author")));

                comments.add(comment);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        user.setComments(comments);
        new GetUserHugsTask(user).execute();
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

    public class GetStarredUsersTask extends AsyncTask<String, String, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet get = new HttpGet("http://156.35.95.69:8002/users/" + params[0] + "/starred");
            get.setHeader("content-type", "application/json");

            try {
                HttpResponse httpResponse = httpClient.execute(get);
                JSONObject response = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
                if (response.getBoolean("type")) {
                    processUserList(response.getJSONArray("data"));
                    return true;
                }
            } catch (Exception e) {
                Log.v("RestService", "FollowUserTask error: " + e);
            }

            return false;
        }
    }

    public class GetNotifications extends AsyncTask<String, String, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet get = new HttpGet("http://156.35.95.69:8002/users/" + params[0] + "/hugs/notifications");
            get.setHeader("content-type", "application/json");

            try {
                HttpResponse httpResponse = httpClient.execute(get);
                JSONObject response = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
                if (response.getBoolean("type")) {
                    processNotifications(response.getJSONArray("data"));
                    return true;
                }
            } catch (Exception e) {
                Log.v("RestService", "GetNotifications error: " + e);
            }

            return false;
        }
    }

    public class GetAcceptedHugs extends AsyncTask<String, String, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet get = new HttpGet("http://156.35.95.69:8002/users/" + params[0] + "/hugs/accepted");
            get.setHeader("content-type", "application/json");

            try {
                HttpResponse httpResponse = httpClient.execute(get);
                JSONObject response = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
                if (response.getBoolean("type")) {
                    processAcceptedHugs(response.getJSONArray("data"));
                    return true;
                }
            } catch (Exception e) {
                Log.v("RestService", "GetAcceptedHugs error: " + e);
            }

            return false;
        }
    }

    private class GetUserCommentsTask extends AsyncTask<Void, Void, Boolean> {

        private User user;
        ;

        public GetUserCommentsTask(User user) {
            this.user = user;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = false;

            HttpClient httpClient = new DefaultHttpClient();
            HttpGet get = new HttpGet("http://156.35.95.69:8002/users/" + user.getId());
            get.setHeader("content-type", "application/json");

            try {
                HttpResponse httpResponse = httpClient.execute(get);
                JSONObject response = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
                if (response.getBoolean("type")) {
                    parseComments(user, response.getJSONObject("data").getJSONArray("comments"));
                    result = true;
                }
            } catch (Exception e) {
                Log.v("RestService", "GetUserHugsTask error: " + e);
            }

            return result;
        }
    }

    private class GetUserHugsTask extends AsyncTask<Void, Void, Boolean> {

        private User user;
        ;

        public GetUserHugsTask(User user) {
            this.user = user;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = false;

            HttpClient httpClient = new DefaultHttpClient();
            HttpGet get = new HttpGet("http://156.35.95.69:8002/users/" + user.getId() + "/hugs");
            get.setHeader("content-type", "application/json");

            try {
                HttpResponse httpResponse = httpClient.execute(get);
                JSONObject response = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
                if (response.getBoolean("type")) {
                    user.setHugs(response.getJSONObject("data").getInt("hugs"));
                    result = true;
                }
            } catch (Exception e) {
                Log.v("RestService", "GetUserHugsTask error: " + e);
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            setCheckedItemNavigation(2, false);

            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment fragment = new ProfileFragment().newInstance(user);
            fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();
        }
    }

    private class UpdateLocationTask extends AsyncTask<String, String, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            boolean result = false;

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://156.35.95.69:8002/users/" + params[0] + "/location");
            post.setHeader("content-type", "application/json");

            try {
                JSONObject location = new JSONObject();
                location.put("latitude", params[1]);
                location.put("longitude", params[2]);

                post.setEntity(new StringEntity(location.toString()));

                HttpResponse httpResponse = httpClient.execute(post);
                JSONObject response = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
                if (response.getBoolean("type")) {
                    result = true;
                }
            } catch (Exception e) {
                Log.v("RestService", "UpdateLocationTask error: " + e);
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
            }
        }
    }
}
