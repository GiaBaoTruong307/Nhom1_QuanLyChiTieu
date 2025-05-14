package com.example.nhom1_quanlychitieu.ui.LapKeHoach;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.nhom1_quanlychitieu.R;

public class MenuLapKeHoachFragment extends DialogFragment {

    private static final String ARG_GOAL_ID = "goal_id";
    private static final String ARG_GOAL_NAME = "goal_name";
    private static final String ARG_GOAL_AMOUNT = "goal_amount";

    private GoalActionListener listener;

    // Interface để giao tiếp với Fragment cha
    public interface GoalActionListener {
        void onEditGoal(String goalId, String name, String amount);
        void onDeleteGoal(String goalId);
    }

    public void setGoalActionListener(GoalActionListener listener) {
        this.listener = listener;
    }

    // Tạo phương thức để truyền dữ liệu vào fragment
    public static MenuLapKeHoachFragment newInstance(String goalId, String goalName, String goalAmount) {
        MenuLapKeHoachFragment fragment = new MenuLapKeHoachFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GOAL_ID, goalId);
        args.putString(ARG_GOAL_NAME, goalName);
        args.putString(ARG_GOAL_AMOUNT, goalAmount);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Nạp layout cho dialog fragment
        View view = inflater.inflate(R.layout.fragment_lapkehoach_menu, container, false);

        // Tùy chỉnh dialog
        setCancelable(true); // Cho phép đóng dialog khi bấm ngoài hoặc nút Back
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Lấy dữ liệu từ arguments
        Bundle args = getArguments();
        if (args == null) {
            dismiss();
            return view;
        }

        String goalId = args.getString(ARG_GOAL_ID, "");
        String goalName = args.getString(ARG_GOAL_NAME, "");
        String goalAmount = args.getString(ARG_GOAL_AMOUNT, "0");

        // Tìm các view trong layout
        setupClickListeners(view, goalId, goalName, goalAmount);

        return view;
    }

    private void setupClickListeners(View view, final String goalId, final String goalName,
                                     final String goalAmount) {
        LinearLayout layoutEditGoal = view.findViewById(R.id.layoutEditGoal);
        LinearLayout layoutDeleteGoal = view.findViewById(R.id.layoutDeleteGoal);

        // Xử lý sự kiện khi bấm "Sửa mục tiêu"
        layoutEditGoal.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditGoal(goalId, goalName, goalAmount);
            }
            dismiss();
        });

        // Xử lý sự kiện khi bấm "Xóa mục tiêu"
        layoutDeleteGoal.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteGoal(goalId);
            }
            dismiss();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Tùy chỉnh kích thước dialog
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(500, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}