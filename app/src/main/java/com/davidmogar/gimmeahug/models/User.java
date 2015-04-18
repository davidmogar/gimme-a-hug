package com.davidmogar.gimmeahug.models;

import android.graphics.drawable.Drawable;

import com.davidmogar.gimmeahug.R;

public class User {

    private String name;
    private String email;
    private int profileImage;

    public User(String name, String email) {
        this.name = name;
        this.email = email;

        profileImage = R.drawable.profile;
    }

    public int getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(int profileImage) {
        this.profileImage = profileImage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
