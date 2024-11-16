package com.lbtmo.ticketingsystem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.ViewHolder> {
    private List<AnnouncementModel> announcements;
    private OnDeleteClickListener onDeleteClickListener;
    private OnAnnouncementClickListener onAnnouncementClickListener;

    // Interface for delete click listener
    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    // New interface for announcement click listener
    public interface OnAnnouncementClickListener {
        void onAnnouncementClick(AnnouncementModel announcement);
    }

    public AnnouncementAdapter(List<AnnouncementModel> announcements,
                               OnDeleteClickListener onDeleteClickListener,
                               OnAnnouncementClickListener onAnnouncementClickListener) {
        this.announcements = announcements;
        this.onDeleteClickListener = onDeleteClickListener;
        this.onAnnouncementClickListener = onAnnouncementClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the custom layout for each item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_announcement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the current announcement
        AnnouncementModel announcement = announcements.get(position);
        // Set the title, content, and dateTime
        holder.title.setText(announcement.getTitle());
        holder.content.setText(announcement.getContent());
        holder.dateTime.setText(announcement.getDateTime());

        // Set the delete button listener
        holder.buttonDelete.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(position);
            }
        });

        // Set the item click listener
        holder.itemView.setOnClickListener(v -> {
            if (onAnnouncementClickListener != null) {
                onAnnouncementClickListener.onAnnouncementClick(announcement);
            }
        });
    }

    @Override
    public int getItemCount() {
        return announcements.size();  // Return the size of the list
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView content;
        TextView dateTime;
        ImageView buttonDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.announcement_title);
            content = itemView.findViewById(R.id.announcement_body);
            dateTime = itemView.findViewById(R.id.announcement_date_time);
            buttonDelete = itemView.findViewById(R.id.button_delete);
        }
    }

    // Method to update the announcement list and notify changes
    public void updateAnnouncements(List<AnnouncementModel> newAnnouncements) {
        this.announcements.clear();
        this.announcements.addAll(newAnnouncements);
        notifyDataSetChanged();
    }
}

