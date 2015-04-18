package com.davidmogar.gimmeahug.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.davidmogar.gimmeahug.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapFragment extends Fragment implements OnMapReadyCallback {

    SupportMapFragment supportMapFragment;
    private GoogleMap googleMap;

    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public MapFragment() {
        // Required empty public constructor
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (googleMap != null) {
            googleMap.getUiSettings().setScrollGesturesEnabled(false);
            googleMap.setMyLocationEnabled(true);
            googleMap.addMarker(new MarkerOptions().position(new LatLng(10, 10)).title("Hello world"));
        }
    }
}
