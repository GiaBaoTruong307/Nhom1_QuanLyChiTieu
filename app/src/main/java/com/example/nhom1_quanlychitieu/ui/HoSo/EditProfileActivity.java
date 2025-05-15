package com.example.nhom1_quanlychitieu.ui.HoSo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom1_quanlychitieu.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";
    public static final int RESULT_PROFILE_UPDATED = 100;

    // Các khóa cho Firebase Database
    private static final String USERS_PATH = "users";
    private static final String FULL_NAME_KEY = "fullName";
    private static final String PHONE_NUMBER_KEY = "phoneNumber";

    // UI components
    private EditText etFullName, etPhoneNumber;
    private ProgressDialog progressDialog;

    // Firebase components
    private FirebaseUser currentUser;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_hoso_edit);

        if (!initFirebase()) {
            return;
        }

        initViews();
        loadUserData();
    }

    private boolean initFirebase() {
        try {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            currentUser = mAuth.getCurrentUser();

            if (currentUser == null) {
                showToast("Bạn cần đăng nhập để thực hiện chức năng này");
                finish();
                return false;
            }

            userRef = FirebaseDatabase.getInstance().getReference()
                    .child(USERS_PATH)
                    .child(currentUser.getUid());
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Firebase initialization error", e);
            showToast("Lỗi khởi tạo Firebase: " + e.getMessage());
            finish();
            return false;
        }
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        Button btnSave = findViewById(R.id.btnSave);
        ImageButton btnBack = findViewById(R.id.btnBack);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang xử lý...");
        progressDialog.setCancelable(false);

        // Thiết lập sự kiện cho các nút
        btnSave.setOnClickListener(v -> saveUserProfile());
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void loadUserData() {
        progressDialog.setMessage("Đang tải...");
        progressDialog.show();

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressDialog.dismiss();

                if (dataSnapshot.exists()) {
                    // Lấy thông tin người dùng
                    String fullName = dataSnapshot.child(FULL_NAME_KEY).getValue(String.class);
                    String phoneNumber = dataSnapshot.child(PHONE_NUMBER_KEY).getValue(String.class);

                    // Cập nhật UI
                    if (fullName != null) etFullName.setText(fullName);
                    if (phoneNumber != null) etPhoneNumber.setText(phoneNumber);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
                Log.e(TAG, "loadUserData:onCancelled", databaseError.toException());
                showToast("Không thể tải thông tin người dùng");
            }
        });
    }

    private void saveUserProfile() {
        String fullName = etFullName.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();

        if (fullName.isEmpty()) {
            etFullName.setError("Vui lòng nhập họ tên");
            etFullName.requestFocus();
            return;
        }

        progressDialog.show();

        Map<String, Object> updates = new HashMap<>();
        updates.put(FULL_NAME_KEY, fullName);
        updates.put(PHONE_NUMBER_KEY, phoneNumber);

        userRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    showToast("Cập nhật thông tin thành công");

                    // Đặt kết quả và trả về dữ liệu
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(FULL_NAME_KEY, fullName);
                    setResult(RESULT_PROFILE_UPDATED, resultIntent);

                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.e(TAG, "Error updating profile", e);
                    showToast("Lỗi cập nhật thông tin: " + e.getMessage());
                });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}