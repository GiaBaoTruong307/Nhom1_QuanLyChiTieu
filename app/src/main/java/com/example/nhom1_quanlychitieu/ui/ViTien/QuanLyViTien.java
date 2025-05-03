package com.example.nhom1_quanlychitieu.ui.ViTien;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom1_quanlychitieu.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class QuanLyViTien extends Fragment implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener, AccountAdapter.AccountDeleteListener {

    private RecyclerView rvAccounts;
    private AccountAdapter accountAdapter;
    private List<Account> accountList;
    private View root;

    // Singleton để lưu trữ dữ liệu tài khoản giữa các fragment
    private static AccountDataManager accountDataManager;

    public static AccountDataManager getAccountDataManager() {
        if (accountDataManager == null) {
            accountDataManager = new AccountDataManager();
        }
        return accountDataManager;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // Cho phép fragment xử lý menu options
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_vitien_quanly, container, false);

        // Thiết lập ActionBar
        setupActionBar();

        // Khởi tạo RecyclerView
        rvAccounts = root.findViewById(R.id.rv_accounts);
        rvAccounts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAccounts.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        // Lấy dữ liệu từ AccountDataManager
        accountList = getAccountDataManager().getAllAccounts();

        // Khởi tạo adapter
        accountAdapter = new AccountAdapter(accountList, this);
        rvAccounts.setAdapter(accountAdapter);

        // Thiết lập ItemTouchHelper cho vuốt để xóa
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(rvAccounts);

        // Xử lý sự kiện khi nhấn nút "Thêm vào" để chuyển đến màn hình Tài khoản mới
        FloatingActionButton btnThemVao = root.findViewById(R.id.btn_them_vao);
        btnThemVao.setOnClickListener(v -> {
            Navigation.findNavController(root).navigate(R.id.action_quanly_vitien_to_taikhoan_moi);
        });

        return root;
    }

    private void setupActionBar() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null && activity.getSupportActionBar() != null) {
            ActionBar actionBar = activity.getSupportActionBar();
            actionBar.setTitle("Quản lý ví tiền");
            actionBar.setDisplayHomeAsUpEnabled(true); // Hiển thị nút quay lại
            actionBar.setDisplayShowHomeEnabled(true); // Hiển thị icon home
            actionBar.show(); // Hiển thị ActionBar
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Quay lại màn hình Ví tiền
            Navigation.findNavController(requireView()).navigate(R.id.action_quanly_vitien_to_vitien);
            return true;
        }
        return super.onOptionsItemSelected(item);
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
    public void onStop() {
        super.onStop();
        // Ẩn ActionBar khi rời khỏi fragment này (tùy chọn)
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null && activity.getSupportActionBar() != null && !activity.isFinishing()) {
            activity.getSupportActionBar().hide();
        }
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder.getItemViewType() == 0) {
            final int adapterPosition = viewHolder.getAdapterPosition();

            // Lưu lại item bị xóa để có thể hoàn tác
            final Account deletedItem = accountList.get(adapterPosition);
            final int deletedIndex = adapterPosition;

            // Xóa item
            accountAdapter.removeItem(adapterPosition);

            // Cập nhật dữ liệu trong AccountDataManager
            getAccountDataManager().removeAccount(deletedItem);

            // Hiển thị Snackbar với tùy chọn hoàn tác
            Snackbar snackbar = Snackbar.make(root, "Đã xóa " + deletedItem.getName(), Snackbar.LENGTH_LONG);
            snackbar.setAction("HOÀN TÁC", view -> {
                // Khôi phục item đã xóa
                accountList.add(deletedIndex, deletedItem);
                accountAdapter.notifyItemInserted(deletedIndex);
                rvAccounts.scrollToPosition(deletedIndex);

                // Cập nhật lại dữ liệu trong AccountDataManager
                getAccountDataManager().addAccount(deletedItem);
            });
            snackbar.show();
        }
    }

    @Override
    public void onAccountDeleted(int position, String type, long amount) {
        // Không cần cập nhật UI ở đây vì đã xử lý trong AccountDataManager
    }
}
