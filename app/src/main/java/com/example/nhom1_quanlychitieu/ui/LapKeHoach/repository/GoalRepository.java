package com.example.nhom1_quanlychitieu.ui.LapKeHoach.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.nhom1_quanlychitieu.ui.LapKeHoach.model.Goal;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoalRepository {
    private static final String GOALS_REF = "goals";
    private final DatabaseReference goalsRef;
    private final MutableLiveData<List<Goal>> goalsLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private final String userId;
    private ValueEventListener goalsListener;

    public GoalRepository() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        goalsRef = database.getReference(GOALS_REF);

        // Lấy ID người dùng hiện tại
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = currentUser != null ? currentUser.getUid() : null;

        if (userId != null) {
            loadGoals();
        } else {
            errorMessageLiveData.setValue("Bạn cần đăng nhập để sử dụng tính năng này");
            goalsLiveData.setValue(new ArrayList<>());
        }
    }

    // Lấy danh sách mục tiêu từ Firebase
    private void loadGoals() {
        // Hủy listener cũ nếu có
        if (goalsListener != null) {
            goalsRef.removeEventListener(goalsListener);
        }

        goalsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Goal> goals = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Goal goal = snapshot.getValue(Goal.class);
                    if (goal != null && userId.equals(goal.getUserId())) {
                        goal.setId(snapshot.getKey());
                        goals.add(goal);
                    }
                }
                goalsLiveData.setValue(goals);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                errorMessageLiveData.setValue("Lỗi: " + databaseError.getMessage());
            }
        };

        // Đăng ký listener mới
        goalsRef.orderByChild("userId").equalTo(userId).addValueEventListener(goalsListener);
    }

    // Thêm mục tiêu mới
    public void addGoal(String name, long targetAmount, String goalType) {
        if (userId == null) {
            errorMessageLiveData.setValue("Bạn cần đăng nhập để thêm mục tiêu");
            return;
        }

        Goal goal = new Goal(name, targetAmount, goalType);
        goal.setUserId(userId);

        String goalId = goalsRef.push().getKey();
        if (goalId != null) {
            goalsRef.child(goalId).setValue(goal)
                    .addOnSuccessListener(aVoid -> {
                        // Thêm thành công
                    })
                    .addOnFailureListener(e ->
                            errorMessageLiveData.setValue("Lỗi khi thêm mục tiêu: " + e.getMessage())
                    );
        }
    }

    // Cập nhật mục tiêu
    public void updateGoal(String goalId, String name, long targetAmount) {
        if (userId == null) {
            errorMessageLiveData.setValue("Bạn cần đăng nhập để cập nhật mục tiêu");
            return;
        }

        DatabaseReference goalRef = goalsRef.child(goalId);

        // Sử dụng updateChildren để cập nhật nhiều trường cùng lúc
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("targetAmount", targetAmount);
        updates.put("updatedAt", System.currentTimeMillis());

        goalRef.updateChildren(updates)
                .addOnFailureListener(e ->
                        errorMessageLiveData.setValue("Lỗi khi cập nhật mục tiêu: " + e.getMessage())
                );
    }

    // Cập nhật số tiền hiện tại
    public void updateCurrentAmount(String goalId, long amount) {
        if (userId == null) {
            errorMessageLiveData.setValue("Bạn cần đăng nhập để cập nhật tiến độ");
            return;
        }

        DatabaseReference goalRef = goalsRef.child(goalId);

        // Sử dụng updateChildren để cập nhật nhiều trường cùng lúc
        Map<String, Object> updates = new HashMap<>();
        updates.put("currentAmount", amount);
        updates.put("updatedAt", System.currentTimeMillis());

        goalRef.updateChildren(updates)
                .addOnFailureListener(e ->
                        errorMessageLiveData.setValue("Lỗi khi cập nhật tiến độ: " + e.getMessage())
                );
    }

    // Xóa mục tiêu
    public void deleteGoal(String goalId) {
        if (userId == null) {
            errorMessageLiveData.setValue("Bạn cần đăng nhập để xóa mục tiêu");
            return;
        }

        goalsRef.child(goalId).removeValue()
                .addOnFailureListener(e ->
                        errorMessageLiveData.setValue("Lỗi khi xóa mục tiêu: " + e.getMessage())
                );
    }

    // Getter cho LiveData
    public LiveData<List<Goal>> getGoalsLiveData() {
        return goalsLiveData;
    }

    public LiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    // Dọn dẹp tài nguyên
    public void cleanup() {
        if (goalsListener != null) {
            goalsRef.orderByChild("userId").equalTo(userId).removeEventListener(goalsListener);
        }
    }
}