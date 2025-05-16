package com.example.nhom1_quanlychitieu.ui.BaoCao;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.nhom1_quanlychitieu.R;

public class BaoCaoFragment extends Fragment {

    private static final String TAG = "BaoCaoFragment";

    // UI components
    private TextView tabExpense, tabIncome;
    private View indicatorExpense, indicatorIncome;
    private FrameLayout fragmentContainer;

    // Fragments
    private BaoCaoChiTieuFragment chiTieuFragment;
    private BaoCaoThuNhapFragment thuNhapFragment;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Bắt lỗi không xử lý được để ghi log
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
                Log.e(TAG, "Uncaught exception: ", throwable);
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_baocao, container, false);

            // Ánh xạ các thành phần giao diện
            initializeViews(view);

            // Thiết lập sự kiện
            setupEventListeners();

            return view;
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateView", e);
            Toast.makeText(getContext(), "Lỗi khi tạo giao diện: " + e.getMessage(), Toast.LENGTH_LONG).show();
            // Trả về view trống nếu có lỗi
            return new View(getContext());
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            // Khởi tạo các fragment
            chiTieuFragment = new BaoCaoChiTieuFragment();
            thuNhapFragment = new BaoCaoThuNhapFragment();

            // Hiển thị fragment chi tiêu mặc định
            showFragment(chiTieuFragment);
        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated", e);
            Toast.makeText(getContext(), "Lỗi khi khởi tạo các fragment: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initializeViews(View view) {
        tabExpense = view.findViewById(R.id.tabExpense);
        tabIncome = view.findViewById(R.id.tabIncome);
        indicatorExpense = view.findViewById(R.id.indicatorExpense);
        indicatorIncome = view.findViewById(R.id.indicatorIncome);
        fragmentContainer = view.findViewById(R.id.fragmentContainer);
    }

    private void setupEventListeners() {
        // Sự kiện chọn tab chi tiêu
        tabExpense.setOnClickListener(v -> {
            updateTabSelection(true);
            showFragment(chiTieuFragment);
        });

        // Sự kiện chọn tab thu nhập
        tabIncome.setOnClickListener(v -> {
            updateTabSelection(false);
            showFragment(thuNhapFragment);
        });
    }

    private void updateTabSelection(boolean isExpenseSelected) {
        // Cập nhật trạng thái tab
        tabExpense.setTextColor(getResources().getColor(isExpenseSelected ?
                android.R.color.white : android.R.color.darker_gray));
        tabIncome.setTextColor(getResources().getColor(isExpenseSelected ?
                android.R.color.darker_gray : android.R.color.white));

        // Cập nhật trạng thái indicator
        indicatorExpense.setVisibility(isExpenseSelected ? View.VISIBLE : View.INVISIBLE);
        indicatorIncome.setVisibility(isExpenseSelected ? View.INVISIBLE : View.VISIBLE);
    }

    private void showFragment(Fragment fragment) {
        try {
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.replace(R.id.fragmentContainer, fragment);
            transaction.commit();
        } catch (Exception e) {
            Log.e(TAG, "Error showing fragment", e);
            Toast.makeText(getContext(), "Lỗi khi hiển thị nội dung: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
