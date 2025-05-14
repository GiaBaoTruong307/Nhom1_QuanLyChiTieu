package com.example.nhom1_quanlychitieu.ui.LapKeHoach;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.nhom1_quanlychitieu.R;

public class SuamuctieuFragment extends DialogFragment {

    private static final String ARG_GOAL_ID = "goal_id";
    private static final String ARG_GOAL_NAME = "goal_name";
    private static final String ARG_GOAL_AMOUNT = "goal_amount";

    private EditText editTextGoal;
    private EditText editTextAmount;
    private GoalUpdateListener listener;

    // Interface để giao tiếp với Fragment cha
    public interface GoalUpdateListener {
        void onGoalUpdated(String goalId, String name, long amount);
    }

    public void setGoalUpdateListener(GoalUpdateListener listener) {
        this.listener = listener;
    }

    // Tạo phương thức để truyền dữ liệu vào fragment
    public static SuamuctieuFragment newInstance(String goalId, String goalName, String goalAmount) {
        SuamuctieuFragment fragment = new SuamuctieuFragment();
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
        View view = inflater.inflate(R.layout.fragment_lapkehoach_suamuctieu, container, false);

        // Tùy chỉnh dialog
        setCancelable(true); // Cho phép đóng dialog khi bấm ngoài hoặc nút Back
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Tìm các view trong layout
        initializeViews(view);

        return view;
    }

    private void initializeViews(View view) {
        editTextGoal = view.findViewById(R.id.editTextGoal);
        editTextAmount = view.findViewById(R.id.editTextAmount);
        Button buttonCancel = view.findViewById(R.id.buttonCancel);
        Button buttonSave = view.findViewById(R.id.buttonSave);

        // Lấy dữ liệu mục tiêu từ arguments và hiển thị
        Bundle args = getArguments();
        if (args != null) {
            String goalName = args.getString(ARG_GOAL_NAME, "");
            String goalAmount = args.getString(ARG_GOAL_AMOUNT, "0");
            editTextGoal.setText(goalName);
            editTextAmount.setText(goalAmount);
        }

        // Xử lý nút "HỦY" - Đóng dialog
        buttonCancel.setOnClickListener(v -> dismiss());

        // Xử lý nút "LƯU" - Lưu dữ liệu đã chỉnh sửa
        buttonSave.setOnClickListener(v -> saveUpdatedGoal());
    }

    private void saveUpdatedGoal() {
        String updatedGoal = editTextGoal.getText().toString().trim();
        String updatedAmountStr = editTextAmount.getText().toString().trim();

        // Kiểm tra dữ liệu nhập
        if (TextUtils.isEmpty(updatedGoal)) {
            Toast.makeText(getContext(), "Vui lòng nhập tên mục tiêu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(updatedAmountStr)) {
            Toast.makeText(getContext(), "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Loại bỏ dấu phân cách
            long updatedAmount = Long.parseLong(updatedAmountStr.replaceAll("[.,]", ""));
            if (updatedAmount <= 0) {
                Toast.makeText(getContext(), "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gọi callback để cập nhật mục tiêu
            if (listener != null && getArguments() != null) {
                String goalId = getArguments().getString(ARG_GOAL_ID, "");
                listener.onGoalUpdated(goalId, updatedGoal, updatedAmount);
            }
            dismiss(); // Đóng dialog sau khi lưu
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Tùy chỉnh kích thước dialog
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(600, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}