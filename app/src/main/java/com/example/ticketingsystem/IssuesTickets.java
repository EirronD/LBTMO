package com.example.ticketingsystem;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.graphics.drawable.BitmapDrawable;

import java.io.ByteArrayOutputStream;

public class IssuesTickets extends AppCompatActivity {
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private Bitmap imageBitmap;

    private ImageView ticketIv;
    private Button captureCameraBtn, deleteCameraBtn;
    private EditText dateEt;
    private AutoCompleteTextView barangaySpinner, streetSpinner;
    private DrawerLayout drawerLayout;
    private ImageButton buttonDrawerToggle;
    private NavigationView navigationView;
    private RecyclerView recyclerView;

    private TextView titleTextView; // Declare the TextView

    private ViolationListAdapter adapter;
    private List<ViolationListFetch> violationList;

    // Define URLs as constants
    private static final String VIOLATION_URL = "http://192.168.1.11/violationlist.php";
    private static final String BARANGAY_URL = "http://192.168.1.11/fetch_barangays.php";
    private static final String STREET_URL = "http://192.168.1.11/fetch_streets.php?barangay_id=";
    private static final String SUBMIT_URL = "http://192.168.1.11/ticket_issue_credentials.php";
    //*************************************************************************************************************************************************
    //NOTE:Main Function
    //**************************************************************************************************************************************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_tickets);

        titleTextView = findViewById(R.id.titletextview); // Initialize the TextView

        initializeViews();
        setButtonListeners();
        updateButtonVisibility(false); // Initially hide delete button
        ticketIv.setImageResource(R.drawable.picture_placeholder); // Set initial placeholder image
        dateEt = findViewById(R.id.dateEt);

        // Set a click listener for the date EditText
        dateEt.setOnClickListener(v -> showDatePicker());



        barangaySpinner = findViewById(R.id.barangaySpinner);
        streetSpinner = findViewById(R.id.streetSpinner);

        // Populate the barangay spinner
        populateBarangaySpinner();

        // Populate the street spinner
        populateStreetSpinner();

        fetchViolation();

        // Fetch barangays to populate the barangay spinner
        fetchBarangays();

        // Add listener for barangay selection to fetch streets
        barangaySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Barangay selectedBarangay = (Barangay) parent.getItemAtPosition(position);
                fetchStreets(selectedBarangay.getBarangayId()); // Assuming Barangay class has getBarangayId method
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Initialize submit button
        Button submitButton = findViewById(R.id.submitBtn); // Replace with your actual button ID

        // Set OnClickListener for the submit button
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an AlertDialog for confirmation
                AlertDialog.Builder builder = new AlertDialog.Builder(IssuesTickets.this);
                builder.setTitle("Confirm Submission");
                builder.setMessage("Are you sure the credentials are correct?");

                // If the user confirms
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // If confirmed, trigger the function to submit data
                        submitCredentials();

