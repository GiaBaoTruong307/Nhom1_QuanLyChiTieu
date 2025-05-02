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

    private static final String ARG_GOAL_NAME = "goal_name";
    private static final String ARG_GOAL_AMOUNT = "goal_amount";

    // Tạo phương thức để truyền dữ liệu vào fragment
    public static MenuLapKeHoachFragment newInstance(String goalName, String goalAmount) {
        MenuLapKeHoachFragment fragment = new MenuLapKeHoachFragment();
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
        View view = inflater.inflate(R.layout.fragment_menu_lap_ke_hoach, container, false);

        // Tùy chỉnh dialog
        setCancelable(true); // Cho phép đóng dialog khi bấm ngoài hoặc nút Back
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent); // Loại bỏ nền mặc định

        // Tìm các view trong layout
        LinearLayout layoutEditGoal = view.findViewById(R.id.layoutEditGoal);
        LinearLayout layoutDeleteGoal = view.findViewById(R.id.layoutDeleteGoal);

        // Lấy dữ liệu mục tiêu từ arguments
        String goalName = getArguments().getString(ARG_GOAL_NAME);
        String goalAmount = getArguments().getString(ARG_GOAL_AMOUNT);

        // Xử lý sự kiện khi bấm "Sửa mục tiêu"
        layoutEditGoal.setOnClickListener(v -> {
            // Hiển thị SuamuctieuFragment và truyền dữ liệu mục tiêu
            SuamuctieuFragment suamuctieuFragment = SuamuctieuFragment.newInstance(goalName, goalAmount);
            suamuctieuFragment.show(getParentFragmentManager(), "SuamuctieuFragment");
            dismiss(); // Đóng dialog MenuLapKeHoachFragment
        });

        // Xử lý sự kiện khi bấm "Xóa mục tiêu"
        layoutDeleteGoal.setOnClickListener(v -> {
            // Logic để xóa mục tiêu (chưa có, có thể thêm sau)
            dismiss(); // Đóng dialog sau khi chọn
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Tùy chỉnh kích thước dialog
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(500, ViewGroup.LayoutParams.WRAP_CONTENT); // Đặt chiều rộng cố định
        }
    }
}