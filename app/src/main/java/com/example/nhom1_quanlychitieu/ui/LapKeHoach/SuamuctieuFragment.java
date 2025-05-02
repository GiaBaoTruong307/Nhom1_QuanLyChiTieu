package com.example.nhom1_quanlychitieu.ui.LapKeHoach;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.nhom1_quanlychitieu.R;

public class SuamuctieuFragment extends DialogFragment {

    private static final String ARG_GOAL_NAME = "goal_name";
    private static final String ARG_GOAL_AMOUNT = "goal_amount";

    private EditText editTextGoal;
    private EditText editTextAmount;

    // Tạo phương thức để truyền dữ liệu vào fragment
    public static SuamuctieuFragment newInstance(String goalName, String goalAmount) {
        SuamuctieuFragment fragment = new SuamuctieuFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GOAL_NAME, goalName);
        args.putString(ARG_GOAL_AMOUNT, goalAmount);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Nạp layout cho dialog fragment
        View view = inflater.inflate(R.layout.fragment_suamuctieu, container, false);

        // Tùy chỉnh dialog
        setCancelable(true); // Cho phép đóng dialog khi bấm ngoài hoặc nút Back
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent); // Loại bỏ nền mặc định

        // Tìm các view trong layout
        editTextGoal = view.findViewById(R.id.editTextGoal);
        editTextAmount = view.findViewById(R.id.editTextAmount);
        Button buttonCancel = view.findViewById(R.id.buttonCancel);
        Button buttonSave = view.findViewById(R.id.buttonSave);

        // Lấy dữ liệu mục tiêu từ arguments và hiển thị
        if (getArguments() != null) {
            String goalName = getArguments().getString(ARG_GOAL_NAME);
            String goalAmount = getArguments().getString(ARG_GOAL_AMOUNT);
            editTextGoal.setText(goalName);
            editTextAmount.setText(goalAmount);
        }

        // Xử lý nút "HỦY" - Đóng dialog
        buttonCancel.setOnClickListener(v -> dismiss());

        // Xử lý nút "LƯU" - Lưu dữ liệu đã chỉnh sửa (chưa có logic lưu, chỉ đóng dialog)
        buttonSave.setOnClickListener(v -> {
            String updatedGoal = editTextGoal.getText().toString();
            String updatedAmount = editTextAmount.getText().toString();
            // Thêm logic để lưu dữ liệu đã chỉnh sửa (ví dụ: cập nhật database)
            dismiss(); // Đóng dialog sau khi lưu
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Tùy chỉnh kích thước dialog
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(600, ViewGroup.LayoutParams.WRAP_CONTENT); // Đặt chiều rộng cố định
        }
    }
}