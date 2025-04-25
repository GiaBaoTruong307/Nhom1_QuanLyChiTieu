package com.example.nhom1_quanlychitieu;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom1_quanlychitieu.R;
import com.example.nhom1_quanlychitieu.ResetPasswordActivity;

public class OtpVerificationActivity extends AppCompatActivity {

    private EditText etOtp1, etOtp2, etOtp3, etOtp4;
    private Button btnVerifyOtp;
    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_otp_verification);

        // Get phone from intent
        phone = getIntent().getStringExtra("phone");

        // Initialize views
        etOtp1 = findViewById(R.id.etOtp1);
        etOtp2 = findViewById(R.id.etOtp2);
        etOtp3 = findViewById(R.id.etOtp3);
        etOtp4 = findViewById(R.id.etOtp4);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);

        // Set up OTP input auto-focus
        setupOtpInputs();

        // Set click listeners
        btnVerifyOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Validate input
                String otp = etOtp1.getText().toString() +
                        etOtp2.getText().toString() +
                        etOtp3.getText().toString() +
                        etOtp4.getText().toString();

                if (otp.length() < 4) {
                    Toast.makeText(OtpVerificationActivity.this, "Vui lòng nhập đầy đủ mã OTP", Toast.LENGTH_SHORT).show();
                    return;
                }

                // TODO: Implement actual OTP verification logic
                // For now, just navigate to reset password
                Intent intent = new Intent(OtpVerificationActivity.this, ResetPasswordActivity.class);
                intent.putExtra("phone", phone);
                startActivity(intent);
            }
        });
    }

    private void setupOtpInputs() {
        etOtp1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) {
                    etOtp2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etOtp2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) {
                    etOtp3.requestFocus();
                } else if (s.length() == 0) {
                    etOtp1.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etOtp3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) {
                    etOtp4.requestFocus();
                } else if (s.length() == 0) {
                    etOtp2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etOtp4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    etOtp3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
}