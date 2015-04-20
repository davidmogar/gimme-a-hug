package com.davidmogar.gimmeahug.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import com.davidmogar.gimmeahug.R;
import com.davidmogar.gimmeahug.activities.MainActivity;
import com.davidmogar.gimmeahug.helpers.Constants;

public class SettingsFragment extends Fragment {

    private SeekBar sbMaxRadius;
    private SeekBar sbZoom;

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        sbMaxRadius = (SeekBar) view.findViewById(R.id.max_radius);
        sbZoom = (SeekBar) view.findViewById(R.id.zoom);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.shared_preferences_name), Context.MODE_PRIVATE);
        sbMaxRadius.setProgress(sharedPreferences.getInt(getString(R.id.max_radius), Constants.MAX_RADIUS));
        sbZoom.setProgress(sharedPreferences.getInt(getString(R.id.zoom), Constants.ZOOM));

        view.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.shared_preferences_name), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(getString(R.string.max_radius), sbMaxRadius.getProgress());
                editor.putInt(getString(R.string.zoom), sbZoom.getProgress());
                editor.commit();
                Toast.makeText(getActivity(), getText(R.string.settings_saved), Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

}
