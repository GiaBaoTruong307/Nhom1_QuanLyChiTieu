package com.example.nhom1_quanlychitieu.ui.QuanLyChungThuc;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom1_quanlychitieu.R;

public class SuccessActivity extends AppCompatActivity {

    private static final String TAG = "SuccessActivity";
    private static final String DEFAULT_SUCCESS_MESSAGE = "Thao tác thành công!";

    private TextView tvSuccessMessage;
    private Button btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_login_success);

        setupBackPressedCallback();
        initializeViews();
        setupListeners();
    }

    private void setupBackPressedCallback() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToLogin();
            }
        });
    }

    private void initializeViews() {
        tvSuccessMessage = findViewById(R.id.tvSuccessMessage);
        btnConfirm = findViewById(R.id.btnConfirm);

        String message = getIntent().getStringExtra("message");
        if (message == null || message.isEmpty()) {
            message = DEFAULT_SUCCESS_MESSAGE;
        }

        if (tvSuccessMessage != null) {
            tvSuccessMessage.setText(message);
        }
    }

    private void setupListeners() {
        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> navigateToLogin());
        }
    }

    private void navigateToLogin() {
        try {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to LoginActivity", e);
            showToast("Lỗi chuyển màn hình: " + e.getMessage());
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}