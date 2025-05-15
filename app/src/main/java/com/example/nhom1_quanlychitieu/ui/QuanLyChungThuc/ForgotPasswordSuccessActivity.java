package com.example.nhom1_quanlychitieu.ui.QuanLyChungThuc;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom1_quanlychitieu.R;

public class ForgotPasswordSuccessActivity extends AppCompatActivity {

    private static final String TAG = "ForgotPasswordSuccess";

    private TextView tvSuccessMessage;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.fragment_forgot_password_success);

            String email = getIntent().getStringExtra("email");
            initializeViews(email);
            setupListeners();
        } catch (Exception e) {
            Log.e(TAG, "onCreate error: " + e.getMessage());
            Toast.makeText(this, "Lỗi khởi tạo màn hình: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initializeViews(String email) {
        btnLogin = findViewById(R.id.btnLogin);
        tvSuccessMessage = findViewById(R.id.tvSuccessMessage);

        if (email != null && tvSuccessMessage != null) {
            String message = "Email đặt lại mật khẩu đã được gửi đến " + email +
                    ". Vui lòng kiểm tra hộp thư của bạn và làm theo hướng dẫn để đặt lại mật khẩu.";
            tvSuccessMessage.setText(message);
        }
    }

    private void setupListeners() {
        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> navigateToLogin());
        }
    }

    private void navigateToLogin() {
        try {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Login navigation error: " + e.getMessage());
            Toast.makeText(this, "Lỗi chuyển màn hình: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}