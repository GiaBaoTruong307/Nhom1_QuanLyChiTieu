package com.example.nhom1_quanlychitieu.ui.LapKeHoach.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.nhom1_quanlychitieu.ui.LapKeHoach.model.Goal;
import com.example.nhom1_quanlychitieu.ui.LapKeHoach.repository.GoalRepository;

import java.util.List;

public class GoalViewModel extends ViewModel {
    private final GoalRepository repository;

    public GoalViewModel() {
        repository = new GoalRepository();
    }

    // Lấy danh sách mục tiêu
    public LiveData<List<Goal>> getGoals() {
        return repository.getGoalsLiveData();
    }

    // Lấy thông báo lỗi
    public LiveData<String> getErrorMessage() {
        return repository.getErrorMessageLiveData();
    }

    // Thêm mục tiêu mới
    public void addGoal(String name, long targetAmount, String goalType) {
        repository.addGoal(name, targetAmount, goalType);
    }

    // Cập nhật mục tiêu
    public void updateGoal(String goalId, String name, long targetAmount) {
        repository.updateGoal(goalId, name, targetAmount);
    }

    // Cập nhật số tiền hiện tại
    public void updateCurrentAmount(String goalId, long amount) {
        repository.updateCurrentAmount(goalId, amount);
    }

    // Xóa mục tiêu
    public void deleteGoal(String goalId) {
        repository.deleteGoal(goalId);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.cleanup();
    }
}
