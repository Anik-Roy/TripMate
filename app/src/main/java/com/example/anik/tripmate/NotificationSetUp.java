package com.example.anik.tripmate;

/**
 * Created by anik on 3/18/18.
 */

public class NotificationSetUp {
    String from, type;
    Long timestamp;

    public NotificationSetUp() {
    }

    public NotificationSetUp(String from, String type, Long timestamp) {
        this.from = from;
        this.type = type;
        this.timestamp = timestamp;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
