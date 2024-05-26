package com.eatery.tarakain.model;

public class Owners {
    String id, email, eateryName, idPicUrl, permitDocUrl,
            ownerName, eateryImage, latitude, longitude, password, status;

    public Owners() {
    }

    public Owners(String id, String email, String eateryName, String idPicUrl, String permitDocUrl,
                  String ownerName, String eateryImage, String latitude, String longitude,
                  String password, String status) {
        this.id = id;
        this.email = email;
        this.eateryName = eateryName;
        this.idPicUrl = idPicUrl;
        this.permitDocUrl = permitDocUrl;
        this.ownerName = ownerName;
        this.eateryImage = eateryImage;
        this.latitude = latitude;
        this.longitude = longitude;
        this.password = password;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEateryName() {
        return eateryName;
    }

    public void setEateryName(String eateryName) {
        this.eateryName = eateryName;
    }

    public String getIdPicUrl() {
        return idPicUrl;
    }

    public void setIdPicUrl(String idPicUrl) {
        this.idPicUrl = idPicUrl;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPermitDocUrl() {
        return permitDocUrl;
    }

    public void setPermitDocUrl(String permitDocUrl) {
        this.permitDocUrl = permitDocUrl;
    }

    public String getEateryImage() {
        return eateryImage;
    }

    public void setEateryImage(String eateryImage) {
        this.eateryImage = eateryImage;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
