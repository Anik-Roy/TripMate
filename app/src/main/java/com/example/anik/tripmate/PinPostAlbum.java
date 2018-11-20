package com.example.anik.tripmate;

public class PinPostAlbum {
    String namePinPost, pinpostText, pinpostDate;

    public String getNamePinPost() {
        return namePinPost;
    }

    public void setNamePinPost(String namePinPost) {
        this.namePinPost = namePinPost;
    }

    public PinPostAlbum(String pinpostText, String pinpostDate, String namePinPost) {
        this.pinpostText = pinpostText;
        this.pinpostDate = pinpostDate;
        this.namePinPost = namePinPost;

    }

    public PinPostAlbum() {

    }

    public String getPinpostText() {
        return pinpostText;
    }

    public void setPinpostText(String pinpostText) {
        this.pinpostText = pinpostText;
    }

    public String getPinpostDate() {
        return pinpostDate;
    }

    public void setPinpostDate(String pinpostDate) {
        this.pinpostDate = pinpostDate;
    }
}
