package com.example.nhom1_quanlychitieu.model;

import java.io.Serializable;

public class Transaction implements Serializable {
    private String id;
    private String category;
    private long amount;
    private long timestamp;
    private String note;
    private String walletId;
    private String userId;
    private String categoryIconUrl;

    // Constructor mặc định cần thiết cho Firebase
    public Transaction() {
    }

    public Transaction(String category, long amount, long timestamp, String note) {
        this.category = category;
        this.amount = amount;
        this.timestamp = timestamp;
        this.note = note;
    }

    public Transaction(String category, long amount, long timestamp, String note, String walletId, String userId) {
        this.category = category;
        this.amount = amount;
        this.timestamp = timestamp;
        this.note = note;
        this.walletId = walletId;
        this.userId = userId;
    }

    // Getters và Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getWalletId() {
        return walletId;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCategoryIconUrl() {
        return categoryIconUrl;
    }

    public void setCategoryIconUrl(String categoryIconUrl) {
        this.categoryIconUrl = categoryIconUrl;
    }
}