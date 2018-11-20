package com.example.anik.tripmate;

/**
 * Created by anik on 2/12/18.
 */

public class userListAlbum {
    String uri, userName, uid;

    public userListAlbum() {
    }

    public userListAlbum(String uri, String userName) {
        this.uri = uri;
        this.userName = userName;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
