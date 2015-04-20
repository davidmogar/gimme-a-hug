package com.davidmogar.gimmeahug.fragments;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.davidmogar.gimmeahug.R;
import com.davidmogar.gimmeahug.models.User;

import it.neokree.materialtabs.MaterialTab;
import it.neokree.materialtabs.MaterialTabHost;
import it.neokree.materialtabs.MaterialTabListener;

public class ProfileFragment extends Fragment implements MaterialTabListener {

    private UserInfoFragment userInfoFragment;
    private CommentsFragment commentsFragment;
    private MaterialTabHost tabHost;
    private Resources resources;
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private User user;

    public static ProfileFragment newInstance(User user) {
        ProfileFragment fragment = new ProfileFragment();
        fragment.user = user;
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

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

        tabHost.addTab(tabHost.newTab().setIcon(resources.getDrawable(R.drawable.ic_person_white_24dp)).setTabListener(this));
        tabHost.addTab(tabHost.newTab().setIcon(resources.getDrawable(R.drawable.ic_comment_white_24dp)).setTabListener(this));

        createChildFragments();

        return view;
    }

    private void createChildFragments() {
        userInfoFragment = new UserInfoFragment().newInstance(user);
        commentsFragment = new CommentsFragment().newInstance(user);
    }

    @Override
    public void onTabSelected(MaterialTab materialTab) {
        viewPager.setCurrentItem(materialTab.getPosition());
    }

    @Override
    public void onTabReselected(MaterialTab materialTab) {

    }

    @Override
    public void onTabUnselected(MaterialTab materialTab) {

    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);

        }

        public android.support.v4.app.Fragment getItem(int num) {
            switch (num) {
                case 1:
                    return commentsFragment;
                default:
                    return userInfoFragment;
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
}
