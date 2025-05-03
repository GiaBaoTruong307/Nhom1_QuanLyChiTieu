package com.example.nhom1_quanlychitieu.ui.ViTien;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.nhom1_quanlychitieu.R;

public class TaiKhoanMoi extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // Cho phép fragment xử lý menu options
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_vitien_taikhoanmoi, container, false);

        // Thiết lập ActionBar
        setupActionBar();

        // Xử lý sự kiện khi nhấn vào "Tiền tài khoản"
        LinearLayout itemTienTaiKhoan = root.findViewById(R.id.item_tien_tai_khoan);
        itemTienTaiKhoan.setOnClickListener(v -> {
            Navigation.findNavController(root).navigate(R.id.action_taikhoan_moi_to_taonen_taikhoan);
        });

        // Xử lý sự kiện khi nhấn vào "Tiền mặt"
        LinearLayout itemTienMat = root.findViewById(R.id.item_tien_mat);
        itemTienMat.setOnClickListener(v -> {
            Navigation.findNavController(root).navigate(R.id.action_taikhoan_moi_to_taonen_tienmat);
        });

        return root;
    }

    private void setupActionBar() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null && activity.getSupportActionBar() != null) {
            ActionBar actionBar = activity.getSupportActionBar();
            actionBar.setTitle("Tài khoản mới");
            actionBar.setDisplayHomeAsUpEnabled(true); // Hiển thị nút quay lại
            actionBar.setDisplayShowHomeEnabled(true); // Hiển thị icon home
            actionBar.show(); // Hiển thị ActionBar
        }
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
            // Quay lại màn hình Quản lý ví tiền
            Navigation.findNavController(requireView()).navigate(R.id.action_taikhoan_moi_to_quanly_vitien);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
