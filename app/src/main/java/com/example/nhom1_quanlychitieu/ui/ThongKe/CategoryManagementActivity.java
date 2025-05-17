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
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import androidx.viewpager2.widget.ViewPager2;

import com.example.nhom1_quanlychitieu.R;
import com.example.nhom1_quanlychitieu.ui.ThongKe.model.Category;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
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

    // Tag để sử dụng trong log
    private static final String TAG = "CategoryManagement";

    // Interface cho sự kiện chọn icon
    private interface OnIconSelectedListener {
        void onIconSelected(int position);
    }

    // Các thành phần UI
    private ImageButton btnBack;
    private Button btnAddCategory;
    private TextView tvNoCategories;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    // Dữ liệu
    private CategoryAdapter expenseAdapter;
    private CategoryAdapter incomeAdapter;
    private final List<Category> allCategories = new ArrayList<>();
    private final List<Category> expenseCategories = new ArrayList<>();
    private final List<Category> incomeCategories = new ArrayList<>();

    // Firebase
    private FirebaseAuth mAuth; // Xác thực người dùng
    private DatabaseReference mDatabase; // Tham chiếu đến database
    private String userId; // ID của người dùng hiện tại

    // Mảng các icon mặc định cho chi tiêu
    private final int[] expenseIcons = {
            R.drawable.ic_expense_food,
            R.drawable.ic_expense_transport,
            R.drawable.ic_expense_shopping,
            R.drawable.ic_expense_entertainment,
            R.drawable.ic_expense_medical,
            R.drawable.ic_expense_bills,
            R.drawable.ic_expense_education,
            R.drawable.ic_expense_gift,
            R.drawable.ic_expense_home,
            R.drawable.ic_expense_beauty,
            R.drawable.ic_expense_car,
            R.drawable.ic_expense_clothes,
            R.drawable.ic_expense_coffee,
            R.drawable.ic_expense_electronics,
            R.drawable.ic_other
    };

    // Mảng các icon mặc định cho thu nhập
    private final int[] incomeIcons = {
            R.drawable.ic_income_salary,
            R.drawable.ic_income_bonus,
            R.drawable.ic_income_gift,
            R.drawable.ic_income_investment,
            R.drawable.ic_income_lottery,
            R.drawable.ic_income_rental,
            R.drawable.ic_income_sale,
            R.drawable.ic_income_refund,
            R.drawable.ic_income_business,
            R.drawable.ic_income_commission,
            R.drawable.ic_income_dividend,
            R.drawable.ic_income_freelance,
            R.drawable.ic_income_interest,
            R.drawable.ic_income_pension,
            R.drawable.ic_other
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_thongke_category_management);

        // Khởi tạo Firebase
        initFirebase();
        if (userId == null) return; // Nếu không có người dùng, thoát

        // Khởi tạo các thành phần UI
        initializeViews();

        // Thiết lập TabLayout và ViewPager
        setupTabLayout();

        // Thiết lập sự kiện
        setupEventListeners();

        // Tải danh sách danh mục
        loadCategories();
    }

    private void initFirebase() {
        try {
            mAuth = FirebaseAuth.getInstance();
            mDatabase = FirebaseDatabase.getInstance().getReference();

            // Kiểm tra người dùng đã đăng nhập chưa
            if (mAuth.getCurrentUser() != null) {
                userId = mAuth.getCurrentUser().getUid();
            } else {
                // Nếu chưa đăng nhập, hiển thị thông báo và đóng Activity
                Toast.makeText(this, "Vui lòng đăng nhập để quản lý danh mục", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            // Xử lý lỗi khi khởi tạo Firebase
            Log.e(TAG, "Error initializing Firebase", e);
            Toast.makeText(this, "Lỗi khởi tạo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        btnAddCategory = findViewById(R.id.btnAddCategory);
        tvNoCategories = findViewById(R.id.tvNoCategories);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
    }

    private void setupTabLayout() {
        // Khởi tạo adapters
        expenseAdapter = new CategoryAdapter(expenseCategories);
        incomeAdapter = new CategoryAdapter(incomeCategories);

        // Thiết lập ViewPager2 với adapter
        CategoryPagerAdapter pagerAdapter = new CategoryPagerAdapter();
        viewPager.setAdapter(pagerAdapter);

        // Kết nối TabLayout với ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Chi tiêu"); // Tab chi tiêu
                    break;
                case 1:
                    tab.setText("Thu nhập"); // Tab thu nhập
                    break;
            }
        }).attach();
    }

    private void setupEventListeners() {
        // Nút quay lại
        btnBack.setOnClickListener(v -> finish());

        // Nút thêm danh mục mới
        btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());
    }

    private void loadCategories() {
        if (userId == null) {
            tvNoCategories.setVisibility(View.VISIBLE);
            return;
        }

        mDatabase.child("categories").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allCategories.clear();
                expenseCategories.clear();
                incomeCategories.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        Category category = snapshot.getValue(Category.class);
                        if (category != null) {
                            category.setId(snapshot.getKey());
                            allCategories.add(category);

                            // Phân loại danh mục
                            if (category.isIncome()) {
                                incomeCategories.add(category); // Danh mục thu nhập
                            } else {
                                expenseCategories.add(category); // Danh mục chi tiêu
                            }
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

    private void updateUI() {
        if (allCategories.isEmpty()) {
            // Nếu không có danh mục, hiển thị thông báo
            tvNoCategories.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.GONE);
            tabLayout.setVisibility(View.GONE);
        } else {
            // Nếu có danh mục, hiển thị danh sách
            tvNoCategories.setVisibility(View.GONE);
            viewPager.setVisibility(View.VISIBLE);
            tabLayout.setVisibility(View.VISIBLE);

            // Cập nhật adapters
            expenseAdapter.notifyDataSetChanged();
            incomeAdapter.notifyDataSetChanged();
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
        RadioGroup rgCategoryType = dialog.findViewById(R.id.rgCategoryType);
        RadioButton rbExpense = dialog.findViewById(R.id.rbExpense);
        RadioButton rbIncome = dialog.findViewById(R.id.rbIncome);
        RecyclerView rvIcons = dialog.findViewById(R.id.rvIcons);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnSave = dialog.findViewById(R.id.btnSave);

        // Thiết lập tiêu đề dialog
        tvDialogTitle.setText(category == null ? "Thêm danh mục mới" : "Chỉnh sửa danh mục");

        // Thiết lập dữ liệu nếu là chỉnh sửa
        if (category != null) {
            etCategoryName.setText(category.getName());
            if (category.isIncome()) {
                rbIncome.setChecked(true); // Thu nhập
            } else {
                rbExpense.setChecked(true); // Chi tiêu
            }
        }

        // Mặc định hiển thị icons chi tiêu
        final int[] currentIcons = rbExpense.isChecked() ? expenseIcons : incomeIcons;

        // Thiết lập RecyclerView cho danh sách icon
        rvIcons.setLayoutManager(new GridLayoutManager(this, 4));
        IconAdapter iconAdapter = new IconAdapter(currentIcons, position -> {
            // Xử lý khi chọn icon
        });

        // Nếu là chỉnh sửa, chọn icon hiện tại
        if (category != null) {
            int[] icons = category.isIncome() ? incomeIcons : expenseIcons;
            for (int i = 0; i < icons.length; i++) {
                if (icons[i] == category.getIconResourceId()) {
                    iconAdapter.setSelectedPosition(i);
                    break;
                }
            }
        }

        rvIcons.setAdapter(iconAdapter);

        // Xử lý sự kiện khi chọn loại danh mục
        rgCategoryType.setOnCheckedChangeListener((group, checkedId) -> {
            int[] icons = checkedId == R.id.rbExpense ? expenseIcons : incomeIcons;
            iconAdapter.updateIcons(icons);
            iconAdapter.setSelectedPosition(-1); // Reset selection
        });

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

            // Xác định loại danh mục
            String type = rbIncome.isChecked() ? "income" : "expense";

            // Xác định icon đã chọn
            int[] icons = type.equals("income") ? incomeIcons : expenseIcons;
            int iconResourceId = icons[selectedIconPosition];

            if (category == null) {
                // Thêm danh mục mới
                addCategory(name, iconResourceId, type);
            } else {
                // Cập nhật danh mục
                updateCategory(category.getId(), name, iconResourceId, type);
            }

            dialog.dismiss();
        });

        dialog.show();
    }

    private void addCategory(String name, int iconResourceId, String type) {
        if (userId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để thêm danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo đối tượng Category mới
        Category category = new Category(name, iconResourceId, type);
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

    private void updateCategory(String categoryId, String name, int iconResourceId, String type) {
        if (userId == null || categoryId == null) {
            Toast.makeText(this, "Lỗi cập nhật danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo map chứa các trường cần cập nhật
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("iconResourceId", iconResourceId);
        updates.put("type", type);

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

    private class CategoryPagerAdapter extends RecyclerView.Adapter<CategoryPagerAdapter.CategoryPageViewHolder> {

        @NonNull
        @Override
        public CategoryPageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            RecyclerView recyclerView = new RecyclerView(parent.getContext());
            recyclerView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            return new CategoryPageViewHolder(recyclerView);
        }

        @Override
        public void onBindViewHolder(@NonNull CategoryPageViewHolder holder, int position) {
            holder.bind(position);
        }

        @Override
        public int getItemCount() {
            return 2; // Chi tiêu và Thu nhập
        }

        class CategoryPageViewHolder extends RecyclerView.ViewHolder {
            RecyclerView recyclerView;

            CategoryPageViewHolder(@NonNull View itemView) {
                super(itemView);
                recyclerView = (RecyclerView) itemView;
                recyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            }

            void bind(int position) {
                if (position == 0) {
                    // Tab Chi tiêu
                    recyclerView.setAdapter(expenseAdapter);
                } else {
                    // Tab Thu nhập
                    recyclerView.setAdapter(incomeAdapter);
                }
            }
        }
    }

    private class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

        private final List<Category> categories;

        CategoryAdapter(List<Category> categories) {
            this.categories = categories;
        }

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
                    imgCategoryIcon.setImageResource(R.drawable.ic_other);
                }

                // Thiết lập sự kiện
                btnEditCategory.setOnClickListener(v -> showEditCategoryDialog(category));
                btnDeleteCategory.setOnClickListener(v -> deleteCategory(category.getId()));
            }
        }
    }

    private class IconAdapter extends RecyclerView.Adapter<IconAdapter.IconViewHolder> {

        private int[] icons;
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

        void updateIcons(int[] newIcons) {
            this.icons = newIcons;
            notifyDataSetChanged();
        }

        class IconViewHolder extends RecyclerView.ViewHolder {
            ImageView imgIcon;
            CardView cardView;
            View selectedOverlay;

            IconViewHolder(@NonNull View itemView) {
                super(itemView);
                imgIcon = itemView.findViewById(R.id.imgIcon);
                cardView = (CardView) itemView;
                selectedOverlay = itemView.findViewById(R.id.selectedOverlay);
            }

            void bind(int iconResId, boolean isSelected) {
                imgIcon.setImageResource(iconResId);

                // Hiển thị hiệu ứng khi chọn
                if (isSelected) {
                    cardView.setCardBackgroundColor(ContextCompat.getColor(CategoryManagementActivity.this, R.color.colorAccent));
                    cardView.setCardElevation(8f);
                    selectedOverlay.setVisibility(View.VISIBLE);
                } else {
                    cardView.setCardBackgroundColor(ContextCompat.getColor(CategoryManagementActivity.this, android.R.color.white));
                    cardView.setCardElevation(2f);
                    selectedOverlay.setVisibility(View.GONE);
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