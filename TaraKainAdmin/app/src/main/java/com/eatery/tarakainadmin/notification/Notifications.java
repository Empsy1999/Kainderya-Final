package com.eatery.tarakainadmin.notification;

public class Notifications {
    String id, userId, senderId, receiverId, title, body, status, date;

    public Notifications() {
    }

    public Notifications(String id, String userId, String senderId, String receiverId,
                         String title, String body, String status, String date) {
        this.id = id;
        this.userId = userId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.title = title;
        this.body = body;
        this.status = status;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
