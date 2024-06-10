package com.example.contactmanagementapp;

public class Employee {
    private String name,email,position, idUnit;
    private int imageResource, telephone,id;

    public Employee() {
    }
    public Employee(String name, int id, String email, String position, String idUnit, int imageResource, int telephone) {
        this.name = name;
        this.id = id;
        this.email = email;
        this.position = position;
        this.idUnit = idUnit;
        this.imageResource = imageResource;
        this.telephone = telephone;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setIdUnit(String idUnit) {
        this.idUnit = idUnit;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }

    public void setTelephone(int telephone) {
        this.telephone = telephone;
    }

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPosition() {
        return position;
    }

    public String getIdUnit() {
        return idUnit;
    }

    public int getTelephone() {
        return telephone;
    }

    public Employee(String name, int imageResource) {
        this.name = name;
        this.imageResource = imageResource;
    }

    public String getName() {
        return name;
    }

    public int getImageResource() {
        return imageResource;
    }
}

