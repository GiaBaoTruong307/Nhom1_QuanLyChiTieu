package com.example.nhom1_quanlychitieu.ui.ViTien;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom1_quanlychitieu.R;

import java.util.List;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {

    private List<Account> accountList;
    private AccountDeleteListener deleteListener;

    public interface AccountDeleteListener {
        void onAccountDeleted(int position, String type, long amount);
    }

    public AccountAdapter(List<Account> accountList, AccountDeleteListener deleteListener) {
        this.accountList = accountList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account, parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        Account account = accountList.get(position);
        holder.tvName.setText(account.getName());
        holder.tvAmount.setText(account.getFormattedAmount());
        holder.imgIcon.setImageResource(account.getIconResId());
    }

    @Override
    public int getItemCount() {
        return accountList.size();
    }

    public void removeItem(int position) {
        Account deletedAccount = accountList.get(position);
        accountList.remove(position);
        notifyItemRemoved(position);
        if (deleteListener != null) {
            deleteListener.onAccountDeleted(position, deletedAccount.getType(), deletedAccount.getAmount());
        }
    }

    public void restoreItem(Account account, int position) {
        accountList.add(position, account);
        notifyItemInserted(position);
    }

    public class AccountViewHolder extends RecyclerView.ViewHolder {
        public TextView tvName, tvAmount;
        public ImageView imgIcon;
        public RelativeLayout viewForeground, viewBackground;

        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            imgIcon = itemView.findViewById(R.id.img_icon);
            viewForeground = itemView.findViewById(R.id.view_foreground);
            viewBackground = itemView.findViewById(R.id.view_background);
        }
    }
}
