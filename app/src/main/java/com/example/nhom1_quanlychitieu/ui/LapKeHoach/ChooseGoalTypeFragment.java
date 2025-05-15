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

public class ChooseGoalTypeFragment extends DialogFragment {

    private GoalTypeListener listener;

    // Interface để giao tiếp với Fragment cha
    public interface GoalTypeListener {
        void onGoalTypeSelected(String type, String typeName);
    }

    public void setGoalTypeListener(GoalTypeListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Nạp layout cho dialog fragment
        View view = inflater.inflate(R.layout.fragment_lapkehoach_choose_type, container, false);

        // Tùy chỉnh dialog
        setCancelable(true); // Cho phép đóng dialog khi bấm ngoài hoặc nút Back
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Thiết lập các sự kiện click cho các loại mục tiêu
        setupGoalTypeClickListeners(view);

        return view;
    }

    private void setupGoalTypeClickListeners(View view) {
        // Định nghĩa các loại mục tiêu
        final GoalTypeOption[] goalTypes = {
                new GoalTypeOption(R.id.layoutTypeCar, "car", "Mua xe"),
                new GoalTypeOption(R.id.layoutTypeTravel, "travel", "Du lịch"),
                new GoalTypeOption(R.id.layoutTypeHouse, "house", "Mua nhà"),
                new GoalTypeOption(R.id.layoutTypePhone, "phone", "Mua điện thoại"),
                new GoalTypeOption(R.id.layoutTypeToy, "toy", "Mua đồ chơi"),
                new GoalTypeOption(R.id.layoutTypeJewelry, "jewelry", "Mua trang sức"),
                new GoalTypeOption(R.id.layoutTypeEducation, "education", "Học tập"),
                new GoalTypeOption(R.id.layoutTypeWedding, "wedding", "Đám cưới"),
                new GoalTypeOption(R.id.layoutTypeInvestment, "investment", "Đầu tư"),
                new GoalTypeOption(R.id.layoutTypeOther, "other", "Khác")
        };

        // Thiết lập sự kiện click cho từng loại
        for (final GoalTypeOption option : goalTypes) {
            View optionView = view.findViewById(option.viewId);
            if (optionView != null) {
                optionView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onGoalTypeSelected(option.type, option.name);
                    }
                    dismiss();
                });
            }
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

    // Lớp tiện ích để lưu trữ thông tin về loại mục tiêu
    private static class GoalTypeOption {
        final int viewId;
        final String type;
        final String name;

        GoalTypeOption(int viewId, String type, String name) {
            this.viewId = viewId;
            this.type = type;
            this.name = name;
        }
    }
}
