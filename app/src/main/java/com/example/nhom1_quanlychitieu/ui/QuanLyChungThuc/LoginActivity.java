package com.example.nhom1_quanlychitieu.ui.QuanLyChungThuc;

// Import các thư viện cần thiết
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

// Import các annotation
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

// Import các thành phần từ ứng dụng và thư viện Firebase
import com.example.nhom1_quanlychitieu.MainActivity;
import com.example.nhom1_quanlychitieu.R;
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

    // TAG dùng cho logging, giúp lọc log trong Logcat
    private static final String TAG = "LoginActivity";

    // Khai báo các thành phần UI
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword, tvRegister;
    private ImageButton btnTogglePassword;
    private ProgressDialog progressDialog;

    // Khai báo các thành phần Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Gọi phương thức onCreate của lớp cha (bắt buộc)
        super.onCreate(savedInstanceState);

        // Thiết lập layout từ file XML
        setContentView(R.layout.fragment_login);

        // Khởi tạo Firebase
        initializeFirebase();

        // Khởi tạo các thành phần UI
        initializeViews();

        // Thiết lập các sự kiện (listeners)
        setupListeners();

        // Kiểm tra thông báo từ Intent (nếu có)
        checkIncomingMessages();

        // Kiểm tra người dùng hiện tại (nếu đã đăng nhập)
        checkCurrentUser();
    }

    private void initializeFirebase() {
        try {
            // Khởi tạo Firebase Authentication
            mAuth = FirebaseAuth.getInstance();

            // Khởi tạo tham chiếu đến Firebase Realtime Database
            mDatabase = FirebaseDatabase.getInstance().getReference();
        } catch (Exception e) {
            // Ghi log lỗi để debug
            Log.e(TAG, "Firebase initialization error", e);

            // Hiển thị thông báo lỗi cho người dùng
            showToast("Lỗi khởi tạo Firebase: " + e.getMessage());
        }
    }

    private void initializeViews() {
        // Liên kết các biến với các thành phần UI thông qua ID
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister = findViewById(R.id.tvRegister);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);

        // Khởi tạo ProgressDialog để hiển thị khi đang xử lý đăng nhập
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang đăng nhập...");
        progressDialog.setCancelable(false);
    }

    private void setupListeners() {
        // Khi nhấn nút hiển thị/ẩn mật khẩu
        btnTogglePassword.setOnClickListener(v -> togglePasswordVisibility());

        // Khi nhấn nút đăng nhập
        btnLogin.setOnClickListener(v -> handleLogin());

        // Khi nhấn vào text quên mật khẩu
        tvForgotPassword.setOnClickListener(v -> navigateToForgotPassword());

        // Khi nhấn vào text đăng ký
        tvRegister.setOnClickListener(v -> navigateToRegister());
    }

    private void checkIncomingMessages() {
        // Lấy thông báo từ Intent (nếu có)
        String message = getIntent().getStringExtra("message");

        // Hiển thị thông báo nếu có
        if (message != null && !message.isEmpty()) {
            showToast(message);
        }
    }

    private void checkCurrentUser() {
        // Lấy thông tin người dùng hiện tại
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Nếu đã đăng nhập và xác thực email, chuyển đến màn hình chính
        if (currentUser != null && currentUser.isEmailVerified()) {
            navigateToMainActivity();
        }
    }

    private void handleLogin() {
        // Lấy email và mật khẩu từ các trường nhập liệu
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Kiểm tra tính hợp lệ của dữ liệu
        if (!validateInputFields(email, password)) {
            return; // Nếu dữ liệu không hợp lệ, dừng quá trình đăng nhập
        }

        // Hiển thị dialog tiến trình
        progressDialog.show();

        // Gọi phương thức đăng nhập
        loginUser(email, password);
    }

    private boolean validateInputFields(String email, String password) {
        // Kiểm tra email và mật khẩu không được để trống
        if (email.isEmpty() || password.isEmpty()) {
            showToast("Vui lòng nhập đầy đủ thông tin");
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

    private void loginUser(final String email, final String password) {
        // Ghi log để debug
        Log.d(TAG, "Attempting to login with email: " + email);

        // Gọi phương thức đăng nhập của Firebase Authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    // Đóng dialog tiến trình
                    progressDialog.dismiss();

                    if (task.isSuccessful()) {
                        // Đăng nhập thành công
                        handleSuccessfulLogin();
                    } else {
                        // Đăng nhập thất bại
                        handleLoginError(task.getException());
                    }
                });
    }

    private void handleLoginError(Exception exception) {
        // Ghi log lỗi để debug
        Log.w(TAG, "signInWithEmail:failure", exception);

        if (exception instanceof FirebaseAuthInvalidUserException) {
            // Lỗi tài khoản không tồn tại
            showToast("Tài khoản không tồn tại");
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            // Lỗi thông tin đăng nhập không đúng
            showToast("Email hoặc mật khẩu không đúng");
        } else {
            // Các lỗi khác
            showToast("Đăng nhập thất bại: " + exception.getMessage());
        }
    }

    private void handleSuccessfulLogin() {
        // Lấy thông tin người dùng hiện tại
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            if (user.isEmailVerified()) {
                // Nếu đã xác thực email, cập nhật trạng thái đăng nhập
                updateUserLoginStatus(user.getUid());
            } else {
                // Nếu chưa xác thực email, yêu cầu xác thực
                showToast("Vui lòng xác thực email trước khi đăng nhập");
                sendVerificationEmail(user);
                mAuth.signOut(); // Đăng xuất
            }
        }
    }

    private void updateUserLoginStatus(String userId) {
        // Lấy tham chiếu đến node của người dùng trong database
        DatabaseReference userRef = mDatabase.child("users").child(userId);

        // Lắng nghe dữ liệu một lần
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Nếu người dùng đã tồn tại trong database, cập nhật thông tin
                    userRef.child("lastLogin").setValue(System.currentTimeMillis());
                    userRef.child("isVerified").setValue(true);
                    showLoginSuccessDialog();
                } else {
                    // Nếu người dùng chưa tồn tại trong database, tạo mới
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        createNewUserProfile(user.getUid(), user.getEmail());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi kết nối database
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                showToast("Lỗi kết nối cơ sở dữ liệu");
                showLoginSuccessDialog(); // Vẫn cho phép đăng nhập nếu có lỗi DB
            }
        });
    }

    private void createNewUserProfile(String userId, String email) {
        // Lấy tham chiếu đến node của người dùng trong database
        DatabaseReference userRef = mDatabase.child("users").child(userId);

        // Thiết lập các thông tin cơ bản
        userRef.child("email").setValue(email);
        userRef.child("createdAt").setValue(System.currentTimeMillis());
        userRef.child("lastLogin").setValue(System.currentTimeMillis());
        userRef.child("isVerified").setValue(true);

        // Hiển thị dialog đăng nhập thành công
        showLoginSuccessDialog();
    }

    private void sendVerificationEmail(FirebaseUser user) {
        // Gọi phương thức gửi email xác thực của Firebase
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Gửi email thành công
                        showToast("Email xác thực đã được gửi lại");
                    } else {
                        // Gửi email thất bại
                        showToast("Không thể gửi email xác thực: " + task.getException().getMessage());
                    }
                });
    }

    private void navigateToMainActivity() {
        // Tạo Intent để chuyển đến MainActivity
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);

        // Thiết lập flags để xóa tất cả Activity trước đó
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Chuyển đến Activity mới
        startActivity(intent);

        // Đóng Activity hiện tại
        finish();
    }

    private void navigateToForgotPassword() {
        // Tạo Intent để chuyển đến ForgotPasswordActivity
        Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);

        // Chuyển đến Activity mới
        startActivity(intent);
    }

    private void navigateToRegister() {
        // Tạo Intent để chuyển đến RegisterActivity
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);

        // Chuyển đến Activity mới
        startActivity(intent);
    }

    private void togglePasswordVisibility() {
        // Kiểm tra xem mật khẩu đang hiển thị hay đang ẩn
        boolean isVisible = etPassword.getInputType() ==
                (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

        // Xác định inputType mới dựa trên trạng thái hiện tại
        int inputType = isVisible
                ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD  // Ẩn mật khẩu
                : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;  // Hiển thị mật khẩu

        // Xác định icon mới dựa trên trạng thái hiện tại
        int iconResource = isVisible ? R.drawable.eye_closed : R.drawable.eye_open;

        // Cập nhật inputType cho EditText
        etPassword.setInputType(inputType);

        // Cập nhật icon cho button
        btnTogglePassword.setImageResource(iconResource);

        // Giữ con trỏ ở cuối văn bản sau khi thay đổi inputType
        etPassword.setSelection(etPassword.getText().length());
    }

    private void showLoginSuccessDialog() {
        // Tạo dialog mới
        final Dialog dialog = new Dialog(this);

        // Không hiển thị tiêu đề
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Thiết lập layout từ file XML
        dialog.setContentView(R.layout.fragment_login_success);

        // Thiết lập thuộc tính của cửa sổ dialog
        Window window = dialog.getWindow();
        if (window != null) {
            // Thiết lập kích thước
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

            // Thiết lập nền trong suốt
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Lấy nút xác nhận từ dialog
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);

        // Thiết lập sự kiện khi nhấn nút xác nhận
        btnConfirm.setOnClickListener(v -> {
            // Đóng dialog
            dialog.dismiss();

            // Chuyển đến màn hình chính
            navigateToMainActivity();
        });

        // Không cho phép hủy dialog bằng cách nhấn bên ngoài
        dialog.setCancelable(false);

        // Hiển thị dialog
        dialog.show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}