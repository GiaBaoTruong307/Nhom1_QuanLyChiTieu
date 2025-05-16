package com.example.nhom1_quanlychitieu.ui.BaoCao;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.nhom1_quanlychitieu.R;

public class BaoCao extends Fragment {

    private TextView tabExpense, tabIncome;
    private View indicatorExpense, indicatorIncome;
    private Fragment currentFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_baocao, container, false);

        // Initialize views
        tabExpense = rootView.findViewById(R.id.tabExpense);
        tabIncome = rootView.findViewById(R.id.tabIncome);
        indicatorExpense = rootView.findViewById(R.id.indicatorExpense);
        indicatorIncome = rootView.findViewById(R.id.indicatorIncome);

        // Set click listeners for tabs
        tabExpense.setOnClickListener(v -> switchToTab(true));
        tabIncome.setOnClickListener(v -> switchToTab(false));

        // Load default fragment
        if (savedInstanceState == null) {
            switchToTab(true);
        } else {
            // Khôi phục trạng thái tab
            boolean isExpenseTab = savedInstanceState.getBoolean("isExpenseTab", true);
            switchToTab(isExpenseTab);
        }

        return rootView;
    }

    private void switchToTab(boolean isExpenseTab) {
        // Tránh tải lại fragment nếu đã hiển thị
        if ((isExpenseTab && currentFragment instanceof ExpenseFragment) ||
                (!isExpenseTab && currentFragment instanceof IncomeFragment)) {
            return;
        }

        Fragment fragment;
        if (isExpenseTab) {
            // Switch to expense tab
            tabExpense.setTextColor(getResources().getColor(android.R.color.white));
            tabIncome.setTextColor(getResources().getColor(android.R.color.darker_gray));
            indicatorExpense.setVisibility(View.VISIBLE);
            indicatorIncome.setVisibility(View.INVISIBLE);
            fragment = new ExpenseFragment();
        } else {
            // Switch to income tab
            tabExpense.setTextColor(getResources().getColor(android.R.color.darker_gray));
            tabIncome.setTextColor(getResources().getColor(android.R.color.white));
            indicatorExpense.setVisibility(View.INVISIBLE);
            indicatorIncome.setVisibility(View.VISIBLE);
            fragment = new IncomeFragment();
        }

        // Replace the fragment
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();

        currentFragment = fragment;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Lưu trạng thái tab hiện tại
        outState.putBoolean("isExpenseTab", currentFragment instanceof ExpenseFragment);
    }
}
