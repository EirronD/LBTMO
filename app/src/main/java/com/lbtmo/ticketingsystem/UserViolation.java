package com.lbtmo.ticketingsystem;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserViolation extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ViolationAdapter violationAdapter;
    private ArrayList<Violation> violationList;
    private DrawerLayout drawerLayout;
    private ImageButton buttonDrawerToggle;
    private NavigationView navigationView;
    private String userKey; // Make userKey a class-level variable
    private DatabaseReference violationListRef;
    private int offensesCount = 0; // Counter for fetched offenses
    private TextView titleTextView;

    private ProgressBar progressBar;
    private LinearLayout mainLayout;

    private LottieAnimationView noInternetAnimation;
    private TextView noInternetMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_violation);
        titleTextView = findViewById(R.id.titletextview); // Initialize the TextView

        // Initialize UI elements
        violationList = new ArrayList<>();
        recyclerView = findViewById(R.id.violationsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mainLayout = findViewById(R.id.main);
        progressBar = findViewById(R.id.loadingIndicator);

        violationAdapter = new ViolationAdapter(this, violationList);
        recyclerView.setAdapter(violationAdapter);

        drawerLayout = findViewById(R.id.drawerLayout);
        buttonDrawerToggle = findViewById(R.id.buttonDrawerToggle);
        navigationView = findViewById(R.id.navigationView);

        // Initialize Lottie animation view
        noInternetAnimation = findViewById(R.id.noInternetAnimation);
        noInternetMessage = findViewById(R.id.noInternetMessage);


        violationListRef = FirebaseDatabase.getInstance().getReference("tbl_violation");

        // Retrieve the userKey from the intent
        userKey = getIntent().getStringExtra("userKey");
        setupDrawer();
        fetchUserViolations(userKey);
        // Call function to check internet and show animation if necessary
        checkInternetAndShowAnimation();
    }
    private void checkInternetAndShowAnimation() {
        noInternetAnimation.setVisibility(View.GONE); // Hide the animation initially
        noInternetMessage.setVisibility(View.GONE);   // Hide the message initially

        // Use Handler to wait for 10 seconds to check the internet availability
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isInternetAvailable()) {
                    // If no internet after 10 seconds, show Lottie animation and "No Internet" message
                    noInternetAnimation.setVisibility(View.VISIBLE);
                    noInternetMessage.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);  // Hide the progress bar
                    mainLayout.setVisibility(View.GONE);   // Hide the main content
                } else {
                    // If internet is available, hide the progress bar and show main content
                    progressBar.setVisibility(View.GONE);  // Hide the progress bar after 10 seconds
                    mainLayout.setVisibility(View.VISIBLE);  // Show the main content
                }
            }
        }, 10000);  // Wait for 10 seconds before checking the internet status
    }

    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void fetchUserViolations(String driverId) {
        progressBar.setVisibility(View.VISIBLE);
        mainLayout.setVisibility(View.GONE);

        violationListRef.orderByChild("DRIVER_ID").equalTo(driverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                new android.os.Handler().postDelayed(() -> {
                    // Data fetching complete, show the LinearLayout and hide the ProgressBar
                    progressBar.setVisibility(View.GONE);
                    // Remove animation and "No Internet" message if internet is available
                    noInternetAnimation.setVisibility(View.GONE);
                    noInternetMessage.setVisibility(View.GONE);
                    mainLayout.setVisibility(View.VISIBLE);
                    // Apply fade-in animation
                    mainLayout.startAnimation(android.view.animation.AnimationUtils.loadAnimation(UserViolation.this, R.anim.fade_in));
                }, 3000); // 3-second delay
                offensesCount = (int) snapshot.getChildrenCount(); // Count the number of offenses

                if (offensesCount == 0) {
                    Toast.makeText(UserViolation.this, "No violations found.", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (DataSnapshot violationSnapshot : snapshot.getChildren()) {
                    String offenseId = violationSnapshot.child("TICKET_NO").getValue(String.class);
                    String streetId = violationSnapshot.child("STREET").getValue(String.class);
                    String barangayId = violationSnapshot.child("BARANGAY").getValue(String.class);
                    String dateViolated = violationSnapshot.child("DATE_VIOLATED").getValue(String.class);
                    String status = violationSnapshot.child("STATUS").getValue(String.class);

                    if (offenseId != null) {
                        fetchDriverDetails(driverId, offenseId, streetId, barangayId, dateViolated, status); // Pass additional fields
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE); // Hide loading indicator on error
                Toast.makeText(UserViolation.this, "Error fetching violations: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchDriverDetails(String driverId, String offenseId, String streetId, String barangayId, String dateViolated, String status) {
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference("tbl_drivers");

        driverRef.child(driverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String fName = snapshot.child("FNAME").getValue(String.class);
                    String lName = snapshot.child("LNAME").getValue(String.class);
                    String mi = snapshot.child("MI").getValue(String.class);

                    fetchOffenseDetails(offenseId, streetId, barangayId, fName, lName, mi, dateViolated, status); // Pass additional fields
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE); // Hide loading indicator on error
                Toast.makeText(UserViolation.this, "Error fetching driver details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void fetchOffenseDetails(String offenseId, String streetId, String barangayId, String fName, String lName, String mi, String dateViolated, String status) {
        DatabaseReference violationRef = FirebaseDatabase.getInstance().getReference("tbl_violation_list");

        violationRef.orderByChild("TICKET_NO").equalTo(offenseId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    for (DataSnapshot violationSnapshot : snapshot.getChildren()) {
                        String offenseReferenceId = violationSnapshot.child("OFFENSE_ID").getValue(String.class);
                        fetchOffenseName(offenseReferenceId, streetId, barangayId, fName, lName, mi, dateViolated, status);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE); // Hide loading indicator on error
                Toast.makeText(UserViolation.this, "Error fetching offense: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchOffenseName(String offenseReferenceId, String streetId, String barangayId, String fName, String lName, String mi, String dateViolated, String status) {
        DatabaseReference offenseRef = FirebaseDatabase.getInstance().getReference("tbl_offense");

        offenseRef.child(offenseReferenceId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String offenseName = snapshot.child("OFFENSE").getValue(String.class);
                    fetchStreetAndBarangay(streetId, barangayId, offenseName, fName, lName, mi, dateViolated, status);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE); // Hide loading indicator on error
                Toast.makeText(UserViolation.this, "Error fetching offense details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchStreetAndBarangay(String streetId, String barangayId, String offenseName, String fName, String lName, String mi, String dateViolated, String status) {

        DatabaseReference streetRef = FirebaseDatabase.getInstance().getReference("tbl_streets");
        DatabaseReference barangayRef = FirebaseDatabase.getInstance().getReference("tbl_barangay");

        streetRef.orderByChild("SID").equalTo(streetId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot streetSnapshot) {
                if (streetSnapshot.exists()) {
                    DataSnapshot streetData = streetSnapshot.getChildren().iterator().next();
                    String streetName = streetData.child("STREET").getValue(String.class);

                    barangayRef.orderByChild("ID").equalTo(barangayId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot barangaySnapshot) {
                            if (barangaySnapshot.exists()) {
                                DataSnapshot barangayData = barangaySnapshot.getChildren().iterator().next();
                                String barangayName = barangayData.child("BARANGAY").getValue(String.class);

                                violationList.add(new Violation(offenseName, fName, lName, mi, streetName, barangayName, "Los Baños", dateViolated, status));

                                if (violationList.size() == offensesCount) {
                                    violationAdapter.notifyDataSetChanged();
                                    Log.d("ViolationList", "Size after adding violation: " + violationList.size());
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                            Toast.makeText(UserViolation.this, "Error fetching barangay: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.d("ViolationDetails", "Street not found for SID: " + streetId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE); // Hide loading indicator on error
                Toast.makeText(UserViolation.this, "Error fetching street: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void setupDrawer() {
        // Toggle drawer when buttonDrawerToggle is clicked
        buttonDrawerToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerOpen(navigationView)) {
                    drawerLayout.closeDrawer(navigationView);  // Close the drawer
                } else {
                    drawerLayout.openDrawer(navigationView);  // Open the drawer
                    // Hide the TextView when a drawer item is clicked
                    titleTextView.setVisibility(View.GONE);
                }
            }
        });

        // Set up navigation item click listener
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();

                // Ensure userKey is passed when switching activities
                if (itemId == R.id.home) {
                    Intent intent = new Intent(UserViolation.this, HomeUsers.class);
                    intent.putExtra("userKey", userKey);
                    startActivity(intent);
                }
                if (itemId == R.id.profile) {
                    Intent intent = new Intent(UserViolation.this, UserProfile.class);
                    intent.putExtra("userKey", userKey);
                    startActivity(intent);
                }

                if (itemId == R.id.seeviolations) {
                    Intent intent = new Intent(UserViolation.this, UserViolation.class);
                    intent.putExtra("userKey", userKey);
                    startActivity(intent);
                }

                if (itemId == R.id.announcement) {
                    Intent intent = new Intent(UserViolation.this, Announcement.class);
                    intent.putExtra("userKey", userKey);  // Pass userKey to Announcement as well
                    startActivity(intent);
                }

                if (itemId == R.id.logout) {
                    Intent intent = new Intent(UserViolation.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }

                drawerLayout.closeDrawer(navigationView);  // Close the drawer after selecting an item
                return true;
            }
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
    }
}
