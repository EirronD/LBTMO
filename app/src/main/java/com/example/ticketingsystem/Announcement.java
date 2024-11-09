package com.example.ticketingsystem;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Announcement extends AppCompatActivity implements AnnouncementAdapter.OnAnnouncementClickListener {

    private RecyclerView recyclerView;
    private AnnouncementAdapter announcementAdapter;
    private List<AnnouncementModel> announcementList;
    private DrawerLayout drawerLayout;
    private ImageButton buttonDrawerToggle;
    private NavigationView navigationView;
    private String userKey;
    private TextView titleTextView;

    // Declare the "No Announcements" Views
    private ImageView noAnnouncementImage;
    private TextView noAnnouncementMessage;
    private TextView noAnnouncementSubMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement);

        titleTextView = findViewById(R.id.titletextview);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        announcementList = new ArrayList<>();

        noAnnouncementImage = findViewById(R.id.noAnnouncementImage);
        noAnnouncementMessage = findViewById(R.id.noAnnouncementMessage);
        noAnnouncementSubMessage = findViewById(R.id.noAnnouncementSubMessage);

        // Initialize the adapter with the new interface for item clicks
        announcementAdapter = new AnnouncementAdapter(announcementList, this::showDeleteConfirmationDialog, this);
        recyclerView.setAdapter(announcementAdapter);

        drawerLayout = findViewById(R.id.drawer_layout);
        buttonDrawerToggle = findViewById(R.id.buttonDrawerToggle);
        navigationView = findViewById(R.id.navigationView);

        userKey = getIntent().getStringExtra("userKey");

        buttonDrawerToggle.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(navigationView)) {
                drawerLayout.closeDrawer(navigationView);
            } else {
                drawerLayout.openDrawer(navigationView);
                titleTextView.setVisibility(View.GONE);
            }
        });

        // Set up NavigationView item click listener
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int itemId = menuItem.getItemId();

            if (itemId == R.id.home) {
                startActivity(new Intent(Announcement.this, HomeUsers.class).putExtra("userKey", userKey));
            } else if (itemId == R.id.profile) {
                startActivity(new Intent(Announcement.this, UserProfile.class).putExtra("userKey", userKey));
            } else if (itemId == R.id.seeviolations) {
                startActivity(new Intent(Announcement.this, UserViolation.class).putExtra("userKey", userKey));
            } else if (itemId == R.id.announcement) {
                startActivity(new Intent(Announcement.this, Announcement.class).putExtra("userKey", userKey));
            } else if (itemId == R.id.logout) {
                startActivity(new Intent(Announcement.this, MainActivity.class));
                finish();
            }

            drawerLayout.closeDrawer(navigationView);
            return true;
        });

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {}

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {}

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                titleTextView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onDrawerStateChanged(int newState) {}
        });

        fetchEvents();
    }

    private void fetchEvents() {
        announcementList.clear();
        DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference("tbl_events");

        eventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                    String endDateStr = eventSnapshot.child("end_datetime").getValue(String.class);

                    try {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
                        Date endDate = dateFormat.parse(endDateStr);
                        Date today = new Date();

                        if (endDate != null && !endDate.before(today)) {
                            String title = eventSnapshot.child("Title").getValue(String.class);
                            String description = eventSnapshot.child("Description").getValue(String.class);
                            String startDatetime = eventSnapshot.child("start_datetime").getValue(String.class);
                            if (startDatetime != null) {
                                startDatetime = startDatetime.replace("T", " "); // or use any other separator
                            }
                            announcementList.add(new AnnouncementModel(title, description, startDatetime));
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                if (announcementList.isEmpty()) {
                    noAnnouncementImage.setVisibility(View.VISIBLE);
                    noAnnouncementMessage.setVisibility(View.VISIBLE);
                    noAnnouncementSubMessage.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    noAnnouncementImage.setVisibility(View.GONE);
                    noAnnouncementMessage.setVisibility(View.GONE);
                    noAnnouncementSubMessage.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
                announcementAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("fetchEvents", "Database error: " + error.getMessage());
            }
        });
    }

    private void showDeleteConfirmationDialog(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Announcement")
                .setMessage("Are you sure you want to delete this announcement?")
                .setPositiveButton("Yes", (dialog, which) -> deleteAnnouncement(position))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void deleteAnnouncement(int position) {
        // Check if the position is valid
        if (position >= 0 && position < announcementList.size()) {
            // Remove the announcement from the list
            announcementList.remove(position);
            // Notify the adapter of the item removal
            announcementAdapter.notifyItemRemoved(position);

            // Check if the list is now empty and update visibility
            if (announcementList.isEmpty()) {
                noAnnouncementImage.setVisibility(View.VISIBLE);
                noAnnouncementMessage.setVisibility(View.VISIBLE);
                noAnnouncementSubMessage.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                // Notify the adapter that the data set has changed if not empty
                announcementAdapter.notifyDataSetChanged();
            }

            Toast.makeText(this, "Announcement deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Invalid position", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAnnouncementClick(AnnouncementModel announcement) {
        showAnnouncementDetails(announcement);
    }

    private void showAnnouncementDetails(AnnouncementModel announcement) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_announcement_details, null);
        builder.setView(dialogView);

        TextView titleTextView = dialogView.findViewById(R.id.announcement_title);
        TextView contentTextView = dialogView.findViewById(R.id.dialog_announcement_content);
        TextView dateTextView = dialogView.findViewById(R.id.dialog_announcement_date_time);

        titleTextView.setText(announcement.getTitle());
        contentTextView.setText(announcement.getContent());
        dateTextView.setText(announcement.getDateTime());

        builder.setTitle("Announcement Details")
                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }
}
