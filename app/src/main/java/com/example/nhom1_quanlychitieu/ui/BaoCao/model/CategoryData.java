package com.example.nhom1_quanlychitieu.ui.BaoCao.model;

/**
 * Lớp dữ liệu cho danh mục trong báo cáo
 */
public class CategoryData {
    private String name;
    private long amount;
    private int color;
    private float percentage;

    public CategoryData(String name, long amount, int color) {
        this.name = name;
        this.amount = amount;
        this.color = color;
        this.percentage = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public float getPercentage() {
        return percentage;
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }
}
