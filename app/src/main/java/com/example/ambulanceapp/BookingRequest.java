package com.example.ambulanceapp;



public class BookingRequest {
    private String requestId;
    private String userId;
    private String driverId;
    private String status;

    public BookingRequest() {
        // Default constructor required for DataSnapshot.getValue(BookingRequest.class)
    }

    public BookingRequest(String requestId, String userId, String driverId, String status) {
        this.requestId = requestId;
        this.userId = userId;
        this.driverId = driverId;
        this.status = status;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getUserId() {
        return userId;
    }

    public String getDriverId() {
        return driverId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

}