package com.lbtmo.ticketingsystem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class ViolationAdapter extends RecyclerView.Adapter<ViolationAdapter.ViolationViewHolder> {

    private Context context;  // Context for inflating views
    private ArrayList<Violation> violationList;  // List of violations

    // Constructor to initialize context and violation list
    public ViolationAdapter(Context context, ArrayList<Violation> violationList) {
        this.context = context;
        this.violationList = violationList;
    }

    @NonNull
    @Override
    public ViolationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item of the RecyclerView
        View view = LayoutInflater.from(context).inflate(R.layout.violation_item, parent, false);
        return new ViolationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViolationViewHolder holder, int position) {
        // Get the current violation item
        Violation violation = violationList.get(position);

        // Bind data to the UI elements
        holder.violationTitle.setText(violation.getOffenseName());
        holder.firstNameTextView.setText(violation.getFirstName());
        holder.lastNameTextView.setText(violation.getLastName());
        holder.middleNameTextView.setText(violation.getMiddleName());
        holder.violationStreet.setText(violation.getStreet());
        holder.violationBarangay.setText(violation.getBarangay());
        holder.violationMunicipality.setText(violation.getMunicipality());
        holder.dateTimeTextView.setText(violation.getDateTime());
        holder.status.setText(violation.getStatus());

    }

    @Override
    public int getItemCount() {
        // Return the total number of items in the list
        return violationList.size();
    }

    // ViewHolder class to hold the UI elements for each violation item
    public static class ViolationViewHolder extends RecyclerView.ViewHolder {
        TextView violationTitle;
        TextView firstNameTextView;
        TextView lastNameTextView;
        TextView middleNameTextView;
        TextView violationStreet;
        TextView violationBarangay;
        TextView violationMunicipality;
        TextView dateTimeTextView;
        TextView status;

        public ViolationViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize the UI elements
            violationTitle = itemView.findViewById(R.id.violationTitle);
            firstNameTextView = itemView.findViewById(R.id.firstNameTextView);
            lastNameTextView = itemView.findViewById(R.id.lastNameTextView);
            middleNameTextView = itemView.findViewById(R.id.middleNameTextView);
            violationStreet = itemView.findViewById(R.id.violationStreet);
            violationBarangay = itemView.findViewById(R.id.violationBarangay);
            violationMunicipality = itemView.findViewById(R.id.violationMunicipality);
            dateTimeTextView = itemView.findViewById(R.id.dateTimeTextView);
            status = itemView.findViewById(R.id.status);
        }
    }
}
