package com.ril.ampoc.model;

import android.os.Parcel;
import android.os.Parcelable;

public final class User implements Parcelable {

    private String username;
    private String password;
    private String authToken;

    public User(String username, String password, String authToken) {
        this.username = username;
        this.password = password;
        this.authToken = authToken;
    }

    protected User(Parcel in) {
        username = in.readString();
        password = in.readString();
        authToken = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeString(password);
        dest.writeString(authToken);
    }

    public void readFromParcel(Parcel in) {
        this.username = in.readString();
        this.password = in.readString();
        this.authToken = in.readString();
    }
}
