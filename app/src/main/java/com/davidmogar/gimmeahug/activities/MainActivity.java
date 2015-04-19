package com.davidmogar.gimmeahug.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.davidmogar.gimmeahug.R;
import com.davidmogar.gimmeahug.fragments.MainFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import br.liveo.interfaces.NavigationLiveoListener;
import br.liveo.navigationliveo.NavigationLiveo;

public class MainActivity extends NavigationLiveo implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, NavigationLiveoListener,
        MainFragment.OnFragmentInteractionListener, LocationListener {

    private GoogleApiClient googleApiClient;
    private List<String> itemsNames;
    private LocationRequest locationRequest;

    @Override
    public void onClickFooterItemNavigation(View view) {

    }

    @Override
    public void onClickUserPhotoNavigation(View view) {

    }

    @Override
    public void onInt(Bundle bundle) {
        this.setNavigationListener(this);
        this.setDefaultStartPositionNavigation(0);

        itemsNames = new ArrayList<>();
        itemsNames.add(0, "Active users");
        itemsNames.add(1, "Explore");
        itemsNames.add(2, "Starred");
        itemsNames.add(3, "Account");
        itemsNames.add(4, "Edit profile");
        itemsNames.add(5, "Log out");


        List<Integer> itemsIcons = new ArrayList<>();
        itemsIcons.add(0, R.drawable.ic_group_black_24dp);
        itemsIcons.add(1, R.drawable.ic_explore_black_24dp);
        itemsIcons.add(2, R.drawable.ic_star_black_24dp);
        itemsIcons.add(3, 0);
        itemsIcons.add(4, R.drawable.ic_mode_edit_black_24dp);
        itemsIcons.add(5, R.drawable.ic_exit_to_app_black_24dp);

        List<Integer> headers = new ArrayList<>();
        headers.add(3);

        this.setFooterInformationDrawer("Settings", R.drawable.ic_settings_black_24dp);
        this.setNavigationAdapter(itemsNames, itemsIcons, headers, null);

        buildGoogleApiClient();
        createLocationRequest();
    }

    @Override
    public void onItemClickNavigation(int position, int layoutContainerId) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = new MainFragment().newInstance();

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
                this.mUserPhoto.setImageResource(R.drawable.ic_no_user);
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        Log.v("FUCK", "Connecting");
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
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.v("FUCK", "Connected");
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.v("FUCK", "Connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.v("FUCK", "Connection failed " + connectionResult.toString());
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
        Log.v("FUCK", "Lat: " + location.getLatitude() + ", long: " + location.getLongitude());
    }
}
