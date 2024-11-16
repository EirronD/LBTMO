package com.lbtmo.ticketingsystem;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.facebook.shimmer.ShimmerFrameLayout;
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
import java.util.Locale;

public class HomeUsers extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ImageButton buttonDrawerToggle;
    private NavigationView navigationView;
    private TextView titleTextView, welcomefname;
    private TextView violationTypeTextView;
    private  TextView dateViolated;
    private TextView violationStreet;
    private TextView violationBarangay;
    private TextView Status;
    private String userKey;

    private RecyclerView recyclerView;
    private HorizontalAdapter adapter;
    private List<ItemHorizontalRecyclerViewModel> itemList;

    private ProgressBar progressBar;
    private LinearLayout mainLayout;

    private LottieAnimationView noInternetAnimation;
    private TextView noInternetMessage;

    private ArrayList<String>imageUrls = new ArrayList<>();
    private ArrayList<String>names = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_users);

        // Initialize DrawerLayout, NavigationView, and TextView
        drawerLayout = findViewById(R.id.drawer_layout);
        buttonDrawerToggle = findViewById(R.id.buttonDrawerToggle);
        navigationView = findViewById(R.id.navigationView);
        titleTextView = findViewById(R.id.titletextview);
        violationTypeTextView = findViewById(R.id.violationType);
        dateViolated = findViewById(R.id.violationDateTime);
        violationStreet = findViewById(R.id.violationStreet);
        violationBarangay = findViewById(R.id.violationBarangay);
        Status = findViewById(R.id.violationStatus);

        welcomefname = findViewById(R.id.welcomefname);

        progressBar = findViewById(R.id.loadingIndicator);
        mainLayout = findViewById(R.id.main);

        // Initialize Lottie animation view
        noInternetAnimation = findViewById(R.id.noInternetAnimation);
        noInternetMessage = findViewById(R.id.noInternetMessage);

        setupHorizontalRecyclerView();

        // Call function to check internet and show animation if necessary
        checkInternetAndShowAnimation();

        userKey = getIntent().getStringExtra("userKey");
        if (userKey != null) {
            retriveFname(userKey);
        } else {
            welcomefname.setText("Welcome Users!");
        }


        // Set up drawer toggle button to open/close navigation drawer
        buttonDrawerToggle.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(navigationView)) {

                drawerLayout.closeDrawer(navigationView);
            }
            else {
                drawerLayout.openDrawer(navigationView);
                // Hide the TextView when a drawer item is clicked
                titleTextView.setVisibility(View.GONE);
            }
        });

        // Set up NavigationView item click listener
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();

                // Ensure userKey is passed when switching activities
                if (itemId == R.id.home) {
                    Intent intent = new Intent(HomeUsers.this, HomeUsers.class);
                    intent.putExtra("userKey", userKey);
                    startActivity(intent);
                }
                if (itemId == R.id.profile) {
                    Intent intent = new Intent(HomeUsers.this, UserProfile.class);
                    intent.putExtra("userKey", userKey);
                    startActivity(intent);
                }

                if (itemId == R.id.seeviolations) {
                    Intent intent = new Intent(HomeUsers.this, UserViolation.class);
                    intent.putExtra("userKey", userKey);
                    startActivity(intent);
                }

                if (itemId == R.id.announcement) {
                    Intent intent = new Intent(HomeUsers.this, Announcement.class);
                    intent.putExtra("userKey", userKey);  // Pass userKey to Announcement as well
                    startActivity(intent);
                }

                if (itemId == R.id.logout) {
                    Intent intent = new Intent(HomeUsers.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }

                drawerLayout.closeDrawer(navigationView);  // Close the drawer after selecting an item
                return true;
            }
        });
        // Add a listener to detect when the drawer is closed
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

        TextView emailLink = findViewById(R.id.emailLink);
        emailLink.setOnClickListener(v -> openWebPage("https://losbanos.gov.ph/sendmail"));

        countViolations();
        loadLatestEvent();
        fetchlatestViolation();
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

    private void retriveFname(String userKey) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("tbl_drivers").child(userKey);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String firstName = snapshot.child("FNAME").getValue(String.class);
                    // Populate your TextViews with the retrieved data
                    welcomefname.setText("Welcome, " + firstName);
                } else {
                    welcomefname.setText("Welcome Users!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", error.getMessage());
                Toast.makeText(HomeUsers.this, "Error retrieving data.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void fetchlatestViolation() {
        progressBar.setVisibility(View.VISIBLE);
        mainLayout.setVisibility(View.GONE);
        DatabaseReference violationRef = FirebaseDatabase.getInstance().getReference("tbl_violation");
        DatabaseReference violationListRef = FirebaseDatabase.getInstance().getReference("tbl_violation_list");
        DatabaseReference offenseRef = FirebaseDatabase.getInstance().getReference("tbl_offense");
        DatabaseReference streetsRef = FirebaseDatabase.getInstance().getReference("tbl_streets");
        DatabaseReference barangayRef = FirebaseDatabase.getInstance().getReference("tbl_barangay");

        violationRef.orderByChild("DRIVER_ID").equalTo(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Simulate a delay for fetching data
                new android.os.Handler().postDelayed(() -> {
                    // Data fetching complete, show the LinearLayout and hide the ProgressBar
                    progressBar.setVisibility(View.GONE);
                    // Remove animation and "No Internet" message if internet is available
                    noInternetAnimation.setVisibility(View.GONE);
                    noInternetMessage.setVisibility(View.GONE);
                    mainLayout.setVisibility(View.VISIBLE);

                    // Apply fade-in animation
                    mainLayout.startAnimation(android.view.animation.AnimationUtils.loadAnimation(HomeUsers.this, R.anim.fade_in));
                }, 3000); // 3-second delay
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String ticketNo = dataSnapshot.child("TICKET_NO").getValue(String.class);
                    String DateViolated = dataSnapshot.child("DATE_VIOLATED").getValue(String.class);
                    String streetId = dataSnapshot.child("STREET").getValue(String.class);
                    String barangayId = dataSnapshot.child("BARANGAY").getValue(String.class);
                    String status = dataSnapshot.child("STATUS").getValue(String.class);

                    dateViolated.setText("Date: " + DateViolated);
                    Status.setText("Status: " + status);

                    // Now fetch the street name using streetId (SID) from tbl_streets
                    streetsRef.orderByChild("SID").equalTo(streetId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot streetSnapshot : snapshot.getChildren()) {
                                    String streetName = streetSnapshot.child("STREET").getValue(String.class);

                                    // Set the street name
                                    violationStreet.setText(streetName);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            progressBar.setVisibility(View.GONE);
                            Log.e("FirebaseError", error.getMessage());
                            // Handle error if necessary
                        }
                    });
                    barangayRef.orderByChild("ID").equalTo(barangayId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot barangaySnapshot : snapshot.getChildren()) {
                                    String barangayName = barangaySnapshot.child("BARANGAY").getValue(String.class);

                                    violationBarangay.setText(barangayName);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    // Fetch the violations from tbl_violation_list using ticketNo
                    violationListRef.orderByChild("TICKET_NO").equalTo(ticketNo).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            int totalOffenses = (int) snapshot.getChildrenCount();
                            final int[] offensesProcessed = {0};
                            StringBuilder offenseNames = new StringBuilder();

                            for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                                String offenseId = dataSnapshot1.child("OFFENSE_ID").getValue(String.class);

                                offenseRef.child(offenseId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            String offenseName = snapshot.child("OFFENSE").getValue(String.class);

                                            if (offenseNames.length() > 0) {
                                                offenseNames.append(", ");
                                            }
                                            offenseNames.append(offenseName);

                                            offensesProcessed[0]++;

                                            // After processing all offenses, set the violation type
                                            if (offensesProcessed[0] == totalOffenses) {
                                                violationTypeTextView.setText("Violation Type: " + offenseNames.toString());
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        // Handle error if necessary
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle error if necessary
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error if necessary
            }
        });
    }


    private void loadLatestEvent() {
        progressBar.setVisibility(View.VISIBLE);
        mainLayout.setVisibility(View.GONE);
        DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference("tbl_events");

        eventsRef.orderByChild("start_datetime")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Simulate a delay for fetching data
                        new android.os.Handler().postDelayed(() -> {
                            // Data fetching complete, show the LinearLayout and hide the ProgressBar
                            progressBar.setVisibility(View.GONE);
                            // Remove animation and "No Internet" message if internet is available
                            noInternetAnimation.setVisibility(View.GONE);
                            noInternetMessage.setVisibility(View.GONE);
                            mainLayout.setVisibility(View.VISIBLE);
                        }, 3000); // 3-second delay
                        String latestDescription = null;
                        long currentTimeMillis = System.currentTimeMillis();

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault());

                        for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                            String endDatetimeString = eventSnapshot.child("end_datetime").getValue(String.class);

                            try {
                                Date endDate = dateFormat.parse(endDatetimeString);
                                long endDatetimeMillis = endDate != null ? endDate.getTime() : 0;

                                // Check if the event is still active (not in the past)
                                if (endDatetimeMillis >= currentTimeMillis) {
                                    // Get the latest announcement description
                                    latestDescription = eventSnapshot.child("Description").getValue(String.class);
                                    break; // Once we find the latest valid event, exit the loop
                                }
                            } catch (ParseException e) {
                                Log.e("DateParseError", "Error parsing end_datetime: " + endDatetimeString, e);
                            }
                        }

                        TextView announcementContent = findViewById(R.id.announcementContent);
                        if (latestDescription != null) {
                            announcementContent.setText(latestDescription);
                        } else {
                            announcementContent.setText("No announcements available.");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        Log.e("FirebaseError", "Error loading events: " + error.getMessage());
                    }
                });
    }


    private void countViolations() {
        DatabaseReference violationsRef = FirebaseDatabase.getInstance().getReference("tbl_violation");

        violationsRef.orderByChild("DRIVER_ID").equalTo(userKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int totalCount = 0;
                        int pendingCount = 0;

                        for (DataSnapshot violationSnapshot : snapshot.getChildren()) {
                            totalCount++;

                            String status = violationSnapshot.child("STATUS").getValue(String.class);
                            if ("PENDING".equals(status)) {
                                pendingCount++;
                            }
                        }

                        TextView violationsCountYesterday = findViewById(R.id.violationsCountYesterday);
                        violationsCountYesterday.setText(String.valueOf(totalCount));

                        TextView violatorsCountNow = findViewById(R.id.violatorsCountNow);
                        violatorsCountNow.setText(String.valueOf(pendingCount));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FirebaseError", "Error counting violations: " + error.getMessage());
                    }
                });
    }


    private void setupHorizontalRecyclerView() {
        recyclerView = findViewById(R.id.horizontal_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<ItemHorizontalRecyclerViewModel> itemList = new ArrayList<>();
        // Populate the list with data
        itemList.add(new ItemHorizontalRecyclerViewModel(R.drawable.screwdriver, "Maintain Vehicle Condition", "Regular maintenance ensures your vehicle is safe and reliable."));
        itemList.add(new ItemHorizontalRecyclerViewModel(R.drawable.trafficlight, "Obey Traffic Laws", "Following traffic signals helps prevent accidents and keeps traffic flowing."));
        itemList.add(new ItemHorizontalRecyclerViewModel(R.drawable.nocellphone, "Avoid Distraction", "Stay focused on the road by avoiding phone use while driving."));
        itemList.add(new ItemHorizontalRecyclerViewModel(R.drawable.beermug, "Don't Drive Drunk", "Driving under the influence poses a serious risk to you and others."));
        itemList.add(new ItemHorizontalRecyclerViewModel(R.drawable.helmet, "Wear Safety Gear", "Always wear your helmet for protection while riding."));
        itemList.add(new ItemHorizontalRecyclerViewModel(R.drawable.speedometer, "Watch Your Speed Limits", "Adhere to speed limits to ensure safety for yourself and others."));
        itemList.add(new ItemHorizontalRecyclerViewModel(R.drawable.caution, "Stay Alert", "Being attentive can help you react quickly to potential hazards."));
        adapter = new HorizontalAdapter(this, itemList);
        recyclerView.setAdapter(adapter);
    }

    private void openWebPage(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}
