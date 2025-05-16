package com.example.nhom1_quanlychitieu.ui.HoSo;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.nhom1_quanlychitieu.R;
import com.example.nhom1_quanlychitieu.ui.QuanLyChungThuc.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HoSo extends Fragment implements ProfileUpdateListener {

    private static final String TAG = "HoSoFragment";
    private static final int REQUEST_EDIT_PROFILE = 1001;

    // Các khóa cho Firebase Database
    private static final String USERS_PATH = "users";
    private static final String FULL_NAME_KEY = "fullName";
    private static final String USERNAME_KEY = "username";
    private static final String NOTIFICATIONS_ENABLED_KEY = "notificationsEnabled";
    private static final String LANGUAGE_KEY = "language";

    // UI components
    private TextView tvUsername, tvEmail, tvAppVersion;
    private Button btnLogout;
    private CardView cardEditProfile, cardChangePassword, cardLanguage;
    private SwitchCompat switchNotifications;

    // Firebase components
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    //Gọi khi Fragment được tạo, trước khi giao diện được xây dựng.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFirebase();
    }
//tạo và trả về giao diện
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_hoso, container, false);
    }
//gọi ngay sau khi giao diện được tạo
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //lấy tp giao diện
        initViews(view);
        //thiết lập các sk
        setupListeners();
        //tải data ng dùng
        loadUserData();
    }

    private void initFirebase() {
        try {
            mAuth = FirebaseAuth.getInstance();
            mDatabase = FirebaseDatabase.getInstance().getReference();
            currentUser = mAuth.getCurrentUser();

            if (currentUser == null) {
                navigateToLogin();
            }
        } catch (Exception e) {
            Log.e(TAG, "Firebase initialization error", e);
            showToast("Lỗi khởi tạo Firebase: " + e.getMessage());
        }
    }

    private void initViews(View view) {
        tvUsername = view.findViewById(R.id.tvUsername);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvAppVersion = view.findViewById(R.id.tvAppVersion);
        btnLogout = view.findViewById(R.id.btnLogout);
        cardEditProfile = view.findViewById(R.id.cardEditProfile);
        cardChangePassword = view.findViewById(R.id.cardChangePassword);
        cardLanguage = view.findViewById(R.id.cardLanguage);
        switchNotifications = view.findViewById(R.id.switchNotifications);

        // Set app version
        setAppVersion();
    }

    private void setAppVersion() {
        try {
            String versionName = getContext().getPackageManager()
                    .getPackageInfo(getContext().getPackageName(), 0).versionName;
            tvAppVersion.setText(getString(R.string.app_version_format, versionName));
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Error getting package info", e);
            tvAppVersion.setText(getString(R.string.app_version_format, "1.0.0"));
        }
    }
//Gắn các sự kiện tương tác vào các thành phần giao diện
    private void setupListeners() {
        btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());
        cardEditProfile.setOnClickListener(v -> navigateToEditProfile());
        cardChangePassword.setOnClickListener(v -> navigateToChangePassword());
        cardLanguage.setOnClickListener(v -> showLanguageSelectionDialog());
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateNotificationPreference(isChecked));
    }
//Tải và hiển thị thông tin người dùng từ Firebase.
    private void loadUserData() {
        if (currentUser == null) {
            navigateToLogin();
            return;
        }

        // Set email from FirebaseUser
        tvEmail.setText(currentUser.getEmail());

        // Load additional user data from Realtime Database
        mDatabase.child(USERS_PATH).child(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            updateUserDisplay(dataSnapshot);
                        } else {
                            tvUsername.setText(R.string.no_username);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "loadUserData:onCancelled", databaseError.toException());
                        showToast("Không thể tải thông tin người dùng");
                    }
                });
    }
//Cập nhật giao diện với dữ liệu từ Firebase.
    private void updateUserDisplay(DataSnapshot dataSnapshot) {
        // Ưu tiên hiển thị Họ Tên nếu có
        String fullName = dataSnapshot.child(FULL_NAME_KEY).getValue(String.class);
        if (fullName != null && !fullName.isEmpty()) {
            tvUsername.setText(fullName);
        } else {
            // Nếu không có Họ Tên, hiển thị Username
            String username = dataSnapshot.child(USERNAME_KEY).getValue(String.class);
            if (username != null && !username.isEmpty()) {
                tvUsername.setText(username);
            } else {
                tvUsername.setText(R.string.no_username);
            }
        }

        // Get notification preference
        Boolean notificationsEnabled = dataSnapshot.child(NOTIFICATIONS_ENABLED_KEY).getValue(Boolean.class);
        if (notificationsEnabled != null) {
            switchNotifications.setChecked(notificationsEnabled);
        }
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.logout_confirmation_title)
                .setMessage(R.string.logout_confirmation_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> logout())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void logout() {
        try {
            mAuth.signOut();
            navigateToLogin();
            showToast("Đăng xuất thành công");
        } catch (Exception e) {
            Log.e(TAG, "Logout error", e);
            showToast("Lỗi đăng xuất: " + e.getMessage());
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void navigateToEditProfile() {
        Intent intent = new Intent(getActivity(), EditProfileActivity.class);
        startActivityForResult(intent, REQUEST_EDIT_PROFILE);
    }

    private void navigateToChangePassword() {
        Intent intent = new Intent(getActivity(), ChangePasswordActivity.class);
        startActivity(intent);
    }

    private void showLanguageSelectionDialog() {
        final String[] languages = {"Tiếng Việt", "English"};

        new AlertDialog.Builder(getContext())
                .setTitle(R.string.select_language)
                .setItems(languages, (dialog, which) -> {
                    // Save selected language
                    String selectedLanguage = languages[which];
                    saveLanguagePreference(selectedLanguage);
                    showToast("Đã chọn: " + selectedLanguage);
                    // Thực tế cần thêm code để thay đổi ngôn ngữ ứng dụng
                })
                .show();
    }

    private void updateNotificationPreference(boolean enabled) {
        if (currentUser != null) {
            mDatabase.child(USERS_PATH).child(currentUser.getUid())
                    .child(NOTIFICATIONS_ENABLED_KEY).setValue(enabled)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Notification preference updated");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error updating notification preference", e);
                        showToast("Không thể cập nhật cài đặt thông báo");
                        // Revert switch state
                        switchNotifications.setChecked(!enabled);
                    });
        }
    }

    private void saveLanguagePreference(String language) {
        if (currentUser != null) {
            mDatabase.child(USERS_PATH).child(currentUser.getUid())
                    .child(LANGUAGE_KEY).setValue(language)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Language preference updated");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error updating language preference", e);
                        showToast("Không thể cập nhật cài đặt ngôn ngữ");
                    });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_EDIT_PROFILE && resultCode == EditProfileActivity.RESULT_PROFILE_UPDATED) {
            // Cập nhật UI ngay lập tức với dữ liệu mới
            if (data != null && data.hasExtra(FULL_NAME_KEY)) {
                String newFullName = data.getStringExtra(FULL_NAME_KEY);
                tvUsername.setText(newFullName);
            } else {
                // Nếu không có dữ liệu trả về, tải lại từ Firebase
                loadUserData();
            }
        }
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onProfileUpdated() {
        loadUserData();
    }
}