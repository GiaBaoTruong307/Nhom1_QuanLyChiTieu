package com.example.nhom1_quanlychitieu.ui.BaoCao;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.nhom1_quanlychitieu.R;
import com.example.nhom1_quanlychitieu.ui.BaoCao.helper.FirebaseDataHelper;
import com.example.nhom1_quanlychitieu.ui.BaoCao.model.CategoryData;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ExpenseFragment extends Fragment {
    private static final String TAG = "ExpenseFragment";

    private Button btnStartDate, btnEndDate, btnApplyFilter;
    private TextView valueExpense;
    private LinearLayout categoryList;
    private FirebaseDataHelper dataHelper;
    private View loadingView;

    private Calendar startDateCalendar;
    private Calendar endDateCalendar;
    private boolean isDataLoading = false;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_baocao_chitieu, container, false);

        // Initialize views
        btnStartDate = view.findViewById(R.id.btnStartDate);
        btnEndDate = view.findViewById(R.id.btnEndDate);
        btnApplyFilter = view.findViewById(R.id.btnApplyFilter);
        valueExpense = view.findViewById(R.id.valueExpense);
        categoryList = view.findViewById(R.id.categoryList);
        loadingView = view.findViewById(R.id.loadingView);

        // Initialize data helper
        dataHelper = new FirebaseDataHelper();

        // Initialize date calendars
        startDateCalendar = Calendar.getInstance();
        endDateCalendar = Calendar.getInstance();

        // Set start date to first day of current month
        startDateCalendar.set(Calendar.DAY_OF_MONTH, 1);

        // Update button text
        updateDateButtonsText();

        // Set up click listeners for date buttons
        btnStartDate.setOnClickListener(v -> showDatePicker(true));
        btnEndDate.setOnClickListener(v -> showDatePicker(false));

        // Set up click listener for apply filter button
        btnApplyFilter.setOnClickListener(v -> loadData());

        // Load data
        loadData();

        return view;
    }

    private void updateDateButtonsText() {
        btnStartDate.setText(dateFormat.format(startDateCalendar.getTime()));
        btnEndDate.setText(dateFormat.format(endDateCalendar.getTime()));
    }

    private void showDatePicker(final boolean isStartDate) {
        Calendar calendar = isStartDate ? startDateCalendar : endDateCalendar;

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        if (isStartDate) {
                            startDateCalendar.set(year, month, dayOfMonth);
                            // Ensure start date is not after end date
                            if (startDateCalendar.after(endDateCalendar)) {
                                Toast.makeText(getContext(), "Ngày bắt đầu không thể sau ngày kết thúc", Toast.LENGTH_SHORT).show();
                                startDateCalendar.setTime(endDateCalendar.getTime());
                            }
                        } else {
                            endDateCalendar.set(year, month, dayOfMonth);
                            // Ensure end date is not before start date
                            if (endDateCalendar.before(startDateCalendar)) {
                                Toast.makeText(getContext(), "Ngày kết thúc không thể trước ngày bắt đầu", Toast.LENGTH_SHORT).show();
                                endDateCalendar.setTime(startDateCalendar.getTime());
                            }
                        }
                        updateDateButtonsText();
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
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

        // Format dates for API
        String startDate = apiDateFormat.format(startDateCalendar.getTime());
        String endDate = apiDateFormat.format(endDateCalendar.getTime());

        Log.d(TAG, "Loading expense data from " + startDate + " to " + endDate);

        // Get total expense
        dataHelper.getTotalExpenseByDateRange(startDate, endDate, new FirebaseDataHelper.OnTotalLoadedListener() {
            @Override
            public void onTotalLoaded(long total) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Total expense loaded: " + total);
                            valueExpense.setText("-" + FirebaseDataHelper.formatCurrency(total));
                        }
                    });
                }
            }
        });

        // Get expense by categories
        dataHelper.getExpenseByDateRange(startDate, endDate, new FirebaseDataHelper.OnCategoriesLoadedListener() {
            @Override
            public void onCategoriesLoaded(List<CategoryData> categories) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Categories loaded: " + categories.size());
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

    private void updateCategoryList(List<CategoryData> categoryDataList) {
        // Clear existing views
        categoryList.removeAllViews();

        // Calculate total amount for percentage calculation
        long totalAmount = 0;
        for (CategoryData category : categoryDataList) {
            totalAmount += category.getAmount();
        }

        Log.d(TAG, "Updating category list with " + categoryDataList.size() + " categories, total: " + totalAmount);

        // Add category items
        for (CategoryData category : categoryDataList) {
            View categoryItem = getLayoutInflater().inflate(R.layout.fragment_baocao_category_item, categoryList, false);

            View colorIndicator = categoryItem.findViewById(R.id.colorIndicator);
            TextView categoryName = categoryItem.findViewById(R.id.categoryName);
            TextView categoryAmount = categoryItem.findViewById(R.id.categoryAmount);
            TextView categoryPercentage = categoryItem.findViewById(R.id.categoryPercentage);

            // Calculate percentage
            int percentage = totalAmount > 0 ? (int) ((category.getAmount() * 100) / totalAmount) : 0;

            Log.d(TAG, "Category: " + category.getName() + ", Amount: " + category.getAmount() + ", Percentage: " + percentage + "%");

            colorIndicator.setBackgroundColor(category.getColor());
            categoryName.setText(category.getName());
            categoryAmount.setText(FirebaseDataHelper.formatCurrency(category.getAmount()));
            categoryPercentage.setText(percentage + "%");

            categoryList.addView(categoryItem);
        }

        // If no data, show a message
        if (categoryDataList.isEmpty()) {
            TextView emptyText = new TextView(getContext());
            emptyText.setText("Không có dữ liệu chi tiêu trong khoảng thời gian này");
            emptyText.setTextColor(Color.WHITE);
            emptyText.setTextSize(16);
            emptyText.setGravity(Gravity.CENTER);
            emptyText.setPadding(0, 50, 0, 0);

            Log.d(TAG, "No expense data found, showing empty message");

            categoryList.addView(emptyText);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Đảm bảo không có memory leak
        btnStartDate = null;
        btnEndDate = null;
        btnApplyFilter = null;
        valueExpense = null;
        categoryList = null;
        loadingView = null;
    }
}
