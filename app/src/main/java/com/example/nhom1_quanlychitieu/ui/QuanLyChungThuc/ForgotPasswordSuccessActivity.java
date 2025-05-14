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

            // Lấy email từ intent
            String email = getIntent().getStringExtra("email");

            // Khởi tạo các thành phần giao diện
            btnLogin = findViewById(R.id.btnLogin);
            tvSuccessMessage = findViewById(R.id.tvSuccessMessage);

            // Cập nhật thông báo với email
            String message = "Email đặt lại mật khẩu đã được gửi đến " + email + ". Vui lòng kiểm tra hộp thư của bạn và làm theo hướng dẫn để đặt lại mật khẩu.";
            if (tvSuccessMessage != null) {
                tvSuccessMessage.setText(message);
            }

            // Thiết lập sự kiện cho nút đăng nhập
            if (btnLogin != null) {
                btnLogin.setOnClickListener(v -> navigateToLogin());
            }
        } catch (Exception e) {
            Log.e(TAG, "onCreate error: " + e.getMessage());
            Toast.makeText(this, "Lỗi khởi tạo màn hình: " + e.getMessage(), Toast.LENGTH_LONG).show();
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