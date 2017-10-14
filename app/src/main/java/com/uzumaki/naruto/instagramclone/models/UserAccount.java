package com.uzumaki.naruto.instagramclone.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Gaurav Bist on 06-07-2017.
 */

public class UserAccount implements Parcelable {
    private String description;
    private String display_name;
    private int followers;
    private int following;
    private int posts;
    private String profile_photo;
    private String user_name;
    private String user_id;
    private String website;

    public UserAccount(){

    }

    public UserAccount(String description, String display_name,
                       int followers, int following, int posts,
                       String profile_photo, String user_name, String user_id, String website) {
        this.description = description;
        this.display_name = display_name;
        this.followers = followers;
        this.following = following;
        this.posts = posts;
        this.profile_photo = profile_photo;
        this.user_name = user_name;
        this.user_id = user_id;
        this.website = website;
    }

    protected UserAccount(Parcel in) {
        description = in.readString();
        display_name = in.readString();
        followers = in.readInt();
        following = in.readInt();
        posts = in.readInt();
        profile_photo = in.readString();
        user_name = in.readString();
        user_id = in.readString();
        website = in.readString();
    }

    public static final Creator<UserAccount> CREATOR = new Creator<UserAccount>() {
        @Override
        public UserAccount createFromParcel(Parcel in) {
            return new UserAccount(in);
        }

        @Override
        public UserAccount[] newArray(int size) {
            return new UserAccount[size];
        }
    };

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public int getFollowers() {
        return followers;
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public int getFollowing() {
        return following;
    }

    public void setFollowing(int following) {
        this.following = following;
    }

    public int getPosts() {
        return posts;
    }

    public void setPosts(int posts) {
        this.posts = posts;
    }

    public String getProfile_photo() {
        return profile_photo;
    }

    public void setProfile_photo(String profile_photo) {
        this.profile_photo = profile_photo;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    @Override
    public String toString() {
        return "UserAccount{" +
                "description='" + description + '\'' +
                ", display_name='" + display_name + '\'' +
                ", followers=" + followers +
                ", following=" + following +
                ", posts=" + posts +
                ", profile_photo='" + profile_photo + '\'' +
                ", user_name='" + user_name + '\'' +
                ", user_id='" + user_id + '\'' +
                ", website='" + website + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(description);
        parcel.writeString(display_name);
        parcel.writeInt(followers);
        parcel.writeInt(following);
        parcel.writeInt(posts);
        parcel.writeString(profile_photo);
        parcel.writeString(user_name);
        parcel.writeString(user_id);
        parcel.writeString(website);
    }
}
