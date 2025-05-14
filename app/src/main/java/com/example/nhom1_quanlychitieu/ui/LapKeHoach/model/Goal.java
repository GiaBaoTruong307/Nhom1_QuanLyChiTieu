package com.example.nhom1_quanlychitieu.ui.LapKeHoach.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@IgnoreExtraProperties
public class Goal {
    private String id;
    private String name;
    private long targetAmount;
    private long currentAmount;
    private long createdAt;
    private long updatedAt;
    private String userId;
    private String goalType; // Loại mục tiêu: car, travel, house, phone, toy, jewelry

    // Biến tạm để lưu tiến độ dựa trên thu nhập (không lưu vào database)
    @Exclude
    private int progressBasedOnIncome = -1;

    // Constructor mặc định cần thiết cho Firebase
    public Goal() {
    }

    public Goal(String name, long targetAmount, String goalType) {
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = 0;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.goalType = goalType;
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

    public long getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(long targetAmount) {
        this.targetAmount = targetAmount;
    }

    public long getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(long currentAmount) {
        this.currentAmount = currentAmount;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGoalType() {
        return goalType;
    }

    public void setGoalType(String goalType) {
        this.goalType = goalType;
    }

    @Exclude
    public int getProgressBasedOnIncome() {
        return progressBasedOnIncome;
    }

    @Exclude
    public void setProgressBasedOnIncome(int progressBasedOnIncome) {
        this.progressBasedOnIncome = progressBasedOnIncome;
    }

    // Tính phần trăm hoàn thành
    @Exclude
    public int getProgressPercentage() {
        // Nếu có tiến độ dựa trên thu nhập, sử dụng nó
        if (progressBasedOnIncome >= 0) {
            return progressBasedOnIncome;
        }

        // Ngược lại, tính dựa trên số tiền hiện tại
        if (targetAmount == 0) return 0;
        return (int) ((currentAmount * 100) / targetAmount);
    }

    // Kiểm tra mục tiêu đã hoàn thành chưa
    @Exclude
    public boolean isCompleted() {
        // Nếu có tiến độ dựa trên thu nhập, kiểm tra nó
        if (progressBasedOnIncome >= 0) {
            return progressBasedOnIncome >= 100;
        }

        // Ngược lại, kiểm tra dựa trên số tiền hiện tại
        return currentAmount >= targetAmount;
    }

    // Phương thức để chuyển đối tượng thành Map (hữu ích cho Firebase)
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("targetAmount", targetAmount);
        result.put("currentAmount", currentAmount);
        result.put("createdAt", createdAt);
        result.put("updatedAt", updatedAt);
        result.put("userId", userId);
        result.put("goalType", goalType);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Goal goal = (Goal) o;
        return targetAmount == goal.targetAmount &&
                currentAmount == goal.currentAmount &&
                Objects.equals(id, goal.id) &&
                Objects.equals(name, goal.name) &&
                Objects.equals(goalType, goal.goalType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, targetAmount, currentAmount, goalType);
    }
}