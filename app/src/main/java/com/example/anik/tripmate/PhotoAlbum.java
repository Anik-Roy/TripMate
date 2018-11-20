package com.example.anik.tripmate;

import android.net.Uri;

/**
 * Created by anik on 1/29/18.
 */

public class PhotoAlbum {
    Uri bm;

    PhotoAlbum() {

    }

    PhotoAlbum(Uri bm) {
        this.bm = bm;
    }

    public Uri getThumbnail() {
        return bm;
    }

    public void setThumbnail(Uri bm) {
        this.bm = bm;
    }
}
