package com.example.nhom1_quanlychitieu.ui.QuanLyChungThuc;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom1_quanlychitieu.MainActivity;
import com.example.nhom1_quanlychitieu.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    // UI components
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword, tvRegister;
    private ImageButton btnTogglePassword;
    private ProgressDialog progressDialog;

    // Firebase components
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_login);

        initializeFirebase();
        initializeViews();
        setupListeners();
        checkIncomingMessages();
        checkCurrentUser();
    }

    private void initializeFirebase() {
        try {
            mAuth = FirebaseAuth.getInstance();
            mDatabase = FirebaseDatabase.getInstance().getReference();
        } catch (Exception e) {
            Log.e(TAG, "Firebase initialization error", e);
            showToast("Lỗi khởi tạo Firebase: " + e.getMessage());
        }
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister = findViewById(R.id.tvRegister);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang đăng nhập...");
        progressDialog.setCancelable(false);
    }

    private void setupListeners() {
        btnTogglePassword.setOnClickListener(v -> togglePasswordVisibility());
        btnLogin.setOnClickListener(v -> handleLogin());
        tvForgotPassword.setOnClickListener(v -> navigateToForgotPassword());
        tvRegister.setOnClickListener(v -> navigateToRegister());
    }

    private void checkIncomingMessages() {
        String message = getIntent().getStringExtra("message");
        if (message != null && !message.isEmpty()) {
            showToast(message);
        }
    }

    private void checkCurrentUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            // Người dùng đã đăng nhập và đã xác thực email
            navigateToMainActivity();
        }
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!validateInputFields(email, password)) {
            return;
        }

        progressDialog.show();
        loginUser(email, password);
    }

    private boolean validateInputFields(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            showToast("Vui lòng nhập đầy đủ thông tin");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Email không hợp lệ");
            return false;
        }

        return true;
    }

    private void loginUser(final String email, final String password) {
        Log.d(TAG, "Attempting to login with email: " + email);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();

                        if (task.isSuccessful()) {
                            handleSuccessfulLogin();
                        } else {
                            handleLoginError(task.getException());
                        }
                    }
                });
    }

    private void handleLoginError(Exception exception) {
        Log.w(TAG, "signInWithEmail:failure", exception);

        if (exception instanceof FirebaseAuthInvalidUserException) {
            showToast("Tài khoản không tồn tại");
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            showToast("Email hoặc mật khẩu không đúng");
        } else {
            showToast("Đăng nhập thất bại: " + exception.getMessage());
        }
    }

    private void handleSuccessfulLogin() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            if (user.isEmailVerified()) {
                // Kiểm tra và cập nhật thông tin người dùng trong Realtime Database
                updateUserLoginStatus(user.getUid());
            } else {
                showToast("Vui lòng xác thực email trước khi đăng nhập");
                sendVerificationEmail(user);
                mAuth.signOut();
            }
        }
    }

    private void updateUserLoginStatus(String userId) {
        DatabaseReference userRef = mDatabase.child("users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Cập nhật thông tin đăng nhập
                    userRef.child("lastLogin").setValue(System.currentTimeMillis());
                    userRef.child("isVerified").setValue(true);

                    showLoginSuccessDialog();
                } else {
                    // Tạo thông tin người dùng nếu chưa tồn tại
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        createNewUserProfile(user.getUid(), user.getEmail());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                showToast("Lỗi kết nối cơ sở dữ liệu");
                showLoginSuccessDialog(); // Vẫn cho phép đăng nhập nếu có lỗi DB
            }
        });
    }

    private void createNewUserProfile(String userId, String email) {
        DatabaseReference userRef = mDatabase.child("users").child(userId);

        userRef.child("email").setValue(email);
        userRef.child("createdAt").setValue(System.currentTimeMillis());
        userRef.child("lastLogin").setValue(System.currentTimeMillis());
        userRef.child("isVerified").setValue(true);

        showLoginSuccessDialog();
    }

    private void sendVerificationEmail(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showToast("Email xác thực đã được gửi lại");
                    } else {
                        showToast("Không thể gửi email xác thực: " + task.getException().getMessage());
                    }
                });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToForgotPassword() {
        Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
        startActivity(intent);
    }

    private void navigateToRegister() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    private void togglePasswordVisibility() {
        boolean isVisible = etPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

        int inputType = isVisible
                ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
                : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;

        int iconResource = isVisible ? R.drawable.eye_closed : R.drawable.eye_open;

        etPassword.setInputType(inputType);
        btnTogglePassword.setImageResource(iconResource);
        etPassword.setSelection(etPassword.getText().length());
    }

    private void showLoginSuccessDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.fragment_login_success);

        // Thiết lập kích thước dialog
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Thiết lập nút xác nhận
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            navigateToMainActivity();
        });

        dialog.setCancelable(false);
        dialog.show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}