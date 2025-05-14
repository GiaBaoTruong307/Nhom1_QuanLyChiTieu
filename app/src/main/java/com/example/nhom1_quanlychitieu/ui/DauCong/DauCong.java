package com.example.nhom1_quanlychitieu.ui.DauCong;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.nhom1_quanlychitieu.R;
import com.example.nhom1_quanlychitieu.model.Transaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class DauCong extends Fragment {

    private EditText edtNhomGiaoDich, edtSoTien, edtThoiGian, edtGhiChu;
    private ImageView imgCalendar;
    private Button btnHuy, btnThem;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;

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

        // Khởi tạo các view
        initViews(view);

        // Thiết lập sự kiện
        setupEvents();

        return view;
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }
    }

    private void initViews(View view) {
        edtNhomGiaoDich = view.findViewById(R.id.edtNhomGiaoDich);
        edtSoTien = view.findViewById(R.id.edtSoTien);
        edtThoiGian = view.findViewById(R.id.edtThoiGian);
        edtGhiChu = view.findViewById(R.id.edtGhiChu);
        imgCalendar = view.findViewById(R.id.imgCalendar);
        btnHuy = view.findViewById(R.id.btnHuy);
        btnThem = view.findViewById(R.id.btnThem);

        // Khởi tạo calendar và định dạng ngày
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        // Hiển thị ngày hiện tại
        edtThoiGian.setText(dateFormat.format(calendar.getTime()));
    }

    private void setupEvents() {
        // Sự kiện khi nhấn vào icon lịch
        imgCalendar.setOnClickListener(v -> showDatePickerDialog());

        // Sự kiện khi nhấn vào EditText thời gian
        edtThoiGian.setOnClickListener(v -> showDatePickerDialog());

        // Sự kiện khi nhấn nút Hủy
        btnHuy.setOnClickListener(v -> clearForm());

        // Sự kiện khi nhấn nút Thêm
        btnThem.setOnClickListener(v -> addTransaction());
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    edtThoiGian.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void clearForm() {
        edtNhomGiaoDich.setText("");
        edtSoTien.setText("");
        calendar = Calendar.getInstance();
        edtThoiGian.setText(dateFormat.format(calendar.getTime()));
        edtGhiChu.setText("");
    }

    private void addTransaction() {
        // Kiểm tra người dùng đã đăng nhập chưa
        if (userId == null) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập để thêm giao dịch", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy dữ liệu từ form
        String nhomGiaoDich = edtNhomGiaoDich.getText().toString().trim();
        String soTienStr = edtSoTien.getText().toString().trim();
        String thoiGian = edtThoiGian.getText().toString().trim();
        String ghiChu = edtGhiChu.getText().toString().trim();

        // Kiểm tra dữ liệu
        if (TextUtils.isEmpty(nhomGiaoDich)) {
            Toast.makeText(requireContext(), "Vui lòng nhập nhóm giao dịch", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(soTienStr)) {
            Toast.makeText(requireContext(), "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chuyển đổi số tiền
        long soTien;
        try {
            soTien = Long.parseLong(soTienStr.replaceAll("[,.]", ""));
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo đối tượng Transaction
        String transactionId = UUID.randomUUID().toString();
        Transaction transaction = new Transaction();
        transaction.setId(transactionId);
        transaction.setCategory(nhomGiaoDich);
        transaction.setAmount(soTien);
        transaction.setNote(ghiChu);

        // Chuyển đổi thời gian
        try {
            Date date = dateFormat.parse(thoiGian);
            if (date != null) {
                transaction.setTimestamp(date.getTime());
            } else {
                transaction.setTimestamp(System.currentTimeMillis());
            }
        } catch (Exception e) {
            transaction.setTimestamp(System.currentTimeMillis());
        }

        transaction.setUserId(userId);

        // Lưu vào Firebase
        mDatabase.child("transactions").child(userId).child(transactionId).setValue(transaction)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Thêm giao dịch thành công", Toast.LENGTH_SHORT).show();
                    clearForm();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}