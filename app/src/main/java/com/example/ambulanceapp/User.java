package com.example.ambulanceapp;


public class User {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private double latitude;
    private double longitude;


public User() {

}

    public User(String lastName,String firstName,String phoneNumber,double latitude,double longitude) {
        this.phoneNumber = phoneNumber;
        this.latitude = latitude;
        this.longitude = longitude;
        this.firstName = firstName;
        this.lastName = lastName;

    }



    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void  setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;

    }
    public String getFirstName() {
        return firstName;
    }
    public void  setFirstName(String firstName) {
        this.firstName = firstName;}
    public String getLastName() {
        return lastName;
    }
    public void  setLastName(String lastName) {
        this.lastName = lastName;}

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
}

