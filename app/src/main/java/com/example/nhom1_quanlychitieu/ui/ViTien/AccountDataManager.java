package com.example.nhom1_quanlychitieu.ui.ViTien;

import com.example.nhom1_quanlychitieu.R;

import java.util.ArrayList;
import java.util.List;

public class AccountDataManager {
    private List<Account> bankAccounts;
    private List<Account> cashAccounts;
    private List<AccountDataChangeListener> listeners;

    public interface AccountDataChangeListener {
        void onDataChanged();
    }

    public AccountDataManager() {
        bankAccounts = new ArrayList<>();
        cashAccounts = new ArrayList<>();
        listeners = new ArrayList<>();

        // Khởi tạo dữ liệu mẫu
        initSampleData();
    }

    private void initSampleData() {
        // Dữ liệu mẫu cho tài khoản ngân hàng
        bankAccounts.add(new Account("MB Bank", 5000000, R.drawable.ic_bank, "bank"));
        bankAccounts.add(new Account("VietABank", 5000000, R.drawable.ic_bank, "bank"));

        // Dữ liệu mẫu cho tiền mặt
        cashAccounts.add(new Account("Tiền mặt", 30000000, R.drawable.ic_money, "cash"));
        cashAccounts.add(new Account("Tiền bỏ heo", 30000000, R.drawable.ic_piggy_bank, "cash"));
    }

    public List<Account> getBankAccounts() {
        return bankAccounts;
    }

    public List<Account> getCashAccounts() {
        return cashAccounts;
    }

    public List<Account> getAllAccounts() {
        List<Account> allAccounts = new ArrayList<>();
        allAccounts.addAll(bankAccounts);
        allAccounts.addAll(cashAccounts);
        return allAccounts;
    }

    public void addAccount(Account account) {
        if ("bank".equals(account.getType())) {
            bankAccounts.add(account);
        } else if ("cash".equals(account.getType())) {
            cashAccounts.add(account);
        }
        notifyDataChanged();
    }

    public void removeAccount(Account account) {
        if ("bank".equals(account.getType())) {
            bankAccounts.remove(account);
        } else if ("cash".equals(account.getType())) {
            cashAccounts.remove(account);
        }
        notifyDataChanged();
    }

    public long getTotalBankAmount() {
        long total = 0;
        for (Account account : bankAccounts) {
            total += account.getAmount();
        }
        return total;
    }

    public long getTotalCashAmount() {
        long total = 0;
        for (Account account : cashAccounts) {
            total += account.getAmount();
        }
        return total;
    }

    public long getTotalAmount() {
        return getTotalBankAmount() + getTotalCashAmount();
    }

    public void addListener(AccountDataChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(AccountDataChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyDataChanged() {
        for (AccountDataChangeListener listener : listeners) {
            listener.onDataChanged();
        }
    }
}
