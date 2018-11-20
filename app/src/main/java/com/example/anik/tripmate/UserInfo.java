package com.example.anik.tripmate;

import java.util.HashMap;

/**
 * Created by anik on 2/12/18.
 */

public class UserInfo {
    String about = null, birthday = null, userImage = null, email = null, gender = null, hometown = null, interest = null, language = null;
    String name = null, occupation = null, travel = null, uid = null, website = null;
    String visitedCountry = null, location = null, locationShare = null, device_token = null, latitude = null, longitude = null;
    String available = null, nationality = null;
    String year = null;
    Long lastSeen = null;
    HashMap<String, String> profilephoto = null;

    public UserInfo() {
    }

    public UserInfo(String about, String birthday, String userImage, String email, String gender, String hometown, String interest, String language, String name, String occupation, String travel, String uid, String website, String visitedCountry, String location, String locationShare, String device_token, String latitude, String longitude, String available, String nationality, String year, Long lastSeen, HashMap<String, String> profilephoto) {
        this.about = about;
        this.birthday = birthday;
        this.userImage = userImage;
        this.email = email;
        this.gender = gender;
        this.hometown = hometown;
        this.interest = interest;
        this.language = language;
        this.name = name;
        this.occupation = occupation;
        this.travel = travel;
        this.uid = uid;
        this.website = website;
        this.visitedCountry = visitedCountry;
        this.location = location;
        this.locationShare = locationShare;
        this.device_token = device_token;
        this.latitude = latitude;
        this.longitude = longitude;
        this.available = available;
        this.nationality = nationality;
        this.year = year;
        this.lastSeen = lastSeen;
        this.profilephoto = profilephoto;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getHometown() {
        return hometown;
    }

    public void setHometown(String hometown) {
        this.hometown = hometown;
    }

    public String getInterest() {
        return interest;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getTravel() {
        return travel;
    }

    public void setTravel(String travel) {
        this.travel = travel;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getVisitedCountry() {
        return visitedCountry;
    }

    public void setVisitedCountry(String visitedCountry) {
        this.visitedCountry = visitedCountry;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocationShare() {
        return locationShare;
    }

    public void setLocationShare(String locationShare) {
        this.locationShare = locationShare;
    }

    public String getDevice_token() {
        return device_token;
    }

    public void setDevice_token(String device_token) {
        this.device_token = device_token;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getAvailable() {
        return available;
    }

    public void setAvailable(String available) {
        this.available = available;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public Long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public HashMap<String, String> getProfilephoto() {
        return profilephoto;
    }

    public void setProfilephoto(HashMap<String, String> profilephoto) {
        this.profilephoto = profilephoto;
    }
}
