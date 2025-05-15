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

public class SuamuctieuFragment extends DialogFragment {

    private static final String ARG_GOAL_ID = "goal_id";
    private static final String ARG_GOAL_NAME = "goal_name";
    private static final String ARG_GOAL_AMOUNT = "goal_amount";

    private EditText editTextGoal;
    private EditText editTextAmount;
    private Button buttonCancel;
    private Button buttonSave;
    private GoalUpdateListener listener;
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

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
        buttonCancel = view.findViewById(R.id.buttonCancel);
        buttonSave = view.findViewById(R.id.buttonSave);

        // Thiết lập định dạng tiền tệ cho EditText số tiền
        setupCurrencyFormatting(editTextAmount);

        // Lấy dữ liệu mục tiêu từ arguments và hiển thị
        Bundle args = getArguments();
        if (args != null) {
            String goalName = args.getString(ARG_GOAL_NAME, "");
            String goalAmount = args.getString(ARG_GOAL_AMOUNT, "0");

            editTextGoal.setText(goalName);

            // Định dạng số tiền trước khi hiển thị
            try {
                long amount = Long.parseLong(goalAmount.replaceAll("[.,]", ""));
                editTextAmount.setText(numberFormat.format(amount));
            } catch (NumberFormatException e) {
                editTextAmount.setText(goalAmount);
            }
        }

        // Xử lý nút "HỦY" - Đóng dialog
        if (buttonCancel != null) {
            buttonCancel.setOnClickListener(v -> dismiss());
        }

        // Xử lý nút "LƯU" - Lưu dữ liệu đã chỉnh sửa
        if (buttonSave != null) {
            buttonSave.setOnClickListener(v -> saveUpdatedGoal());
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

    private void saveUpdatedGoal() {
        String updatedGoal = editTextGoal.getText().toString().trim();
        String updatedAmountStr = editTextAmount.getText().toString().trim();

        // Kiểm tra dữ liệu nhập
        if (TextUtils.isEmpty(updatedGoal)) {
            showToast("Vui lòng nhập tên mục tiêu");
            return;
        }

        if (TextUtils.isEmpty(updatedAmountStr)) {
            showToast("Vui lòng nhập số tiền");
            return;
        }

        try {
            // Loại bỏ dấu phân cách
            long updatedAmount = Long.parseLong(updatedAmountStr.replaceAll("[.,]", ""));
            if (updatedAmount <= 0) {
                showToast("Số tiền phải lớn hơn 0");
                return;
            }

            // Gọi callback để cập nhật mục tiêu
            if (listener != null && getArguments() != null) {
                String goalId = getArguments().getString(ARG_GOAL_ID, "");
                listener.onGoalUpdated(goalId, updatedGoal, updatedAmount);
            }
            dismiss(); // Đóng dialog sau khi lưu
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
    public void onStart() {
        super.onStart();
        // Tùy chỉnh kích thước dialog
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
