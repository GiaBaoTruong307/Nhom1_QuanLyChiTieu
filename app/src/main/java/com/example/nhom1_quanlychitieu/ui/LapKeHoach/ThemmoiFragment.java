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
public class ThemmoiFragment extends DialogFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Nạp layout cho dialog fragment
        View view = inflater.inflate(R.layout.fragment_themmoi, container, false);
        // Tùy chỉnh dialog
        setCancelable(true); // Cho phép đóng dialog khi bấm ngoài hoặc nút Back
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent); // Loại bỏ nền mặc định
        // Tìm các view trong layout
        EditText editTextGoal = view.findViewById(R.id.editTextGoal);
        EditText editTextAmount = view.findViewById(R.id.editTextAmount);
        Button buttonCancel = view.findViewById(R.id.buttonCancel);
        Button buttonAdd = view.findViewById(R.id.buttonAdd);
        // Xử lý nút "HỦY" - Đóng dialog
        buttonCancel.setOnClickListener(v -> dismiss());
        // Xử lý nút "THÊM" - Lấy dữ liệu và đóng dialog (chưa có logic lưu)
        buttonAdd.setOnClickListener(v -> {
            String goal = editTextGoal.getText().toString();
            String amount = editTextAmount.getText().toString();
            // Thêm logic lưu mục tiêu mới nếu cần (ví dụ: lưu vào database)
            dismiss(); // Đóng dialog sau khi thêm
        });

        return view;
    }
    @Override
    public void onStart() {
        super.onStart();
        // Tăng chiều rộng dialog để hiển thị thoải mái hơn
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(900, ViewGroup.LayoutParams.WRAP_CONTENT); // Tăng chiều rộng lên 600dp
        }
    }
}