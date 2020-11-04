package com.max.hobnob.Model;

public class User {

    private String id;
    private String username;
    private String phone;
    private String imageURL;
    private String status;


    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    private String about;

    public User(String id,String phone, String username, String imageURL, String status,String about) {
        this.id = id;

        this.phone = phone;
        this.username = username;
        this.imageURL = imageURL;
        this.status = status;

        this.about = about;
    }



    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public User() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }



}
