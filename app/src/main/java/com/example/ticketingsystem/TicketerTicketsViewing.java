package com.example.ticketingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TicketerTicketsViewing extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private ImageButton buttonDrawerToggle;
    private NavigationView navigationView;

    private RecyclerView ticketRecyclerView;
    private ImageView noViolationsImage;
    private TextView noViolationsMessage, noViolationsSubMessage;
    private TextView titleTextView;
    private String userKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticketer_tickets_viewing);

        titleTextView = findViewById(R.id.titletextview);
        // Initialize Drawer Layout
        drawerLayout = findViewById(R.id.drawerLayout);
        buttonDrawerToggle = findViewById(R.id.buttonDrawerToggle);
        navigationView = findViewById(R.id.navigationView);

        userKey = getIntent().getStringExtra("userKey");

        fetchUser();

        //buttonDrawerToggle.setOnClickListener(v -> drawerLayout.open());
        // Set up drawer toggle button to open/close navigation drawer
        buttonDrawerToggle.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(navigationView)) {

                drawerLayout.closeDrawer(navigationView);
            }
            else {
                drawerLayout.openDrawer(navigationView);
                titleTextView.setVisibility(View.GONE);
            }
        });
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int itemId = menuItem.getItemId();

            if (itemId == R.id.home) {
                Intent intent = new Intent(TicketerTicketsViewing.this, HomeTicketer.class);
                intent.putExtra("userKey", userKey);
                startActivity(intent);
            } else if (itemId == R.id.profile) {
                Intent intent = new Intent(TicketerTicketsViewing.this, Ticketer_Profile.class);
                intent.putExtra("userKey", userKey);
                startActivity(intent);
            } else if (itemId == R.id.issuetickets) {
                Intent intent = new Intent(TicketerTicketsViewing.this, IssuesTickets.class);
                intent.putExtra("userKey", userKey);
                startActivity(intent);
            } else if (itemId == R.id.mytickets) {
                drawerLayout.close();
            } else if (itemId == R.id.logout) {
                Intent intent = new Intent(TicketerTicketsViewing.this, MainActivity.class);
                startActivity(intent);
            }

            drawerLayout.close();
            return true;
        });
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                // No action needed here
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                // No action needed here
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                // Show the TextView when the drawer is closed
                titleTextView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                // No action needed here
            }
        });

        ticketRecyclerView = findViewById(R.id.ticketRecyclerView);
        noViolationsImage = findViewById(R.id.noViolationsImage);
        noViolationsMessage = findViewById(R.id.noViolationsMessage);
        noViolationsSubMessage = findViewById(R.id.noViolationsSubMessage);

        ticketRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    }
    private void fetchUser() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("tbl_users").child(userKey);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String firstName = snapshot.child("FIRSTNAME").getValue(String.class);
                    String lastName = snapshot.child("LASTNAME").getValue(String.class);

                    String formattedName = lastName + ", " + firstName;

                    DatabaseReference violationRef = FirebaseDatabase.getInstance().getReference("tbl_violation");
                    violationRef.orderByChild("REPORTING_OFFICER").equalTo(formattedName).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot vsnapshot) {
                            List<Ticket> tickets = new ArrayList<>();
                            if (vsnapshot.exists()) {
                                for (DataSnapshot violation : vsnapshot.getChildren()) {
                                    String dateViolated = violation.child("DATE_VIOLATED").getValue(String.class);
                                    String ticketNo = violation.child("TICKET_NO").getValue(String.class);
                                    String plateNo = violation.child("PLATE_NUMBER").getValue(String.class);
                                    String streetId = violation.child("STREET").getValue(String.class);
                                    String barangayId = violation.child("BARANGAY").getValue(String.class);

                                    DatabaseReference streetRef = FirebaseDatabase.getInstance().getReference("tbl_streets");
                                    streetRef.orderByChild("SID").equalTo(streetId).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot streetSnapshot) {

                                            DataSnapshot streetData = streetSnapshot.getChildren().iterator().next();
                                            String streetName = streetData.child("STREET").getValue(String.class);

                                            DatabaseReference barangayRef = FirebaseDatabase.getInstance().getReference("tbl_barangay");
                                            barangayRef.orderByChild("ID").equalTo(barangayId).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot barangaySnapshot) {
                                                    DataSnapshot barangayData = barangaySnapshot.getChildren().iterator().next();
                                                    String barangayName = barangayData.child("BARANGAY").getValue(String.class);

                                                    DatabaseReference violationListRef = FirebaseDatabase.getInstance().getReference("tbl_violation_list");
                                                    violationListRef.orderByChild("TICKET_NO").equalTo(ticketNo).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot vlsnapshot) {
                                                            if (vlsnapshot.exists()) {
                                                                for (DataSnapshot violationList : vlsnapshot.getChildren()) {
                                                                    String driverId = violationList.child("DRIVER_ID").getValue(String.class);
                                                                    String offenseId = violationList.child("OFFENSE_ID").getValue(String.class);

                                                                    DatabaseReference offensesRef = FirebaseDatabase.getInstance().getReference("tbl_offense").child(offenseId);
                                                                    offensesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(@NonNull DataSnapshot osnapshot) {
                                                                            if (osnapshot.exists()) {
                                                                                String code = osnapshot.child("CODE").getValue(String.class);
                                                                                String offense = osnapshot.child("OFFENSE").getValue(String.class);

                                                                                DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference("tbl_drivers").child(driverId);
                                                                                driverRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                    @Override
                                                                                    public void onDataChange(@NonNull DataSnapshot dsnapshot) {
                                                                                        if (dsnapshot.exists()) {
                                                                                            String fname = dsnapshot.child("FNAME").getValue(String.class);
                                                                                            String lname = dsnapshot.child("LNAME").getValue(String.class);
                                                                                            String mi = dsnapshot.child("MI").getValue(String.class);

                                                                                            tickets.add(new Ticket(code, offense, dateViolated, fname, lname, mi, plateNo, barangayName, streetName));
                                                                                            updateUIWithTickets(tickets);
                                                                                        }
                                                                                    }

                                                                                    @Override
                                                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                                                        Log.e("fetchUser", "Failed to read driver data", error.toException());
                                                                                    }
                                                                                });
                                                                            }
                                                                        }

                                                                        @Override
                                                                        public void onCancelled(@NonNull DatabaseError error) {
                                                                            Log.e("fetchUser", "Failed to read offense data", error.toException());
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {
                                                            Log.e("fetchUser", "Failed to read violation list data", error.toException());
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {
                                                    Log.e("fetchUser", "Failed to read barangay data", error.toException());
                                                }
                                            });
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.e("fetchUser", "Failed to read street data", error.toException());
                                        }
                                    });
                                }
                            } else {
                                updateUIWithTickets(tickets);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("fetchUser", "Failed to read violation data", error.toException());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("fetchUser", "Failed to read user data", error.toException());
            }
        });
    }

    private void updateUIWithTickets(List<Ticket> tickets) {
        if (tickets.isEmpty()) {
            noViolationsImage.setVisibility(View.VISIBLE);
            noViolationsMessage.setVisibility(View.VISIBLE);
            noViolationsSubMessage.setVisibility(View.VISIBLE);
            ticketRecyclerView.setVisibility(View.GONE);
        } else {
            noViolationsImage.setVisibility(View.GONE);
            noViolationsMessage.setVisibility(View.GONE);
            noViolationsSubMessage.setVisibility(View.GONE);
            ticketRecyclerView.setVisibility(View.VISIBLE);

            TicketerTicketsViewingAdapter ticketAdapter = new TicketerTicketsViewingAdapter(TicketerTicketsViewing.this, tickets);
            ticketRecyclerView.setAdapter(ticketAdapter);
        }
    }

}
