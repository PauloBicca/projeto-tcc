package com.financeiro.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiro.app.R;
import com.financeiro.app.models.Reminder;

import java.util.List;

/**
 * Adapter para lista de lembretes/notificações.
 */
public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {

    public interface OnReminderListener {
        void onToggle(Reminder reminder, boolean isActive);
        void onDelete(Reminder reminder);
    }

    private final List<Reminder>     reminders;
    private final OnReminderListener listener;

    public ReminderAdapter(List<Reminder> reminders, OnReminderListener listener) {
        this.reminders = reminders;
        this.listener  = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reminder, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reminder r = reminders.get(position);
        holder.tvTitle.setText(r.getTitle());
        holder.tvTime.setText(r.getTime());
        holder.tvMessage.setText(r.getMessage());

        // Evitar trigger duplo ao setar o switch programaticamente
        holder.switchActive.setOnCheckedChangeListener(null);
        holder.switchActive.setChecked(r.isActive());
        holder.switchActive.setOnCheckedChangeListener((btn, isChecked) -> listener.onToggle(r, isChecked));

        holder.btnDelete.setOnClickListener(v -> listener.onDelete(r));
    }

    @Override
    public int getItemCount() { return reminders.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView    tvTitle, tvTime, tvMessage;
        Switch      switchActive;
        ImageButton btnDelete;

        ViewHolder(View v) {
            super(v);
            tvTitle      = v.findViewById(R.id.tv_reminder_title);
            tvTime       = v.findViewById(R.id.tv_reminder_time);
            tvMessage    = v.findViewById(R.id.tv_reminder_message);
            switchActive = v.findViewById(R.id.switch_reminder_active);
            btnDelete    = v.findViewById(R.id.btn_reminder_delete);
        }
    }
}
