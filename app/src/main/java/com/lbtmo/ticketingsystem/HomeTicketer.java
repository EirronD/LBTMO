package com.lbtmo.ticketingsystem;

import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
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

import com.airbnb.lottie.LottieAnimationView;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
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

    private ProgressBar progressBar;
    private LinearLayout mainLayout;

    private LottieAnimationView noInternetAnimation;
    private TextView noInternetMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_ticketer);
        titleTextView = findViewById(R.id.titletextview); // Initialize the TextView

        violatorsCountNow = findViewById(R.id.violatorsCountNow);
        violationsCountYesterday = findViewById(R.id.violationsCountYesterday);
        barChart = findViewById(R.id.barChart); // Initialize BarChart
        doughnutChart = findViewById(R.id.doughnutChart); // Initialize Doughnut Chart

        progressBar = findViewById(R.id.loadingIndicator);
        mainLayout = findViewById(R.id.main);

        // Initialize Lottie animation view
        noInternetAnimation = findViewById(R.id.noInternetAnimation);
        noInternetMessage = findViewById(R.id.noInternetMessage);

        // Initialize Firebase database reference
        violationsDatabase = FirebaseDatabase.getInstance().getReference("tbl_violation");

        // Call function to check internet and show animation if necessary
        checkInternetAndShowAnimation();

        fetchViolationType();

        // Get current date and yesterday's date
        String today = getCurrentDate();
        String yesterday = getYesterdayDate();

        // Query Firebase for violations from today and yesterday
        fetchViolationsByDate(today, true);

        // Get top barangay
        getTopBarangays();

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
                    progressBar.setVisibility(View.GONE);  // Hide the progress bar
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

    private void fetchViolationType() {
        ArrayList<PieEntry> entries = new ArrayList<>();
        Map<String, Integer> offenseCountMap = new HashMap<>();
        progressBar.setVisibility(View.VISIBLE);
        mainLayout.setVisibility(View.GONE);
        DatabaseReference violationListRef = FirebaseDatabase.getInstance().getReference("tbl_violation_list");

        violationListRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
                    mainLayout.startAnimation(android.view.animation.AnimationUtils.loadAnimation(HomeTicketer.this, R.anim.fade_in));
                }, 3000); // 3-second delay
                int totalViolations = (int) snapshot.getChildrenCount(); // Total number of offenses
                final int[] processedViolations = {0}; // Track number of processed offenses

                for (DataSnapshot violationSnapshot : snapshot.getChildren()) {
                    String offenseId = violationSnapshot.child("OFFENSE_ID").getValue(String.class);
                    DatabaseReference offenseRef = FirebaseDatabase.getInstance().getReference("tbl_offense").child(offenseId);

                    offenseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot offensesnapshot) {
                            String offenseType = offensesnapshot.child("CODE").getValue(String.class);
                            if (offenseType != null) {
                                // Update count in the map
                                int count = offenseCountMap.getOrDefault(offenseType, 0);
                                offenseCountMap.put(offenseType, count + 1);
                            }

                            // Increment processed count and check if it's the last offense
                            processedViolations[0]++;
                            if (processedViolations[0] == totalViolations) {
                                // Sort the offenseCountMap by count in descending order
                                List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(offenseCountMap.entrySet());
                                sortedList.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

                                // Fetch only the top 5 most violated offenses
                                int limit = Math.min(5, sortedList.size());
                                for (int i = 0; i < limit; i++) {
                                    Map.Entry<String, Integer> entry = sortedList.get(i);
                                    String offenseLabel = entry.getKey();
                                    // Truncate the label if it's too long and add ellipses
                                    if (offenseLabel.length() > 5) {
                                        offenseLabel = offenseLabel.substring(0, 5) + "...";
                                    }
                                    entries.add(new PieEntry(entry.getValue(), offenseLabel));
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
                progressBar.setVisibility(View.GONE);
                Log.e("fetchViolationType", "Error fetching violation list", error.toException());
            }
        });
        TextView emailLink = findViewById(R.id.emailLink);
        emailLink.setOnClickListener(v -> openWebPage("https://losbanos.gov.ph/sendmail"));
    }

    private void updatePieChart(ArrayList<PieEntry> entries) {
        // Create a PieDataSet with entries
        PieDataSet dataSet = new PieDataSet(entries, "Violation Codes");
        dataSet.setColors(getColorPalette(entries.size()));  // Use a custom color palette
        dataSet.setSliceSpace(3f);  // Slightly increased space between slices for a cleaner look
        dataSet.setSelectionShift(12f);  // Enhanced slice highlighting on selection

        // Create PieData
        PieData data = new PieData(dataSet);
        data.setValueTextSize(14f);  // Increased text size for better readability
        data.setValueTextColor(Color.WHITE);
        data.setValueFormatter(new PercentFormatter());  // Display percentages on the slices

        // Set data to the chart
        doughnutChart.setData(data);
        doughnutChart.setUsePercentValues(true);
        doughnutChart.getDescription().setEnabled(false);  // Disable the default description
        doughnutChart.setDrawHoleEnabled(true);
        doughnutChart.setHoleRadius(50f);  // Hole radius for a more balanced look
        doughnutChart.setTransparentCircleRadius(60f);  // Slightly larger transparent circle radius

        // Customize chart appearance
        int labelColor = isDarkMode() ? Color.WHITE : Color.BLACK;
        doughnutChart.getLegend().setTextColor(labelColor);
        doughnutChart.getLegend().setOrientation(Legend.LegendOrientation.HORIZONTAL);
        doughnutChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        doughnutChart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        doughnutChart.getLegend().setTextSize(10f);  // Adjusted legend text size

        doughnutChart.getLegend().setWordWrapEnabled(true);



        // Add smooth animation for a polished experience
        doughnutChart.animateY(6000, Easing.EaseInOutQuad);  // Longer animation duration for smoother transition

        // Enable interaction for better user experience
        doughnutChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                // Handle slice selection, maybe show detailed info
                if (e instanceof PieEntry) {
                    PieEntry selectedEntry = (PieEntry) e;
                    String label = selectedEntry.getLabel();
                    float value = selectedEntry.getValue();
                    // Optionally display a toast or a dialog with detailed information
                    Log.d("PieChart", "Selected: " + label + " with value: " + value);
                }
            }

            @Override
            public void onNothingSelected() {
                // Handle case when nothing is selected
            }
        });

        // Refresh the chart with new data
        doughnutChart.invalidate();
    }

    // Method to generate a custom color palette based on the number of entries
    private int[] getColorPalette(int entryCount) {
        // Generate a dynamic color palette
        int[] colors = new int[entryCount];
        for (int i = 0; i < entryCount; i++) {
            colors[i] = Color.rgb(
                    (int) (Math.random() * 256),
                    (int) (Math.random() * 256),
                    (int) (Math.random() * 256)
            );
        }
        return colors;
    }

    // Custom value formatter for displaying percentages
    private class PercentFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            return String.format("%.1f%%", value);
        }
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

                // Iterate through each violation
                for (DataSnapshot violationSnapshot : snapshot.getChildren()) {
                    //String v = snapshot.child("OFFENSE")
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
                progressBar.setVisibility(View.GONE);
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
                progressBar.setVisibility(View.GONE);
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
                progressBar.setVisibility(View.GONE);
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

            // Use a gradient color for the bars (if required) or pick dynamic colors
            int barColor = ColorTemplate.COLORFUL_COLORS[i % ColorTemplate.COLORFUL_COLORS.length];
            BarDataSet barDataSet = new BarDataSet(Collections.singletonList(entry), barangayNames.get(i));
            barDataSet.setColor(barColor); // Assign the color
            barDataSet.setValueTextColor(Color.WHITE);  // Set text color for the values
            barDataSet.setValueTextSize(12f);  // Set size of the value text
            barDataSets.add(barDataSet); // Add this dataset to the list
        }

        BarData barData = new BarData();
        for (BarDataSet dataSet : barDataSets) {
            barData.addDataSet(dataSet);
        }

        // Set the data to the chart
        barChart.setData(barData);

        // Set description and other styling options
        barChart.getDescription().setText(" ");
        barChart.getDescription().setTextColor(labelColor);
        barChart.getDescription().setTextSize(12f);

        // Hide right Y-axis if you don't need it
        barChart.getAxisRight().setEnabled(false);

        // Customizing the X-axis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setTextColor(labelColor);
        xAxis.setTextSize(8f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(barangayNames));  // Set custom labels for X-axis

        // Customize the Y-axis
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setTextColor(labelColor);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setGranularity(1f);
        leftAxis.setTextSize(10f);

        // Set axis line colors
        barChart.getAxisLeft().setAxisLineColor(labelColor);
        barChart.getXAxis().setAxisLineColor(labelColor);

        // Customize the chart's legend
        barChart.getLegend().setTextColor(labelColor);
        barChart.getLegend().setTextSize(10f);
        barChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        barChart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);

        // Set animation for bar chart
        barChart.animateY(6000, Easing.EaseInOutCubic);

        // Displaying values on top of bars
        barData.setValueFormatter(new LargeValueFormatter());  // Format large numbers

        // Refresh the chart
        barChart.invalidate();
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

    private void openWebPage(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

}
