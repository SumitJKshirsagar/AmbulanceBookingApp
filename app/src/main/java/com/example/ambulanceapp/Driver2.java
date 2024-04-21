package com.example.ambulanceapp;

public class Driver2 {
    private String fullName;
    private String email;
    private String mobile;
    private double latitude;
    private double longitude;
    private boolean driversAvaibility;
    private String fcmToken;

    public Driver2() {
        // Empty constructor needed for Firebase
    }

    public Driver2(String fullName, String email, String mobile,double latitude,double longitude,boolean driversAvaibility,String fcmToken) {
        this.fullName = fullName;
        this.email = email;
        this.mobile = mobile;
        this.latitude = latitude;
        this.longitude = longitude;
        this.fcmToken = fcmToken;
    }

public boolean getDriversAvaibility(){
        return driversAvaibility;
}
    public void setDriversAvaibility(boolean driversAvaibility) {
        this.driversAvaibility = driversAvaibility;
    }
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getFcmToken() {
        return fcmToken;
    }
    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}

