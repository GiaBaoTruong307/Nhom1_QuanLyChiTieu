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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.nhom1_quanlychitieu.R;
/*import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.ArrayList;
import java.util.List;*/

public class ExpenseFragment extends Fragment {

    /*private PieChart donutChart;*/
    private Button btnMonthly, btnYear;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_baocao_chitieu, container, false);

        // Initialize views
        /*donutChart = view.findViewById(R.id.donutChart);*/
        btnMonthly = view.findViewById(R.id.btnMonthly);
        btnYear = view.findViewById(R.id.btnYear);

        // Set up click listeners for filter buttons
        btnMonthly.setOnClickListener(v -> showDropdownMenu(v, true));
        btnYear.setOnClickListener(v -> showDropdownMenu(v, false));

        // Set up the donut chart
        /*setupDonutChart();*/

        return view;
    }

    /*private void setupDonutChart() {
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

        // Create data entries
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(40, "Gia đình"));
        entries.add(new PieEntry(47, "Xã giao"));
        entries.add(new PieEntry(23, "Riêng tôi"));
        entries.add(new PieEntry(20, "Thú cưng"));

        // Create dataset
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(
                Color.parseColor("#4FC3F7"),  // Blue - Gia đình
                Color.parseColor("#FFA726"),  // Orange - Xã giao
                Color.parseColor("#9575CD"),  // Purple - Riêng tôi
                Color.parseColor("#EC407A")   // Pink - Thú cưng
        );
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
    }*/

    private void showDropdownMenu(View anchorView, boolean isMonthly) {
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
            } else {
                btnYear.setText("2023");
            }
            popupWindow.dismiss();
        });

        item2.setOnClickListener(v -> {
            if (isMonthly) {
                btnMonthly.setText("Tháng 2");
            } else {
                btnYear.setText("2024");
            }
            popupWindow.dismiss();
        });

        item3.setOnClickListener(v -> {
            if (isMonthly) {
                btnMonthly.setText("Tháng 3");
            } else {
                btnYear.setText("2025");
            }
            popupWindow.dismiss();
        });

        // Show the popup window
        popupWindow.setBackgroundDrawable(null);
        popupWindow.showAsDropDown(anchorView, 0, 0, Gravity.START);
    }
}