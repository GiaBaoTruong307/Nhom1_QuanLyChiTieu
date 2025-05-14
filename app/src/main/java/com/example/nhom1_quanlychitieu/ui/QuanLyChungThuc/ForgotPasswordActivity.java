package com.example.nhom1_quanlychitieu.ui.QuanLyChungThuc;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom1_quanlychitieu.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ForgotPasswordActivity";

    private EditText etEmail;
    private Button btnResetPassword;
    private ImageButton btnBack;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_forgot_password);

        initializeComponents();
        setupListeners();
    }

    private void initializeComponents() {
        try {
            mAuth = FirebaseAuth.getInstance();
        } catch (Exception e) {
            Log.e(TAG, "Firebase initialization error", e);
            showToast("Lỗi khởi tạo Firebase: " + e.getMessage());
            finish();
            return;
        }

        etEmail = findViewById(R.id.etEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        btnBack = findViewById(R.id.btnBack);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang xử lý...");
        progressDialog.setCancelable(false);
    }

    private void setupListeners() {
        btnResetPassword.setOnClickListener(v -> processResetPassword());
        btnBack.setOnClickListener(v -> finish());
    }

    private void processResetPassword() {
        String email = etEmail.getText().toString().trim();

        if (!validateEmail(email)) {
            return;
        }

        progressDialog.show();
        sendPasswordResetEmail(email);
    }

    private boolean validateEmail(String email) {
        if (email.isEmpty()) {
            showToast("Vui lòng nhập email");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Email không hợp lệ");
            return false;
        }

        return true;
    }

    private void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();

                        if (task.isSuccessful()) {
                            navigateToSuccessScreen(email);
                        } else {
                            handleResetError(task.getException());
                        }
                    }
                });
    }

    private void handleResetError(Exception exception) {
        if (exception instanceof FirebaseAuthInvalidUserException) {
            showToast("Không tìm thấy tài khoản với email này");
        } else {
            String errorMessage = exception != null ?
                    exception.getMessage() : "Không thể gửi email đặt lại mật khẩu";
            showToast(errorMessage);
        }
        Log.e(TAG, "Password reset failed", exception);
    }

    private void navigateToSuccessScreen(String email) {
        Intent intent = new Intent(ForgotPasswordActivity.this, ForgotPasswordSuccessActivity.class);
        intent.putExtra("email", email);
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}