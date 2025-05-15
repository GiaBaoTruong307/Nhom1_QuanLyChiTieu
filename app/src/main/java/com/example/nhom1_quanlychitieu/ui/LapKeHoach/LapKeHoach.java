package com.example.nhom1_quanlychitieu.ui.LapKeHoach;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom1_quanlychitieu.R;
import com.example.nhom1_quanlychitieu.ui.LapKeHoach.adapter.GoalAdapter;
import com.example.nhom1_quanlychitieu.ui.LapKeHoach.model.Goal;
import com.example.nhom1_quanlychitieu.ui.LapKeHoach.viewmodel.GoalViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class LapKeHoach extends Fragment implements
        GoalAdapter.OnGoalClickListener,
        MenuLapKeHoachFragment.GoalActionListener,
        ThemmoiFragment.GoalAddListener,
        SuamuctieuFragment.GoalUpdateListener {

    private static final String TAG = "LapKeHoachFragment";
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    // ViewModel
    private GoalViewModel viewModel;

    // UI Components
    private RecyclerView recyclerViewGoals;
    private GoalAdapter goalAdapter;
    private ProgressBar progressBarTotal;
    private TextView tvTotalAmount;
    private LinearLayout emptyView;
    private FloatingActionButton fabAddGoal;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String userId;

    // Tổng số dư từ các ví
    private long totalBalance = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lapkehoach, container, false);
        initializeViews(view);
        setupFirebase();
        setupRecyclerView();
        return view;
    }

    private void initializeViews(View view) {
        recyclerViewGoals = view.findViewById(R.id.recyclerViewGoals);
        progressBarTotal = view.findViewById(R.id.progressBarTotal);
        tvTotalAmount = view.findViewById(R.id.tvTotalAmount);
        emptyView = view.findViewById(R.id.emptyView);
        fabAddGoal = view.findViewById(R.id.fabAddGoal);

        // Thiết lập sự kiện click cho FAB
        fabAddGoal.setOnClickListener(v -> showAddGoalDialog());
    }

    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }
    }

    private void setupRecyclerView() {
        recyclerViewGoals.setLayoutManager(new LinearLayoutManager(getContext()));
        goalAdapter = new GoalAdapter(this);
        recyclerViewGoals.setAdapter(goalAdapter);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViewModel();
        loadData();
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(GoalViewModel.class);

        // Observe goals LiveData
        viewModel.getGoals().observe(getViewLifecycleOwner(), this::updateUI);

        // Observe error messages
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                showToast(message);
            }
        });
    }

    private void loadData() {
        loadWalletBalance();
    }

    private void loadWalletBalance() {
        if (userId == null) return;

        mDatabase.child("wallets").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                totalBalance = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        // Lấy giá trị balance từ wallet
                        Long balance = snapshot.child("balance").getValue(Long.class);
                        if (balance != null) {
                            // Cộng dồn số dư của tất cả các ví
                            totalBalance += balance;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing wallet", e);
                    }
                }

                // Cập nhật adapter với tổng số dư mới
                if (goalAdapter != null) {
                    goalAdapter.setTotalIncome(totalBalance);
                }

                // Cập nhật lại UI sau khi có dữ liệu số dư mới
                if (viewModel.getGoals().getValue() != null) {
                    updateUI(viewModel.getGoals().getValue());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                showToast("Lỗi tải dữ liệu số dư: " + databaseError.getMessage());
            }
        });
    }

    private void updateUI(List<Goal> goals) {
        // Cập nhật adapter
        goalAdapter.submitList(goals);

        // Hiển thị emptyView nếu không có mục tiêu
        if (goals == null || goals.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerViewGoals.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerViewGoals.setVisibility(View.VISIBLE);
        }

        // Cập nhật tổng số dư và thanh tiến độ
        updateTotalBalance(goals);
    }

    private void updateTotalBalance(List<Goal> goals) {
        // Format số tiền tổng số dư
        tvTotalAmount.setText(CURRENCY_FORMAT.format(totalBalance));

        // Tính tổng mục tiêu
        long totalTarget = 0;
        if (goals != null) {
            for (Goal goal : goals) {
                totalTarget += goal.getTargetAmount();
            }
        }

        // Cập nhật thanh tiến độ dựa trên số dư
        int progress;
        if (totalTarget > 0) {
            progress = (int) ((totalBalance * 100) / totalTarget);
        } else {
            progress = 0;
        }

        progressBarTotal.setProgress(Math.min(progress, 100));
    }

    // Hiển thị dialog thêm mục tiêu mới
    private void showAddGoalDialog() {
        ThemmoiFragment dialog = new ThemmoiFragment();
        dialog.setGoalAddListener(this);
        dialog.show(getChildFragmentManager(), "ThemmoiFragment");
    }

    // Hiển thị dialog chỉnh sửa mục tiêu
    private void showEditGoalDialog(String goalId, String name, String amount) {
        SuamuctieuFragment dialog = SuamuctieuFragment.newInstance(goalId, name, amount);
        dialog.setGoalUpdateListener(this);
        dialog.show(getChildFragmentManager(), "SuamuctieuFragment");
    }

    // Hiển thị dialog xác nhận xóa mục tiêu
    private void confirmDeleteGoal(String goalId) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa mục tiêu này?")
                .setPositiveButton("Xóa", (dialog, which) -> viewModel.deleteGoal(goalId))
                .setNegativeButton("Hủy", null)
                .show();
    }

    // Hiển thị menu tùy chọn cho mục tiêu
    private void showGoalOptionsMenu(Goal goal) {
        MenuLapKeHoachFragment dialog = MenuLapKeHoachFragment.newInstance(
                goal.getId(),
                goal.getName(),
                String.valueOf(goal.getTargetAmount())
        );
        dialog.setGoalActionListener(this);
        dialog.show(getChildFragmentManager(), "MenuLapKeHoachFragment");
    }

    // Hiển thị thông báo Toast
    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    // Implement GoalAdapter.OnGoalClickListener
    @Override
    public void onGoalClick(Goal goal) {
        showGoalOptionsMenu(goal);
    }

    // Implement MenuLapKeHoachFragment.GoalActionListener
    @Override
    public void onEditGoal(String goalId, String name, String amount) {
        showEditGoalDialog(goalId, name, amount);
    }

    @Override
    public void onDeleteGoal(String goalId) {
        confirmDeleteGoal(goalId);
    }

    // Implement ThemmoiFragment.GoalAddListener
    @Override
    public void onGoalAdded(String name, long amount, String goalType) {
        viewModel.addGoal(name, amount, goalType);
    }

    // Implement SuamuctieuFragment.GoalUpdateListener
    @Override
    public void onGoalUpdated(String goalId, String name, long amount) {
        viewModel.updateGoal(goalId, name, amount);
    }
}
