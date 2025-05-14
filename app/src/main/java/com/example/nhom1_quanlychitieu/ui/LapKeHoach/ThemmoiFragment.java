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

public class ThemmoiFragment extends DialogFragment {

    private EditText editTextGoal;
    private EditText editTextAmount;
    private GoalAddListener listener;
    private String selectedGoalType = "";
    private String selectedGoalTypeName = "";

    // Interface để giao tiếp với Fragment cha
    public interface GoalAddListener {
        void onGoalAdded(String name, long amount, String goalType);
    }

    public void setGoalAddListener(GoalAddListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Nạp layout cho dialog fragment
        View view = inflater.inflate(R.layout.fragment_lapkehoach_themmoi, container, false);

        // Tùy chỉnh dialog
        setCancelable(true); // Cho phép đóng dialog khi bấm ngoài hoặc nút Back
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Khôi phục trạng thái nếu có
        if (savedInstanceState != null) {
            selectedGoalType = savedInstanceState.getString("selectedGoalType", "");
            selectedGoalTypeName = savedInstanceState.getString("selectedGoalTypeName", "");
        }

        // Tìm các view trong layout và thiết lập sự kiện
        initializeViews(view);

        return view;
    }

    private void initializeViews(View view) {
        editTextGoal = view.findViewById(R.id.editTextGoal);
        editTextAmount = view.findViewById(R.id.editTextAmount);
        Button buttonCancel = view.findViewById(R.id.buttonCancel);
        Button buttonAdd = view.findViewById(R.id.buttonAdd);

        // Hiển thị dialog chọn loại mục tiêu khi nhấn vào EditText
        editTextGoal.setOnClickListener(v -> showGoalTypeDialog());
        editTextGoal.setFocusable(false); // Không cho phép nhập trực tiếp

        // Nếu đã có dữ liệu từ trước, hiển thị lại
        if (!TextUtils.isEmpty(selectedGoalTypeName)) {
            editTextGoal.setText(selectedGoalTypeName);
        }

        // Xử lý nút "HỦY" - Đóng dialog
        buttonCancel.setOnClickListener(v -> dismiss());

        // Xử lý nút "THÊM" - Lấy dữ liệu và thêm mục tiêu mới
        buttonAdd.setOnClickListener(v -> addNewGoal());
    }

    private void showGoalTypeDialog() {
        ChooseGoalTypeFragment chooseGoalTypeFragment = new ChooseGoalTypeFragment();
        chooseGoalTypeFragment.setGoalTypeListener((type, typeName) -> {
            selectedGoalType = type;
            selectedGoalTypeName = typeName;
            editTextGoal.setText(typeName);
        });
        chooseGoalTypeFragment.show(getParentFragmentManager(), "ChooseGoalTypeFragment");
    }

    private void addNewGoal() {
        String goalName = editTextGoal.getText().toString().trim();
        String amountStr = editTextAmount.getText().toString().trim();

        // Kiểm tra dữ liệu nhập
        if (TextUtils.isEmpty(goalName)) {
            Toast.makeText(getContext(), "Vui lòng chọn mục tiêu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(selectedGoalType)) {
            Toast.makeText(getContext(), "Vui lòng chọn loại mục tiêu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(amountStr)) {
            Toast.makeText(getContext(), "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Loại bỏ dấu phân cách
            long amount = Long.parseLong(amountStr.replaceAll("[.,]", ""));
            if (amount <= 0) {
                Toast.makeText(getContext(), "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gọi callback để thêm mục tiêu
            if (listener != null) {
                listener.onGoalAdded(selectedGoalTypeName, amount, selectedGoalType);
            }
            dismiss(); // Đóng dialog sau khi thêm
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Lưu trạng thái khi xoay màn hình
        outState.putString("selectedGoalType", selectedGoalType);
        outState.putString("selectedGoalTypeName", selectedGoalTypeName);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Tùy chỉnh kích thước dialog
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(900, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}