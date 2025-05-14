package com.example.nhom1_quanlychitieu.ui.QuanLyChungThuc;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom1_quanlychitieu.R;

public class RegisterSuccessActivity extends AppCompatActivity {

    private static final String TAG = "RegisterSuccessActivity";
    private static final String SUCCESS_MESSAGE = "Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản.";
    private static final String LOGIN_MESSAGE = "Đăng ký thành công. Vui lòng xác thực email và đăng nhập.";

    private Button btnLogin;
    private TextView tvSuccessMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_register_success);

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        btnLogin = findViewById(R.id.btnLogin);
        tvSuccessMessage = findViewById(R.id.tvSuccessMessage);

        if (tvSuccessMessage != null) {
            tvSuccessMessage.setText(SUCCESS_MESSAGE);
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
            intent.putExtra("message", LOGIN_MESSAGE);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Login navigation error", e);
            showToast("Lỗi chuyển màn hình: " + e.getMessage());
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}