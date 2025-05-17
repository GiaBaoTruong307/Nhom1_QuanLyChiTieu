package com.example.nhom1_quanlychitieu.ui.ThongKe.model;

import java.io.Serializable;
public class Wallet implements Serializable {
    // Các thuộc tính của ví
    private String id;
    private String name;
    private long balance;
    private String userId;
    private String currency;
    private boolean isDefault;

    public Wallet() {
    }

    public Wallet(String name, long balance) {
        this.name = name;
        this.balance = balance;
        this.currency = "VND";  // Mặc định là VND
        this.isDefault = false;
    }

    public Wallet(String name, long balance, String userId) {
        this.name = name;
        this.balance = balance;
        this.userId = userId;
        this.currency = "VND";  // Mặc định là VND
        this.isDefault = false;
    }

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

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
}