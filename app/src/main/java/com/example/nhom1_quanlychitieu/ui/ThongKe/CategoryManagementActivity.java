package com.example.nhom1_quanlychitieu.ui.ThongKe;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom1_quanlychitieu.R;
import com.example.nhom1_quanlychitieu.model.Category;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryManagementActivity extends AppCompatActivity {

    private static final String TAG = "CategoryManagement";

    // Interface cho sự kiện chọn icon
    private interface OnIconSelectedListener {
        void onIconSelected(int position);
    }

    // UI components
    private ImageButton btnBack;
    private RecyclerView rvCategories;
    private Button btnAddCategory;
    private TextView tvNoCategories;

    // Data
    private CategoryAdapter adapter;
    private final List<Category> categories = new ArrayList<>();

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String userId;

    // Mảng các icon mặc định
    private final int[] defaultIcons = {
            R.drawable.thongke_ic_food,
            R.drawable.thongke_ic_transport,
            R.drawable.thongke_ic_shopping,
            R.drawable.thongke_ic_entertainment,
            R.drawable.thongke_ic_medical,
            R.drawable.thongke_ic_other
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_thongke_category_management);

        initFirebase();
        if (userId == null) return;

        initializeViews();
        setupRecyclerView();
        setupEventListeners();
        loadCategories();
    }

    private void initFirebase() {
        try {
            mAuth = FirebaseAuth.getInstance();
            mDatabase = FirebaseDatabase.getInstance().getReference();

            if (mAuth.getCurrentUser() != null) {
                userId = mAuth.getCurrentUser().getUid();
            } else {
                Toast.makeText(this, "Vui lòng đăng nhập để quản lý danh mục", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase", e);
            Toast.makeText(this, "Lỗi khởi tạo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        rvCategories = findViewById(R.id.rvCategories);
        btnAddCategory = findViewById(R.id.btnAddCategory);
        tvNoCategories = findViewById(R.id.tvNoCategories);
    }

    private void setupRecyclerView() {
        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CategoryAdapter();
        rvCategories.setAdapter(adapter);
    }

    private void setupEventListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());
    }

    /**
     * Tải danh sách danh mục từ Firebase
     */
    private void loadCategories() {
        if (userId == null) {
            tvNoCategories.setVisibility(View.VISIBLE);
            return;
        }

        mDatabase.child("categories").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                categories.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        Category category = snapshot.getValue(Category.class);
                        if (category != null) {
                            category.setId(snapshot.getKey());
                            categories.add(category);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing category", e);
                    }
                }

                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                Toast.makeText(CategoryManagementActivity.this,
                        "Lỗi tải dữ liệu: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Cập nhật giao diện người dùng
     */
    private void updateUI() {
        if (categories.isEmpty()) {
            tvNoCategories.setVisibility(View.VISIBLE);
            rvCategories.setVisibility(View.GONE);
        } else {
            tvNoCategories.setVisibility(View.GONE);
            rvCategories.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }

    private void showAddCategoryDialog() {
        showCategoryDialog(null);
    }

    private void showEditCategoryDialog(Category category) {
        showCategoryDialog(category);
    }

    private void showCategoryDialog(Category category) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.fragment_thongke_dialog_add_edit_category);

        // Thiết lập kích thước dialog
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        // Ánh xạ các thành phần trong dialog
        TextView tvDialogTitle = dialog.findViewById(R.id.tvDialogTitle);
        EditText etCategoryName = dialog.findViewById(R.id.etCategoryName);
        RecyclerView rvIcons = dialog.findViewById(R.id.rvIcons);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnSave = dialog.findViewById(R.id.btnSave);

        // Thiết lập tiêu đề dialog
        tvDialogTitle.setText(category == null ? "Thêm danh mục mới" : "Chỉnh sửa danh mục");

        // Thiết lập dữ liệu nếu là chỉnh sửa
        if (category != null) {
            etCategoryName.setText(category.getName());
        }

        // Thiết lập RecyclerView cho danh sách icon
        rvIcons.setLayoutManager(new GridLayoutManager(this, 4));
        IconAdapter iconAdapter = new IconAdapter(defaultIcons, position -> {
            // Xử lý khi chọn icon
        });

        // Nếu là chỉnh sửa, chọn icon hiện tại
        if (category != null) {
            for (int i = 0; i < defaultIcons.length; i++) {
                if (defaultIcons[i] == category.getIconResourceId()) {
                    iconAdapter.setSelectedPosition(i);
                    break;
                }
            }
        }

        rvIcons.setAdapter(iconAdapter);

        // Thiết lập sự kiện cho các nút
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String name = etCategoryName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên danh mục", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedIconPosition = iconAdapter.getSelectedPosition();
            if (selectedIconPosition == -1) {
                Toast.makeText(this, "Vui lòng chọn biểu tượng", Toast.LENGTH_SHORT).show();
                return;
            }

            if (category == null) {
                // Thêm danh mục mới
                addCategory(name, defaultIcons[selectedIconPosition]);
            } else {
                // Cập nhật danh mục
                updateCategory(category.getId(), name, defaultIcons[selectedIconPosition]);
            }

            dialog.dismiss();
        });

        dialog.show();
    }

    private void addCategory(String name, int iconResourceId) {
        if (userId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để thêm danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo đối tượng Category mới
        Category category = new Category(name, iconResourceId);
        category.setUserId(userId);

        // Tạo key mới cho danh mục
        String categoryId = mDatabase.child("categories").child(userId).push().getKey();
        if (categoryId == null) {
            Toast.makeText(this, "Lỗi tạo ID danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lưu danh mục vào Firebase
        mDatabase.child("categories").child(userId).child(categoryId).setValue(category)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(CategoryManagementActivity.this, "Thêm danh mục thành công", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(CategoryManagementActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateCategory(String categoryId, String name, int iconResourceId) {
        if (userId == null || categoryId == null) {
            Toast.makeText(this, "Lỗi cập nhật danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo map chứa các trường cần cập nhật
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("iconResourceId", iconResourceId);

        // Cập nhật danh mục trong Firebase
        mDatabase.child("categories").child(userId).child(categoryId).updateChildren(updates)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(CategoryManagementActivity.this, "Cập nhật danh mục thành công", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(CategoryManagementActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void deleteCategory(String categoryId) {
        if (userId == null || categoryId == null) {
            Toast.makeText(this, "Lỗi xóa danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hiển thị dialog xác nhận
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa danh mục này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Xóa danh mục khỏi Firebase
                    mDatabase.child("categories").child(userId).child(categoryId).removeValue()
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(CategoryManagementActivity.this, "Xóa danh mục thành công", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(CategoryManagementActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /**
     * Adapter cho RecyclerView hiển thị danh sách danh mục
     */
    private class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

        @NonNull
        @Override
        public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.fragment_thongke_item_category, parent, false);
            return new CategoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
            Category category = categories.get(position);
            holder.bind(category);
        }

        @Override
        public int getItemCount() {
            return categories.size();
        }

        class CategoryViewHolder extends RecyclerView.ViewHolder {
            ImageView imgCategoryIcon;
            TextView tvCategoryName;
            ImageButton btnEditCategory, btnDeleteCategory;

            CategoryViewHolder(@NonNull View itemView) {
                super(itemView);
                imgCategoryIcon = itemView.findViewById(R.id.imgCategoryIcon);
                tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
                btnEditCategory = itemView.findViewById(R.id.btnEditCategory);
                btnDeleteCategory = itemView.findViewById(R.id.btnDeleteCategory);
            }

            void bind(Category category) {
                if (category == null) return;

                tvCategoryName.setText(category.getName());

                // Thiết lập icon
                if (category.getIconResourceId() != 0) {
                    imgCategoryIcon.setImageResource(category.getIconResourceId());
                } else {
                    imgCategoryIcon.setImageResource(R.drawable.thongke_ic_other);
                }

                // Thiết lập sự kiện
                btnEditCategory.setOnClickListener(v -> showEditCategoryDialog(category));
                btnDeleteCategory.setOnClickListener(v -> deleteCategory(category.getId()));
            }
        }
    }

    /**
     * Adapter cho RecyclerView hiển thị danh sách icon
     */
    private class IconAdapter extends RecyclerView.Adapter<IconAdapter.IconViewHolder> {

        private final int[] icons;
        private int selectedPosition = -1;
        private final OnIconSelectedListener listener;

        IconAdapter(int[] icons, OnIconSelectedListener listener) {
            this.icons = icons;
            this.listener = listener;
        }

        @NonNull
        @Override
        public IconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.fragment_thongke_item_icon, parent, false);
            return new IconViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull IconViewHolder holder, int position) {
            holder.bind(icons[position], position == selectedPosition);
        }

        @Override
        public int getItemCount() {
            return icons.length;
        }

        void setSelectedPosition(int position) {
            int oldPosition = selectedPosition;
            selectedPosition = position;

            if (oldPosition != -1) {
                notifyItemChanged(oldPosition);
            }
            if (selectedPosition != -1) {
                notifyItemChanged(selectedPosition);
            }
        }

        int getSelectedPosition() {
            return selectedPosition;
        }

        class IconViewHolder extends RecyclerView.ViewHolder {
            ImageView imgIcon;
            CardView cardView;

            IconViewHolder(@NonNull View itemView) {
                super(itemView);
                imgIcon = itemView.findViewById(R.id.imgIcon);
                cardView = (CardView) itemView;
            }

            void bind(int iconResId, boolean isSelected) {
                imgIcon.setImageResource(iconResId);

                // Thêm hiệu ứng khi chọn - sử dụng cách tiếp cận tương thích với CardView tiêu chuẩn
                if (isSelected) {
                    cardView.setCardBackgroundColor(ContextCompat.getColor(CategoryManagementActivity.this, R.color.colorAccent));
                    cardView.setCardElevation(8f);
                    // Sử dụng background drawable thay vì setStrokeWidth và setStrokeColor
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        cardView.setForeground(ContextCompat.getDrawable(CategoryManagementActivity.this, R.drawable.thongke_selected_icon_background));
                    }
                } else {
                    cardView.setCardBackgroundColor(ContextCompat.getColor(CategoryManagementActivity.this, android.R.color.transparent));
                    cardView.setCardElevation(2f);
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        cardView.setForeground(null);
                    }
                }

                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        setSelectedPosition(position);
                        if (listener != null) {
                            listener.onIconSelected(position);
                        }
                    }
                });
            }
        }
    }
}