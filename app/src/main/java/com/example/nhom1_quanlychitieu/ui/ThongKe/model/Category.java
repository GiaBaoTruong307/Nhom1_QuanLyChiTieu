package com.example.nhom1_quanlychitieu.ui.ThongKe.model;

import java.io.Serializable;

public class Category implements Serializable {
    // Các thuộc tính của danh mục
    private String id;
    private String name;
    private String iconUrl;
    private String userId;
    private boolean isDefault;
    private int iconResourceId;
    private String type;

    public Category() {
    }

    public Category(String name, String iconUrl) {
        this.name = name;
        this.iconUrl = iconUrl;
        this.type = "expense"; // Mặc định là chi tiêu
    }

    public Category(String name, int iconResourceId) {
        this.name = name;
        this.iconResourceId = iconResourceId;
        this.isDefault = false;
        this.type = "expense"; // Mặc định là chi tiêu
    }

    public Category(String name, int iconResourceId, String type) {
        this.name = name;
        this.iconResourceId = iconResourceId;
        this.isDefault = false;
        this.type = type;
    }

    // Các phương thức getter và setter
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isExpense() {
        return "expense".equals(type);
    }

    public boolean isIncome() {
        return "income".equals(type);
    }
}