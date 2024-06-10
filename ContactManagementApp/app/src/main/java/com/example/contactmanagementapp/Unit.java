package com.example.contactmanagementapp;

import android.net.Uri;

public class Unit {
    private String name,email,website, address;
    private int  id,telephone;
    private Uri imageUri;

    public Unit(String name, Uri imageUri) {
        this.name = name;
        this.imageUri = imageUri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTelephone() {
        return telephone;
    }

    public void setTelephone(int telephone) {
        this.telephone = telephone;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public Unit() {
    }

    public Unit(String name, String email, String website, String address, int id, int telephone, Uri imageUri) {
        this.name = name;
        this.email = email;
        this.website = website;
        this.address = address;
        this.id = id;
        this.telephone = telephone;
        this.imageUri = imageUri;
    }
}
