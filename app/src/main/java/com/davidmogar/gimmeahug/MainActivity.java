package com.davidmogar.gimmeahug;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import br.liveo.interfaces.NavigationLiveoListener;
import br.liveo.navigationliveo.NavigationLiveo;

public class MainActivity extends NavigationLiveo implements NavigationLiveoListener {

    @Override
    public void onClickFooterItemNavigation(View view) {

    }

    @Override
    public void onClickUserPhotoNavigation(View view) {

    }

    @Override
    public void onInt(Bundle bundle) {
        this.setNavigationListener(this);

        List<String> itemsNames = new ArrayList<>();
        itemsNames.add(0, "Change this");
        itemsNames.add(1, "Change this");

        List<Integer> itemsIcons = new ArrayList<>();
        itemsIcons.add(0, 0);
        itemsIcons.add(1, 0);

        this.setNavigationAdapter(itemsNames, itemsIcons);
    }

    @Override
    public void onItemClickNavigation(int i, int i2) {

    }

    @Override
    public void onPrepareOptionsMenuNavigation(Menu menu, int i, boolean b) {

    }

    @Override
    public void onUserInformation() {

    }

}
