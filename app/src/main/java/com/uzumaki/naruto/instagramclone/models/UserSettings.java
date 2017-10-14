package com.uzumaki.naruto.instagramclone.models;

/**
 * Created by Gaurav Bist on 06-07-2017.
 */

public class UserSettings {
    private UserPrivate user;
    private UserAccount settings;

    public UserSettings(UserPrivate user, UserAccount settings) {
        this.user = user;
        this.settings = settings;
    }

    public UserSettings() {

    }

    public UserPrivate getUser() {
        return user;
    }

    public void setUser(UserPrivate user) {
        this.user = user;
    }

    public UserAccount getSettings() {
        return settings;
    }

    public void setSettings(UserAccount settings) {
        this.settings = settings;
    }

    @Override
    public String toString() {
        return "UserSettings{" +
                "user=" + user +
                ", settings=" + settings +
                '}';
    }
}
