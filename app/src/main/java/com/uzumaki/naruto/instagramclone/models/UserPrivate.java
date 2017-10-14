package com.uzumaki.naruto.instagramclone.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Gaurav Bist on 06-07-2017.
 */

public class UserPrivate implements Parcelable {
    private String emailId;
    private long phone_number;
    private String user_id;
    private String user_name;

    public UserPrivate(String emailId, long phone_number, String user_id, String user_name) {
        this.emailId = emailId;
        this.phone_number = phone_number;
        this.user_id = user_id;
        this.user_name = user_name;
    }

    public UserPrivate() {

    }

    protected UserPrivate(Parcel in) {
        emailId = in.readString();
        phone_number = in.readLong();
        user_id = in.readString();
        user_name = in.readString();
    }

    public static final Creator<UserPrivate> CREATOR = new Creator<UserPrivate>() {
        @Override
        public UserPrivate createFromParcel(Parcel in) {
            return new UserPrivate(in);
        }

        @Override
        public UserPrivate[] newArray(int size) {
            return new UserPrivate[size];
        }
    };

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public long getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(long phone_number) {
        this.phone_number = phone_number;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(emailId);
        parcel.writeLong(phone_number);
        parcel.writeString(user_id);
        parcel.writeString(user_name);
    }
}
