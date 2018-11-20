package com.example.anik.tripmate;

import android.widget.Toast;

/**
 * Created by anik on 3/16/18.
 */

class ChatSetUp {
    public boolean seen;
    public long timestamp;

    public ChatSetUp() {
    }

    public ChatSetUp(boolean seen, long timestamp) {
        this.seen = seen;
        this.timestamp = timestamp;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
