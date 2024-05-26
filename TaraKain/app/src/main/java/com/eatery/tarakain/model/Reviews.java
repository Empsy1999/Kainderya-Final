package com.eatery.tarakain.model;

public class Reviews {
    String id;
    float rating;
    String review, customerName, time, date, dateTime, ownerId;

    public Reviews() {
    }

    public Reviews(String id, float rating, String review, String customerName,
                   String time, String date, String dateTime, String ownerId) {
        this.id = id;
        this.rating = rating;
        this.review = review;
        this.customerName = customerName;
        this.time = time;
        this.date = date;
        this.dateTime = dateTime;
        this.ownerId = ownerId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
}
