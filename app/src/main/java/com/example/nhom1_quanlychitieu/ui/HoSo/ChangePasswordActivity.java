package com.example.nhom1_quanlychitieu.ui.HoSo;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom1_quanlychitieu.R;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChangePasswordActivity extends AppCompatActivity {

    private static final String TAG = "ChangePasswordActivity";
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final String USERS_PATH = "users";
    private static final String PASSWORD_KEY = "password";

    // UI components
    private EditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private ImageButton btnToggleCurrentPassword, btnToggleNewPassword, btnToggleConfirmPassword;
    private ProgressDialog progressDialog;

    // Firebase components
    private FirebaseUser currentUser;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_hoso_change_password);

        if (!initFirebase()) {
            return;
        }

        initViews();
        setupListeners();
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
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        Button btnChangePassword = findViewById(R.id.btnChangePassword);
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnToggleCurrentPassword = findViewById(R.id.btnToggleCurrentPassword);
        btnToggleNewPassword = findViewById(R.id.btnToggleNewPassword);
        btnToggleConfirmPassword = findViewById(R.id.btnToggleConfirmPassword);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang cập nhật mật khẩu...");
        progressDialog.setCancelable(false);

        // Thiết lập sự kiện cho các nút
        btnChangePassword.setOnClickListener(v -> changePassword());
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void setupListeners() {
        btnToggleCurrentPassword.setOnClickListener(v -> togglePasswordVisibility(etCurrentPassword, btnToggleCurrentPassword));
        btnToggleNewPassword.setOnClickListener(v -> togglePasswordVisibility(etNewPassword, btnToggleNewPassword));
        btnToggleConfirmPassword.setOnClickListener(v -> togglePasswordVisibility(etConfirmPassword, btnToggleConfirmPassword));
    }

    private void togglePasswordVisibility(EditText editText, ImageButton button) {
        boolean isVisible = editText.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

        int inputType = isVisible
                ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
                : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;

        int iconResource = isVisible ? R.drawable.eye_closed : R.drawable.eye_open;

        editText.setInputType(inputType);
        button.setImageResource(iconResource);
        editText.setSelection(editText.getText().length());
    }

    private void changePassword() {
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (!validateInputFields(currentPassword, newPassword, confirmPassword)) {
            return;
        }

        progressDialog.show();
        reauthenticateUser(currentPassword, newPassword);
    }

    private boolean validateInputFields(String currentPassword, String newPassword, String confirmPassword) {
        // Kiểm tra trường trống
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showToast("Vui lòng nhập đầy đủ thông tin");
            return false;
        }

        // Kiểm tra độ dài mật khẩu mới
        if (newPassword.length() < MIN_PASSWORD_LENGTH) {
            showToast("Mật khẩu mới phải có ít nhất " + MIN_PASSWORD_LENGTH + " ký tự");
            return false;
        }

        // Kiểm tra mật khẩu mới và xác nhận mật khẩu
        if (!newPassword.equals(confirmPassword)) {
            showToast("Mật khẩu mới không khớp");
            return false;
        }

        // Kiểm tra mật khẩu mới không trùng với mật khẩu cũ
        if (currentPassword.equals(newPassword)) {
            showToast("Mật khẩu mới không được trùng với mật khẩu hiện tại");
            return false;
        }

        return true;
    }

    private void reauthenticateUser(String currentPassword, String newPassword) {
        String email = currentUser.getEmail();
        if (email == null) {
            progressDialog.dismiss();
            showToast("Không thể xác định email người dùng");
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(email, currentPassword);

        currentUser.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> updatePassword(newPassword))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    showToast("Mật khẩu hiện tại không đúng");
                    Log.e(TAG, "Reauthentication failed", e);
                });
    }

    private void updatePassword(String newPassword) {
        currentUser.updatePassword(newPassword)
                .addOnSuccessListener(aVoid -> {
                    // Cập nhật mật khẩu trong Realtime Database
                    updatePasswordInDatabase(newPassword);
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    showToast("Lỗi đổi mật khẩu: " + e.getMessage());
                    Log.e(TAG, "Password update failed", e);
                });
    }

    private void updatePasswordInDatabase(String newPassword) {
        userRef.child(PASSWORD_KEY).setValue(newPassword)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    showToast("Đổi mật khẩu thành công");
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    showToast("Đổi mật khẩu thành công nhưng không thể cập nhật cơ sở dữ liệu");
                    Log.e(TAG, "Database password update failed", e);
                    finish();
                });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}