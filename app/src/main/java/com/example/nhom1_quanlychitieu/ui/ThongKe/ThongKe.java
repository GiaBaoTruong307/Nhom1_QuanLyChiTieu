package com.example.nhom1_quanlychitieu.ui.ThongKe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom1_quanlychitieu.R;
import com.example.nhom1_quanlychitieu.model.Category;
import com.example.nhom1_quanlychitieu.model.Transaction;
import com.example.nhom1_quanlychitieu.model.Wallet;
import com.example.nhom1_quanlychitieu.ui.QuanLyChungThuc.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ThongKe extends Fragment implements BottomNavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "ThongKe";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private static final SimpleDateFormat DAY_OF_WEEK_FORMAT = new SimpleDateFormat("EEEE", new Locale("vi", "VN"));
    private static final DecimalFormat AMOUNT_FORMAT = new DecimalFormat("#,###,###");

    // UI components
    private TextView tvGreeting, tvTotalAmount, tvNoTransactions, tvUserName;
    private TextView tvIncomeAmount, tvExpenseAmount;
    private ProgressBar progressIncome, progressExpense;
    private RecyclerView rvTransactions;
    private ImageButton btnLogout;
    private FloatingActionButton fabAddTransaction, btnManageCategories;
    private BottomNavigationView bottomNavigation;

    // Data
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private final List<Transaction> transactions = new ArrayList<>();
    private final List<Category> categories = new ArrayList<>();
    private final List<Wallet> wallets = new ArrayList<>();
    private TransactionAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_thongke, container, false);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Ánh xạ các thành phần giao diện
        initializeViews(view);

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Thiết lập sự kiện
        setupEventListeners();

        return view;
    }

    private void initializeViews(View view) {
        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvTotalAmount = view.findViewById(R.id.tvTotalAmount);
        tvNoTransactions = view.findViewById(R.id.tvNoTransactions);
        tvIncomeAmount = view.findViewById(R.id.tvIncomeAmount);
        tvExpenseAmount = view.findViewById(R.id.tvExpenseAmount);
        progressIncome = view.findViewById(R.id.progressIncome);
        progressExpense = view.findViewById(R.id.progressExpense);
        rvTransactions = view.findViewById(R.id.rvTransactions);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnManageCategories = view.findViewById(R.id.btnManageCategories);
        fabAddTransaction = view.findViewById(R.id.fabAddTransaction);
        bottomNavigation = view.findViewById(R.id.bottomNavigation);
    }

    private void setupRecyclerView() {
        if (getContext() == null) return;

        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TransactionAdapter();
        rvTransactions.setAdapter(adapter);
    }

    private void setupEventListeners() {
        btnLogout.setOnClickListener(v -> logout());
        btnManageCategories.setOnClickListener(v -> openCategoryManagement());
        fabAddTransaction.setOnClickListener(v -> openAddTransaction());

        // Thiết lập bottom navigation
        bottomNavigation.setOnNavigationItemSelectedListener(this);

        // Đảm bảo menu đã được inflate
        if (bottomNavigation.getMenu().findItem(R.id.thongke) != null) {
            bottomNavigation.setSelectedItemId(R.id.thongke);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Hiển thị thông tin người dùng
        displayUserInfo();

        // Tải dữ liệu
        loadCategories();
        loadWallets();
        loadTransactions();
    }

    /**
     * Xử lý sự kiện khi chọn item trong bottom navigation
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.thongke) {
            // Đã ở màn hình Thống kê
            return true;
        } else if (itemId == R.id.lapkehoach) {
            // Chuyển đến màn hình Lập kế hoạch
            showFeatureInDevelopment("Lập kế hoạch");
            return true;
        } else if (itemId == R.id.daucong) {
            // Mở màn hình thêm giao dịch
            openAddTransaction();
            return true;
        } else if (itemId == R.id.baocao) {
            // Chuyển đến màn hình Báo cáo
            showFeatureInDevelopment("Báo cáo");
            return true;
        } else if (itemId == R.id.hoso) {
            // Chuyển đến màn hình Ví tiền
            showFeatureInDevelopment("Ví tiền");
            return true;
        }

        return false;
    }

    private void showFeatureInDevelopment(String featureName) {
        if (isAdded()) {
            Toast.makeText(getContext(), "Chức năng " + featureName + " đang phát triển", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Hiển thị thông tin người dùng
     */
    private void displayUserInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && isAdded()) {
            String email = currentUser.getEmail();
            String displayName = currentUser.getDisplayName();

            tvGreeting.setText("Xin chào, " + (email != null ? email : ""));

            if (displayName != null && !displayName.isEmpty()) {
                tvUserName.setText(displayName);
            } else {
                tvUserName.setText("Người dùng");
            }
        }
    }

    /**
     * Tải danh sách danh mục từ Firebase
     */
    private void loadCategories() {
        String userId = getCurrentUserId();
        if (userId == null) return;

        mDatabase.child("categories").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                categories.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        Category category = snapshot.getValue(Category.class);
                        if (category != null) {
                            category.setId(snapshot.getKey());
                            categories.add(category);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing category", e);
                    }
                }

                // Cập nhật adapter nếu đã có dữ liệu giao dịch
                if (adapter != null && isAdded()) {
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                if (isAdded()) {
                    Toast.makeText(getContext(), "Lỗi tải danh mục: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Tải danh sách ví từ Firebase
     */
    private void loadWallets() {
        String userId = getCurrentUserId();
        if (userId == null) return;

        mDatabase.child("wallets").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                wallets.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        Wallet wallet = snapshot.getValue(Wallet.class);
                        if (wallet != null) {
                            wallet.setId(snapshot.getKey());
                            wallets.add(wallet);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing wallet", e);
                    }
                }

                // Cập nhật adapter nếu đã có dữ liệu giao dịch
                if (adapter != null && isAdded()) {
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                if (isAdded()) {
                    Toast.makeText(getContext(), "Lỗi tải ví: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Tải danh sách giao dịch từ Firebase
     */
    private void loadTransactions() {
        String userId = getCurrentUserId();
        if (userId == null) return;

        mDatabase.child("transactions").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                transactions.clear();
                long totalAmount = 0;
                long totalIncome = 0;
                long totalExpense = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        Transaction transaction = snapshot.getValue(Transaction.class);
                        if (transaction != null) {
                            transaction.setId(snapshot.getKey());
                            transactions.add(transaction);
                            totalAmount += transaction.getAmount();

                            // Tính tổng thu nhập và chi tiêu
                            if (transaction.getAmount() > 0) {
                                totalIncome += transaction.getAmount();
                            } else {
                                totalExpense += Math.abs(transaction.getAmount());
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing transaction", e);
                    }
                }

                // Sắp xếp giao dịch theo thời gian giảm dần (mới nhất lên đầu)
                Collections.sort(transactions, (t1, t2) ->
                        Long.compare(t2.getTimestamp(), t1.getTimestamp()));

                // Hiển thị tổng số tiền
                if (isAdded()) {
                    tvTotalAmount.setText(AMOUNT_FORMAT.format(totalAmount) + " VND");

                    // Hiển thị thu nhập và chi tiêu
                    tvIncomeAmount.setText("+" + AMOUNT_FORMAT.format(totalIncome) + " VND");
                    tvExpenseAmount.setText("-" + AMOUNT_FORMAT.format(totalExpense) + " VND");

                    // Cập nhật progress bar
                    updateProgressBars(totalIncome, totalExpense);

                    // Cập nhật giao diện
                    updateUI();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                if (isAdded()) {
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateProgressBars(long totalIncome, long totalExpense) {
        long total = totalIncome + totalExpense;
        if (total > 0) {
            progressIncome.setProgress((int) (totalIncome * 100 / total));
            progressExpense.setProgress((int) (totalExpense * 100 / total));
        } else {
            progressIncome.setProgress(0);
            progressExpense.setProgress(0);
        }
    }

    private String getCurrentUserId() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            if (isAdded()) {
                Toast.makeText(getContext(), "Vui lòng đăng nhập để xem dữ liệu", Toast.LENGTH_SHORT).show();
            }
            return null;
        }
        return currentUser.getUid();
    }

    /**
     * Cập nhật giao diện người dùng
     */
    private void updateUI() {
        if (transactions.isEmpty()) {
            tvNoTransactions.setVisibility(View.VISIBLE);
            rvTransactions.setVisibility(View.GONE);
        } else {
            tvNoTransactions.setVisibility(View.GONE);
            rvTransactions.setVisibility(View.VISIBLE);
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * Đăng xuất khỏi ứng dụng
     */
    private void logout() {
        mAuth.signOut();
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    /**
     * Mở màn hình quản lý danh mục
     */
    private void openCategoryManagement() {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), CategoryManagementActivity.class);
            startActivity(intent);
        }
    }

    /**
     * Mở màn hình thêm giao dịch mới
     */
    private void openAddTransaction() {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), TransactionManagementActivity.class);
            startActivity(intent);
        }
    }

    /**
     * Mở màn hình chỉnh sửa giao dịch
     */
    private void openEditTransaction(Transaction transaction) {
        if (transaction != null && transaction.getId() != null && getActivity() != null) {
            Intent intent = new Intent(getActivity(), TransactionManagementActivity.class);
            intent.putExtra("transaction_id", transaction.getId());
            startActivity(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tải lại dữ liệu khi quay lại fragment
        loadTransactions();
    }

    /**
     * Adapter cho RecyclerView hiển thị danh sách giao dịch
     */
    private class TransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int VIEW_TYPE_DATE_HEADER = 0;
        private static final int VIEW_TYPE_TRANSACTION = 1;

        private final List<Object> items = new ArrayList<>();

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Object item = items.get(position);

            if (holder instanceof DateHeaderViewHolder && item instanceof Date) {
                ((DateHeaderViewHolder) holder).bind((Date) item);
            } else if (holder instanceof TransactionViewHolder && item instanceof Transaction) {
                ((TransactionViewHolder) holder).bind((Transaction) item);
            }
        }

        @Override
        public int getItemCount() {
            prepareItems();
            return items.size();
        }

        @Override
        public int getItemViewType(int position) {
            return items.get(position) instanceof Date ? VIEW_TYPE_DATE_HEADER : VIEW_TYPE_TRANSACTION;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_DATE_HEADER) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_thongke_item_date_header, parent, false);
                return new DateHeaderViewHolder(view);
            } else {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_thongke_item_transaction, parent, false);
                return new TransactionViewHolder(view);
            }
        }

        /**
         * Chuẩn bị dữ liệu cho adapter, nhóm giao dịch theo ngày
         */
        private void prepareItems() {
            items.clear();

            if (transactions.isEmpty()) {
                return;
            }

            // Nhóm giao dịch theo ngày
            String currentDateStr = null;
            for (Transaction transaction : transactions) {
                String dateStr = DATE_FORMAT.format(new Date(transaction.getTimestamp()));

                if (currentDateStr == null || !currentDateStr.equals(dateStr)) {
                    currentDateStr = dateStr;
                    try {
                        Date date = DATE_FORMAT.parse(dateStr);
                        if (date != null) {
                            items.add(date);
                        }
                    } catch (ParseException e) {
                        Log.e(TAG, "Error parsing date", e);
                    }
                }

                items.add(transaction);
            }
        }

        /**
         * ViewHolder cho header ngày
         */
        class DateHeaderViewHolder extends RecyclerView.ViewHolder {
            TextView tvDate, tvDayOfWeek;

            DateHeaderViewHolder(@NonNull View itemView) {
                super(itemView);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvDayOfWeek = itemView.findViewById(R.id.tvDayOfWeek);
            }

            void bind(Date date) {
                tvDate.setText(DATE_FORMAT.format(date));
                tvDayOfWeek.setText(DAY_OF_WEEK_FORMAT.format(date));
            }
        }

        /**
         * ViewHolder cho giao dịch
         */
        class TransactionViewHolder extends RecyclerView.ViewHolder {
            ImageView imgCategory;
            TextView tvCategory, tvAmount, tvNote, tvWallet;

            TransactionViewHolder(@NonNull View itemView) {
                super(itemView);
                imgCategory = itemView.findViewById(R.id.imgCategory);
                tvCategory = itemView.findViewById(R.id.tvCategory);
                tvAmount = itemView.findViewById(R.id.tvAmount);
                tvNote = itemView.findViewById(R.id.tvNote);
                tvWallet = itemView.findViewById(R.id.tvWallet);
            }

            void bind(Transaction transaction) {
                if (transaction == null) return;

                tvCategory.setText(transaction.getCategory());

                // Format số tiền
                String amountText = AMOUNT_FORMAT.format(Math.abs(transaction.getAmount())) + " đ";
                if (transaction.getAmount() < 0) {
                    tvAmount.setText("-" + amountText);
                    tvAmount.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                } else {
                    tvAmount.setText("+" + amountText);
                    tvAmount.setTextColor(getResources().getColor(android.R.color.holo_green_light));
                }

                // Hiển thị ghi chú nếu có
                if (transaction.getNote() != null && !transaction.getNote().isEmpty()) {
                    tvNote.setVisibility(View.VISIBLE);
                    tvNote.setText(transaction.getNote());
                } else {
                    tvNote.setVisibility(View.GONE);
                }

                // Hiển thị tên ví
                String walletName = "Ví của tôi";
                for (Wallet wallet : wallets) {
                    if (wallet.getId() != null && wallet.getId().equals(transaction.getWalletId())) {
                        walletName = wallet.getName();
                        break;
                    }
                }
                tvWallet.setText(walletName);

                // Thiết lập icon cho danh mục
                setIconForCategory(imgCategory, transaction.getCategory());

                // Thiết lập sự kiện click
                itemView.setOnClickListener(v -> openEditTransaction(transaction));
            }

            /**
             * Thiết lập icon cho danh mục
             */
            private void setIconForCategory(ImageView imageView, String categoryName) {
                // Tìm icon từ danh sách danh mục
                for (Category category : categories) {
                    if (category.getName() != null &&
                            category.getName().equals(categoryName) &&
                            category.getIconResourceId() != 0) {
                        imageView.setImageResource(category.getIconResourceId());
                        return;
                    }
                }

                // Nếu không tìm thấy, sử dụng icon mặc định
                imageView.setImageResource(R.drawable.thongke_ic_other);
            }
        }
    }
}