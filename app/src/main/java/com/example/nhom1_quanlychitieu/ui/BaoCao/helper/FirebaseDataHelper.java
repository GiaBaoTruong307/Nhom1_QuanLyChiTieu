package com.example.nhom1_quanlychitieu.ui.BaoCao.helper;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.nhom1_quanlychitieu.ui.BaoCao.model.CategoryData;
import com.example.nhom1_quanlychitieu.ui.ThongKe.model.Transaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FirebaseDataHelper {
    private static final String TAG = "FirebaseDataHelper";
    private final DatabaseReference db;

    // Mảng màu cho các danh mục
    private static final int[] CATEGORY_COLORS = {
            android.graphics.Color.rgb(255, 99, 132),
            android.graphics.Color.rgb(54, 162, 235),
            android.graphics.Color.rgb(255, 206, 86),
            android.graphics.Color.rgb(75, 192, 192),
            android.graphics.Color.rgb(153, 102, 255),
            android.graphics.Color.rgb(255, 159, 64),
            android.graphics.Color.rgb(255, 0, 0),
            android.graphics.Color.rgb(0, 255, 0),
            android.graphics.Color.rgb(0, 0, 255),
            android.graphics.Color.rgb(128, 0, 128)
    };

    // Constructor
    public FirebaseDataHelper() {
        this.db = FirebaseDatabase.getInstance().getReference();
        Log.d(TAG, "FirebaseDataHelper initialized with Realtime Database");
    }

    /**
     * Định dạng số tiền thành chuỗi có dấu phẩy ngăn cách
     */
    public static String formatCurrency(long amount) {
        DecimalFormat formatter = new DecimalFormat("#,###,### VND");
        return formatter.format(amount);
    }

    /**
     * Lấy tổng chi tiêu trong khoảng thời gian
     */
    public void getTotalExpenseByDateRange(String startDate, String endDate, final OnTotalLoadedListener listener) {
        Log.d(TAG, "Fetching expenses from " + startDate + " to " + endDate);

        try {
            // Chuyển đổi chuỗi ngày thành timestamp
            long startTimestamp = convertDateToTimestamp(startDate);
            long endTimestamp = convertDateToTimestamp(endDate) + 86400000; // Cộng thêm 1 ngày (tính đến cuối ngày)

            String userId = getCurrentUserId();
            if (userId == null) {
                listener.onTotalLoaded(0);
                return;
            }

            db.child("transactions").child(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            long total = 0;
                            int count = 0;

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                try {
                                    Transaction transaction = snapshot.getValue(Transaction.class);
                                    if (transaction != null && transaction.getAmount() < 0) { // Chi tiêu có amount < 0
                                        long timestamp = transaction.getTimestamp();
                                        if (timestamp >= startTimestamp && timestamp <= endTimestamp) {
                                            total += Math.abs(transaction.getAmount());
                                            count++;
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing transaction: " + e.getMessage());
                                }
                            }

                            Log.d(TAG, "Found " + count + " expense transactions, total: " + total);
                            listener.onTotalLoaded(total);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e(TAG, "Error getting expense data: ", databaseError.toException());
                            listener.onTotalLoaded(0);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in getTotalExpenseByDateRange: " + e.getMessage());
            listener.onTotalLoaded(0);
        }
    }

    /**
     * Lấy tổng thu nhập trong khoảng thời gian
     */
    public void getTotalIncomeByDateRange(String startDate, String endDate, final OnTotalLoadedListener listener) {
        Log.d(TAG, "Fetching income from " + startDate + " to " + endDate);

        try {
            // Chuyển đổi chuỗi ngày thành timestamp
            long startTimestamp = convertDateToTimestamp(startDate);
            long endTimestamp = convertDateToTimestamp(endDate) + 86400000; // Cộng thêm 1 ngày

            String userId = getCurrentUserId();
            if (userId == null) {
                listener.onTotalLoaded(0);
                return;
            }

            db.child("transactions").child(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            long total = 0;
                            int count = 0;

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                try {
                                    Transaction transaction = snapshot.getValue(Transaction.class);
                                    if (transaction != null && transaction.getAmount() > 0) { // Thu nhập có amount > 0
                                        long timestamp = transaction.getTimestamp();
                                        if (timestamp >= startTimestamp && timestamp <= endTimestamp) {
                                            total += transaction.getAmount();
                                            count++;
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing transaction: " + e.getMessage());
                                }
                            }

                            Log.d(TAG, "Found " + count + " income transactions, total: " + total);
                            listener.onTotalLoaded(total);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e(TAG, "Error getting income data: ", databaseError.toException());
                            listener.onTotalLoaded(0);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in getTotalIncomeByDateRange: " + e.getMessage());
            listener.onTotalLoaded(0);
        }
    }

    /**
     * Lấy danh sách chi tiêu theo danh mục trong khoảng thời gian
     */
    public void getExpenseByDateRange(String startDate, String endDate, final OnCategoriesLoadedListener listener) {
        Log.d(TAG, "Fetching expense categories from " + startDate + " to " + endDate);

        try {
            // Chuyển đổi chuỗi ngày thành timestamp
            long startTimestamp = convertDateToTimestamp(startDate);
            long endTimestamp = convertDateToTimestamp(endDate) + 86400000; // Cộng thêm 1 ngày

            String userId = getCurrentUserId();
            if (userId == null) {
                listener.onCategoriesLoaded(new ArrayList<>());
                return;
            }

            db.child("transactions").child(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Map<String, Long> categoryMap = new HashMap<>();
                            int count = 0;

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                try {
                                    Transaction transaction = snapshot.getValue(Transaction.class);
                                    if (transaction != null && transaction.getAmount() < 0) { // Chi tiêu có amount < 0
                                        long timestamp = transaction.getTimestamp();
                                        if (timestamp >= startTimestamp && timestamp <= endTimestamp) {
                                            String categoryName = transaction.getCategory();
                                            if (categoryName == null || categoryName.isEmpty()) {
                                                categoryName = "Khác";
                                            }

                                            long amount = Math.abs(transaction.getAmount());

                                            if (categoryMap.containsKey(categoryName)) {
                                                categoryMap.put(categoryName, categoryMap.get(categoryName) + amount);
                                            } else {
                                                categoryMap.put(categoryName, amount);
                                            }
                                            count++;
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing transaction: " + e.getMessage());
                                }
                            }

                            Log.d(TAG, "Processed " + count + " expense documents, found " +
                                    categoryMap.size() + " unique categories");

                            // Chuyển đổi Map thành List<CategoryData>
                            List<CategoryData> categoryDataList = new ArrayList<>();
                            int colorIndex = 0;

                            for (Map.Entry<String, Long> entry : categoryMap.entrySet()) {
                                int color = CATEGORY_COLORS[colorIndex % CATEGORY_COLORS.length];
                                colorIndex++;

                                categoryDataList.add(new CategoryData(entry.getKey(), entry.getValue(), color));
                            }

                            // Sắp xếp danh sách theo số tiền giảm dần
                            Collections.sort(categoryDataList, (o1, o2) ->
                                    Long.compare(o2.getAmount(), o1.getAmount()));

                            listener.onCategoriesLoaded(categoryDataList);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e(TAG, "Error getting expense category data: ", databaseError.toException());
                            listener.onCategoriesLoaded(new ArrayList<>());
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in getExpenseByDateRange: " + e.getMessage());
            listener.onCategoriesLoaded(new ArrayList<>());
        }
    }

    /**
     * Lấy danh sách thu nhập theo danh mục trong khoảng thời gian
     */
    public void getIncomeByDateRange(String startDate, String endDate, final OnCategoriesLoadedListener listener) {
        Log.d(TAG, "Fetching income categories from " + startDate + " to " + endDate);

        try {
            // Chuyển đổi chuỗi ngày thành timestamp
            long startTimestamp = convertDateToTimestamp(startDate);
            long endTimestamp = convertDateToTimestamp(endDate) + 86400000; // Cộng thêm 1 ngày

            String userId = getCurrentUserId();
            if (userId == null) {
                listener.onCategoriesLoaded(new ArrayList<>());
                return;
            }

            db.child("transactions").child(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Map<String, Long> categoryMap = new HashMap<>();
                            int count = 0;

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                try {
                                    Transaction transaction = snapshot.getValue(Transaction.class);
                                    if (transaction != null && transaction.getAmount() > 0) { // Thu nhập có amount > 0
                                        long timestamp = transaction.getTimestamp();
                                        if (timestamp >= startTimestamp && timestamp <= endTimestamp) {
                                            String categoryName = transaction.getCategory();
                                            if (categoryName == null || categoryName.isEmpty()) {
                                                categoryName = "Khác";
                                            }

                                            long amount = transaction.getAmount();

                                            if (categoryMap.containsKey(categoryName)) {
                                                categoryMap.put(categoryName, categoryMap.get(categoryName) + amount);
                                            } else {
                                                categoryMap.put(categoryName, amount);
                                            }
                                            count++;
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing transaction: " + e.getMessage());
                                }
                            }

                            Log.d(TAG, "Processed " + count + " income documents, found " +
                                    categoryMap.size() + " unique categories");

                            // Chuyển đổi Map thành List<CategoryData>
                            List<CategoryData> categoryDataList = new ArrayList<>();
                            int colorIndex = 0;

                            for (Map.Entry<String, Long> entry : categoryMap.entrySet()) {
                                int color = CATEGORY_COLORS[colorIndex % CATEGORY_COLORS.length];
                                colorIndex++;

                                categoryDataList.add(new CategoryData(entry.getKey(), entry.getValue(), color));
                            }

                            // Sắp xếp danh sách theo số tiền giảm dần
                            Collections.sort(categoryDataList, (o1, o2) ->
                                    Long.compare(o2.getAmount(), o1.getAmount()));

                            listener.onCategoriesLoaded(categoryDataList);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e(TAG, "Error getting income category data: ", databaseError.toException());
                            listener.onCategoriesLoaded(new ArrayList<>());
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in getIncomeByDateRange: " + e.getMessage());
            listener.onCategoriesLoaded(new ArrayList<>());
        }
    }

    /**
     * Lấy dữ liệu chi tiêu theo ngày trong khoảng thời gian
     */
    public void getExpenseByDayInRange(String startDate, String endDate, final OnDailyDataLoadedListener listener) {
        Log.d(TAG, "Fetching daily expenses from " + startDate + " to " + endDate);

        try {
            // Chuyển đổi chuỗi ngày thành timestamp
            long startTimestamp = convertDateToTimestamp(startDate);
            long endTimestamp = convertDateToTimestamp(endDate) + 86400000; // Cộng thêm 1 ngày

            String userId = getCurrentUserId();
            if (userId == null) {
                listener.onDailyDataLoaded(new ArrayList<>(), new ArrayList<>());
                return;
            }

            db.child("transactions").child(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            // Tạo map để lưu trữ tổng chi tiêu theo ngày
                            Map<String, Long> dailyExpenseMap = new HashMap<>();

                            // Tạo danh sách các ngày trong khoảng thời gian
                            List<String> dateLabels = generateDateLabels(startDate, endDate);

                            // Khởi tạo giá trị 0 cho tất cả các ngày
                            for (String date : dateLabels) {
                                dailyExpenseMap.put(date, 0L);
                            }

                            // Xử lý dữ liệu giao dịch
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                try {
                                    Transaction transaction = snapshot.getValue(Transaction.class);
                                    if (transaction != null && transaction.getAmount() < 0) { // Chi tiêu có amount < 0
                                        long timestamp = transaction.getTimestamp();
                                        if (timestamp >= startTimestamp && timestamp <= endTimestamp) {
                                            // Chuyển timestamp thành chuỗi ngày
                                            String dateStr = convertTimestampToDateString(timestamp);

                                            // Cập nhật tổng chi tiêu cho ngày đó
                                            if (dailyExpenseMap.containsKey(dateStr)) {
                                                dailyExpenseMap.put(dateStr,
                                                        dailyExpenseMap.get(dateStr) + Math.abs(transaction.getAmount()));
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing transaction: " + e.getMessage());
                                }
                            }

                            // Chuyển map thành danh sách giá trị theo thứ tự ngày
                            List<Long> expenseValues = new ArrayList<>();
                            for (String date : dateLabels) {
                                expenseValues.add(dailyExpenseMap.get(date));
                            }

                            Log.d(TAG, "Processed daily expenses for " + dateLabels.size() + " days");
                            listener.onDailyDataLoaded(dateLabels, expenseValues);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e(TAG, "Error getting daily expense data: ", databaseError.toException());
                            listener.onDailyDataLoaded(new ArrayList<>(), new ArrayList<>());
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in getExpenseByDayInRange: " + e.getMessage());
            listener.onDailyDataLoaded(new ArrayList<>(), new ArrayList<>());
        }
    }

    /**
     * Lấy dữ liệu thu nhập theo ngày trong khoảng thời gian
     */
    public void getIncomeByDayInRange(String startDate, String endDate, final OnDailyDataLoadedListener listener) {
        Log.d(TAG, "Fetching daily income from " + startDate + " to " + endDate);

        try {
            // Chuyển đổi chuỗi ngày thành timestamp
            long startTimestamp = convertDateToTimestamp(startDate);
            long endTimestamp = convertDateToTimestamp(endDate) + 86400000; // Cộng thêm 1 ngày

            String userId = getCurrentUserId();
            if (userId == null) {
                listener.onDailyDataLoaded(new ArrayList<>(), new ArrayList<>());
                return;
            }

            db.child("transactions").child(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            // Tạo map để lưu trữ tổng thu nhập theo ngày
                            Map<String, Long> dailyIncomeMap = new HashMap<>();

                            // Tạo danh sách các ngày trong khoảng thời gian
                            List<String> dateLabels = generateDateLabels(startDate, endDate);

                            // Khởi tạo giá trị 0 cho tất cả các ngày
                            for (String date : dateLabels) {
                                dailyIncomeMap.put(date, 0L);
                            }

                            // Xử lý dữ liệu giao dịch
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                try {
                                    Transaction transaction = snapshot.getValue(Transaction.class);
                                    if (transaction != null && transaction.getAmount() > 0) { // Thu nhập có amount > 0
                                        long timestamp = transaction.getTimestamp();
                                        if (timestamp >= startTimestamp && timestamp <= endTimestamp) {
                                            // Chuyển timestamp thành chuỗi ngày
                                            String dateStr = convertTimestampToDateString(timestamp);

                                            // Cập nhật tổng thu nhập cho ngày đó
                                            if (dailyIncomeMap.containsKey(dateStr)) {
                                                dailyIncomeMap.put(dateStr,
                                                        dailyIncomeMap.get(dateStr) + transaction.getAmount());
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing transaction: " + e.getMessage());
                                }
                            }

                            // Chuyển map thành danh sách giá trị theo thứ tự ngày
                            List<Long> incomeValues = new ArrayList<>();
                            for (String date : dateLabels) {
                                incomeValues.add(dailyIncomeMap.get(date));
                            }

                            Log.d(TAG, "Processed daily income for " + dateLabels.size() + " days");
                            listener.onDailyDataLoaded(dateLabels, incomeValues);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e(TAG, "Error getting daily income data: ", databaseError.toException());
                            listener.onDailyDataLoaded(new ArrayList<>(), new ArrayList<>());
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in getIncomeByDayInRange: " + e.getMessage());
            listener.onDailyDataLoaded(new ArrayList<>(), new ArrayList<>());
        }
    }

    /**
     * Kiểm tra kết nối Firebase
     */
    public void testFirebaseConnection(final OnConnectionTestListener listener) {
        try {
            String userId = getCurrentUserId();
            if (userId == null) {
                listener.onConnectionTested(false, "Người dùng chưa đăng nhập");
                return;
            }

            db.child("transactions").child(userId).limitToFirst(1)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Log.d(TAG, "Firebase connection test: SUCCESS");
                            Log.d(TAG, "Documents available: " + dataSnapshot.getChildrenCount());

                            if (dataSnapshot.getChildrenCount() > 0) {
                                DataSnapshot firstChild = dataSnapshot.getChildren().iterator().next();
                                Log.d(TAG, "Sample document: " + firstChild.getValue());
                            }

                            listener.onConnectionTested(true, null);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e(TAG, "Firebase connection test: FAILED", databaseError.toException());
                            listener.onConnectionTested(false, databaseError.getMessage());
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in testFirebaseConnection: " + e.getMessage());
            listener.onConnectionTested(false, e.getMessage());
        }
    }

    /**
     * Chuyển đổi chuỗi ngày thành timestamp
     */
    private long convertDateToTimestamp(String dateString) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = format.parse(dateString);
            return date != null ? date.getTime() : 0;
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date: " + dateString, e);
            return 0;
        }
    }

    /**
     * Chuyển đổi timestamp thành chuỗi ngày
     */
    private String convertTimestampToDateString(long timestamp) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return format.format(new Date(timestamp));
    }

    /**
     * Tạo danh sách các ngày trong khoảng thời gian
     */
    private List<String> generateDateLabels(String startDate, String endDate) {
        List<String> dateLabels = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        try {
            Date start = format.parse(startDate);
            Date end = format.parse(endDate);

            if (start != null && end != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(start);

                while (!calendar.getTime().after(end)) {
                    dateLabels.add(format.format(calendar.getTime()));
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error generating date labels", e);
        }

        return dateLabels;
    }

    /**
     * Lấy ID người dùng hiện tại
     */
    private String getCurrentUserId() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getUid();
        }
        Log.e(TAG, "No user logged in");
        return null;
    }

    /**
     * Interface cho callback khi tải tổng số tiền
     */
    public interface OnTotalLoadedListener {
        void onTotalLoaded(long total);
    }

    /**
     * Interface cho callback khi tải danh sách danh mục
     */
    public interface OnCategoriesLoadedListener {
        void onCategoriesLoaded(List<CategoryData> categories);
    }

    /**
     * Interface cho callback khi tải dữ liệu theo ngày
     */
    public interface OnDailyDataLoadedListener {
        void onDailyDataLoaded(List<String> dateLabels, List<Long> values);
    }

    /**
     * Interface cho callback khi kiểm tra kết nối
     */
    public interface OnConnectionTestListener {
        void onConnectionTested(boolean success, String errorMessage);
    }
}
