package com.example.nhom1_quanlychitieu.ui.QuanLyChungThuc;

// Import các thư viện cần thiết
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

// Sử dụng AppCompatActivity để tương thích với các phiên bản Android cũ
import androidx.appcompat.app.AppCompatActivity;

// Import các thành phần từ thư viện Firebase
import com.example.nhom1_quanlychitieu.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

// Import các cấu trúc dữ liệu
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    // TAG dùng cho logging, giúp lọc log trong Logcat
    private static final String TAG = "RegisterActivity";

    // Độ dài tối thiểu của mật khẩu
    private static final int MIN_PASSWORD_LENGTH = 6;

    // Khai báo các thành phần UI
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private EditText etUsername;
    private Button btnRegister;
    private TextView tvLogin;
    private TextView tvRegisterLabel;
    private ImageButton btnTogglePassword;
    private ImageButton btnToggleConfirmPassword;
    private ProgressDialog progressDialog;

    // Khai báo các thành phần Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // Phương thức được gọi khi Activity được tạo
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Thiết lập layout từ file XML
        setContentView(R.layout.fragment_register);
        initFirebase();
        initViews();
        setupListeners();
    }
    private void initFirebase() {
        try {
            // Lấy instance của FirebaseAuth
            mAuth = FirebaseAuth.getInstance();

            // Lấy tham chiếu đến root của Realtime Database
            mDatabase = FirebaseDatabase.getInstance().getReference();
        } catch (Exception e) {
            // Ghi log lỗi để debug
            Log.e(TAG, "Firebase initialization error", e);

            // Hiển thị thông báo lỗi cho người dùng
            showToast("Lỗi khởi tạo Firebase: " + e.getMessage());

            // Đóng Activity nếu không khởi tạo được Firebase
            finish();
        }
    }
    private void initViews() {
        // Liên kết các biến với các thành phần UI thông qua ID
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etUsername = findViewById(R.id.etUsername);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        tvRegisterLabel = findViewById(R.id.tvRegisterLabel);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        btnToggleConfirmPassword = findViewById(R.id.btnToggleConfirmPassword);

        // Khởi tạo ProgressDialog để hiển thị khi đang xử lý đăng ký
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang đăng ký...");
        progressDialog.setCancelable(false);
    }
    private void setupListeners() {
        // Khi nhấn nút đăng ký, gọi phương thức xử lý đăng ký
        btnRegister.setOnClickListener(v -> handleRegistration());

        // Khi nhấn vào tiêu đề, cũng kích hoạt nút đăng ký
        tvRegisterLabel.setOnClickListener(v -> btnRegister.performClick());

        // Khi nhấn vào text đăng nhập, chuyển đến màn hình đăng nhập
        tvLogin.setOnClickListener(v -> navigateToLogin());

        // Khi nhấn vào nút hiển thị/ẩn mật khẩu
        btnTogglePassword.setOnClickListener(v ->
                togglePasswordVisibility(etPassword, btnTogglePassword));

        // Khi nhấn vào nút hiển thị/ẩn xác nhận mật khẩu
        btnToggleConfirmPassword.setOnClickListener(v ->
                togglePasswordVisibility(etConfirmPassword, btnToggleConfirmPassword));
    }
    private void togglePasswordVisibility(EditText editText, ImageButton button) {
        boolean isVisible = editText.getInputType() ==
                (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

        int inputType = isVisible
                ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
                : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;

        // Xác định icon mới dựa trên trạng thái hiện tại
        int iconResource = isVisible ? R.drawable.eye_closed : R.drawable.eye_open;

        // Cập nhật inputType cho EditText
        editText.setInputType(inputType);

        // Cập nhật icon cho button
        button.setImageResource(iconResource);

        // Giữ con trỏ ở cuối văn bản sau khi thay đổi inputType
        editText.setSelection(editText.getText().length());
    }

    private void handleRegistration() {
        // Lấy dữ liệu từ các trường nhập liệu và loại bỏ khoảng trắng thừa
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String username = etUsername.getText().toString().trim();

        // Kiểm tra tính hợp lệ của dữ liệu
        if (!validateInputFields(email, password, confirmPassword, username)) {
            return;
        }

        // Hiển thị dialog tiến trình
        progressDialog.show();

        // Gọi phương thức đăng ký với Firebase
        registerWithFirebaseAuth(email, password, username);
    }
    private boolean validateInputFields(String email, String password, String confirmPassword, String username) {
        // Kiểm tra các trường bắt buộc không được để trống
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showToast("Vui lòng nhập đầy đủ thông tin email và mật khẩu");
            return false;
        }

        // Kiểm tra tên người dùng không được để trống
        if (username.isEmpty()) {
            showToast("Vui lòng nhập tên người dùng");
            return false;
        }

        // Kiểm tra định dạng email hợp lệ
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Email không hợp lệ");
            return false;
        }

        // Kiểm tra mật khẩu và xác nhận mật khẩu khớp nhau
        if (!password.equals(confirmPassword)) {
            showToast("Mật khẩu không khớp");
            return false;
        }

        // Kiểm tra độ dài mật khẩu
        if (password.length() < MIN_PASSWORD_LENGTH) {
            showToast("Mật khẩu phải có ít nhất " + MIN_PASSWORD_LENGTH + " ký tự");
            return false;
        }

        // Nếu tất cả kiểm tra đều pass, trả về true
        return true;
    }

    private void registerWithFirebaseAuth(String email, String password, String username) {
        // Gọi phương thức tạo tài khoản của Firebase Authentication
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
            // Lỗi email đã được sử dụng
            showToast("Email đã được sử dụng");
        } else if (exception instanceof FirebaseAuthWeakPasswordException) {
            // Lỗi mật khẩu quá yếu
            showToast("Mật khẩu quá yếu. Vui lòng chọn mật khẩu mạnh hơn");
        } else {
            // Các lỗi khác
            showToast("Đăng ký thất bại: " +
                    (exception != null ? exception.getMessage() : "Lỗi không xác định"));
        }
        // Ghi log lỗi để debug
        Log.e(TAG, "Registration error", exception);
    }

    private void sendVerificationEmail(FirebaseUser user, String username, String password) {
        // Gọi phương thức gửi email xác thực của Firebase
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    // Đóng dialog tiến trình
                    progressDialog.dismiss();

                    if (task.isSuccessful()) {
                        // Gửi email thành công
                        // Lưu thông tin người dùng vào database
                        saveUserToDatabase(user.getUid(), user.getEmail(), username, password);
                        // Chuyển đến màn hình đăng ký thành công
                        navigateToRegisterSuccess();
                    } else {
                        // Gửi email thất bại
                        showToast("Không thể gửi email xác thực: " + task.getException().getMessage());
                        // Xóa tài khoản đã tạo nếu không gửi được email xác thực
                        user.delete();
                    }
                });
    }
    // Lưu thông tin người dùng vào Firebase Realtime Database
    private void saveUserToDatabase(String userId, String email, String username, String password) {
        try {
            // Tạo Map chứa thông tin người dùng
            Map<String, Object> userData = new HashMap<>();
            userData.put("email", email);
            userData.put("username", username);
            userData.put("fullName", username);
            userData.put("password", password);
            userData.put("createdAt", System.currentTimeMillis());
            userData.put("isVerified", false);

            // Lưu thông tin vào database
            mDatabase.child("users").child(userId).setValue(userData)
                    .addOnSuccessListener(aVoid -> {
                        // Lưu thành công
                        Log.d(TAG, "User data saved successfully");
                        // Đăng xuất sau khi lưu dữ liệu
                        mAuth.signOut();
                    })
                    .addOnFailureListener(e -> {
                        // Lưu thất bại
                        Log.e(TAG, "Save user to database error", e);
                        showToast("Lỗi lưu dữ liệu: " + e.getMessage());
                    });
        } catch (Exception e) {
            // Xử lý các lỗi khác
            Log.e(TAG, "Error saving user data", e);
            showToast("Lỗi lưu dữ liệu người dùng: " + e.getMessage());
        }
    }

    private void navigateToRegisterSuccess() {
        // Tạo Intent để chuyển đến RegisterSuccessActivity
        Intent intent = new Intent(this, RegisterSuccessActivity.class);
        startActivity(intent);
        // Đóng Activity hiện tại để người dùng không thể quay lại
        finish();
    }

    private void navigateToLogin() {
        // Tạo Intent để chuyển đến LoginActivity
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        // Đóng Activity hiện tại để người dùng không thể quay lại
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}