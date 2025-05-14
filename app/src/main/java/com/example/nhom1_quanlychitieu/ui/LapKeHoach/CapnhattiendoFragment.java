package com.example.nhom1_quanlychitieu.ui.LapKeHoach;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.nhom1_quanlychitieu.R;

import java.text.NumberFormat;
import java.util.Locale;

public class CapnhattiendoFragment extends DialogFragment {

    private static final String ARG_GOAL_ID = "goal_id";
    private static final String ARG_GOAL_NAME = "goal_name";
    private static final String ARG_TARGET_AMOUNT = "target_amount";
    private static final String ARG_CURRENT_AMOUNT = "current_amount";

    private TextView tvGoalName;
    private TextView tvTargetAmount;
    private TextView tvCurrentAmount;
    private EditText editTextUpdateAmount;
    private ProgressUpdateListener listener;
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    // Interface để giao tiếp với Fragment cha
    public interface ProgressUpdateListener {
        void onProgressUpdated(String goalId, long amount);
    }

    public void setProgressUpdateListener(ProgressUpdateListener listener) {
        this.listener = listener;
    }

    // Tạo phương thức để truyền dữ liệu vào fragment
    public static CapnhattiendoFragment newInstance(String goalId, String goalName, long targetAmount, long currentAmount) {
        CapnhattiendoFragment fragment = new CapnhattiendoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GOAL_ID, goalId);
        args.putString(ARG_GOAL_NAME, goalName);
        args.putLong(ARG_TARGET_AMOUNT, targetAmount);
        args.putLong(ARG_CURRENT_AMOUNT, currentAmount);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Nạp layout cho dialog fragment
        View view = inflater.inflate(R.layout.fragment_lapkehoach_capnhattiendo, container, false);

        // Tùy chỉnh dialog
        setCancelable(true); // Cho phép đóng dialog khi bấm ngoài hoặc nút Back
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Tìm các view trong layout
        initializeViews(view);

        // Lấy dữ liệu mục tiêu từ arguments và hiển thị
        setupDataFromArguments();

        return view;
    }

    private void initializeViews(View view) {
        tvGoalName = view.findViewById(R.id.tvGoalName);
        tvTargetAmount = view.findViewById(R.id.tvTargetAmount);
        tvCurrentAmount = view.findViewById(R.id.tvCurrentAmount);
        editTextUpdateAmount = view.findViewById(R.id.editTextUpdateAmount);
        Button buttonCancel = view.findViewById(R.id.buttonCancel);
        Button buttonUpdate = view.findViewById(R.id.buttonUpdate);

        // Xử lý nút "HỦY" - Đóng dialog
        buttonCancel.setOnClickListener(v -> dismiss());

        // Xử lý nút "CẬP NHẬT" - Cập nhật tiến độ
        buttonUpdate.setOnClickListener(v -> updateProgress());
    }

    private void setupDataFromArguments() {
        Bundle args = getArguments();
        if (args == null) return;

        String goalId = args.getString(ARG_GOAL_ID, "");
        String goalName = args.getString(ARG_GOAL_NAME, "");
        long targetAmount = args.getLong(ARG_TARGET_AMOUNT, 0);
        long currentAmount = args.getLong(ARG_CURRENT_AMOUNT, 0);

        tvGoalName.setText(goalName);
        tvTargetAmount.setText("Mục tiêu: " + CURRENCY_FORMAT.format(targetAmount) + " VND");
        tvCurrentAmount.setText("Hiện tại: " + CURRENCY_FORMAT.format(currentAmount) + " VND");
        editTextUpdateAmount.setText(String.valueOf(currentAmount));
    }

    private void updateProgress() {
        String amountStr = editTextUpdateAmount.getText().toString().trim();

        // Kiểm tra dữ liệu nhập
        if (TextUtils.isEmpty(amountStr)) {
            Toast.makeText(getContext(), "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            long amount = Long.parseLong(amountStr);
            if (amount < 0) {
                Toast.makeText(getContext(), "Số tiền không được âm", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gọi callback để cập nhật tiến độ
            if (listener != null && getArguments() != null) {
                String goalId = getArguments().getString(ARG_GOAL_ID, "");
                listener.onProgressUpdated(goalId, amount);
            }
            dismiss(); // Đóng dialog sau khi cập nhật
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Tùy chỉnh kích thước dialog
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(700, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}