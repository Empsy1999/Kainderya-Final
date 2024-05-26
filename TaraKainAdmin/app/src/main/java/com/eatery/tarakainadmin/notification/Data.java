package com.eatery.tarakainadmin.notification;

public class Data {
    private String senderId, body, title, receiverId, type;
    private Integer icon;


  public Data() {
 }

    public Data(String senderId, String body, String title, String receiverId, String type, Integer icon) {
        this.senderId = senderId;
        this.body = body;
        this.title = title;
        this.receiverId = receiverId;
        this.type = type;
        this.icon = icon;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public Integer getIcon() {
        return icon;
    }

    public void setIcon(Integer icon) {
        this.icon = icon;
    }
}
