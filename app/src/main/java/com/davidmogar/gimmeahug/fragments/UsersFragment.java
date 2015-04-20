package com.davidmogar.gimmeahug.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.davidmogar.gimmeahug.R;
import com.davidmogar.gimmeahug.activities.MainActivity;
import com.davidmogar.gimmeahug.helpers.Constants;
import com.davidmogar.gimmeahug.models.Comment;
import com.davidmogar.gimmeahug.models.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import it.neokree.materialtabs.MaterialTab;
import it.neokree.materialtabs.MaterialTabHost;
import it.neokree.materialtabs.MaterialTabListener;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UsersFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UsersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UsersFragment extends Fragment implements MaterialTabListener, ListFragment.OnProfileRequestListener {

    private ListFragment listFragment;
    private MapFragment mapFragment;
    private MaterialTabHost tabHost;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private Resources resources;
    private List<User> users;

    public static UsersFragment newInstance() {
        UsersFragment fragment = new UsersFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        tabHost = (MaterialTabHost) view.findViewById(R.id.tab_host);
        viewPager = (ViewPager) view.findViewById(R.id.view_pager);

        ActionBarActivity activity = (ActionBarActivity) getActivity();

        viewPagerAdapter = new ViewPagerAdapter(activity.getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                tabHost.setSelectedNavigationItem(position);
            }
        });

        resources = getResources();

        tabHost.addTab(tabHost.newTab().setIcon(resources.getDrawable(R.drawable.ic_group_white_24dp)).setTabListener(this));
        tabHost.addTab(tabHost.newTab().setIcon(resources.getDrawable(R.drawable.ic_place_white_24dp)).setTabListener(this));

        createChildFragments();

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeColors(R.color.primary, R.color.accent);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestNearUsers();
            }
        });
        requestNearUsers();

        return view;
    }

    private void createChildFragments() {
        listFragment = new ListFragment().newInstance();
        mapFragment = new MapFragment().newInstance(true);
    }

    private void requestNearUsers() {
        if (!swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
        }

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.shared_preferences_name), Context.MODE_PRIVATE);
        int radius = sharedPreferences.getInt(getString(R.string.max_radius), Constants.MAX_RADIUS);

        GetUsersTask task = new GetUsersTask();
        task.execute(radius);
    }

    public void centerMapCamera(Location location) {
        if (mapFragment != null) {
            mapFragment.centerCamera(location);
        }
    }

    @Override
    public void onTabReselected(MaterialTab materialTab) {

    }

    @Override
    public void onTabSelected(MaterialTab materialTab) {
        viewPager.setCurrentItem(materialTab.getPosition());
    }

    @Override
    public void onTabUnselected(MaterialTab materialTab) {

    }

    private void processUserList(JSONArray data) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.shared_preferences_name), Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString(getString(R.string.user_id), "");
        users = new ArrayList<>();

        for (int i = 0; i < data.length(); i++) {
            try {
                JSONObject userObject = data.getJSONObject(i);
                User user = new User(userObject.getString("_id"), userObject.getString("username"), userObject.getString("email"));
                if (user.getId().compareTo(userId) != 0) {
                    user.setFollowers(userObject.getJSONArray("followers").length());

                    if (userObject.has("latitude")) {
                        double latitude = userObject.getDouble("latitude");
                        if (latitude != 0) {
                            user.setLatitude(latitude);
                        }
                    }
                    if (userObject.has("longitude")) {
                        double longitude = userObject.getDouble("longitude");
                        if (longitude != 0) {
                            user.setLongitude(longitude);
                        }
                    }
                    users.add(user);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        updateUI();
    }

    private void updateUI() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
            listFragment.updateUsersList(users);
            mapFragment.updateMarkers(users);
            }
        });
    }

    @Override
    public void onProfileRequest(User user) {
        new GetUserCommentsTask(user).execute();
    }

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);

        }

        public Fragment getItem(int num) {
            switch (num) {
                case 1:
                    return mapFragment;
                default:
                    return listFragment;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Section " + position;
        }

    }

    private class GetUsersTask extends AsyncTask<Integer, String, Boolean> {

        @Override
        protected Boolean doInBackground(Integer... params) {
            boolean result = false;

            HttpClient httpClient = new DefaultHttpClient();
            HttpGet get = new HttpGet("http://156.35.95.69:8002/users");
            get.setHeader("content-type", "application/json");

            try {
                HttpResponse httpResponse = httpClient.execute(get);
                JSONObject response = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
                if (response.getBoolean("type")) {
                    processUserList(response.getJSONArray("data"));
                    result = true;
                }
            } catch (Exception e) {
                Log.v("RestService", "CreateUserTask error: " + e);
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            swipeRefreshLayout.setRefreshing(false);
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

    private class GetUserCommentsTask extends AsyncTask<Void, Void, Boolean> {

        private User user;;

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

        private User user;;

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
                    user.setHugs(response.getInt("data"));
                    result = true;
                }
            } catch (Exception e) {
                Log.v("RestService", "GetUserHugsTask error: " + e);
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.setCheckedItemNavigation(0, false);

            FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
            Fragment fragment = new ProfileFragment().newInstance(user);
            fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();
        }
    }

}
