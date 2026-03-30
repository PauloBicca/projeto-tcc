package com.financeiro.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiro.app.R;
import com.financeiro.app.models.Goal;
import com.financeiro.app.utils.FormatUtils;

import java.util.List;

/**
 * Adapter para lista de metas financeiras.
 */
public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.ViewHolder> {

    public interface OnGoalListener {
        void onEdit(Goal goal);
        void onDelete(Goal goal);
    }

    private final List<Goal> goals;
    private final OnGoalListener listener;

    public GoalAdapter(List<Goal> goals, OnGoalListener listener) {
        this.goals    = goals;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_goal, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Goal g = goals.get(position);

        holder.tvTitle.setText(g.getTitle());
        holder.tvDescription.setText(g.getDescription() != null ? g.getDescription() : "");

        int percent = g.getProgressPercent();
        holder.progressBar.setProgress(percent);
        holder.tvProgress.setText(percent + "%");

        holder.tvCurrent.setText(FormatUtils.formatCurrency(g.getCurrentAmount()));
        holder.tvTarget.setText("/ " + FormatUtils.formatCurrency(g.getTargetAmount()));
        holder.tvRemaining.setText("Faltam: " + FormatUtils.formatCurrency(g.getRemainingAmount()));

        if (g.getDeadline() > 0) {
            holder.tvDeadline.setText("Prazo: " + FormatUtils.formatDate(g.getDeadline()));
            holder.tvDeadline.setVisibility(View.VISIBLE);
        } else {
            holder.tvDeadline.setVisibility(View.GONE);
        }

        // Cor da barra de progresso
        if (g.isCompleted()) {
            holder.progressBar.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(0xFF4CAF50));
            holder.tvProgress.setTextColor(0xFF4CAF50);
        } else if (percent >= 75) {
            holder.progressBar.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(0xFF2196F3));
        }

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(g));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(g));
    }

    @Override
    public int getItemCount() { return goals.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvProgress, tvCurrent, tvTarget, tvRemaining, tvDeadline;
        ProgressBar progressBar;
        ImageButton btnEdit, btnDelete;

        ViewHolder(View v) {
            super(v);
            tvTitle       = v.findViewById(R.id.tv_goal_title);
            tvDescription = v.findViewById(R.id.tv_goal_desc);
            tvProgress    = v.findViewById(R.id.tv_goal_percent);
            tvCurrent     = v.findViewById(R.id.tv_goal_current);
            tvTarget      = v.findViewById(R.id.tv_goal_target);
            tvRemaining   = v.findViewById(R.id.tv_goal_remaining);
            tvDeadline    = v.findViewById(R.id.tv_goal_deadline);
            progressBar   = v.findViewById(R.id.progress_goal);
            btnEdit       = v.findViewById(R.id.btn_goal_edit);
            btnDelete     = v.findViewById(R.id.btn_goal_delete);
        }
    }
}
