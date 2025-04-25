package com.example.nhom1_quanlychitieu;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom1_quanlychitieu.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etPhone;
    private Button btnGetOtp;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_forgot_password);

        // Hiển thị nút Back trên ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quên mật khẩu");
        }

        // Initialize views
        etPhone = findViewById(R.id.etPhone);
        btnGetOtp = findViewById(R.id.btnGetOtp);
        btnBack = findViewById(R.id.btnBack);

        // Set click listeners
        btnGetOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Validate input
                String phone = etPhone.getText().toString().trim();

                if (phone.isEmpty()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show();
                    return;
                }

                // TODO: Implement actual OTP sending logic
                // For now, just navigate to OTP verification
                Intent intent = new Intent(ForgotPasswordActivity.this, OtpVerificationActivity.class);
                intent.putExtra("phone", phone);
                startActivity(intent);
            }
        });

        // Thêm xử lý sự kiện cho nút Back trong layout
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Quay lại màn hình trước đó
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Quay lại màn hình trước đó khi nhấn nút Back trên ActionBar
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}