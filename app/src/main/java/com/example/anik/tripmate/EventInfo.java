package com.example.anik.tripmate;

import java.util.HashMap;

/**
 * Created by anik on 2/10/18.
 */

public class EventInfo {
    String from, to, image, description, meetingLocation, maximumTraver;
    HashMap<String, String> going;
    HashMap<String, String> profilephoto = null;

    public EventInfo() {
    }

    public HashMap<String, String> getProfilephoto() {
        return profilephoto;
    }

    public void setProfilephoto(HashMap<String, String> profilephoto) {
        this.profilephoto = profilephoto;
    }

    public EventInfo(String from, String to, String image, String description, String meetingLocation, String maximumTraver, HashMap<String, String> going, HashMap<String, String> profilephoto) {
        this.from = from;
        this.to = to;
        this.image = image;
        this.description = description;
        this.meetingLocation = meetingLocation;
        this.maximumTraver = maximumTraver;
        this.going = going;
        this.profilephoto = profilephoto;
    }

    public String getMaximumTraver() {
        return maximumTraver;
    }

    public void setMaximumTraver(String maximumTraver) {
        this.maximumTraver = maximumTraver;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMeetingLocation() {
        return meetingLocation;
    }

    public void setMeetingLocation(String meetingLocation) {
        this.meetingLocation = meetingLocation;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public HashMap<String, String> getGoing() {
        return going;
    }

    public void setGoing(HashMap<String, String> going) {
        this.going = going;
    }
}
