package com.example.nhom1_quanlychitieu.ui.QuanLyChungThuc;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom1_quanlychitieu.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private static final int MIN_PASSWORD_LENGTH = 6;

    // UI components
    private EditText etEmail, etPassword, etConfirmPassword, etUsername;
    private Button btnRegister;
    private TextView tvLogin, tvRegisterLabel;
    private ImageButton btnTogglePassword, btnToggleConfirmPassword;
    private ProgressDialog progressDialog;

    // Firebase components
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_register);

        initFirebase();
        initViews();
        setupListeners();
    }

    private void initFirebase() {
        try {
            mAuth = FirebaseAuth.getInstance();
            mDatabase = FirebaseDatabase.getInstance().getReference();
        } catch (Exception e) {
            Log.e(TAG, "Firebase initialization error", e);
            showToast("Lỗi khởi tạo Firebase: " + e.getMessage());
            finish();
        }
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etUsername = findViewById(R.id.etUsername);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        tvRegisterLabel = findViewById(R.id.tvRegisterLabel);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        btnToggleConfirmPassword = findViewById(R.id.btnToggleConfirmPassword);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang đăng ký...");
        progressDialog.setCancelable(false);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> handleRegistration());
        tvRegisterLabel.setOnClickListener(v -> btnRegister.performClick());
        tvLogin.setOnClickListener(v -> navigateToLogin());

        btnTogglePassword.setOnClickListener(v -> togglePasswordVisibility(etPassword, btnTogglePassword));
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

    private void handleRegistration() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String username = etUsername.getText().toString().trim();

        if (!validateInputFields(email, password, confirmPassword, username)) {
            return;
        }

        progressDialog.show();
        registerWithFirebaseAuth(email, password, username);
    }

    private boolean validateInputFields(String email, String password, String confirmPassword, String username) {
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showToast("Vui lòng nhập đầy đủ thông tin email và mật khẩu");
            return false;
        }

        if (username.isEmpty()) {
            showToast("Vui lòng nhập tên người dùng");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Email không hợp lệ");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showToast("Mật khẩu không khớp");
            return false;
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            showToast("Mật khẩu phải có ít nhất " + MIN_PASSWORD_LENGTH + " ký tự");
            return false;
        }

        return true;
    }

    private void registerWithFirebaseAuth(String email, String password, String username) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            sendVerificationEmail(user, username, password);
                        } else {
                            progressDialog.dismiss();
                            showToast("Lỗi tạo tài khoản");
                        }
                    } else {
                        progressDialog.dismiss();
                        handleRegistrationError(task.getException());
                    }
                });
    }

    private void handleRegistrationError(Exception exception) {
        if (exception instanceof FirebaseAuthUserCollisionException) {
            showToast("Email đã được sử dụng");
        } else if (exception instanceof FirebaseAuthWeakPasswordException) {
            showToast("Mật khẩu quá yếu. Vui lòng chọn mật khẩu mạnh hơn");
        } else {
            showToast("Đăng ký thất bại: " + (exception != null ? exception.getMessage() : "Lỗi không xác định"));
        }
        Log.e(TAG, "Registration error", exception);
    }

    private void sendVerificationEmail(FirebaseUser user, String username, String password) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();

                    if (task.isSuccessful()) {
                        saveUserToDatabase(user.getUid(), user.getEmail(), username, password);
                        navigateToRegisterSuccess();
                    } else {
                        showToast("Không thể gửi email xác thực: " + task.getException().getMessage());
                        user.delete(); // Xóa tài khoản nếu không gửi được email xác thực
                    }
                });
    }

    private void saveUserToDatabase(String userId, String email, String username, String password) {
        try {
            Map<String, Object> userData = new HashMap<>();
            userData.put("email", email);
            userData.put("username", username);
            userData.put("fullName", username);
            userData.put("password", password); // Lưu mật khẩu vào database
            userData.put("createdAt", System.currentTimeMillis());
            userData.put("isVerified", false);

            mDatabase.child("users").child(userId).setValue(userData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "User data saved successfully");
                        mAuth.signOut(); // Đăng xuất sau khi lưu dữ liệu
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Save user to database error", e);
                        showToast("Lỗi lưu dữ liệu: " + e.getMessage());
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error saving user data", e);
            showToast("Lỗi lưu dữ liệu người dùng: " + e.getMessage());
        }
    }

    private void navigateToRegisterSuccess() {
        Intent intent = new Intent(this, RegisterSuccessActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}