                        // Create an intent to go to TicketerTicketsViewing activity
                        Intent intent = new Intent(IssuesTickets.this, TicketViewing.class);
                        startActivity(intent); // Start the TicketerTicketsViewing activity
                    }
                });

                // If the user cancels
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Dismiss the dialog
                        dialog.dismiss();
                    }
                });

                // Show the dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    private void fetchViolation() {
        recyclerView = findViewById(R.id.ViolationRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        violationList = new ArrayList<>();

        DatabaseReference offenseRef = FirebaseDatabase.getInstance().getReference("tbl_offense");
        offenseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                violationList.clear();
                for (DataSnapshot offenseSnapshot : snapshot.getChildren()) {
                    String code = offenseSnapshot.child("CODE").getValue(String.class);
                    String offense = offenseSnapshot.child("OFFENSE").getValue(String.class);

                    violationList.add(new ViolationListFetch(code, offense));
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("fetchViolation", "Error fetching data", error.toException());
            }
        });
        adapter = new ViolationListAdapter(violationList);
        recyclerView.setAdapter(adapter);
    }

    private void populateStreetSpinner() {
        String[] violationList = getResources().getStringArray(R.array.Street);
        // Create an ArrayAdapter using the dropdown_item layout and the violation list
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_item, violationList);
        // Set the adapter to the AutoCompleteTextView
        streetSpinner.setAdapter(adapter);
    }

    private void populateBarangaySpinner() {
        String[] violationList = getResources().getStringArray(R.array.Barangay);
        // Create an ArrayAdapter using the dropdown_item layout and the violation list
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_item, violationList);
        // Set the adapter to the AutoCompleteTextView
        barangaySpinner.setAdapter(adapter);
    }


    private void submitCredentials() {
        try {
            // Get values from the form (EditTexts, Spinners, etc.)
            String lastname = ((EditText) findViewById(R.id.lastnameEt)).getText().toString().trim();
            String firstname = ((EditText) findViewById(R.id.firstnameEt)).getText().toString().trim();
            String middleInitial = ((EditText) findViewById(R.id.miEt)).getText().toString().trim();
            String licenseNumber = ((EditText) findViewById(R.id.licenseEt)).getText().toString().trim();
            String municipality = ((EditText) findViewById(R.id.municipalityEt)).getText().toString().trim();
            String vehicleType = ((EditText) findViewById(R.id.vehicleTypeEt)).getText().toString().trim();
            String plateNumber = ((EditText) findViewById(R.id.plateNumberEt)).getText().toString().trim();
            String registrationNumber = ((EditText) findViewById(R.id.regNumberEt)).getText().toString().trim();
            String vehicleNumber = ((EditText) findViewById(R.id.vehicleNumberEt)).getText().toString().trim();
            String barangay = ((AutoCompleteTextView) findViewById(R.id.barangaySpinner)).getText().toString();
            String street = ((AutoCompleteTextView) findViewById(R.id.streetSpinner)).getText().toString();

            String birthday = ((EditText) findViewById(R.id.dateEt)).getText().toString().trim();
            String address = ((EditText) findViewById(R.id.addressEt)).getText().toString().trim(); // Ensure you have this field

            // Input validation
            if (lastname.isEmpty() || firstname.isEmpty() || licenseNumber.isEmpty() || municipality.isEmpty() ||
                    vehicleType.isEmpty() || plateNumber.isEmpty() || registrationNumber.isEmpty() || vehicleNumber.isEmpty() ||
                    barangay.isEmpty() || street.isEmpty() || birthday.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return; // Stop further execution
            }

            // Create an Intent to start the next activity
            Intent intent = new Intent(this, TicketViewing.class);
            intent.putExtra("klastName", lastname);
            intent.putExtra("kfirstName", firstname);
            intent.putExtra("kmiddleName", middleInitial);
            intent.putExtra("kbirthDate", birthday);
            intent.putExtra("klicenseNo", licenseNumber);
            intent.putExtra("kvehicleType", vehicleType);
            intent.putExtra("kplateNumber", plateNumber);
            intent.putExtra("kregistrationNumber", registrationNumber);
            intent.putExtra("kvehicleNumber", vehicleNumber);
            intent.putExtra("kuserAddress", address);
            intent.putExtra("kbarangay_name", barangay);
            intent.putExtra("kstreet_name", street);
            startActivity(intent);
            // Get ticketer_id from SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("TicketerPrefs", MODE_PRIVATE);
            String ticketer_id = sharedPreferences.getString("ticketer_id", "");

            // Get the image as a byte array
            ImageView ticketIv = findViewById(R.id.ticketIv);
            if (ticketIv.getDrawable() == null) {
                Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                return; // Stop execution if no image
            }

            Bitmap bitmap = ((BitmapDrawable) ticketIv.getDrawable()).getBitmap();
            byte[] imageBytes = convertBitmapToByteArray(bitmap); // Convert to byte array

            // Prepare the params for the request
            Map<String, String> params = new HashMap<>();
            params.put("lastName", lastname);
            params.put("firstName", firstname);
            params.put("middleName", middleInitial);
            params.put("birthDate", birthday);
            params.put("licenseNo", licenseNumber);
            params.put("cityMunicipality", municipality);
            params.put("vehicleType", vehicleType);
            params.put("registrationNumber", registrationNumber);
            params.put("plateNumber", plateNumber);
            params.put("vehicleNumber", vehicleNumber);
            params.put("barangay_name", barangay);
            params.put("street_name", street);
            params.put("ticketer_id", ticketer_id); // Add ticketer_id to the params

            // Use a Volley Multipart Request to send the byte array
            VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, SUBMIT_URL,
                    response -> {
                        Log.d("SubmitResponse", "Raw Response: " + response);
                        try {
                            // Create a JSONObject from the response data
                            String responseData = new String(response.data);
                            JSONObject jsonResponse = new JSONObject(responseData);
                            String status = jsonResponse.getString("status");
                            String message = jsonResponse.getString("message");
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            Log.e("JSONError", "JSON parsing error: " + e.getMessage());
                        }
                    },
                    error -> {
                        Log.e("NetworkError", "Error: " + error.toString());
                        Toast.makeText(this, "Network error occurred", Toast.LENGTH_SHORT).show();
                    }) {
                @Override
                protected Map<String, VolleyMultipartRequest.DataPart> getByteData() {
                    Map<String, VolleyMultipartRequest.DataPart> params = new HashMap<>();
                    params.put("imageReport", new VolleyMultipartRequest.DataPart("image.png", imageBytes, "image/png"));
                    return params;
                }

                @Override
                protected Map<String, String> getParams() {
                    return params;
                }
            };

            // Add the request to the queue
            Volley.newRequestQueue(this).add(multipartRequest);
        } catch (Exception e) {
            Log.e("SubmitError", "Error: " + e.getMessage());
            Toast.makeText(this, "An error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show(); // Show error to the user
        }
    }


    private void showDatePicker() {
        // Get current date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                IssuesTickets.this,
                (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
                    // Format the selected date to YYYY-MM-DD with zero-padding
                    String formattedMonth = String.format("%02d", (selectedMonth + 1)); // Zero-pad the month
                    String formattedDay = String.format("%02d", selectedDay); // Zero-pad the day

                    // Set the formatted date to the EditText
                    dateEt.setText(selectedYear + "-" + formattedMonth + "-" + formattedDay);
                },
                year, month, day
        );

        // Restrict future dates
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void initializeViews() {
        ticketIv = findViewById(R.id.ticketIv);
        captureCameraBtn = findViewById(R.id.captureCameraBtn);
        barangaySpinner = findViewById(R.id.barangaySpinner);
        streetSpinner = findViewById(R.id.streetSpinner);
        deleteCameraBtn = findViewById(R.id.deleteCameraBtn);

        drawerLayout = findViewById(R.id.drawerLayout);
        buttonDrawerToggle = findViewById(R.id.buttonDrawerToggle);
        navigationView = findViewById(R.id.navigationView);

        String userKey = getIntent().getStringExtra("userKey");

        //buttonDrawerToggle.setOnClickListener(v -> drawerLayout.open());

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
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int itemId = menuItem.getItemId();

            if (itemId == R.id.home) {
                Intent intent = new Intent(IssuesTickets.this, HomeTicketer.class);
                intent.putExtra("userKey", userKey);
                startActivity(intent);
            } else if (itemId == R.id.profile) {
                Intent intent = new Intent(IssuesTickets.this, Ticketer_Profile.class);
                intent.putExtra("userKey", userKey);
                startActivity(intent);
            } else if (itemId == R.id.issuetickets) {
                Intent intent = new Intent(IssuesTickets.this, IssuesTickets.class);
                intent.putExtra("userKey", userKey);
                startActivity(intent);
            } else if (itemId == R.id.mytickets) {
                Intent intent = new Intent(IssuesTickets.this, TicketerTicketsViewing.class);
                intent.putExtra("userKey", userKey);
                startActivity(intent);
            } else if (itemId == R.id.logout) {
                Intent intent = new Intent(IssuesTickets.this, MainActivity.class);
                startActivity(intent);
            }

            drawerLayout.close();
            return true;
        });
    }

    private void deletePhoto() {
        ticketIv.setImageResource(R.drawable.picture_placeholder); // Set to default drawable
        imageBitmap = null; // Reset the image bitmap
        updateButtonVisibility(false); // Hide delete button, show capture button
    }

    private void updateButtonVisibility(boolean isPhotoCaptured) {
        if (isPhotoCaptured) {
            captureCameraBtn.setVisibility(View.GONE); // Hide capture button
            deleteCameraBtn.setVisibility(View.VISIBLE); // Show delete button
        } else {
            captureCameraBtn.setVisibility(View.VISIBLE); // Show capture button
            deleteCameraBtn.setVisibility(View.GONE); // Hide delete button
        }
    }



    private void fetchBarangays() {
        new FetchBarangaysTask().execute(BARANGAY_URL);
    }

    private class FetchBarangaysTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String urlString = params[0];
            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    String status = jsonResponse.getString("status");

                    if (status.equals("success")) {
                        JSONArray barangaysArray = jsonResponse.getJSONArray("barangays");
                        ArrayList<Barangay> barangayList = new ArrayList<>();

                        for (int i = 0; i < barangaysArray.length(); i++) {
                            JSONObject barangayObject = barangaysArray.getJSONObject(i);
                            String barangayId = barangayObject.getString("barangay_id");
                            String barangayName = barangayObject.getString("barangay_name");
                            barangayList.add(new Barangay(barangayId, barangayName));
                        }

                        // Populate the barangay spinner
                        ArrayAdapter<Barangay> adapter = new ArrayAdapter<>(IssuesTickets.this, android.R.layout.simple_spinner_item, barangayList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        barangaySpinner.setAdapter(adapter);
                    } else {
                        Toast.makeText(IssuesTickets.this, "Failed to fetch barangays", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(IssuesTickets.this, "Error fetching barangays", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchStreets(String barangayId) {
        new FetchStreetsTask().execute(STREET_URL + barangayId);
    }

    private class FetchStreetsTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String urlString = params[0];
            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    String status = jsonResponse.getString("status");

                    if (status.equals("success")) {
                        JSONArray streetsArray = jsonResponse.getJSONArray("streets");
                        ArrayList<String> streetList = new ArrayList<>();

                        for (int i = 0; i < streetsArray.length(); i++) {
                            JSONObject streetObject = streetsArray.getJSONObject(i);
                            String streetName = streetObject.getString("street_name");
                            streetList.add(streetName); // Add street name to list
                        }

                        // Populate the street spinner
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(IssuesTickets.this, android.R.layout.simple_spinner_item, streetList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        streetSpinner.setAdapter(adapter);
                    } else {
                        Toast.makeText(IssuesTickets.this, "Failed to fetch streets", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(IssuesTickets.this, "Error fetching streets", Toast.LENGTH_SHORT).show();
            }
        }

    }
    private void setButtonListeners() {
        captureCameraBtn.setOnClickListener(view -> dispatchTakePictureIntent());
        deleteCameraBtn.setOnClickListener(view -> deletePhoto());
    }

    private void dispatchTakePictureIntent() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            launchCamera();
        }
    }

    private void launchCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (data != null) { // Check if data is not null
                Bundle extras = data.getExtras();
                if (extras != null) { // Check if extras is not null
                    imageBitmap = (Bitmap) extras.get("data");
                    ticketIv.setImageBitmap(imageBitmap); // Show the captured image
                    updateButtonVisibility(true); // Show delete button, hide capture button
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to take pictures", Toast.LENGTH_SHORT).show();
            }
        }
    }



    private String validateInputs(String lastname, String firstname, String middleInitial, String licenseNumber,
                                  String vehicleType, String plateNumber, String registrationNumber,
                                  String vehicleNumber) {
        // Check for empty fields
        if (lastname.isEmpty() || firstname.isEmpty() || licenseNumber.isEmpty() ||
                vehicleType.isEmpty() || plateNumber.isEmpty() || registrationNumber.isEmpty() || vehicleNumber.isEmpty()) {
            return "Please fill all fields";
        }

        // Validate names (no numbers or special characters)
        if (!isValidName(lastname)) {
            return "Last name should not contain numbers or special characters";
        }
        if (!isValidName(firstname)) {
            return "First name should not contain numbers or special characters";
        }
        if (!isValidName(middleInitial)) {
            return "Middle initial should not contain numbers or special characters";
        }

        // Validate license number
        if (!isValidLicenseNumber(licenseNumber)) {
            return "License number must be exactly 10 digits.";
        }

        // Validate plate number
        if (!isValidPlateNumber(plateNumber)) {
            return "Invalid plate number format.";
        }

        return null; // No validation errors
    }

    // Helper method to validate names
    private boolean isValidName(String name) {
        return name.matches("[a-zA-Z\\s]*"); // Only allows letters and spaces
    }

    // Helper method to validate license number
    private boolean isValidLicenseNumber(String licenseNumber) {
        return licenseNumber.matches("\\d{10}"); // Exactly 10 digits
    }

    // Helper method to validate plate number
    private boolean isValidPlateNumber(String plateNumber) {
        return plateNumber.matches("^(?:[A-Z]{3}[0-9]{3}|[A-Z]{2}[0-9]{4}|[0-9]{4}[A-Z]{2})$");
    }
    // Helper method to convert Bitmap to byte array
    private byte[] convertBitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream); // Adjust format and quality as needed
        return stream.toByteArray();
    }
}
