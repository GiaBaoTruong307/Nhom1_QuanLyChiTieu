package com.example.nhom1_quanlychitieu.ui.LapKeHoach;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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

import java.text.NumberFormat;
import java.util.Locale;

public class ThemmoiFragment extends DialogFragment {

    private EditText editTextGoal;
    private EditText editTextAmount;
    private Button buttonCancel;
    private Button buttonAdd;
    private GoalAddListener listener;
    private String selectedGoalType = "";
    private String selectedGoalTypeName = "";
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

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
        buttonCancel = view.findViewById(R.id.buttonCancel);
        buttonAdd = view.findViewById(R.id.buttonAdd);

        // Hiển thị dialog chọn loại mục tiêu khi nhấn vào EditText
        editTextGoal.setOnClickListener(v -> showGoalTypeDialog());
        editTextGoal.setFocusable(false); // Không cho phép nhập trực tiếp

        // Nếu đã có dữ liệu từ trước, hiển thị lại
        if (!TextUtils.isEmpty(selectedGoalTypeName)) {
            editTextGoal.setText(selectedGoalTypeName);
        }

        // Thiết lập định dạng tiền tệ cho EditText số tiền
        setupCurrencyFormatting(editTextAmount);

        // Xử lý nút "HỦY" - Đóng dialog
        if (buttonCancel != null) {
            buttonCancel.setOnClickListener(v -> dismiss());
        }

        // Xử lý nút "THÊM" - Lấy dữ liệu và thêm mục tiêu mới
        if (buttonAdd != null) {
            buttonAdd.setOnClickListener(v -> addNewGoal());
        }
    }

    private void setupCurrencyFormatting(final EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    editText.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[.,]", "");
                    if (!cleanString.isEmpty()) {
                        try {
                            long parsed = Long.parseLong(cleanString);
                            String formatted = numberFormat.format(parsed);
                            current = formatted;
                            editText.setText(formatted);
                            editText.setSelection(formatted.length());
                        } catch (NumberFormatException e) {
                            // Xử lý lỗi nếu cần
                        }
                    } else {
                        current = "";
                    }

                    editText.addTextChangedListener(this);
                }
            }
        });
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
            showToast("Vui lòng chọn mục tiêu");
            return;
        }

        if (TextUtils.isEmpty(selectedGoalType)) {
            showToast("Vui lòng chọn loại mục tiêu");
            return;
        }

        if (TextUtils.isEmpty(amountStr)) {
            showToast("Vui lòng nhập số tiền");
            return;
        }

        try {
            // Loại bỏ dấu phân cách
            long amount = Long.parseLong(amountStr.replaceAll("[.,]", ""));
            if (amount <= 0) {
                showToast("Số tiền phải lớn hơn 0");
                return;
            }

            // Gọi callback để thêm mục tiêu
            if (listener != null) {
                listener.onGoalAdded(selectedGoalTypeName, amount, selectedGoalType);
            }
            dismiss(); // Đóng dialog sau khi thêm
        } catch (NumberFormatException e) {
            showToast("Số tiền không hợp lệ");
        }
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
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
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
