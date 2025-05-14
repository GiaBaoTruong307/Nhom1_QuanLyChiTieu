package com.example.nhom1_quanlychitieu.ui.BaoCao.helper;

import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.nhom1_quanlychitieu.ui.BaoCao.model.CategoryData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FirebaseDataHelper {
    private static final String TAG = "FirebaseDataHelper";
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    private final FirebaseFirestore db;

    // Mảng màu sắc cho các danh mục
    private static final int[] CATEGORY_COLORS = {
            Color.parseColor("#4FC3F7"),  // Blue
            Color.parseColor("#FFA726"),  // Orange
            Color.parseColor("#9575CD"),  // Purple
            Color.parseColor("#EC407A"),  // Pink
            Color.parseColor("#66BB6A"),  // Green
            Color.parseColor("#FFC107"),  // Yellow
            Color.parseColor("#8D6E63"),  // Brown
            Color.parseColor("#26A69A")   // Teal
    };

    public FirebaseDataHelper() {
        this.db = FirebaseFirestore.getInstance();
    }

    // Lấy tổng chi tiêu theo tháng và năm
    public void getTotalExpense(int month, int year, final OnTotalLoadedListener listener) {
        String startDate = String.format(Locale.US, "%04d-%02d-01", year, month);
        String endDate;

        if (month == 12) {
            endDate = String.format(Locale.US, "%04d-%02d-01", year + 1, 1);
        } else {
            endDate = String.format(Locale.US, "%04d-%02d-01", year, month + 1);
        }

        db.collection("giaodich")
                .whereEqualTo("loaigiaodich", "chi")
                .whereGreaterThanOrEqualTo("ngaygiaodich", startDate)
                .whereLessThan("ngaygiaodich", endDate)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            long total = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.contains("sotien")) {
                                    Long amount = document.getLong("sotien");
                                    if (amount != null) {
                                        total += amount;
                                    }
                                }
                            }
                            listener.onTotalLoaded(total);
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                            listener.onTotalLoaded(0);
                        }
                    }
                });
    }

    // Lấy tổng thu nhập theo tháng và năm
    public void getTotalIncome(int month, int year, final OnTotalLoadedListener listener) {
        String startDate = String.format(Locale.US, "%04d-%02d-01", year, month);
        String endDate;

        if (month == 12) {
            endDate = String.format(Locale.US, "%04d-%02d-01", year + 1, 1);
        } else {
            endDate = String.format(Locale.US, "%04d-%02d-01", year, month + 1);
        }

        db.collection("giaodich")
                .whereEqualTo("loaigiaodich", "thu")
                .whereGreaterThanOrEqualTo("ngaygiaodich", startDate)
                .whereLessThan("ngaygiaodich", endDate)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            long total = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.contains("sotien")) {
                                    Long amount = document.getLong("sotien");
                                    if (amount != null) {
                                        total += amount;
                                    }
                                }
                            }
                            listener.onTotalLoaded(total);
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                            listener.onTotalLoaded(0);
                        }
                    }
                });
    }

    // Lấy danh sách chi tiêu theo danh mục
    public void getExpenseByCategories(int month, int year, final OnCategoriesLoadedListener listener) {
        String startDate = String.format(Locale.US, "%04d-%02d-01", year, month);
        String endDate;

        if (month == 12) {
            endDate = String.format(Locale.US, "%04d-%02d-01", year + 1, 1);
        } else {
            endDate = String.format(Locale.US, "%04d-%02d-01", year, month + 1);
        }

        db.collection("giaodich")
                .whereEqualTo("loaigiaodich", "chi")
                .whereGreaterThanOrEqualTo("ngaygiaodich", startDate)
                .whereLessThan("ngaygiaodich", endDate)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Map<String, Long> categoryMap = new HashMap<>();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String categoryName = document.getString("tendanhmuc");
                                Long amount = document.getLong("sotien");

                                if (categoryName != null && amount != null) {
                                    if (categoryMap.containsKey(categoryName)) {
                                        categoryMap.put(categoryName, categoryMap.get(categoryName) + amount);
                                    } else {
                                        categoryMap.put(categoryName, amount);
                                    }
                                }
                            }

                            List<CategoryData> categoryDataList = new ArrayList<>();
                            int colorIndex = 0;

                            for (Map.Entry<String, Long> entry : categoryMap.entrySet()) {
                                int color = CATEGORY_COLORS[colorIndex % CATEGORY_COLORS.length];
                                colorIndex++;

                                categoryDataList.add(new CategoryData(entry.getKey(), entry.getValue(), color));
                            }

                            // Sắp xếp danh sách theo số tiền giảm dần
                            Collections.sort(categoryDataList, new Comparator<CategoryData>() {
                                @Override
                                public int compare(CategoryData o1, CategoryData o2) {
                                    return Long.compare(o2.getAmount(), o1.getAmount());
                                }
                            });

                            listener.onCategoriesLoaded(categoryDataList);
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                            listener.onCategoriesLoaded(new ArrayList<>());
                        }
                    }
                });
    }

    // Lấy danh sách thu nhập theo danh mục
    public void getIncomeByCategories(int month, int year, final OnCategoriesLoadedListener listener) {
        String startDate = String.format(Locale.US, "%04d-%02d-01", year, month);
        String endDate;

        if (month == 12) {
            endDate = String.format(Locale.US, "%04d-%02d-01", year + 1, 1);
        } else {
            endDate = String.format(Locale.US, "%04d-%02d-01", year, month + 1);
        }

        db.collection("giaodich")
                .whereEqualTo("loaigiaodich", "thu")
                .whereGreaterThanOrEqualTo("ngaygiaodich", startDate)
                .whereLessThan("ngaygiaodich", endDate)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Map<String, Long> categoryMap = new HashMap<>();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String categoryName = document.getString("tendanhmuc");
                                Long amount = document.getLong("sotien");

                                if (categoryName != null && amount != null) {
                                    if (categoryMap.containsKey(categoryName)) {
                                        categoryMap.put(categoryName, categoryMap.get(categoryName) + amount);
                                    } else {
                                        categoryMap.put(categoryName, amount);
                                    }
                                }
                            }

                            List<CategoryData> categoryDataList = new ArrayList<>();
                            int colorIndex = 0;

                            for (Map.Entry<String, Long> entry : categoryMap.entrySet()) {
                                int color = CATEGORY_COLORS[colorIndex % CATEGORY_COLORS.length];
                                colorIndex++;

                                categoryDataList.add(new CategoryData(entry.getKey(), entry.getValue(), color));
                            }

                            // Sắp xếp danh sách theo số tiền giảm dần
                            Collections.sort(categoryDataList, new Comparator<CategoryData>() {
                                @Override
                                public int compare(CategoryData o1, CategoryData o2) {
                                    return Long.compare(o2.getAmount(), o1.getAmount());
                                }
                            });

                            listener.onCategoriesLoaded(categoryDataList);
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                            listener.onCategoriesLoaded(new ArrayList<>());
                        }
                    }
                });
    }

    // Lấy tháng hiện tại
    public int getCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH bắt đầu từ 0
    }

    // Lấy năm hiện tại
    public int getCurrentYear() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR);
    }

    // Format số tiền thành chuỗi
    public static String formatCurrency(long amount) {
        return CURRENCY_FORMAT.format(amount) + " VND";
    }

    // Interface để callback khi tổng tiền được tải
    public interface OnTotalLoadedListener {
        void onTotalLoaded(long total);
    }

    // Interface để callback khi danh sách danh mục được tải
    public interface OnCategoriesLoadedListener {
        void onCategoriesLoaded(List<CategoryData> categories);
    }
}