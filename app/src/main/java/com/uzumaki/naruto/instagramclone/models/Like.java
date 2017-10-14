package com.uzumaki.naruto.instagramclone.models;

/**
 * Created by Gaurav Bist on 02-09-2017.
 */

public class Like {
    private String user_id;

    public Like(){
        // empty constructor
    }

    public Like(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    @Override
    public String toString() {
        return "Like{" +
                "user_id='" + user_id + '\'' +
                '}';
    }
}
