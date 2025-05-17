package com.example.nhom1_quanlychitieu.ui.ThongKe;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom1_quanlychitieu.R;
import com.example.nhom1_quanlychitieu.ui.ThongKe.model.Category;
import com.example.nhom1_quanlychitieu.ui.ThongKe.model.Transaction;
import com.example.nhom1_quanlychitieu.ui.ThongKe.model.Wallet;
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

public class TransactionManagementActivity extends AppCompatActivity {

    // Tag để sử dụng trong log
    private static final String TAG = "TransactionManagement";

    // Định dạng ngày tháng và số tiền
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private static final SimpleDateFormat DAY_OF_WEEK_FORMAT = new SimpleDateFormat("EEEE", new Locale("vi", "VN"));
    private static final DecimalFormat AMOUNT_FORMAT = new DecimalFormat("#,###,###");

    // Các thành phần UI
    private ImageButton btnBack;
    private RecyclerView rvTransactions;
    private Button btnAddTransaction;
    private TextView tvNoTransactions;

    // Dữ liệu
    private TransactionAdapter adapter;
    private final List<Transaction> transactions = new ArrayList<>();
    private final List<Category> categories = new ArrayList<>();
    private final List<Wallet> wallets = new ArrayList<>();

    // Firebase
    private FirebaseAuth mAuth; // Xác thực người dùng
    private DatabaseReference mDatabase; // Tham chiếu đến database
    private String userId; // ID của người dùng hiện tại

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_thongke_transaction_management);

        // Khởi tạo Firebase
        initFirebase();
        if (userId == null) return; // Nếu không có người dùng, thoát

        // Khởi tạo các thành phần UI
        initializeViews();

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Thiết lập sự kiện
        setupEventListeners();

        // Tải dữ liệu
        loadData();

        // Xử lý khi mở Activity từ thông báo hoặc màn hình khác
        handleTransactionIdIntent();
    }

    private void initFirebase() {
        try {
            mAuth = FirebaseAuth.getInstance();
            mDatabase = FirebaseDatabase.getInstance().getReference();

            // Kiểm tra người dùng đã đăng nhập chưa
            if (mAuth.getCurrentUser() != null) {
                userId = mAuth.getCurrentUser().getUid();
            } else {
                // Nếu chưa đăng nhập, hiển thị thông báo và đóng Activity
                Toast.makeText(this, "Vui lòng đăng nhập để quản lý giao dịch", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            // Xử lý lỗi khi khởi tạo Firebase
            Log.e(TAG, "Error initializing Firebase", e);
            Toast.makeText(this, "Lỗi khởi tạo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        rvTransactions = findViewById(R.id.rvTransactions);
        btnAddTransaction = findViewById(R.id.btnAddTransaction);
        tvNoTransactions = findViewById(R.id.tvNoTransactions);
    }

    private void setupRecyclerView() {
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter();
        rvTransactions.setAdapter(adapter);
    }

    private void setupEventListeners() {
        // Nút quay lại
        btnBack.setOnClickListener(v -> finish());

        // Nút thêm giao dịch mới
        btnAddTransaction.setOnClickListener(v -> showAddTransactionDialog());
    }

    private void loadData() {
        loadCategories(); // Tải danh sách danh mục
        loadWallets(); // Tải danh sách ví
        loadTransactions(); // Tải danh sách giao dịch
    }

    private void handleTransactionIdIntent() {
        String transactionId = getIntent().getStringExtra("transaction_id");
        if (transactionId == null) return;

        // Tải thông tin giao dịch từ Firebase
        mDatabase.child("transactions").child(userId).child(transactionId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Transaction transaction = dataSnapshot.getValue(Transaction.class);
                        if (transaction != null) {
                            transaction.setId(transactionId);
                            showEditTransactionDialog(transaction); // Hiển thị dialog chỉnh sửa
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(TransactionManagementActivity.this,
                                "Lỗi tải giao dịch: " + databaseError.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadCategories() {
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
            }
        });
    }

    private void loadWallets() {
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

                // Nếu không có ví nào, tạo ví mặc định
                if (wallets.isEmpty()) {
                    createDefaultWallet();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
            }
        });
    }

    private void createDefaultWallet() {
        if (userId == null) return;

        // Tạo đối tượng ví mặc định
        Wallet defaultWallet = new Wallet("Ví của tôi", 0, userId);
        defaultWallet.setDefault(true);

        // Lưu ví mặc định vào Firebase
        String walletId = mDatabase.child("wallets").child(userId).push().getKey();
        if (walletId != null) {
            mDatabase.child("wallets").child(userId).child(walletId).setValue(defaultWallet);
        }
    }

    private void loadTransactions() {
        if (userId == null) {
            tvNoTransactions.setVisibility(View.VISIBLE);
            return;
        }

        mDatabase.child("transactions").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                transactions.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        Transaction transaction = snapshot.getValue(Transaction.class);
                        if (transaction != null) {
                            transaction.setId(snapshot.getKey());
                            transactions.add(transaction);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing transaction", e);
                    }
                }

                // Sắp xếp giao dịch theo thời gian giảm dần (mới nhất lên đầu)
                Collections.sort(transactions, (t1, t2) ->
                        Long.compare(t2.getTimestamp(), t1.getTimestamp()));

                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                Toast.makeText(TransactionManagementActivity.this,
                        "Lỗi tải dữ liệu: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        if (transactions.isEmpty()) {
            // Nếu không có giao dịch, hiển thị thông báo
            tvNoTransactions.setVisibility(View.VISIBLE);
            rvTransactions.setVisibility(View.GONE);
        } else {
            // Nếu có giao dịch, hiển thị danh sách
            tvNoTransactions.setVisibility(View.GONE);
            rvTransactions.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }

    private void showAddTransactionDialog() {
        showTransactionDialog(null);
    }

    private void showEditTransactionDialog(Transaction transaction) {
        showTransactionDialog(transaction);
    }

    private void showTransactionDialog(Transaction transaction) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.fragment_thongke_dialog_add_edit_transaction);

        // Thiết lập kích thước dialog
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        // Ánh xạ các thành phần trong dialog
        TextView tvDialogTitle = dialog.findViewById(R.id.tvDialogTitle);
        RadioGroup rgTransactionType = dialog.findViewById(R.id.rgTransactionType);
        RadioButton rbExpense = dialog.findViewById(R.id.rbExpense);
        RadioButton rbIncome = dialog.findViewById(R.id.rbIncome);
        EditText etAmount = dialog.findViewById(R.id.etAmount);
        Spinner spinnerCategory = dialog.findViewById(R.id.spinnerCategory);
        Spinner spinnerWallet = dialog.findViewById(R.id.spinnerWallet);
        EditText etDate = dialog.findViewById(R.id.etDate);
        EditText etNote = dialog.findViewById(R.id.etNote);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnSave = dialog.findViewById(R.id.btnSave);
        Button btnDelete = dialog.findViewById(R.id.btnDelete);

        // Hiển thị hoặc ẩn nút xóa tùy thuộc vào việc thêm mới hay chỉnh sửa
        if (transaction == null) {
            btnDelete.setVisibility(View.GONE); // Ẩn nút xóa khi thêm mới
        } else {
            btnDelete.setVisibility(View.VISIBLE); // Hiển thị nút xóa khi chỉnh sửa
        }

        // Thiết lập tiêu đề dialog
        tvDialogTitle.setText(transaction == null ? "Thêm giao dịch mới" : "Chỉnh sửa giao dịch");

        // Kiểm tra nếu không có danh mục
        if (categories.isEmpty()) {
            Toast.makeText(this, "Vui lòng thêm danh mục trước khi thêm giao dịch", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            openCategoryManagement(); // Mở màn hình quản lý danh mục
            return;
        }

        // Thiết lập loại giao dịch nếu là chỉnh sửa
        if (transaction != null) {
            if (transaction.getAmount() > 0) {
                rbIncome.setChecked(true); // Thu nhập
            } else {
                rbExpense.setChecked(true); // Chi tiêu
            }
        }

        // Thiết lập adapter cho spinner danh mục và ví
        setupSpinners(spinnerCategory, spinnerWallet, transaction, rbExpense.isChecked());

        // Thiết lập dữ liệu nếu là chỉnh sửa
        if (transaction != null) {
            etAmount.setText(String.valueOf(Math.abs(transaction.getAmount())));
            etDate.setText(DATE_FORMAT.format(new Date(transaction.getTimestamp())));
            if (transaction.getNote() != null) {
                etNote.setText(transaction.getNote());
            }
        } else {
            // Nếu là thêm mới, hiển thị ngày hiện tại
            etDate.setText(DATE_FORMAT.format(new Date()));
        }

        // Thiết lập sự kiện chọn ngày
        setupDatePicker(etDate);

        // Thiết lập sự kiện khi chọn loại giao dịch
        rgTransactionType.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isExpense = checkedId == R.id.rbExpense;
            updateCategorySpinner(spinnerCategory, isExpense);
        });

        // Thiết lập sự kiện cho các nút
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            if (validateInput(etAmount, spinnerCategory, spinnerWallet)) {
                boolean isExpense = rbExpense.isChecked();
                saveTransaction(transaction, etAmount, spinnerCategory, spinnerWallet, etDate, etNote, isExpense);
                dialog.dismiss();
            }
        });

        // Thêm sự kiện cho nút xóa
        btnDelete.setOnClickListener(v -> {
            if (transaction != null) {
                confirmDeleteTransaction(transaction);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void setupSpinners(Spinner spinnerCategory, Spinner spinnerWallet, Transaction transaction, boolean isExpense) {
        // Thiết lập adapter cho spinner danh mục
        updateCategorySpinner(spinnerCategory, isExpense);

        // Thiết lập adapter cho spinner ví
        ArrayAdapter<String> walletAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        walletAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (Wallet wallet : wallets) {
            walletAdapter.add(wallet.getName());
        }
        spinnerWallet.setAdapter(walletAdapter);

        // Chọn danh mục và ví hiện tại nếu là chỉnh sửa
        if (transaction != null) {
            // Tìm và chọn danh mục
            String categoryName = transaction.getCategory();
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerCategory.getAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).equals(categoryName)) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }

            // Tìm và chọn ví
            for (int i = 0; i < wallets.size(); i++) {
                if (wallets.get(i).getId().equals(transaction.getWalletId())) {
                    spinnerWallet.setSelection(i);
                    break;
                }
            }
        }
    }

    private void updateCategorySpinner(Spinner spinnerCategory, boolean isExpense) {
        // Lọc danh mục theo loại (chi tiêu hoặc thu nhập)
        List<String> filteredCategories = new ArrayList<>();
        for (Category category : categories) {
            if ((isExpense && category.isExpense()) || (!isExpense && category.isIncome())) {
                filteredCategories.add(category.getName());
            }
        }

        // Nếu không có danh mục phù hợp, hiển thị thông báo
        if (filteredCategories.isEmpty()) {
            if (isExpense) {
                filteredCategories.add("Chưa có danh mục chi tiêu");
            } else {
                filteredCategories.add("Chưa có danh mục thu nhập");
            }
        }

        // Cập nhật adapter
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filteredCategories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
    }

    private void setupDatePicker(EditText etDate) {
        etDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        calendar.set(selectedYear, selectedMonth, selectedDay);
                        etDate.setText(DATE_FORMAT.format(calendar.getTime()));
                    },
                    year, month, day);
            datePickerDialog.show();
        });
    }

    private boolean validateInput(EditText etAmount, Spinner spinnerCategory, Spinner spinnerWallet) {
        // Kiểm tra số tiền
        if (etAmount.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            Long.parseLong(etAmount.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Kiểm tra danh mục
        if (spinnerCategory.getSelectedItem() == null) {
            Toast.makeText(this, "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show();
            return false;
        }

        String categoryName = spinnerCategory.getSelectedItem().toString();
        if (categoryName.startsWith("Chưa có danh mục")) {
            Toast.makeText(this, "Vui lòng thêm danh mục trước", Toast.LENGTH_SHORT).show();
            openCategoryManagement();
            return false;
        }

        // Kiểm tra ví
        if (spinnerWallet.getSelectedItem() == null) {
            Toast.makeText(this, "Vui lòng chọn ví", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void saveTransaction(Transaction transaction, EditText etAmount, Spinner spinnerCategory,
                                 Spinner spinnerWallet, EditText etDate, EditText etNote, boolean isExpense) {
        // Lấy dữ liệu từ form
        long amount = Long.parseLong(etAmount.getText().toString().trim());
        // Nếu là chi tiêu, đổi dấu số tiền
        if (isExpense) {
            amount = -Math.abs(amount); // Số âm cho chi tiêu
        } else {
            amount = Math.abs(amount); // Số dương cho thu nhập
        }

        String categoryName = spinnerCategory.getSelectedItem().toString();
        String walletName = spinnerWallet.getSelectedItem().toString();
        String note = etNote.getText().toString().trim();

        // Lấy ID của ví
        String walletId = null;
        for (Wallet wallet : wallets) {
            if (wallet.getName().equals(walletName)) {
                walletId = wallet.getId();
                break;
            }
        }

        // Chuyển đổi ngày thành timestamp
        long timestamp;
        try {
            Date date = DATE_FORMAT.parse(etDate.getText().toString());
            timestamp = date != null ? date.getTime() : System.currentTimeMillis();
        } catch (ParseException e) {
            timestamp = System.currentTimeMillis();
        }

        if (transaction == null) {
            // Thêm giao dịch mới
            addTransaction(categoryName, amount, timestamp, note, walletId);
        } else {
            // Cập nhật giao dịch
            updateTransaction(transaction.getId(), categoryName, amount, timestamp, note, walletId);
        }
    }

    private void openCategoryManagement() {
        Intent intent = new Intent(this, CategoryManagementActivity.class);
        startActivity(intent);
    }

    private void addTransaction(String category, long amount, long timestamp, String note, String walletId) {
        if (userId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để thêm giao dịch", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo đối tượng Transaction mới
        Transaction transaction = new Transaction(category, amount, timestamp, note, walletId, userId);

        // Tạo key mới cho giao dịch
        String transactionId = mDatabase.child("transactions").child(userId).push().getKey();
        if (transactionId == null) {
            Toast.makeText(this, "Lỗi tạo ID giao dịch", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lưu giao dịch vào Firebase
        mDatabase.child("transactions").child(userId).child(transactionId).setValue(transaction)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(TransactionManagementActivity.this, "Thêm giao dịch thành công", Toast.LENGTH_SHORT).show();
                    updateWalletBalance(walletId, amount); // Cập nhật số dư ví
                })
                .addOnFailureListener(e ->
                        Toast.makeText(TransactionManagementActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateTransaction(String transactionId, String category, long amount, long timestamp, String note, String walletId) {
        if (userId == null || transactionId == null) {
            Toast.makeText(this, "Lỗi cập nhật giao dịch", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tìm giao dịch cũ để lấy số tiền cũ
        for (Transaction oldTransaction : transactions) {
            if (oldTransaction.getId().equals(transactionId)) {
                // Tạo map chứa các trường cần cập nhật
                Map<String, Object> updates = new HashMap<>();
                updates.put("category", category);
                updates.put("amount", amount);
                updates.put("timestamp", timestamp);
                updates.put("note", note);
                updates.put("walletId", walletId);

                // Cập nhật giao dịch trong Firebase
                mDatabase.child("transactions").child(userId).child(transactionId).updateChildren(updates)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(TransactionManagementActivity.this, "Cập nhật giao dịch thành công", Toast.LENGTH_SHORT).show();

                            // Cập nhật số dư ví
                            if (oldTransaction.getWalletId().equals(walletId)) {
                                // Nếu cùng ví, chỉ cập nhật chênh lệch
                                updateWalletBalance(walletId, amount - oldTransaction.getAmount());
                            } else {
                                // Nếu khác ví, cập nhật cả hai ví
                                updateWalletBalance(oldTransaction.getWalletId(), -oldTransaction.getAmount());
                                updateWalletBalance(walletId, amount);
                            }
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(TransactionManagementActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                break;
            }
        }
    }

    private void confirmDeleteTransaction(Transaction transaction) {
        if (userId == null || transaction == null || transaction.getId() == null) {
            Toast.makeText(this, "Lỗi xóa giao dịch", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hiển thị dialog xác nhận
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa giao dịch này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteTransaction(transaction);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteTransaction(Transaction transaction) {
        if (userId == null || transaction == null || transaction.getId() == null) {
            Toast.makeText(this, "Lỗi xóa giao dịch", Toast.LENGTH_SHORT).show();
            return;
        }

        // Xóa giao dịch khỏi Firebase
        mDatabase.child("transactions").child(userId).child(transaction.getId()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(TransactionManagementActivity.this, "Xóa giao dịch thành công", Toast.LENGTH_SHORT).show();

                    // Cập nhật số dư ví
                    updateWalletBalance(transaction.getWalletId(), -transaction.getAmount());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(TransactionManagementActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateWalletBalance(String walletId, long amountChange) {
        if (userId == null || walletId == null) {
            return;
        }

        // Lấy thông tin ví hiện tại
        mDatabase.child("wallets").child(userId).child(walletId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Wallet wallet = dataSnapshot.getValue(Wallet.class);
                if (wallet != null) {
                    // Cập nhật số dư
                    long newBalance = wallet.getBalance() + amountChange;
                    mDatabase.child("wallets").child(userId).child(walletId).child("balance").setValue(newBalance);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
            }
        });
    }

    private class TransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int VIEW_TYPE_DATE_HEADER = 0; // Loại view cho header ngày
        private static final int VIEW_TYPE_TRANSACTION = 1; // Loại view cho giao dịch

        private final List<Object> items = new ArrayList<>(); // Danh sách các item (Date hoặc Transaction)

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
            prepareItems(); // Chuẩn bị dữ liệu trước khi đếm
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
                            items.add(date); // Thêm header ngày
                        }
                    } catch (ParseException e) {
                        Log.e(TAG, "Error parsing date", e);
                    }
                }

                items.add(transaction); // Thêm giao dịch
            }
        }

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

        class TransactionViewHolder extends RecyclerView.ViewHolder {
            ImageView imgCategory;
            TextView tvCategory, tvAmount, tvNote, tvWallet;
            ImageButton btnDeleteTransaction;

            TransactionViewHolder(@NonNull View itemView) {
                super(itemView);
                imgCategory = itemView.findViewById(R.id.imgCategory);
                tvCategory = itemView.findViewById(R.id.tvCategory);
                tvAmount = itemView.findViewById(R.id.tvAmount);
                tvNote = itemView.findViewById(R.id.tvNote);
                tvWallet = itemView.findViewById(R.id.tvWallet);
                btnDeleteTransaction = itemView.findViewById(R.id.btnDeleteTransaction);
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
                String walletName = "Ví không xác định";
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
                itemView.setOnClickListener(v -> showEditTransactionDialog(transaction));

                // Thiết lập sự kiện cho nút xóa
                btnDeleteTransaction.setOnClickListener(v -> confirmDeleteTransaction(transaction));

                // Thiết lập sự kiện long click để xóa
                itemView.setOnLongClickListener(v -> {
                    confirmDeleteTransaction(transaction);
                    return true;
                });
            }

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
                imageView.setImageResource(R.drawable.ic_other);
            }
        }
    }
}