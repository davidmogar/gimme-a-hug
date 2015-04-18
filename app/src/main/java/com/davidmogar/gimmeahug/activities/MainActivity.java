package com.davidmogar.gimmeahug.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.View;

import com.davidmogar.gimmeahug.R;
import com.davidmogar.gimmeahug.fragments.MainFragment;

import java.util.ArrayList;
import java.util.List;

import br.liveo.interfaces.NavigationLiveoListener;
import br.liveo.navigationliveo.NavigationLiveo;

public class MainActivity extends NavigationLiveo implements NavigationLiveoListener, MainFragment.OnFragmentInteractionListener {

    private List<String> itemsNames;

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
        itemsNames.add(0, "Change this");
        itemsNames.add(1, "Change also this");

        List<Integer> itemsIcons = new ArrayList<>();
        itemsIcons.add(0, 0);
        itemsIcons.add(1, 0);

        this.setNavigationAdapter(itemsNames, itemsIcons);
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
        this.mUserPhoto.setImageResource(R.drawable.ic_no_user);
        this.mUserName.setText(getString(R.string.anonymous_user_name));
        this.mUserBackground.setImageResource(R.drawable.drawer_background);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
