package com.example.nhom1_quanlychitieu.ui.DauCong;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.nhom1_quanlychitieu.R;
import com.example.nhom1_quanlychitieu.ui.ThongKe.model.Category;
import com.example.nhom1_quanlychitieu.ui.ThongKe.model.Transaction;
import com.example.nhom1_quanlychitieu.ui.ThongKe.model.Wallet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Fragment để thêm giao dịch mới nhanh chóng
 */
public class DauCong extends Fragment {

    private static final String TAG = "DauCong";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    // UI components
    private RadioGroup rgTransactionType;
    private RadioButton rbExpense, rbIncome;
    private EditText etAmount, etDate, etNote;
    private Spinner spinnerCategory, spinnerWallet;
    private Button btnCancel, btnSave;
    private TextView tvTitle;

    // Data
    private final List<Category> categories = new ArrayList<>();
    private final List<Wallet> wallets = new ArrayList<>();
    private Calendar calendar;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_daucong, container, false);

        // Khởi tạo Firebase
        initFirebase();
        if (userId == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để thêm giao dịch", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Khởi tạo các view
        initViews(view);

        // Thiết lập sự kiện
        setupEvents();

        // Tải dữ liệu
        loadData();

        return view;
    }

    /**
     * Khởi tạo Firebase
     */
    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }
    }

    /**
     * Khởi tạo các view
     */
    private void initViews(View view) {
        tvTitle = view.findViewById(R.id.tvTitle);
        rgTransactionType = view.findViewById(R.id.rgTransactionType);
        rbExpense = view.findViewById(R.id.rbExpense);
        rbIncome = view.findViewById(R.id.rbIncome);
        etAmount = view.findViewById(R.id.etAmount);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        spinnerWallet = view.findViewById(R.id.spinnerWallet);
        etDate = view.findViewById(R.id.etDate);
        etNote = view.findViewById(R.id.etNote);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnSave = view.findViewById(R.id.btnSave);

        // Khởi tạo calendar và hiển thị ngày hiện tại
        calendar = Calendar.getInstance();
        etDate.setText(DATE_FORMAT.format(calendar.getTime()));
    }

    /**
     * Thiết lập sự kiện cho các thành phần
     */
    private void setupEvents() {
        // Sự kiện khi chọn loại giao dịch
        rgTransactionType.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isExpense = checkedId == R.id.rbExpense;
            updateCategorySpinner(isExpense);
        });

        // Sự kiện khi nhấn vào EditText ngày
        etDate.setOnClickListener(v -> showDatePickerDialog());

        // Sự kiện khi nhấn nút Hủy
        btnCancel.setOnClickListener(v -> clearForm());

        // Sự kiện khi nhấn nút Lưu
        btnSave.setOnClickListener(v -> {
            if (validateInput()) {
                addTransaction();
            }
        });
    }

    /**
     * Tải dữ liệu từ Firebase
     */
    private void loadData() {
        loadCategories();
        loadWallets();
    }

    /**
     * Tải danh sách danh mục từ Firebase
     */
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

                // Cập nhật spinner danh mục
                updateCategorySpinner(rbExpense.isChecked());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi tải danh mục: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Tải danh sách ví từ Firebase
     */
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
                } else {
                    // Cập nhật spinner ví
                    updateWalletSpinner();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi tải ví: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Tạo ví mặc định nếu chưa có ví nào
     */
    private void createDefaultWallet() {
        if (userId == null) return;

        Wallet defaultWallet = new Wallet("Ví của tôi", 0, userId);
        defaultWallet.setDefault(true);

        String walletId = mDatabase.child("wallets").child(userId).push().getKey();
        if (walletId != null) {
            mDatabase.child("wallets").child(userId).child(walletId).setValue(defaultWallet)
                    .addOnSuccessListener(aVoid -> updateWalletSpinner())
                    .addOnFailureListener(e -> {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Lỗi tạo ví mặc định: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    /**
     * Cập nhật spinner danh mục theo loại giao dịch
     */
    private void updateCategorySpinner(boolean isExpense) {
        if (getContext() == null) return;

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
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, filteredCategories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
    }

    /**
     * Cập nhật spinner ví
     */
    private void updateWalletSpinner() {
        if (getContext() == null) return;

        // Tạo danh sách tên ví
        List<String> walletNames = new ArrayList<>();
        for (Wallet wallet : wallets) {
            walletNames.add(wallet.getName());
        }

        // Cập nhật adapter
        ArrayAdapter<String> walletAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, walletNames);
        walletAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWallet.setAdapter(walletAdapter);
    }

    /**
     * Hiển thị dialog chọn ngày
     */
    private void showDatePickerDialog() {
        if (getContext() == null) return;

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    etDate.setText(DATE_FORMAT.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    /**
     * Xóa form
     */
    private void clearForm() {
        etAmount.setText("");
        etNote.setText("");
        calendar = Calendar.getInstance();
        etDate.setText(DATE_FORMAT.format(calendar.getTime()));
        rbExpense.setChecked(true);

        // Reset spinners
        if (spinnerCategory.getAdapter() != null && spinnerCategory.getAdapter().getCount() > 0) {
            spinnerCategory.setSelection(0);
        }
        if (spinnerWallet.getAdapter() != null && spinnerWallet.getAdapter().getCount() > 0) {
            spinnerWallet.setSelection(0);
        }
    }

    /**
     * Kiểm tra dữ liệu nhập vào
     */
    private boolean validateInput() {
        // Kiểm tra số tiền
        if (etAmount.getText().toString().trim().isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            Long.parseLong(etAmount.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Kiểm tra danh mục
        if (spinnerCategory.getSelectedItem() == null) {
            Toast.makeText(getContext(), "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show();
            return false;
        }

        String categoryName = spinnerCategory.getSelectedItem().toString();
        if (categoryName.startsWith("Chưa có danh mục")) {
            Toast.makeText(getContext(), "Vui lòng thêm danh mục trước", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Kiểm tra ví
        if (spinnerWallet.getSelectedItem() == null) {
            Toast.makeText(getContext(), "Vui lòng chọn ví", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Thêm giao dịch mới vào Firebase
     */
    private void addTransaction() {
        if (userId == null || getContext() == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để thêm giao dịch", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy dữ liệu từ form
        long amount = Long.parseLong(etAmount.getText().toString().trim());
        // Nếu là chi tiêu, đổi dấu số tiền
        if (rbExpense.isChecked()) {
            amount = -Math.abs(amount);
        } else {
            amount = Math.abs(amount);
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

        if (walletId == null) {
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy ví", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chuyển đổi ngày thành timestamp
        long timestamp = calendar.getTimeInMillis();

        // Tạo đối tượng Transaction mới
        Transaction transaction = new Transaction(categoryName, amount, timestamp, note, walletId, userId);

        // Tạo key mới cho giao dịch
        String transactionId = mDatabase.child("transactions").child(userId).push().getKey();
        if (transactionId == null) {
            Toast.makeText(getContext(), "Lỗi tạo ID giao dịch", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo biến final để sử dụng trong lambda
        final String finalWalletId = walletId;
        final long finalAmount = amount;

        // Lưu giao dịch vào Firebase
        mDatabase.child("transactions").child(userId).child(transactionId).setValue(transaction)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Thêm giao dịch thành công", Toast.LENGTH_SHORT).show();
                    updateWalletBalance(finalWalletId, finalAmount);
                    clearForm();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Cập nhật số dư ví sau khi thêm giao dịch
     */
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
                    mDatabase.child("wallets").child(userId).child(walletId).child("balance").setValue(newBalance)
                            .addOnFailureListener(e -> Log.e(TAG, "Error updating wallet balance: " + e.getMessage()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
            }
        });
    }
}
