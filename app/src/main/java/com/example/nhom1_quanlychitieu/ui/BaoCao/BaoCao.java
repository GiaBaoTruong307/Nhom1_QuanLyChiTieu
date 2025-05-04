package com.example.nhom1_quanlychitieu.ui.BaoCao;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.nhom1_quanlychitieu.R;

public class BaoCao extends Fragment {

    private TextView tabExpense, tabIncome;
    private View indicatorExpense, indicatorIncome;
    private ImageButton btnBack;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_baocao, container, false);

        // Initialize views
        tabExpense = rootView.findViewById(R.id.tabExpense);
        tabIncome = rootView.findViewById(R.id.tabIncome);
        indicatorExpense = rootView.findViewById(R.id.indicatorExpense);
        indicatorIncome = rootView.findViewById(R.id.indicatorIncome);
        btnBack = rootView.findViewById(R.id.btnBack);

        // Set click listeners for tabs
        tabExpense.setOnClickListener(v -> switchToTab(true));
        tabIncome.setOnClickListener(v -> switchToTab(false));
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed()); // Thay finish() bằng onBackPressed()

        // Load default fragment
        if (savedInstanceState == null) {
            switchToTab(true);
        }

        return rootView;
    }

    private void switchToTab(boolean isExpenseTab) {
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
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }
}