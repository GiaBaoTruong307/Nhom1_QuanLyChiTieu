package com.example.nhom1_quanlychitieu.ui.BaoCao;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.nhom1_quanlychitieu.R;
import com.example.nhom1_quanlychitieu.ui.BaoCao.helper.FirebaseDataHelper;
import com.example.nhom1_quanlychitieu.ui.BaoCao.model.CategoryData;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.ArrayList;
import java.util.List;

public class ExpenseFragment extends Fragment {

    private PieChart donutChart;
    private Button btnMonthly, btnYear;
    private TextView valueExpense;
    private LinearLayout categoryList;
    private FirebaseDataHelper dataHelper;
    private View loadingView;

    private int currentMonth;
    private int currentYear;
    private boolean isDataLoading = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_baocao_chitieu, container, false);

        // Initialize views
        donutChart = view.findViewById(R.id.donutChart);
        btnMonthly = view.findViewById(R.id.btnMonthly);
        btnYear = view.findViewById(R.id.btnYear);
        valueExpense = view.findViewById(R.id.valueExpense);
        categoryList = view.findViewById(R.id.categoryList);
        loadingView = view.findViewById(R.id.loadingView);

        // Initialize data helper
        dataHelper = new FirebaseDataHelper();

        // Get current month and year
        currentMonth = dataHelper.getCurrentMonth();
        currentYear = dataHelper.getCurrentYear();

        // Set initial button text
        btnMonthly.setText("Tháng " + currentMonth);
        btnYear.setText(String.valueOf(currentYear));

        // Set up click listeners for filter buttons
        btnMonthly.setOnClickListener(v -> showDropdownMenu(v, true));
        btnYear.setOnClickListener(v -> showDropdownMenu(v, false));

        // Configure chart appearance
        configureChart();

        // Load data
        loadData();

        return view;
    }

    private void configureChart() {
        // Configure the chart appearance
        donutChart.setDrawHoleEnabled(true);
        donutChart.setHoleRadius(70f);
        donutChart.setTransparentCircleRadius(0f);
        donutChart.setDrawCenterText(false);
        donutChart.setDrawEntryLabels(false);
        donutChart.getDescription().setEnabled(false);
        donutChart.setRotationEnabled(false);
        donutChart.setHighlightPerTapEnabled(false);
        donutChart.getLegend().setEnabled(false);
        donutChart.setTouchEnabled(false);
        donutChart.setHoleColor(Color.parseColor("#121418"));
    }

    private void loadData() {
        // Tránh tải dữ liệu nhiều lần
        if (isDataLoading) return;
        isDataLoading = true;

        // Show loading state
        valueExpense.setText("Đang tải...");
        categoryList.removeAllViews();

        if (loadingView != null) {
            loadingView.setVisibility(View.VISIBLE);
        }

        // Get total expense
        dataHelper.getTotalExpense(currentMonth, currentYear, new FirebaseDataHelper.OnTotalLoadedListener() {
            @Override
            public void onTotalLoaded(long total) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            valueExpense.setText("-" + FirebaseDataHelper.formatCurrency(total));
                        }
                    });
                }
            }
        });

        // Get expense by categories
        dataHelper.getExpenseByCategories(currentMonth, currentYear, new FirebaseDataHelper.OnCategoriesLoadedListener() {
            @Override
            public void onCategoriesLoaded(List<CategoryData> categories) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateChart(categories);
                            updateCategoryList(categories);

                            if (loadingView != null) {
                                loadingView.setVisibility(View.GONE);
                            }

                            isDataLoading = false;
                        }
                    });
                } else {
                    isDataLoading = false;
                }
            }
        });
    }

    private void updateChart(List<CategoryData> categoryDataList) {
        // Create data entries
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        for (CategoryData category : categoryDataList) {
            entries.add(new PieEntry(category.getAmount(), category.getName()));
            colors.add(category.getColor());
        }

        // If no data, add a placeholder
        if (entries.isEmpty()) {
            entries.add(new PieEntry(1, "Không có dữ liệu"));
            colors.add(Color.GRAY);
        }

        // Create dataset
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setSliceSpace(0f);

        // Create pie data
        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(donutChart));

        // Set data to chart
        donutChart.setData(data);
        donutChart.invalidate();
    }

    private void updateCategoryList(List<CategoryData> categoryDataList) {
        // Clear existing views
        categoryList.removeAllViews();

        // Add category items
        for (CategoryData category : categoryDataList) {
            View categoryItem = getLayoutInflater().inflate(R.layout.fragment_baocao_category_item, categoryList, false);

            View colorIndicator = categoryItem.findViewById(R.id.colorIndicator);
            TextView categoryName = categoryItem.findViewById(R.id.categoryName);
            TextView categoryAmount = categoryItem.findViewById(R.id.categoryAmount);

            colorIndicator.setBackgroundColor(category.getColor());
            categoryName.setText(category.getName());
            categoryAmount.setText(FirebaseDataHelper.formatCurrency(category.getAmount()));

            categoryList.addView(categoryItem);
        }

        // If no data, show a message
        if (categoryDataList.isEmpty()) {
            TextView emptyText = new TextView(getContext());
            emptyText.setText("Không có dữ liệu chi tiêu trong tháng này");
            emptyText.setTextColor(Color.WHITE);
            emptyText.setTextSize(16);
            emptyText.setGravity(Gravity.CENTER);
            emptyText.setPadding(0, 50, 0, 0);

            categoryList.addView(emptyText);
        }
    }

    private void showDropdownMenu(View anchorView, boolean isMonthly) {
        // Tránh hiển thị dropdown khi đang tải dữ liệu
        if (isDataLoading) {
            Toast.makeText(getContext(), "Đang tải dữ liệu, vui lòng đợi", Toast.LENGTH_SHORT).show();
            return;
        }

        // Inflate the dropdown layout
        View popupView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_baocao_dropdown_menu, null);

        // Create the popup window
        int width = anchorView.getWidth();
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

        // Set up items in dropdown
        TextView item1 = popupView.findViewById(R.id.item1);
        TextView item2 = popupView.findViewById(R.id.item2);
        TextView item3 = popupView.findViewById(R.id.item3);

        if (isMonthly) {
            item1.setText("Tháng 1");
            item2.setText("Tháng 2");
            item3.setText("Tháng 3");
        } else {
            item1.setText("2023");
            item2.setText("2024");
            item3.setText("2025");
        }

        // Set click listeners for items
        item1.setOnClickListener(v -> {
            if (isMonthly) {
                btnMonthly.setText("Tháng 1");
                currentMonth = 1;
            } else {
                btnYear.setText("2023");
                currentYear = 2023;
            }
            loadData();
            popupWindow.dismiss();
        });

        item2.setOnClickListener(v -> {
            if (isMonthly) {
                btnMonthly.setText("Tháng 2");
                currentMonth = 2;
            } else {
                btnYear.setText("2024");
                currentYear = 2024;
            }
            loadData();
            popupWindow.dismiss();
        });

        item3.setOnClickListener(v -> {
            if (isMonthly) {
                btnMonthly.setText("Tháng 3");
                currentMonth = 3;
            } else {
                btnYear.setText("2025");
                currentYear = 2025;
            }
            loadData();
            popupWindow.dismiss();
        });

        // Show the popup window
        popupWindow.setBackgroundDrawable(null);
        popupWindow.showAsDropDown(anchorView, 0, 0, Gravity.START);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Đảm bảo không có memory leak
        donutChart = null;
        btnMonthly = null;
        btnYear = null;
        valueExpense = null;
        categoryList = null;
        loadingView = null;
    }
}