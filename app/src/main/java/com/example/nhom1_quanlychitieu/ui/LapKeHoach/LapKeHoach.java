package com.example.nhom1_quanlychitieu.ui.LapKeHoach;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.nhom1_quanlychitieu.R;
import com.google.android.material.button.MaterialButton;

public class LapKeHoach extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Nạp layout cho fragment
        View view = inflater.inflate(R.layout.fragment_lapkehoach, container, false);

        // Tìm nút "Thêm mới mục tiêu"
        MaterialButton btnAddGoal = view.findViewById(R.id.btn_add_goal);

        // Thêm sự kiện khi bấm nút "Thêm mới mục tiêu"
        btnAddGoal.setOnClickListener(v -> {
            // Hiển thị ThemmoiFragment như một dialog
            ThemmoiFragment themmoiFragment = new ThemmoiFragment();
            themmoiFragment.show(getParentFragmentManager(), "ThemmoiFragment");
        });

        // Tìm các layout của tất cả mục tiêu
        LinearLayout goal1Layout = view.findViewById(R.id.goal_1); // Mua xe
        LinearLayout goal2Layout = view.findViewById(R.id.goal_2); // Du lịch
        LinearLayout goal3Layout = view.findViewById(R.id.goal_3); // Mua nhà
        LinearLayout goal4Layout = view.findViewById(R.id.goal_4); // Mua điện thoại

        // Dữ liệu mục tiêu (tên và số tiền)
        String[] goalNames = {"Mua xe", "Du lịch", "Mua nhà", "Mua điện thoại"};
        String[] goalAmounts = {"500000000", "50000000", "6000000000", "20000000"};

        // Tạo một mảng chứa tất cả các layout mục tiêu
        LinearLayout[] goalLayouts = new LinearLayout[]{goal1Layout, goal2Layout, goal3Layout, goal4Layout};

        // Thêm sự kiện nhấn cho từng mục tiêu
        for (int i = 0; i < goalLayouts.length; i++) {
            final int index = i; // Lưu chỉ số để truy cập tên và số tiền
            LinearLayout goalLayout = goalLayouts[i];
            if (goalLayout != null) { // Kiểm tra để đảm bảo layout tồn tại
                goalLayout.setOnClickListener(v -> {
                    // Hiển thị MenuLapKeHoachFragment và truyền dữ liệu mục tiêu
                    MenuLapKeHoachFragment menuFragment = MenuLapKeHoachFragment.newInstance(
                            goalNames[index], goalAmounts[index]);
                    menuFragment.show(getParentFragmentManager(), "MenuLapKeHoachFragment");
                });
            }
        }

        return view;
    }
}