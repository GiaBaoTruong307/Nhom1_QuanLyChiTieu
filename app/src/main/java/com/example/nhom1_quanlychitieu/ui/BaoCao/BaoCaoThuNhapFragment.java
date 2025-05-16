package com.example.nhom1_quanlychitieu.ui.BaoCao;

import android.animation.ValueAnimator;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.nhom1_quanlychitieu.R;
import com.example.nhom1_quanlychitieu.ui.BaoCao.model.CategoryData;
import com.example.nhom1_quanlychitieu.ui.BaoCao.helper.FirebaseDataHelper;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BaoCaoThuNhapFragment extends Fragment {
    private static final String TAG = "BaoCaoThuNhapFragment";

    private Button btnStartDate, btnEndDate, btnApplyFilter;
    private TextView valueIncome;
    private LinearLayout categoryList;
    private FirebaseDataHelper dataHelper;
    private View loadingView;
    private PieChart pieChart;

    private Calendar startDateCalendar;
    private Calendar endDateCalendar;
    private boolean isDataLoading = false;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    // Mảng màu đẹp hơn cho biểu đồ
    private static final int[] VIBRANT_COLORS = {
            Color.rgb(46, 204, 113),  // Xanh lá
            Color.rgb(52, 152, 219),  // Xanh dương
            Color.rgb(155, 89, 182),  // Tím
            Color.rgb(241, 196, 15),  // Vàng
            Color.rgb(230, 126, 34),  // Cam
            Color.rgb(231, 76, 60),   // Đỏ
            Color.rgb(26, 188, 156),  // Ngọc lam
            Color.rgb(41, 128, 185),  // Xanh dương đậm
            Color.rgb(142, 68, 173),  // Tím đậm
            Color.rgb(243, 156, 18)   // Cam vàng
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_baocao_thunhap, container, false);

            // Initialize views
            btnStartDate = view.findViewById(R.id.btnStartDate);
            btnEndDate = view.findViewById(R.id.btnEndDate);
            btnApplyFilter = view.findViewById(R.id.btnApplyFilter);
            valueIncome = view.findViewById(R.id.valueIncome);
            categoryList = view.findViewById(R.id.categoryList);
            loadingView = view.findViewById(R.id.loadingView);
            pieChart = view.findViewById(R.id.pieChart);

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

            // Setup pie chart
            setupPieChart();

            // Load data
            loadData();

            return view;
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateView", e);
            Toast.makeText(getContext(), "Lỗi khi tạo giao diện thu nhập: " + e.getMessage(), Toast.LENGTH_LONG).show();
            // Trả về view trống nếu có lỗi
            return new View(getContext());
        }
    }

    private void setupPieChart() {
        try {
            pieChart.setUsePercentValues(true);
            pieChart.getDescription().setEnabled(false);
            pieChart.setExtraOffsets(20, 20, 20, 20); // Tăng offset để biểu đồ không bị cắt

            // Cấu hình hiệu ứng và tương tác
            pieChart.setDragDecelerationFrictionCoef(0.95f);
            pieChart.setDrawHoleEnabled(true);
            pieChart.setHoleColor(Color.parseColor("#121418"));
            pieChart.setTransparentCircleColor(Color.WHITE);
            pieChart.setTransparentCircleAlpha(110);
            pieChart.setHoleRadius(50f); // Tăng kích thước lỗ giữa
            pieChart.setTransparentCircleRadius(55f);

            // Cấu hình văn bản ở giữa
            pieChart.setDrawCenterText(true);
            pieChart.setCenterText("Thu nhập");
            pieChart.setCenterTextSize(22f); // Tăng kích thước chữ
            pieChart.setCenterTextColor(Color.WHITE);
            pieChart.setCenterTextTypeface(Typeface.DEFAULT_BOLD); // Chữ đậm

            // Cấu hình tương tác
            pieChart.setRotationAngle(0);
            pieChart.setRotationEnabled(true);
            pieChart.setHighlightPerTapEnabled(true);

            // Cấu hình nhãn
            pieChart.setEntryLabelColor(Color.WHITE);
            pieChart.setEntryLabelTextSize(14f); // Tăng kích thước chữ
            pieChart.setEntryLabelTypeface(Typeface.DEFAULT_BOLD); // Chữ đậm
            pieChart.setNoDataText("Không có dữ liệu");
            pieChart.setNoDataTextColor(Color.WHITE);

            // Cấu hình chú thích
            Legend legend = pieChart.getLegend();
            legend.setEnabled(true);
            legend.setTextColor(Color.WHITE);
            legend.setTextSize(14f); // Tăng kích thước chữ
            legend.setForm(Legend.LegendForm.CIRCLE);
            legend.setFormSize(12f); // Tăng kích thước biểu tượng
            legend.setXEntrySpace(10f); // Tăng khoảng cách giữa các mục
            legend.setYEntrySpace(8f);
            legend.setWordWrapEnabled(true);
            legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
            legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
            legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            legend.setDrawInside(false);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up pie chart", e);
        }
    }

    private void updatePieChart(List<CategoryData> categories) {
        try {
            if (categories == null || categories.isEmpty()) {
                pieChart.setVisibility(View.GONE);
                return;
            }

            pieChart.setVisibility(View.VISIBLE);
            ArrayList<PieEntry> entries = new ArrayList<>();
            ArrayList<Integer> colors = new ArrayList<>();

            // Hiển thị tất cả các danh mục, không gộp vào mục "Khác"
            for (int i = 0; i < categories.size(); i++) {
                CategoryData category = categories.get(i);
                entries.add(new PieEntry(category.getAmount(), category.getName()));
                colors.add(VIBRANT_COLORS[i % VIBRANT_COLORS.length]); // Sử dụng màu sắc mới
            }

            PieDataSet dataSet = new PieDataSet(entries, "");
            dataSet.setSliceSpace(5f); // Tăng khoảng cách giữa các phần
            dataSet.setSelectionShift(10f); // Tăng hiệu ứng khi chọn
            dataSet.setColors(colors);
            dataSet.setValueLineColor(Color.WHITE);
            dataSet.setValueLinePart1OffsetPercentage(80f);
            dataSet.setValueLinePart1Length(0.3f);
            dataSet.setValueLinePart2Length(0.4f);
            dataSet.setValueLineWidth(2f); // Tăng độ dày đường kẻ
            dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
            dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
            dataSet.setValueTextColor(Color.WHITE);
            dataSet.setValueTextSize(14f); // Tăng kích thước chữ
            dataSet.setValueTypeface(Typeface.DEFAULT_BOLD); // Chữ đậm
            dataSet.setIconsOffset(new MPPointF(0, -10)); // Điều chỉnh vị trí biểu tượng

            // Định dạng nhãn giá trị
            PieData data = new PieData(dataSet);
            data.setValueFormatter(new PercentFormatter(pieChart));
            data.setValueTextSize(14f); // Tăng kích thước chữ
            data.setValueTextColor(Color.WHITE);

            pieChart.setData(data);
            pieChart.highlightValues(null);

            // Thêm animation
            pieChart.animateY(1400, Easing.EaseInOutQuad);

            pieChart.invalidate();
        } catch (Exception e) {
            Log.e(TAG, "Error updating pie chart", e);
        }
    }

    private void updateDateButtonsText() {
        try {
            btnStartDate.setText(dateFormat.format(startDateCalendar.getTime()));
            btnEndDate.setText(dateFormat.format(endDateCalendar.getTime()));
        } catch (Exception e) {
            Log.e(TAG, "Error updating date buttons", e);
        }
    }

    private void showDatePicker(final boolean isStartDate) {
        try {
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
        } catch (Exception e) {
            Log.e(TAG, "Error showing date picker", e);
            Toast.makeText(getContext(), "Không thể hiển thị bộ chọn ngày", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadData() {
        try {
            // Tránh tải dữ liệu nhiều lần
            if (isDataLoading) return;
            isDataLoading = true;

            // Show loading state
            valueIncome.setText("Đang tải...");
            categoryList.removeAllViews();
            pieChart.setVisibility(View.INVISIBLE);

            if (loadingView != null) {
                loadingView.setVisibility(View.VISIBLE);
            }

            // Format dates for API
            String startDate = apiDateFormat.format(startDateCalendar.getTime());
            String endDate = apiDateFormat.format(endDateCalendar.getTime());

            Log.d(TAG, "Loading income data from " + startDate + " to " + endDate);

            // Get total income
            dataHelper.getTotalIncomeByDateRange(startDate, endDate, new FirebaseDataHelper.OnTotalLoadedListener() {
                @Override
                public void onTotalLoaded(long total) {
                    if (getActivity() != null && isAdded()) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Log.d(TAG, "Total income loaded: " + total);
                                    // Thêm animation cho số tiền
                                    animateTextView(0, total, valueIncome, "+");
                                } catch (Exception e) {
                                    Log.e(TAG, "Error updating income value", e);
                                }
                            }
                        });
                    }
                }
            });

            // Get income by categories
            dataHelper.getIncomeByDateRange(startDate, endDate, new FirebaseDataHelper.OnCategoriesLoadedListener() {
                @Override
                public void onCategoriesLoaded(List<CategoryData> categories) {
                    if (getActivity() != null && isAdded()) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Log.d(TAG, "Categories loaded: " + categories.size());
                                    updateCategoryList(categories);
                                    updatePieChart(categories);

                                    if (loadingView != null) {
                                        loadingView.setVisibility(View.GONE);
                                    }

                                    pieChart.setVisibility(View.VISIBLE);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error updating category list", e);
                                } finally {
                                    isDataLoading = false;
                                }
                            }
                        });
                    } else {
                        isDataLoading = false;
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error loading data", e);
            isDataLoading = false;
            if (loadingView != null) {
                loadingView.setVisibility(View.GONE);
            }
            if (getContext() != null) {
                Toast.makeText(getContext(), "Lỗi khi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Thêm animation cho số tiền
    private void animateTextView(long initialValue, long finalValue, final TextView textView, final String prefix) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(initialValue, finalValue);
        valueAnimator.setDuration(1500);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                textView.setText(prefix + FirebaseDataHelper.formatCurrency((long) animatedValue));
            }
        });
        valueAnimator.start();
    }

    private void updateCategoryList(List<CategoryData> categoryDataList) {
        try {
            // Clear existing views
            categoryList.removeAllViews();

            // Calculate total amount for percentage calculation
            long totalAmount = 0;
            for (CategoryData category : categoryDataList) {
                totalAmount += category.getAmount();
            }

            Log.d(TAG, "Updating category list with " + categoryDataList.size() + " categories, total: " + totalAmount);

            // Add category items
            for (int i = 0; i < categoryDataList.size(); i++) {
                CategoryData category = categoryDataList.get(i);
                View categoryItem = getLayoutInflater().inflate(R.layout.fragment_baocao_category_item, categoryList, false);

                View colorIndicator = categoryItem.findViewById(R.id.colorIndicator);
                TextView categoryName = categoryItem.findViewById(R.id.categoryName);
                TextView categoryAmount = categoryItem.findViewById(R.id.categoryAmount);
                TextView categoryPercentage = categoryItem.findViewById(R.id.categoryPercentage);

                // Calculate percentage
                int percentage = totalAmount > 0 ? (int) ((category.getAmount() * 100) / totalAmount) : 0;

                Log.d(TAG, "Category: " + category.getName() + ", Amount: " + category.getAmount() + ", Percentage: " + percentage + "%");

                // Sử dụng màu sắc mới
                colorIndicator.setBackgroundColor(VIBRANT_COLORS[i % VIBRANT_COLORS.length]);
                categoryName.setText(category.getName());
                categoryAmount.setText(FirebaseDataHelper.formatCurrency(category.getAmount()));
                categoryPercentage.setText(percentage + "%");

                // Thêm animation cho item
                categoryItem.setAlpha(0f);
                categoryItem.animate()
                        .alpha(1f)
                        .setDuration(300)
                        .setStartDelay(i * 100)
                        .start();

                categoryList.addView(categoryItem);
            }

            // If no data, show a message
            if (categoryDataList.isEmpty()) {
                TextView emptyText = new TextView(getContext());
                emptyText.setText("Không có dữ liệu thu nhập trong khoảng thời gian này");
                emptyText.setTextColor(Color.WHITE);
                emptyText.setTextSize(16);
                emptyText.setGravity(Gravity.CENTER);
                emptyText.setPadding(0, 50, 0, 0);

                Log.d(TAG, "No income data found, showing empty message");

                categoryList.addView(emptyText);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating category list", e);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Lỗi khi cập nhật danh sách danh mục", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Đảm bảo không có memory leak
        btnStartDate = null;
        btnEndDate = null;
        btnApplyFilter = null;
        valueIncome = null;
        categoryList = null;
        loadingView = null;
        pieChart = null;
    }
}
