package com.example.ticketingsystem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ViolationListAdapter extends RecyclerView.Adapter<ViolationListAdapter.ViolationViewHolder> {

    private List<ViolationListFetch> violationList;

    public ViolationListAdapter(List<ViolationListFetch> violationList) {
        this.violationList = violationList;
    }

    @NonNull
    @Override
    public ViolationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_violation_list, parent, false);
        return new ViolationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViolationViewHolder holder, int position) {
        ViolationListFetch violation = violationList.get(position);
        holder.violationCode.setText(violation.getCode());
        holder.title.setText(violation.getTitle());
        holder.checkbox.setChecked(violation.isChecked());

        holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            violation.setChecked(isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return violationList.size();
    }

    public static class ViolationViewHolder extends RecyclerView.ViewHolder {
        TextView violationCode, title;
        CheckBox checkbox;

        public ViolationViewHolder(View itemView) {
            super(itemView);
            violationCode = itemView.findViewById(R.id.violationCode);
            title = itemView.findViewById(R.id.title);
            checkbox = itemView.findViewById(R.id.checkbox);
        }
    }
}
