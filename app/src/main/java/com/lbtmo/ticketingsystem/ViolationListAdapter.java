package com.lbtmo.ticketingsystem;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ViolationListAdapter extends RecyclerView.Adapter<ViolationListAdapter.ViolationViewHolder> {

    private List<ViolationListFetch> violationList;
    private Context context;

    public ViolationListAdapter(List<ViolationListFetch> violationList, Context context) {
        this.violationList = violationList;
        this.context = context;
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

        // Reset the listener to avoid adding multiple listeners in case of view recycling
        holder.checkbox.setOnCheckedChangeListener(null);
        holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            violation.setChecked(isChecked);
        });

        // Handle item click to pass unique ID and title to TicketViewing activity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TicketViewing.class);
            intent.putExtra("violationId", violation.getId());  // Pass unique ID
            intent.putExtra("violationTitle", violation.getTitle());  // Pass title if needed
            context.startActivity(intent);
        });

        // Handle click on violation code to check/uncheck checkbox
        holder.violationCode.setOnClickListener(v -> {
            boolean isChecked = !violation.isChecked();
            violation.setChecked(isChecked);
            holder.checkbox.setChecked(isChecked);  // Ensure checkbox reflects the state
        });

        // Handle click on violation title to check/uncheck checkbox
        holder.title.setOnClickListener(v -> {
            boolean isChecked = !violation.isChecked();
            violation.setChecked(isChecked);
            holder.checkbox.setChecked(isChecked);  // Ensure checkbox reflects the state
        });

        // Handle click on container (item view) to check/uncheck checkbox
        holder.itemView.setOnClickListener(v -> {
            boolean isChecked = !violation.isChecked();
            violation.setChecked(isChecked);
            holder.checkbox.setChecked(isChecked);  // Ensure checkbox reflects the state
        });
    }

    @Override
    public int getItemCount() {
        return violationList.size();
    }

    public List<String> getSelectedViolationCodes() {
        List<String> selectedViolationCodes = new ArrayList<>();
        for (ViolationListFetch violation : violationList) {
            if (violation.isChecked()) {
                selectedViolationCodes.add(violation.getCode()); // Or .getTitle() if needed
            }
        }
        return selectedViolationCodes;
    }

    public List<String> getSelectedViolationTitles() {
        List<String> selectedViolationTitles = new ArrayList<>();
        for (ViolationListFetch violation : violationList) {
            if (violation.isChecked()) {
                selectedViolationTitles.add(violation.getTitle()); // Or .getTitle() if needed
            }
        }
        return selectedViolationTitles;
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
