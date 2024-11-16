package com.lbtmo.ticketingsystem;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TicketerTicketsViewingAdapter extends RecyclerView.Adapter<TicketerTicketsViewingAdapter.TicketViewHolder> {
    private List<Ticket> ticketList;
    private Context context;

    public TicketerTicketsViewingAdapter(Context context, List<Ticket> ticketList) {
        this.context = context;
        this.ticketList = ticketList;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ticket_item, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        Ticket ticket = ticketList.get(position);
        holder.ticketIdTextView.setText(ticket.getTicketId());
        holder.ticketDescriptionTextView.setText(ticket.getTicketDescription());
        holder.firstNameTextView.setText(ticket.getFirstName());
        holder.lastNameTextView.setText(ticket.getLastName());
        holder.middleNameTextView.setText(ticket.getMiddleName());

        // Set onClickListener for the item to show dialog with details
        holder.itemView.setOnClickListener(v -> showTicketDetailsDialog(ticket));
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    private void showTicketDetailsDialog(Ticket ticket) {
        // Inflate dialog layout
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_ticket_details, null);

        // Initialize dialog views and set values
        TextView ticketIdDetailTextView = dialogView.findViewById(R.id.ticketIdDetailTextView);
        TextView ticketDescriptionDetailTextView = dialogView.findViewById(R.id.ticketDescriptionDetailTextView);
        TextView firstNameDetailTextView = dialogView.findViewById(R.id.firstNameDetailTextView);
        TextView lastNameDetailTextView = dialogView.findViewById(R.id.lastNameDetailTextView);
        TextView middleNameDetailTextView = dialogView.findViewById(R.id.middleNameDetailTextView);
        TextView dateTimeDetailTextView = dialogView.findViewById(R.id.dateTimeDetailTextView);
        TextView plateLicenseDetailTextView = dialogView.findViewById(R.id.plateLicenseDetailTextView);
        TextView barangayDetailTextView = dialogView.findViewById(R.id.barangayDetailTextView);
        TextView streetDetailTextView = dialogView.findViewById(R.id.streetDetailTextView);
        // Add other detail TextViews for date-time, plate license, barangay, street

        // Set values
        ticketIdDetailTextView.setText(ticket.getTicketId());
        ticketDescriptionDetailTextView.setText(ticket.getTicketDescription());
        firstNameDetailTextView.setText(ticket.getFirstName());
        lastNameDetailTextView.setText(ticket.getLastName());
        middleNameDetailTextView.setText(ticket.getMiddleName());
        dateTimeDetailTextView.setText(ticket.getDateTime());
        plateLicenseDetailTextView.setText(ticket.getPlateLicense());
        barangayDetailTextView.setText(ticket.getBarangay());
        streetDetailTextView.setText(ticket.getStreet());

        // Set values for additional fields here

        // Create and show dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView)
                .setPositiveButton("Close", (dialog, id) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView ticketIdTextView;
        TextView ticketDescriptionTextView;
        TextView firstNameTextView;
        TextView lastNameTextView;
        TextView middleNameTextView;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            ticketIdTextView = itemView.findViewById(R.id.ticketIdTextView);
            ticketDescriptionTextView = itemView.findViewById(R.id.ticketDescriptionTextView);
            firstNameTextView = itemView.findViewById(R.id.firstNameTextView);
            lastNameTextView = itemView.findViewById(R.id.lastNameTextView);
            middleNameTextView = itemView.findViewById(R.id.middleNameTextView);
        }
    }
}
