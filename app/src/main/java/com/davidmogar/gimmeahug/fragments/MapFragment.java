package com.davidmogar.gimmeahug.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.davidmogar.gimmeahug.R;
import com.davidmogar.gimmeahug.helpers.Constants;
import com.davidmogar.gimmeahug.models.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;


public class MapFragment extends Fragment implements OnMapReadyCallback {

    SupportMapFragment supportMapFragment;
    private GoogleMap googleMap;
    private boolean limitMap;

    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment newInstance(boolean limitMap) {
        MapFragment fragment = new MapFragment();
        fragment.limitMap = limitMap;
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        supportMapFragment = SupportMapFragment.newInstance();
        getChildFragmentManager().beginTransaction().add(R.id.map, supportMapFragment).commit();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (supportMapFragment != null) {
            supportMapFragment.getMapAsync(this);
        }
    }

    public void centerCamera(Location location) {
        if (googleMap != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.shared_preferences_name), Context.MODE_PRIVATE);
            int zoom = sharedPreferences.getInt(getString(R.string.zoom), Constants.ZOOM);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        }
    }

    public void updateMarkers(List<User> users) {
        if (googleMap != null) {
            googleMap.clear();

            for (User user : users) {
                double latitude = user.getLatitude();
                double longitude = user.getLongitude();

                if (latitude != 0 || longitude != 0) {
                    googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(user.getName()));
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        if (googleMap != null) {
            if (limitMap) {
                googleMap.getUiSettings().setScrollGesturesEnabled(false);
                googleMap.getUiSettings().setZoomGesturesEnabled(false);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            } else {
                googleMap.getUiSettings().setZoomControlsEnabled(true);
            }

            googleMap.setMyLocationEnabled(true);
        }
    }
}
