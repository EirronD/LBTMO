package com.example.ticketingsystem;

import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeTicketer extends AppCompatActivity {

    private TextView violatorsCountNow, violationsCountYesterday;
    private DatabaseReference violationsDatabase;
    private int currentViolatorsCount = 0;
    private int yesterdayViolatorsCount = 0;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageButton buttonDrawerToggle;
    private BarChart barChart;
    private TextView titleTextView; // Declare the TextView
    private PieChart doughnutChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_ticketer);
        titleTextView = findViewById(R.id.titletextview); // Initialize the TextView

        violatorsCountNow = findViewById(R.id.violatorsCountNow);
        violationsCountYesterday = findViewById(R.id.violationsCountYesterday);
        barChart = findViewById(R.id.barChart); // Initialize BarChart
        doughnutChart = findViewById(R.id.doughnutChart); // Initialize Doughnut Chart

        // Initialize Firebase database reference
        violationsDatabase = FirebaseDatabase.getInstance().getReference("tbl_violation");

        // Get current date and yesterday's date
        String today = getCurrentDate();
        String yesterday = getYesterdayDate();

        // Query Firebase for violations from today and yesterday
        fetchViolationsByDate(today, true);
        fetchViolationsByDate(yesterday, false);

        // Get top barangay
        getTopBarangays();

        fetchViolationType();

        // Initialize DrawerLayout and NavigationView
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        buttonDrawerToggle = findViewById(R.id.buttonDrawerToggle);

        String userKey = getIntent().getStringExtra("userKey");

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

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();

                if (itemId == R.id.home) {
                    drawerLayout.close();
                } else if (itemId == R.id.profile) {
                    Intent intent = new Intent(HomeTicketer.this, Ticketer_Profile.class);
                    intent.putExtra("userKey", userKey);
                    startActivity(intent);
                } else if (itemId == R.id.issuetickets) {
                    Intent intent = new Intent(HomeTicketer.this, IssuesTickets.class);
                    intent.putExtra("userKey", userKey);
                    startActivity(intent);
                } else if (itemId == R.id.mytickets) {
                    Intent intent = new Intent(HomeTicketer.this, TicketerTicketsViewing.class);
                    intent.putExtra("userKey", userKey);
                    startActivity(intent);
                } else if (itemId == R.id.logout) {
                    Intent intent = new Intent(HomeTicketer.this, MainActivity.class);
                    startActivity(intent);
                }
                drawerLayout.close(); // Close the drawer
                return true; // Return true to indicate the item click has been handled
            }
        });
    }
    private void fetchViolationType() {
        ArrayList<PieEntry> entries = new ArrayList<>();
        Map<String, Integer> offenseCountMap = new HashMap<>();
        DatabaseReference violationListRef = FirebaseDatabase.getInstance().getReference("tbl_violation_list");

        violationListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalViolations = (int) snapshot.getChildrenCount(); // Total number of offenses
                final int[] processedViolations = {0}; // Track number of processed offenses

                for (DataSnapshot violationSnapshot : snapshot.getChildren()) {
                    String offenseId = violationSnapshot.child("OFFENSE_ID").getValue(String.class);
                    DatabaseReference offenseRef = FirebaseDatabase.getInstance().getReference("tbl_offense").child(offenseId);

                    offenseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot offensesnapshot) {
                            String offenseType = offensesnapshot.child("OFFENSE").getValue(String.class);
                            if (offenseType != null) {
                                // Update count in the map
                                int count = offenseCountMap.getOrDefault(offenseType, 0);
                                offenseCountMap.put(offenseType, count + 1);
                            }

                            // Increment processed count and check if it's the last offense
                            processedViolations[0]++;
                            if (processedViolations[0] == totalViolations) {
                                for (Map.Entry<String, Integer> entry : offenseCountMap.entrySet()) {
                                    entries.add(new PieEntry(entry.getValue(), entry.getKey()));
                                }
                                updatePieChart(entries);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("fetchViolationType", "Error fetching offense data", error.toException());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("fetchViolationType", "Error fetching violation list", error.toException());
            }
        });
    }


    private void updatePieChart(ArrayList<PieEntry> entries) {
        PieDataSet dataSet = new PieDataSet(entries, "Violation Types");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(10f);
        data.setValueTextColor(Color.WHITE);

        doughnutChart.setData(data);
        doughnutChart.setUsePercentValues(true);
        doughnutChart.getDescription().setEnabled(false);
        doughnutChart.setDrawHoleEnabled(true);
        doughnutChart.setHoleRadius(60f);
        doughnutChart.setTransparentCircleRadius(65f);

        // Styling for theme
        int labelColor = isDarkMode() ? Color.WHITE : Color.BLACK;
        doughnutChart.getLegend().setTextColor(labelColor);

        doughnutChart.invalidate();
    }


    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(Calendar.getInstance().getTime());
    }

    // Method to get yesterday's date in "YYYY-MM-DD" format
    private String getYesterdayDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1); // Subtract one day for yesterday
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }

    // Method to fetch violations based on the date
    private void fetchViolationsByDate(String date, boolean isToday) {
        violationsDatabase.orderByChild("DATE_VIOLATED").equalTo(date).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int violationCount = 0;

                for (DataSnapshot violationSnapshot : snapshot.getChildren()) {
                    violationCount++;
                }

                if (isToday) {
                    currentViolatorsCount = violationCount;
                    violatorsCountNow.setText(String.valueOf(currentViolatorsCount));
                } else {
                    yesterdayViolatorsCount = violationCount;
                    violationsCountYesterday.setText(String.valueOf(yesterdayViolatorsCount));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeTicketer.this, "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTopBarangays() {
        DatabaseReference violationsRef = FirebaseDatabase.getInstance().getReference("tbl_violation");
        final Map<String, Integer> barangayCountMap = new HashMap<>();

        violationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot violationSnapshot : snapshot.getChildren()) {
                    String barangayId = violationSnapshot.child("BARANGAY").getValue(String.class);
                    if (barangayId != null) {
                        barangayCountMap.put(barangayId, barangayCountMap.getOrDefault(barangayId, 0) + 1);
                    }
                }
                getBarangayNames(barangayCountMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeTicketer.this, "Error loading violations.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getBarangayNames(final Map<String, Integer> barangayCountMap) {
        DatabaseReference barangayRef = FirebaseDatabase.getInstance().getReference("tbl_barangay");
        final Map<String, String> barangayNamesMap = new HashMap<>();

        barangayRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot barangaySnapshot : snapshot.getChildren()) {
                    String barangayId = barangaySnapshot.child("ID").getValue(String.class);
                    String barangayName = barangaySnapshot.child("BARANGAY").getValue(String.class);
                    if (barangayId != null && barangayName != null) {
                        barangayNamesMap.put(barangayId, barangayName);
                    }
                }
                showTopBarangays(barangayCountMap, barangayNamesMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeTicketer.this, "Error loading barangay names.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showTopBarangays(Map<String, Integer> barangayCountMap, Map<String, String> barangayNamesMap) {
        List<Map.Entry<String, Integer>> sortedBarangays = new ArrayList<>(barangayCountMap.entrySet());

        // Sort barangays by number of violations (descending)
        Collections.sort(sortedBarangays, (a, b) -> b.getValue().compareTo(a.getValue()));

        // Take the top 5 barangays
        List<String> topBarangays = new ArrayList<>();
        List<Integer> topViolations = new ArrayList<>();

        for (int i = 0; i < Math.min(5, sortedBarangays.size()); i++) {
            String barangayId = sortedBarangays.get(i).getKey();
            int violationCount = sortedBarangays.get(i).getValue();

            String barangayName = barangayNamesMap.get(barangayId);
            if (barangayName != null) {
                topBarangays.add(barangayName);
                topViolations.add(violationCount);
            }
        }

        // Create the bar graph using the top 5 barangays and their violation counts
        createBarGraph(topBarangays, topViolations);
    }

    // Method to check if the current theme is dark mode
    private boolean isDarkMode() {
        UiModeManager uiModeManager = (UiModeManager) getSystemService(Context.UI_MODE_SERVICE);
        return uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_NORMAL &&
                (uiModeManager.getNightMode() == UiModeManager.MODE_NIGHT_YES);
    }

    private void createBarGraph(List<String> barangayNames, List<Integer> violationCounts) {
        ArrayList<BarDataSet> barDataSets = new ArrayList<>();
        ArrayList<BarEntry> barEntries = new ArrayList<>();

        // Determine the label color based on the theme
        int labelColor = isDarkMode() ? Color.WHITE : Color.BLACK;

        // Loop through the barangay names and violation counts
        for (int i = 0; i < violationCounts.size(); i++) {
            BarEntry entry = new BarEntry(i, violationCounts.get(i));
            barEntries.add(entry);

            // Create a new BarDataSet for each barangay
            BarDataSet barDataSet = new BarDataSet(Collections.singletonList(entry), barangayNames.get(i));
            barDataSet.setColor(ColorTemplate.COLORFUL_COLORS[i % ColorTemplate.COLORFUL_COLORS.length]); // Assign a unique color
            barDataSets.add(barDataSet); // Add this dataset to the list
        }

        BarData barData = new BarData();

        // Add all datasets to the BarData
        for (BarDataSet dataSet : barDataSets) {
            barData.addDataSet(dataSet);
        }

        // Set the data to the chart
        barChart.setData(barData);

        // Set description and other styling options
        barChart.getDescription().setText("Top 5 Barangays with Most Violations");
        barChart.getXAxis().setEnabled(false);
        barChart.getAxisLeft().setGranularity(1f);  // Set Y-axis granularity to 1
        barChart.getAxisLeft().setGranularityEnabled(true);  // Enable granularity enforcement
        barChart.getAxisRight().setEnabled(false);  // Disable right Y-axis if you don't need it

        // Set the label color based on the theme
        barChart.getXAxis().setTextColor(labelColor);
        barChart.getAxisLeft().setTextColor(labelColor);
        barChart.getAxisRight().setTextColor(labelColor);
        barChart.getDescription().setTextColor(labelColor);
        barChart.getLegend().setTextColor(labelColor);

        barChart.invalidate(); // Refresh the chart
    }
    private void createDoughnutChart(String violationType, int count) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(count, violationType));
        entries.add(new PieEntry(100 - count, "Others")); // Remaining part of the doughnut

        PieDataSet dataSet = new PieDataSet(entries, "Most Violated Violation");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);  // Set colors for the chart
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(10f);
        data.setValueTextColor(Color.WHITE);

        doughnutChart.setData(data);
        doughnutChart.setUsePercentValues(true);
        doughnutChart.getDescription().setEnabled(false); // Disable description
        doughnutChart.setDrawHoleEnabled(true);
        doughnutChart.setHoleRadius(60f);  // Set the radius for the doughnut hole
        doughnutChart.setTransparentCircleRadius(65f);

        // Styling for the theme
        int labelColor = isDarkMode() ? Color.WHITE : Color.BLACK;
        doughnutChart.getLegend().setTextColor(labelColor);
        doughnutChart.invalidate(); // Refresh the chart
    }

}
