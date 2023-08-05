package com.example.courseapp.models;

public class UsersModel {
    private String userName;
    private String userEmail;
    private String userProfile;
    private String userMobile;
    private String userAddress;
    private String latitude;
    private String longitude;


    public UsersModel(){

    }

    public UsersModel(String userName, String userEmail, String userProfile, String userMobile, String userAddress, String latitude, String longitude) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.userProfile = userProfile;
        this.userMobile = userMobile;
        this.userAddress = userAddress;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(String userProfile) {
        this.userProfile = userProfile;
    }

    public String getUserMobile() {
        return userMobile;
    }

    public void setUserMobile(String userMobile) {
        this.userMobile = userMobile;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
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
}
