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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FirebaseDataHelper {
    private static final String TAG = "FirebaseDataHelper";
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    private final FirebaseFirestore db;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    // Mảng màu sắc cho các danh mục
    private static final int[] CATEGORY_COLORS = {
            Color.parseColor("#4FC3F7"),  // Blue
            Color.parseColor("#FFA726"),  // Orange
            Color.parseColor("#9575CD"),  // Purple
            Color.parseColor("#EC407A"),  // Pink
            Color.parseColor("#66BB6A"),  // Green
            Color.parseColor("#FFC107"),  // Yellow
            Color.parseColor("#8D6E63"),  // Brown
            Color.parseColor("#26A69A"),  // Teal
            Color.parseColor("#EF5350"),  // Red
            Color.parseColor("#7E57C2"),  // Deep Purple
            Color.parseColor("#29B6F6"),  // Light Blue
            Color.parseColor("#26C6DA"),  // Cyan
            Color.parseColor("#9CCC65"),  // Light Green
            Color.parseColor("#FFEE58"),  // Yellow
            Color.parseColor("#FF7043"),  // Deep Orange
            Color.parseColor("#78909C")   // Blue Grey
    };

    public FirebaseDataHelper() {
        this.db = FirebaseFirestore.getInstance();
    }

    // Lấy tổng chi tiêu theo khoảng thời gian
    public void getTotalExpenseByDateRange(String startDate, String endDate, final OnTotalLoadedListener listener) {
        Log.d(TAG, "Fetching expenses from " + startDate + " to " + endDate);

        db.collection("giaodich")
                .whereEqualTo("loaigiaodich", "chi")
                .whereGreaterThanOrEqualTo("ngaygiaodich", startDate)
                .whereLessThanOrEqualTo("ngaygiaodich", endDate)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            long total = 0;
                            int count = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                count++;
                                if (document.contains("sotien")) {
                                    Long amount = document.getLong("sotien");
                                    if (amount != null) {
                                        total += amount;
                                    }
                                }
                            }
                            Log.d(TAG, "Found " + count + " expense transactions, total: " + total);
                            listener.onTotalLoaded(total);
                        } else {
                            Log.w(TAG, "Error getting expense documents: ", task.getException());
                            listener.onTotalLoaded(0);
                        }
                    }
                });
    }

    // Lấy tổng thu nhập theo khoảng thời gian
    public void getTotalIncomeByDateRange(String startDate, String endDate, final OnTotalLoadedListener listener) {
        Log.d(TAG, "Fetching income from " + startDate + " to " + endDate);

        db.collection("giaodich")
                .whereEqualTo("loaigiaodich", "thu")
                .whereGreaterThanOrEqualTo("ngaygiaodich", startDate)
                .whereLessThanOrEqualTo("ngaygiaodich", endDate)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            long total = 0;
                            int count = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                count++;
                                if (document.contains("sotien")) {
                                    Long amount = document.getLong("sotien");
                                    if (amount != null) {
                                        total += amount;
                                    }
                                }
                            }
                            Log.d(TAG, "Found " + count + " income transactions, total: " + total);
                            listener.onTotalLoaded(total);
                        } else {
                            Log.w(TAG, "Error getting income documents: ", task.getException());
                            listener.onTotalLoaded(0);
                        }
                    }
                });
    }

    // Lấy danh sách chi tiêu theo khoảng thời gian
    public void getExpenseByDateRange(String startDate, String endDate, final OnCategoriesLoadedListener listener) {
        Log.d(TAG, "Fetching expense categories from " + startDate + " to " + endDate);

        db.collection("giaodich")
                .whereEqualTo("loaigiaodich", "chi")
                .whereGreaterThanOrEqualTo("ngaygiaodich", startDate)
                .whereLessThanOrEqualTo("ngaygiaodich", endDate)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Map<String, Long> categoryMap = new HashMap<>();
                            int count = 0;

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                count++;
                                String categoryName = document.getString("tendanhmuc");
                                Long amount = document.getLong("sotien");

                                Log.d(TAG, "Document ID: " + document.getId() +
                                        ", Category: " + categoryName +
                                        ", Amount: " + amount);

                                if (categoryName != null && amount != null) {
                                    if (categoryMap.containsKey(categoryName)) {
                                        categoryMap.put(categoryName, categoryMap.get(categoryName) + amount);
                                    } else {
                                        categoryMap.put(categoryName, amount);
                                    }
                                }
                            }

                            Log.d(TAG, "Processed " + count + " expense documents, found " +
                                    categoryMap.size() + " unique categories");

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
                            Log.w(TAG, "Error getting expense category documents: ", task.getException());
                            listener.onCategoriesLoaded(new ArrayList<>());
                        }
                    }
                });
    }

    // Lấy danh sách thu nhập theo khoảng thời gian
    public void getIncomeByDateRange(String startDate, String endDate, final OnCategoriesLoadedListener listener) {
        Log.d(TAG, "Fetching income categories from " + startDate + " to " + endDate);

        db.collection("giaodich")
                .whereEqualTo("loaigiaodich", "thu")
                .whereGreaterThanOrEqualTo("ngaygiaodich", startDate)
                .whereLessThanOrEqualTo("ngaygiaodich", endDate)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Map<String, Long> categoryMap = new HashMap<>();
                            int count = 0;

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                count++;
                                String categoryName = document.getString("tendanhmuc");
                                Long amount = document.getLong("sotien");

                                Log.d(TAG, "Document ID: " + document.getId() +
                                        ", Category: " + categoryName +
                                        ", Amount: " + amount);

                                if (categoryName != null && amount != null) {
                                    if (categoryMap.containsKey(categoryName)) {
                                        categoryMap.put(categoryName, categoryMap.get(categoryName) + amount);
                                    } else {
                                        categoryMap.put(categoryName, amount);
                                    }
                                }
                            }

                            Log.d(TAG, "Processed " + count + " income documents, found " +
                                    categoryMap.size() + " unique categories");

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
                            Log.w(TAG, "Error getting income category documents: ", task.getException());
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
