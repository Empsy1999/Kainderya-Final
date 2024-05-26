package com.eatery.tarakain.model;

public class Menus {
    private String title, id, email, price, description, category, idPicUrl, ownerId;

    public Menus() {
    }

    public Menus(String title, String id, String email, String price, String description,
                 String category, String idPicUrl, String ownerId) {
        this.title = title;
        this.id = id;
        this.email = email;
        this.price = price;
        this.description = description;
        this.category = category;
        this.idPicUrl = idPicUrl;
        this.ownerId = ownerId;
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

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIdPicUrl() {
        return idPicUrl;
    }

    public void setIdPicUrl(String idPicUrl) {
        this.idPicUrl = idPicUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
}
