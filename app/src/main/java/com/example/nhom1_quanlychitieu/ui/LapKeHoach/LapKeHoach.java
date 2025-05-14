package com.example.nhom1_quanlychitieu.ui.LapKeHoach;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class LapKeHoach extends Fragment implements GoalAdapter.OnGoalClickListener {

    private static final String TAG = "LapKeHoach";
    private GoalViewModel viewModel;
    private GoalAdapter adapter;
    private TextView tvTotalAmount;
    private ProgressBar progressBarTotal;
    private View emptyView;
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String userId;

    // Tổng thu nhập từ chức năng thống kê
    private long totalIncome = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Nạp layout cho fragment
        View view = inflater.inflate(R.layout.fragment_lapkehoach, container, false);

        // Khởi tạo Firebase
        initFirebase();

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(GoalViewModel.class);

        // Tìm các view trong layout
        initializeViews(view);

        // Thiết lập RecyclerView
        setupRecyclerView(view);

        // Thiết lập FloatingActionButton để thêm mục tiêu mới
        setupFabButton(view);

        // Quan sát dữ liệu từ ViewModel
        observeViewModel();

        // Tải dữ liệu thu nhập từ chức năng thống kê
        loadIncomeData();

        return view;
    }

    private void initFirebase() {
        try {
            mAuth = FirebaseAuth.getInstance();
            mDatabase = FirebaseDatabase.getInstance().getReference();

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                userId = currentUser.getUid();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase", e);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Lỗi khởi tạo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initializeViews(View view) {
        tvTotalAmount = view.findViewById(R.id.tvTotalAmount);
        progressBarTotal = view.findViewById(R.id.progressBarTotal);
        emptyView = view.findViewById(R.id.emptyView);
    }

    private void setupRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewGoals);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GoalAdapter(this);
        recyclerView.setAdapter(adapter);
        // Tối ưu hóa RecyclerView
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
    }

    private void setupFabButton(View view) {
        FloatingActionButton fabAddGoal = view.findViewById(R.id.fabAddGoal);
        fabAddGoal.setOnClickListener(v -> showAddGoalDialog());
    }

    private void observeViewModel() {
        // Quan sát danh sách mục tiêu
        viewModel.getGoals().observe(getViewLifecycleOwner(), this::updateUI);

        // Quan sát thông báo lỗi
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty() && getContext() != null) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadIncomeData() {
        if (userId == null) return;

        mDatabase.child("transactions").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                totalIncome = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        // Lấy giá trị amount từ transaction
                        Long amount = snapshot.child("amount").getValue(Long.class);
                        if (amount != null && amount > 0) {
                            // Chỉ tính các giao dịch có amount > 0 (thu nhập)
                            totalIncome += amount;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing transaction", e);
                    }
                }

                // Cập nhật lại UI sau khi có dữ liệu thu nhập mới
                if (viewModel.getGoals().getValue() != null) {
                    updateUI(viewModel.getGoals().getValue());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu thu nhập: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateUI(List<Goal> goals) {
        // Cập nhật adapter với thu nhập
        adapter.submitList(goals);
        adapter.setTotalIncome(totalIncome);

        // Hiển thị view "không có dữ liệu" nếu danh sách trống
        if (emptyView != null) {
            emptyView.setVisibility(goals.isEmpty() ? View.VISIBLE : View.GONE);
        }

        // Cập nhật tổng thu nhập và tiến độ
        updateTotalIncome(goals);
    }

    private void updateTotalIncome(List<Goal> goals) {
        // Format số tiền thu nhập
        tvTotalAmount.setText(CURRENCY_FORMAT.format(totalIncome) + " VND");

        // Tính tổng mục tiêu
        long totalTarget = 0;
        for (Goal goal : goals) {
            totalTarget += goal.getTargetAmount();
        }

        // Cập nhật thanh tiến độ dựa trên thu nhập
        int progress;
        if (totalTarget > 0) {
            progress = (int) ((totalIncome * 100) / totalTarget);
        } else {
            progress = 0;
        }

        progressBarTotal.setProgress(Math.min(progress, 100));
    }

    private void showAddGoalDialog() {
        ThemmoiFragment themmoiFragment = new ThemmoiFragment();
        themmoiFragment.setGoalAddListener((name, amount, goalType) ->
                viewModel.addGoal(name, amount, goalType)
        );
        themmoiFragment.show(getParentFragmentManager(), "ThemmoiFragment");
    }

    @Override
    public void onGoalClick(Goal goal) {
        // Hiển thị menu tùy chọn khi nhấn vào mục tiêu
        MenuLapKeHoachFragment menuFragment = MenuLapKeHoachFragment.newInstance(
                goal.getId(), goal.getName(), String.valueOf(goal.getTargetAmount()));

        menuFragment.setGoalActionListener(new MenuLapKeHoachFragment.GoalActionListener() {
            @Override
            public void onEditGoal(String goalId, String name, String amount) {
                showEditGoalDialog(goalId, name, amount);
            }

            @Override
            public void onDeleteGoal(String goalId) {
                viewModel.deleteGoal(goalId);
            }
        });

        menuFragment.show(getParentFragmentManager(), "MenuLapKeHoachFragment");
    }

    private void showEditGoalDialog(String goalId, String name, String amount) {
        SuamuctieuFragment suamuctieuFragment = SuamuctieuFragment.newInstance(goalId, name, amount);
        suamuctieuFragment.setGoalUpdateListener((goalId1, name1, amount1) ->
                viewModel.updateGoal(goalId1, name1, amount1)
        );
        suamuctieuFragment.show(getParentFragmentManager(), "SuamuctieuFragment");
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tải lại dữ liệu khi quay lại fragment
        loadIncomeData();
    }
}