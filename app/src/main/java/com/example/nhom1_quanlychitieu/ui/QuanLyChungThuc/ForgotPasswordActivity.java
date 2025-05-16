package com.example.nhom1_quanlychitieu.ui.QuanLyChungThuc;

// Import các thư viện cần thiết
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

// Sử dụng AppCompatActivity để tương thích với các phiên bản Android cũ
import androidx.appcompat.app.AppCompatActivity;

// Import các thành phần từ thư viện Firebase
import com.example.nhom1_quanlychitieu.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class ForgotPasswordActivity extends AppCompatActivity {

    // TAG dùng cho logging, giúp lọc log trong Logcat
    private static final String TAG = "ForgotPasswordActivity";

    // Khai báo các thành phần UI
    private EditText etEmail;
    private Button btnResetPassword;
    private ImageButton btnBack;
    private ProgressDialog progressDialog;

    // Khai báo Firebase Authentication
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Gọi phương thức onCreate của lớp cha (bắt buộc)
        super.onCreate(savedInstanceState);

        // Thiết lập layout từ file XML
        setContentView(R.layout.fragment_forgot_password);

        // Khởi tạo các thành phần
        initializeComponents();

        // Thiết lập các sự kiện (listeners)
        setupListeners();
    }

    private void initializeComponents() {
        try {
            // Khởi tạo Firebase Authentication
            mAuth = FirebaseAuth.getInstance();
        } catch (Exception e) {
            // Ghi log lỗi để debug
            Log.e(TAG, "Firebase initialization error", e);

            // Hiển thị thông báo lỗi cho người dùng
            showToast("Lỗi khởi tạo Firebase: " + e.getMessage());

            // Đóng Activity nếu không khởi tạo được Firebase
            finish();
            return;
        }

        // Liên kết các biến với các thành phần UI thông qua ID
        etEmail = findViewById(R.id.etEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        btnBack = findViewById(R.id.btnBack);

        // Khởi tạo ProgressDialog để hiển thị khi đang xử lý
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang xử lý...");
        progressDialog.setCancelable(false);
    }

    private void setupListeners() {
        // Khi nhấn nút đặt lại mật khẩu, gọi phương thức xử lý
        btnResetPassword.setOnClickListener(v -> processResetPassword());

        // Khi nhấn nút quay lại, đóng Activity hiện tại
        btnBack.setOnClickListener(v -> finish());
    }

    private void processResetPassword() {
        // Lấy email từ trường nhập liệu và loại bỏ khoảng trắng thừa
        String email = etEmail.getText().toString().trim();

        // Kiểm tra tính hợp lệ của email
        if (!validateEmail(email)) {
            return; // Nếu email không hợp lệ, dừng quá trình
        }

        // Hiển thị dialog tiến trình
        progressDialog.show();

        // Gửi yêu cầu đặt lại mật khẩu
        sendPasswordResetEmail(email);
    }

    private boolean validateEmail(String email) {
        // Kiểm tra email không được để trống
        if (email.isEmpty()) {
            showToast("Vui lòng nhập email");
            return false;
        }

        // Kiểm tra định dạng email hợp lệ
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Email không hợp lệ");
            return false;
        }

        // Nếu tất cả kiểm tra đều pass, trả về true
        return true;
    }

    private void sendPasswordResetEmail(String email) {
        // Gọi phương thức gửi email đặt lại mật khẩu của Firebase
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    // Đóng dialog tiến trình
                    progressDialog.dismiss();

                    if (task.isSuccessful()) {
                        // Gửi email thành công, chuyển đến màn hình thành công
                        navigateToSuccessScreen(email);
                    } else {
                        // Gửi email thất bại, xử lý lỗi
                        handleResetError(task.getException());
                    }
                });
    }

    private void handleResetError(Exception exception) {
        if (exception instanceof FirebaseAuthInvalidUserException) {
            // Lỗi không tìm thấy tài khoản với email đã nhập
            showToast("Không tìm thấy tài khoản với email này");
        } else {
            // Các lỗi khác
            String errorMessage = exception != null ?
                    exception.getMessage() : "Không thể gửi email đặt lại mật khẩu";
            showToast(errorMessage);
        }
        // Ghi log lỗi để debug
        Log.e(TAG, "Password reset failed", exception);
    }

    private void navigateToSuccessScreen(String email) {
        // Tạo Intent để chuyển đến ForgotPasswordSuccessActivity
        Intent intent = new Intent(ForgotPasswordActivity.this, ForgotPasswordSuccessActivity.class);

        // Truyền email qua Intent để hiển thị trên màn hình thành công
        intent.putExtra("email", email);

        // Chuyển đến Activity mới
        startActivity(intent);

        // Đóng Activity hiện tại để người dùng không thể quay lại
        finish();
    }
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}