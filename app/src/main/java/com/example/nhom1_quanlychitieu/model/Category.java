package com.example.nhom1_quanlychitieu.model;

import java.io.Serializable;

public class Category implements Serializable {
    private String id;
    private String name;
    private String iconUrl;
    private String userId;
    private boolean isDefault;
    private int iconResourceId; // Để lưu trữ ID resource của icon

    // Constructor mặc định cần thiết cho Firebase
    public Category() {
    }

    public Category(String name, String iconUrl) {
        this.name = name;
        this.iconUrl = iconUrl;
    }

    public Category(String name, int iconResourceId) {
        this.name = name;
        this.iconResourceId = iconResourceId;
        this.isDefault = false;
    }

    // Getters và Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public int getIconResourceId() {
        return iconResourceId;
    }

    public void setIconResourceId(int iconResourceId) {
        this.iconResourceId = iconResourceId;
    }
}