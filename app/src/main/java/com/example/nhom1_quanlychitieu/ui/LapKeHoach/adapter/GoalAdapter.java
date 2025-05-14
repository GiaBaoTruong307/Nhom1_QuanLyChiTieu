package com.example.nhom1_quanlychitieu.ui.LapKeHoach.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom1_quanlychitieu.R;
import com.example.nhom1_quanlychitieu.ui.LapKeHoach.model.Goal;

import java.text.NumberFormat;
import java.util.Locale;

public class GoalAdapter extends ListAdapter<Goal, GoalAdapter.GoalViewHolder> {

    private final OnGoalClickListener listener;
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    private long totalIncome = 0;

    // Interface để xử lý sự kiện click
    public interface OnGoalClickListener {
        void onGoalClick(Goal goal);
    }

    public GoalAdapter(OnGoalClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Goal> DIFF_CALLBACK = new DiffUtil.ItemCallback<Goal>() {
        @Override
        public boolean areItemsTheSame(@NonNull Goal oldItem, @NonNull Goal newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Goal oldItem, @NonNull Goal newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getTargetAmount() == newItem.getTargetAmount() &&
                    oldItem.getCurrentAmount() == newItem.getCurrentAmount() &&
                    oldItem.getGoalType().equals(newItem.getGoalType()) &&
                    oldItem.isCompleted() == newItem.isCompleted();
        }
    };

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_lapkehoach_item_goal, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        Goal goal = getItem(position);
        holder.bind(goal, listener, totalIncome);

        // Hiển thị icon dựa trên loại mục tiêu
        int iconResId = getIconResourceByType(goal.getGoalType());
        holder.ivGoalIcon.setImageResource(iconResId);

        // Hiển thị dấu tích nếu mục tiêu đã hoàn thành dựa trên thu nhập
        boolean isCompleted = totalIncome > 0 ?
                totalIncome >= goal.getTargetAmount() :
                goal.getCurrentAmount() >= goal.getTargetAmount();

        holder.ivCompleted.setVisibility(isCompleted ? View.VISIBLE : View.GONE);
    }

    // Lấy resource ID của icon dựa trên loại mục tiêu
    private int getIconResourceByType(String goalType) {
        if (goalType == null) return R.drawable.lapkehoach_ic_default;

        switch (goalType) {
            case "car":
                return R.drawable.lapkehoach_ic_car;
            case "travel":
                return R.drawable.lapkehoach_ic_travel;
            case "house":
                return R.drawable.lapkehoach_ic_house;
            case "phone":
                return R.drawable.lapkehoach_ic_phone;
            case "toy":
                return R.drawable.lapkehoach_ic_toy;
            case "jewelry":
                return R.drawable.lapkehoach_ic_jewelry;
            default:
                return R.drawable.lapkehoach_ic_default;
        }
    }

    // Cập nhật tổng thu nhập
    public void setTotalIncome(long totalIncome) {
        this.totalIncome = totalIncome;
        notifyDataSetChanged();
    }

    // ViewHolder
    static class GoalViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvGoalName;
        private final TextView tvTargetAmount;
        private final TextView tvProgressPercent;
        private final ProgressBar progressBar;
        private final CardView cardView;
        private final ImageView ivGoalIcon;
        private final ImageView ivCompleted;

        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGoalName = itemView.findViewById(R.id.tvGoalName);
            tvTargetAmount = itemView.findViewById(R.id.tvTargetAmount);
            tvProgressPercent = itemView.findViewById(R.id.tvProgressPercent);
            progressBar = itemView.findViewById(R.id.progressBar);
            cardView = itemView.findViewById(R.id.cardView);
            ivGoalIcon = itemView.findViewById(R.id.ivGoalIcon);
            ivCompleted = itemView.findViewById(R.id.ivCompleted);
        }

        public void bind(Goal goal, OnGoalClickListener listener, long totalIncome) {
            tvGoalName.setText(goal.getName());
            tvTargetAmount.setText(CURRENCY_FORMAT.format(goal.getTargetAmount()) + " VND");

            // Tính toán tiến độ dựa trên thu nhập thay vì số tiền hiện tại
            int progress;
            if (totalIncome > 0) {
                progress = (int) ((totalIncome * 100) / goal.getTargetAmount());
            } else {
                progress = (int) ((goal.getCurrentAmount() * 100) / goal.getTargetAmount());
            }

            // Giới hạn tiến độ tối đa là 100%
            progress = Math.min(progress, 100);

            tvProgressPercent.setText(progress + "%");
            progressBar.setProgress(progress);

            // Xử lý sự kiện click
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onGoalClick(goal);
                }
            });
        }
    }
}