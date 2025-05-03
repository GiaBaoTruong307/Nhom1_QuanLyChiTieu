package com.example.nhom1_quanlychitieu.ui.ViTien;

import java.text.NumberFormat;
import java.util.Locale;

public class Account {
    private String name;
    private long amount; // Lưu số tiền dưới dạng long để dễ tính toán
    private int iconResId;
    private String type; // "bank" hoặc "cash"

    public Account(String name, long amount, int iconResId, String type) {
        this.name = name;
        this.amount = amount;
        this.iconResId = iconResId;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public long getAmount() {
        return amount;
    }

    public String getFormattedAmount() {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return (amount >= 0 ? "+" : "") + formatter.format(amount) + " VND";
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getType() {
        return type;
    }
}
