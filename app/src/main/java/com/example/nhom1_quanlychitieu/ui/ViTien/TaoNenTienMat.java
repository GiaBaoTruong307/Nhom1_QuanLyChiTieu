package com.example.nhom1_quanlychitieu.ui.ViTien;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.nhom1_quanlychitieu.R;
import com.google.android.material.textfield.TextInputEditText;

public class TaoNenTienMat extends Fragment {

    private TextInputEditText etTenTienMat;
    private TextInputEditText etSoTien;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // Cho phép fragment xử lý menu options
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_vitien_taonen_tienmat, container, false);

        // Thiết lập ActionBar
        setupActionBar();

        // Khởi tạo các view
        etTenTienMat = root.findViewById(R.id.et_ten_tien_mat);
        etSoTien = root.findViewById(R.id.et_so_tien);
        Button btnSave = root.findViewById(R.id.btn_save);

        // Xử lý sự kiện khi nhấn nút "Lưu"
        btnSave.setOnClickListener(v -> {
            if (validateInput()) {
                saveAccount();
                // Quay lại màn hình Quản lý ví tiền
                Navigation.findNavController(root).navigate(R.id.action_taonen_tienmat_to_quanly_vitien);
            }
        });

        return root;
    }

    private void setupActionBar() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null && activity.getSupportActionBar() != null) {
            ActionBar actionBar = activity.getSupportActionBar();
            actionBar.setTitle("Tạo nền Tiền mặt");
            actionBar.setDisplayHomeAsUpEnabled(true); // Hiển thị nút quay lại
            actionBar.setDisplayShowHomeEnabled(true); // Hiển thị icon home
            actionBar.show(); // Hiển thị ActionBar
        }
    }

    private boolean validateInput() {
        String tenTienMat = etTenTienMat.getText().toString().trim();
        String soTien = etSoTien.getText().toString().trim();

        if (TextUtils.isEmpty(tenTienMat)) {
            etTenTienMat.setError("Vui lòng nhập tên loại tiền mặt");
            return false;
        }

        if (TextUtils.isEmpty(soTien)) {
            etSoTien.setError("Vui lòng nhập số tiền");
            return false;
        }

        return true;
    }

    private void saveAccount() {
        String tenTienMat = etTenTienMat.getText().toString().trim();
        long soTien = Long.parseLong(etSoTien.getText().toString().trim());

        // Tạo đối tượng Account mới
        Account newAccount = new Account(tenTienMat, soTien, R.drawable.ic_money, "cash");

        // Thêm vào AccountDataManager để đồng bộ dữ liệu giữa các màn hình
        QuanLyViTien.getAccountDataManager().addAccount(newAccount);

        Toast.makeText(getContext(), "Đã lưu tiền mặt", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Đảm bảo ActionBar được hiển thị khi fragment này được hiển thị
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null && activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Quay lại màn hình trước đó
            requireActivity().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
