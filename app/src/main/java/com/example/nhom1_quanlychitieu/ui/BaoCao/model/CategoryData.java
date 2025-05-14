package com.example.nhom1_quanlychitieu.ui.BaoCao.model;

import androidx.annotation.NonNull;

public class CategoryData {
    private final String name;
    private final long amount;
    private final int color;

    public CategoryData(String name, long amount, int color) {
        this.name = name;
        this.amount = amount;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public long getAmount() {
        return amount;
    }

    public int getColor() {
        return color;
    }

    @NonNull
    @Override
    public String toString() {
        return "CategoryData{" +
                "name='" + name + '\'' +
                ", amount=" + amount +
                ", color=" + color +
                '}';
    }
}