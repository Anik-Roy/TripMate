package com.example.anik.tripmate;

import android.net.Uri;

/**
 * Created by anik on 2/7/18.
 */

public class eventAlbum {
    Uri bm;
    String place, from, to, eventRoot, going;

    eventAlbum() {

    }

    eventAlbum(Uri bm, String place, String from, String to, String eventRoot, String going) {
        this.bm = bm;
        this.place = place;
        this.from = from;
        this.to = to;
        this.eventRoot = eventRoot;
        this.going = going;
    }

    public Uri getThumbnail() {
        return bm;
    }

    public String getPlace() {
        return place;
    }

    public String getFrom() {return from;}

    public String getTo() {return to;}

    public String getEventRoot() {
        return eventRoot;
    }

    public String getGoing() {
        return going;
    }

    public void setPlace(String place) {
        this.place = place;
    }
}
