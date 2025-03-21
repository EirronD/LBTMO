package com.lbtmo.ticketingsystem;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.googlecode.tesseract.android.TessBaseAPI;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import java.io.ByteArrayOutputStream;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IssuesTickets extends AppCompatActivity {
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_TICKET_EDIT = 2;
    private static final int STORAGE_REQUEST = 102;
    private static final int STORAGE_PERMISSION = 201;
    private Bitmap imageBitmap;
    private Uri imageUri;

    private ImageView ticketIv;
    private Button captureCameraBtn, deleteCameraBtn, choosegallery;
    private EditText dateEt,plateNumberEt, ticketIdEt, descriptionEt, nationality, lastnameEt, firstnameEt, miEt, licenseEt, addressEt, namebadgeorg;

    private AutoCompleteTextView barangaySpinner, streetSpinner,genderEt;
    private DrawerLayout drawerLayout;
    private ImageButton buttonDrawerToggle;
    private NavigationView navigationView;
    private RecyclerView recyclerView;
    private Button submitButton;
    private int age;
    private TessBaseAPI tessBaseAPI;

    private TextView titleTextView; // Declare the TextView

    private TicketViolationAdapter adapter;
    private TicketViolation selectedViolation;

    private String selectedBarangayId = "";
    private String selectedStreetId;

    private ProgressBar progressBar;
    private LinearLayout mainLayout;

    private LottieAnimationView noInternetAnimation;
    private TextView noInternetMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_tickets);

        titleTextView = findViewById(R.id.titletextview); // Initialize the TextView

        initializeViews();
        updateButtonVisibility(false); // Initially hide delete button
        ticketIv.setImageResource(R.drawable.picture_placeholder); // Set initial placeholder image
        dateEt = findViewById(R.id.dateEt);

        // Set a click listener for the date EditText
        dateEt.setOnClickListener(v -> showDatePicker());

        String userKey = getIntent().getStringExtra("userKey");
        plateNumberEt = findViewById(R.id.plateNumberEt);

        barangaySpinner = findViewById(R.id.barangaySpinner);

        ticketIdEt = findViewById(R.id.ticketIdEt);

        progressBar = findViewById(R.id.loadingIndicator);
        mainLayout = findViewById(R.id.main);
        choosegallery = findViewById(R.id.ChooseFromGallery);
        namebadgeorg = findViewById(R.id.namebadgeorg);

        // Initialize Lottie animation view
        noInternetAnimation = findViewById(R.id.noInternetAnimation);
        noInternetMessage = findViewById(R.id.noInternetMessage);
        // Call function to check internet and show animation if necessary
        checkInternetAndShowAnimation();

        // Find and initialize EditText fields
        lastnameEt = findViewById(R.id.lastnameEt);
        firstnameEt = findViewById(R.id.firstnameEt);
        miEt = findViewById(R.id.miEt);
        licenseEt = findViewById(R.id.licenseEt);
        AutoCompleteTextView vehicleTypeEt = findViewById(R.id.vehicleTypeEt);
        EditText plateNumberEt = findViewById(R.id.plateNumberEt);
        addressEt = findViewById(R.id.addressEt);
        EditText namebadge = findViewById(R.id.namebadgeorg);
        genderEt = findViewById(R.id.genderEt);
        descriptionEt = findViewById(R.id.descriptionEt);
        nationality = findViewById(R.id.nationalityEt);
        AutoCompleteTextView barangaySpinner = findViewById(R.id.barangaySpinner);
        AutoCompleteTextView streetSpinner = findViewById(R.id.streetSpinner);


        // Retrieve data from TicketViewing if available
        Intent intent = getIntent();
        lastnameEt.setText(intent.getStringExtra("lastname"));
        firstnameEt.setText(intent.getStringExtra("firstname"));
        miEt.setText(intent.getStringExtra("middleInitial"));
        licenseEt.setText(intent.getStringExtra("licenseNumber"));
        vehicleTypeEt.setText(intent.getStringExtra("vehicleType"));
        plateNumberEt.setText(intent.getStringExtra("plateNumber"));
        addressEt.setText(intent.getStringExtra("address"));
        barangaySpinner.setText(intent.getStringExtra("barangay"));
        streetSpinner.setText(intent.getStringExtra("street"));
        String gender = intent.getStringExtra("gender");
        String description = intent.getStringExtra("description");
        String namebadgeorg = intent.getStringExtra("namebadgeorg");
        String nationality = intent.getStringExtra("nationality");



        // Set RadioButton based on confiscatedLicense
        RadioGroup radioGroup = findViewById(R.id.radioGroupConfiscated);
        String confiscatedLicense = intent.getStringExtra("confiscatedLicense");
        if ("Yes".equals(confiscatedLicense)) {
            ((RadioButton) findViewById(R.id.radioYes)).setChecked(true);
        } else if ("No".equals(confiscatedLicense)) {
            ((RadioButton) findViewById(R.id.radioNo)).setChecked(true);
        }

        // Load violations into RecyclerView (use fetchViolation as defined in your code)
        //fetchViolation();
        fetchUserData(userKey);
        setupDummyViolationSpinner();

        setUpGenderAutoComplete();

        setUpVehicleTypeAutoComplete();
        extractTessData();
        tessBaseAPI = new TessBaseAPI();
        String dataPath = getFilesDir() + "/tesseract/";
        tessBaseAPI.init(dataPath, "eng");
        tessBaseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);

        choosegallery.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA}, STORAGE_PERMISSION);
            } else {
                BuksanCamera();
            }
        });

        fetchBarangays();

        captureCameraBtn.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        REQUEST_CAMERA_PERMISSION);
            }
        });

        submitButton = findViewById(R.id.submitBtn);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the data from the EditText fields
                String lastname = ((EditText) findViewById(R.id.lastnameEt)).getText().toString().trim();
                String firstname = ((EditText) findViewById(R.id.firstnameEt)).getText().toString().trim();
                String middleInitial = ((EditText) findViewById(R.id.miEt)).getText().toString().trim();
                String licenseNumber = ((EditText) findViewById(R.id.licenseEt)).getText().toString().trim();
                String vehicleType = ((AutoCompleteTextView) findViewById(R.id.vehicleTypeEt)).getText().toString().trim();
                String plateNumber = ((EditText) findViewById(R.id.plateNumberEt)).getText().toString().trim();
                String barangay = ((AutoCompleteTextView) findViewById(R.id.barangaySpinner)).getText().toString().trim();
                String street = ((AutoCompleteTextView) findViewById(R.id.streetSpinner)).getText().toString().trim();
                String birthday = ((EditText) findViewById(R.id.dateEt)).getText().toString().trim();
                String address = ((EditText) findViewById(R.id.addressEt)).getText().toString().trim();
                String ticketId = ((EditText) findViewById(R.id.ticketIdEt)).getText().toString().trim();
                String gender = ((AutoCompleteTextView) findViewById(R.id.genderEt)).getText().toString().trim();
                String description = ((EditText) findViewById(R.id.descriptionEt)).getText().toString().trim();
                String namebadgeorg = ((EditText) findViewById(R.id.namebadgeorg)).getText().toString().trim();
                String nationality = ((EditText) findViewById(R.id.nationalityEt)).getText().toString().trim();

                RadioGroup radioGroup = findViewById(R.id.radioGroupConfiscated);
                int selectedId = radioGroup.getCheckedRadioButtonId();
                String confiscatedLicense = "";

                if (selectedId == R.id.radioYes) {
                    confiscatedLicense = "Yes";
                } else if (selectedId == R.id.radioNo) {
                    confiscatedLicense = "No";
                }

                String validationMessage = validateInputs(lastname, firstname, middleInitial, licenseNumber,
                        vehicleType, plateNumber, gender, namebadgeorg, nationality);

                if (validationMessage != null) {
                    // Show validation error message
                    Toast.makeText(IssuesTickets.this, validationMessage, Toast.LENGTH_SHORT).show();
                } else if (barangay.isEmpty() || street.isEmpty() || birthday.isEmpty() || address.isEmpty() || confiscatedLicense.isEmpty() || ticketId.isEmpty()) {
                    // Check if any required fields are empty (including those not covered by validateInputs)
                    Toast.makeText(IssuesTickets.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                } else {

                    // Create an Intent to pass the data
                    Intent intent = new Intent(IssuesTickets.this, TicketViewing.class);
                    intent.putExtra("lastname", lastname);
                    intent.putExtra("firstname", firstname);
                    intent.putExtra("middleInitial", middleInitial);
                    intent.putExtra("birthday", birthday);
                    intent.putExtra("licenseNumber", licenseNumber);
                    intent.putExtra("vehicleType", vehicleType);
                    intent.putExtra("plateNumber", plateNumber);
                    intent.putExtra("address", address);
                    intent.putExtra("barangay", barangay);
                    intent.putExtra("barangayId", selectedBarangayId);
                    intent.putExtra("street", street);
                    intent.putExtra("streetId", selectedStreetId);  // Pass the selected street ID
                    intent.putExtra("confiscatedLicense", confiscatedLicense);
                    intent.putExtra("ticketId", ticketId);
                    intent.putExtra("gender", gender);
                    intent.putExtra("description", description);
                    intent.putExtra("namebadgeorg", namebadgeorg);
                    intent.putExtra("nationality", nationality);
                    intent.putExtra("userKey", userKey);
                    intent.putExtra("selectedViolation", selectedViolation);

                    intent.putExtra("age", age);  // Use the global variable 'age'

                    // Start the TicketViewing activity
                    startActivity(intent);
                }
            }
        });
        String ticketNumber = generateTicketNumber();
        ticketIdEt.setText(ticketNumber);

    }

    private void extractTessData() {
        File tessDataDir = new File(getFilesDir(), "tesseract/tessdata");
        if (!tessDataDir.exists()) {
            tessDataDir.mkdirs();
        }

        try {
            InputStream inputStream = getAssets().open("tessdata/eng.traineddata");
            FileOutputStream outputStream = new FileOutputStream(new File(tessDataDir, "eng.traineddata"));

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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


    private void setUpVehicleTypeAutoComplete() {
        AutoCompleteTextView vehicleTypeEt = findViewById(R.id.vehicleTypeEt);

        // List of vehicle types
        String[] vehicleTypes = new String[]{"A Light", "A1 Light", "B Light", "B1 Light", "B2 Light", "C Heavy", "D Heavy", "BE Heavy", "CE Heavy"};

        // Create an ArrayAdapter to populate the AutoCompleteTextView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_item, vehicleTypes);

        // Set the adapter to the AutoCompleteTextView
        vehicleTypeEt.setAdapter(adapter);
    }

    private void setUpGenderAutoComplete() {
        AutoCompleteTextView genderEt = findViewById(R.id.genderEt);

        String[] genders = new String[]{"F", "M"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_item, genders);

        genderEt.setAdapter(adapter);
    }

    private String generateTicketNumber() {
        Random random = new Random();
        int number = 10000000 + random.nextInt(90000000); // Ensure it is 8 digits
        return String.valueOf(number);
    }


    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to use this feature", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                BuksanCamera();
            } else {
                Log.e("Error", "Camera permission denied.");
            }
        }
    }
    private void fetchUserData(String userKey) {
        DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference("tbl_users").child(userKey);

        userDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String apprehend = snapshot.child("APPREHEND").getValue(String.class);
                    namebadgeorg.setText(apprehend);
                } else {
                    Toast.makeText(IssuesTickets.this, "User not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Log.e("Ticketer_Profile", "Error fetching user data: " + error.getMessage());
                Toast.makeText(IssuesTickets.this, "Error loading user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setupDummyViolationSpinner() {
        AutoCompleteTextView selectViolation = findViewById(R.id.SelectViolation);
        LinearLayout violationContainer = findViewById(R.id.violationContainer);

        // Initialize Firebase reference
        DatabaseReference offenseRef = FirebaseDatabase.getInstance().getReference("tbl_offense");

        // List to store fetched violations
        List<TicketViolation> violationList = new ArrayList<>();

        // Fetch violations from Firebase
        offenseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                violationList.clear();
                for (DataSnapshot offenseSnapshot : snapshot.getChildren()) {
                    String code = offenseSnapshot.child("CODE").getValue(String.class);
                    String title = offenseSnapshot.child("OFFENSE").getValue(String.class);
                    String id = offenseSnapshot.getKey();

                    if (code != null && title != null) {
                        violationList.add(new TicketViolation(id, code, title));
                    }
                }

                // Set up the custom adapter
                TicketViolationAdapter adapter = new TicketViolationAdapter(IssuesTickets.this, violationList);
                selectViolation.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("setupViolationSpinner", "Error fetching violations", error.toException());
            }
        });

        // Handle item selection from the dropdown
        selectViolation.setOnItemClickListener((parent, view, position, id) -> {
            selectedViolation = violationList.get(position);

            for (int i = 0; i < violationContainer.getChildCount(); i++) {
                View child = violationContainer.getChildAt(i);
                if (child instanceof LinearLayout) {
                    TextView existingText = (TextView) ((LinearLayout) child).getChildAt(0);
                    if (existingText.getText().toString().equals(selectedViolation.getCode() + " " + selectedViolation.getTitle())) {
                        return; // Violation already exists, exit
                    }
                }
            }

            // Create a new layout for the violation item (TextView + Delete Button)
            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setPadding(8, 8, 8, 8);
            itemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            // Create TextView for the violation
            TextView textView = new TextView(this);
            textView.setText(selectedViolation.getCode() + " " + selectedViolation.getTitle());
            textView.setTextSize(14);
            textView.setPadding(8, 8, 8, 8);
            textView.setTypeface(ResourcesCompat.getFont(this, R.font.poppinsbold));
            textView.setLayoutParams(new LinearLayout.LayoutParams(
                    0,  // Make text take up available space
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1    // Give weight to push delete button to the right
            ));

            // Create Delete Button (ImageView)
            ImageView deleteButton = new ImageView(this);
            deleteButton.setImageResource(R.drawable.baseline_delete_24);
            deleteButton.setContentDescription("Delete Violation");
            deleteButton.setPadding(16, 8, 8, 8);

            // Align delete button to the right
            LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            deleteParams.gravity = Gravity.END;
            deleteButton.setLayoutParams(deleteParams);

            // Handle delete action
            deleteButton.setOnClickListener(v -> violationContainer.removeView(itemLayout));

            // Add views to item layout
            itemLayout.addView(textView);
            itemLayout.addView(deleteButton);

            // Add the item layout to the violation container
            violationContainer.addView(itemLayout);
        });
    }

    private void BuksanCamera() {
        File photoFile = new File(getExternalFilesDir(null), "captured_image.jpg");
        imageUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, STORAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                // Handle image capture
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    if (imageBitmap != null) {
                        ticketIv.setImageBitmap(imageBitmap);
                        runTextRecognition(imageBitmap);
                    } else {
                        Log.e("onActivityResult", "Captured image is null.");
                    }
                } else {
                    Log.e("onActivityResult", "No extras found in camera intent.");
                }
            } else if (requestCode == REQUEST_TICKET_EDIT && resultCode == RESULT_OK) {
                String lastname = data.getStringExtra("lastname");
                String firstname = data.getStringExtra("firstname");
                String middleInitial = data.getStringExtra("middleInitial");
                String licenseNumber = data.getStringExtra("licenseNumber");
                String vehicleType = data.getStringExtra("vehicleType");
                String plateNumber = data.getStringExtra("plateNumber");
                String address = data.getStringExtra("address");
                String barangay = data.getStringExtra("barangay");
                String street = data.getStringExtra("street");
                String violation = data.getStringExtra("violation");
                String confiscatedLicense = data.getStringExtra("confiscatedLicense");
                String ticketId = data.getStringExtra("ticketId");
                String gender = data.getStringExtra("gender");
                String description = data.getStringExtra("description");
                String namebadgeorg = data.getStringExtra("namebadgeorg");
                String nationality = data.getStringExtra("nationality");

                // Set data to respective fields in IssuesTickets
                ((EditText) findViewById(R.id.lastnameEt)).setText(lastname);
                ((EditText) findViewById(R.id.firstnameEt)).setText(firstname);
                ((EditText) findViewById(R.id.miEt)).setText(middleInitial);
                ((EditText) findViewById(R.id.licenseEt)).setText(licenseNumber);
                ((AutoCompleteTextView) findViewById(R.id.vehicleTypeEt)).setText(vehicleType);
                ((EditText) findViewById(R.id.plateNumberEt)).setText(plateNumber);
                ((EditText) findViewById(R.id.addressEt)).setText(address);
                ((AutoCompleteTextView) findViewById(R.id.barangaySpinner)).setText(barangay, false);
                ((AutoCompleteTextView) findViewById(R.id.streetSpinner)).setText(street, false);
                ((EditText) findViewById(R.id.descriptionEt)).setText(description);
                ((AutoCompleteTextView) findViewById(R.id.genderEt)).setText(gender);
                ((EditText) findViewById(R.id.namebadgeorg)).setText(namebadgeorg);
                ((EditText) findViewById(R.id.nationalityEt)).setText(nationality);

                // Set RadioButton state based on confiscatedLicense value
                RadioGroup radioGroup = findViewById(R.id.radioGroupConfiscated);
                if ("Yes".equals(confiscatedLicense)) {
                    radioGroup.check(R.id.radioYes);
                } else if ("No".equals(confiscatedLicense)) {
                    radioGroup.check(R.id.radioNo);
                }

                //fetchViolation();
            } else if (requestCode == STORAGE_REQUEST && resultCode == RESULT_OK) {
                sendImageToServer();
            }
        }catch (Exception e) { // Catch general exceptions if needed
            e.printStackTrace();
            Log.e("onActivityResult", "An error occurred.");
        }
    }

    private void sendImageToServer() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                File imageFile = new File(getExternalFilesDir(null), "captured_image.jpg");
                FileInputStream fis = new FileInputStream(imageFile);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                fis.close();
                byte[] imageBytes = baos.toByteArray();

                String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                JSONObject json = new JSONObject();
                json.put("image", base64Image);

                URL url = new URL("https://web-production-18e56.up.railway.app/extract_text");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8);
                writer.write(json.toString());
                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONObject responseJson = new JSONObject(response.toString());
                    runOnUiThread(() -> {
                        try {
                            String name = responseJson.getString("name");
                            String address = responseJson.getString("address");
                            String id_number = responseJson.getString("id_number");
                            String national = responseJson.getString("nationality");
                            String sex = responseJson.getString("sex");
                            String birthday = responseJson.getString("birthday");
                            Parsename(name);
                            addressEt.setText(address);
                            genderEt.setText(sex);
                            nationality.setText(national);
                            licenseEt.setText(id_number);
                            dateEt.setText(birthday);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    runOnUiThread(() -> Log.e("Error", "Error: " + responseCode));
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Log.e("Error", "Error"));
            }
        });
    }

    private void Parsename(String name) {
        String[] parts = name.split(", ");

        if (parts.length < 2) {
            System.out.println("Invalid format");
            return;
        }

        String lastN = parts[0]; // Last name is before the comma
        String[] firstMiddle = parts[1].split(" ");

        if (firstMiddle.length < 2) {
            System.out.println("Invalid format");
            return;
        }
        String firstN = String.join(" ", Arrays.copyOf(firstMiddle, firstMiddle.length - 1));

        String middleN = firstMiddle[firstMiddle.length - 1];

        lastnameEt.setText(lastN);
        firstnameEt.setText(firstN);
        miEt.setText(middleN);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tessBaseAPI.end();
    }

    private void runTextRecognition(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        recognizer.process(image)
                .addOnSuccessListener(visionText -> processTextBlock(visionText))
                .addOnFailureListener(e -> plateNumberEt.setText("Failed to recognize text"));
    }

    private void processTextBlock(Text visionText) {
        StringBuilder recognizedText = new StringBuilder();
        boolean validPlateFound = false;  // Flag to check if a valid license plate is found

        for (Text.TextBlock block : visionText.getTextBlocks()) {
            String blockText = block.getText();
            if (isValidPlate(blockText)) {
                recognizedText.append(blockText).append("\n");
                validPlateFound = true;  // Set flag to true if a valid plate is found
            }
        }

        // If no valid license plate was detected, show a Toast message
        if (!validPlateFound) {
            Toast.makeText(IssuesTickets.this, "No license plate detected", Toast.LENGTH_SHORT).show();
        }

        // Set the recognized text to the EditText
        plateNumberEt.setText(recognizedText.toString());
    }

    private boolean isValidPlate(String text) {
        return text.matches("^[A-Z]{3}\\s?\\d{3,4}$") ||
                text.matches("^[A-Z]{2}\\s?\\d{5}$") ||
                text.matches("^\\d{3}\\s?[A-Z]{3}$") ||
                text.matches("^\\d{5}\\s?[A-Z]{2}$")||
                text.matches("^\\d{4}\\s?[A-Z]{2}$")||
                text.matches("^[A-Z]{2}\\s?\\d{4}$")||
                text.matches("^[A-Z]\\s?\\d{3}\\s?[A-Z]{2}$");
    }

    /*private void fetchViolation() {
        recyclerView = findViewById(R.id.ViolationRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        violationList = new ArrayList<>();

        // Initialize the adapter here before fetching data
        adapter = new ViolationListAdapter(violationList, this);
        recyclerView.setAdapter(adapter);
        progressBar.setVisibility(View.VISIBLE);
        mainLayout.setVisibility(View.GONE);
        DatabaseReference offenseRef = FirebaseDatabase.getInstance().getReference("tbl_offense");
        offenseRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
                    mainLayout.startAnimation(android.view.animation.AnimationUtils.loadAnimation(IssuesTickets.this, R.anim.fade_in));
                }, 3000); // 3-second delay
                for (DataSnapshot offenseSnapshot : snapshot.getChildren()) {
                    String code = offenseSnapshot.child("CODE").getValue(String.class);
                    String offense = offenseSnapshot.child("OFFENSE").getValue(String.class);
                    String id = offenseSnapshot.getKey();

                    violationList.add(new ViolationListFetch(id, code, offense));
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Log.e("fetchViolation", "Error fetching data", error.toException());
            }
        });
    }*/

    private void showDatePicker() {
        // Get current date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Calculate the date for 16 years ago, which should be the latest selectable date
        calendar.set(year - 16, month, day);  // Set the date to 16 years ago

        // Create a DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                IssuesTickets.this,
                (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
                    // Format the selected date to YYYY-MM-DD with zero-padding
                    String formattedMonth = String.format("%02d", (selectedMonth + 1)); // Zero-pad the month
                    String formattedDay = String.format("%02d", selectedDay); // Zero-pad the day

                    // Set the formatted date to the EditText
                    if (dateEt != null) {
                        dateEt.setText(selectedYear + "-" + formattedMonth + "-" + formattedDay);
                    }

                    // Calculate age based on the selected date and store it in the global variable
                    age = calculateAge(selectedYear, selectedMonth, selectedDay);  // Store the age
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Set the maximum date to December 31, 2008 (or the selected date for 16 years ago)
        calendar.set(2008, Calendar.DECEMBER, 31);
        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());

        // Set the minimum date to a very old date (e.g., 1900)
        calendar.set(1900, Calendar.JANUARY, 1);
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());

        datePickerDialog.show();
    }


    // Method to calculate age
    private int calculateAge(int year, int month, int day) {
        Calendar today = Calendar.getInstance();
        int currentYear = today.get(Calendar.YEAR);
        int currentMonth = today.get(Calendar.MONTH);
        int currentDay = today.get(Calendar.DAY_OF_MONTH);

        int age = currentYear - year;

        // If the selected birth month is after the current month, or if it's the same month but the day hasn't passed yet,
        // subtract 1 from the age
        if (currentMonth < month || (currentMonth == month && currentDay < day)) {
            age--;
        }

        return age;
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
    private void fetchBarangays() {
        DatabaseReference barangayRef = FirebaseDatabase.getInstance().getReference("tbl_barangay");
        barangayRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Barangay> barangayList = new ArrayList<>();

                // Iterate through each child (barangay) in the database
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String id = snapshot.child("ID").getValue(String.class);  // Get the ID value
                    String barangayName = snapshot.child("BARANGAY").getValue(String.class);  // Get the BARANGAY name

                    // Create Barangay object and add to the list
                    Barangay barangay = new Barangay(id, barangayName);
                    barangayList.add(barangay);
                }


                // Set up the adapter for the AutoCompleteTextView after data is fetched
                ArrayAdapter<Barangay> adapter = new ArrayAdapter<>(getApplicationContext(),
                        R.layout.dropdown_item, barangayList);  // Use custom layout here
                barangaySpinner.setAdapter(adapter);
                barangaySpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                        Barangay selectedBarangay = barangayList.get(position); // Access the selected barangay using position
                        if (selectedBarangay != null) {
                            selectedBarangayId = selectedBarangay.getId();
                            String barangayId = selectedBarangay.getId();
                            fetchStreets(barangayId);
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors
                Log.e("Firebase", "Error fetching barangay data", databaseError.toException());
            }
        });
    }
    private void fetchStreets(String barangayId) {
        DatabaseReference streetsRef = FirebaseDatabase.getInstance().getReference("tbl_streets");

        // Query to fetch streets based on the Barangay ID
        Query query = streetsRef.orderByChild("BARANGAY_ID").equalTo(barangayId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> streetList = new ArrayList<>();
                Map<String, String> streetIdMap = new HashMap<>();  // Map to hold street name and its corresponding ID

                // Iterate through the child nodes of the dataSnapshot
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String streetName = snapshot.child("STREET").getValue(String.class);
                    String streetId = snapshot.child("SID").getValue(String.class);

                    if (streetName != null && streetId != null) {
                        streetList.add(streetName);  // Add street name to the list
                        streetIdMap.put(streetName, streetId);  // Store street name and its ID in the map
                    }
                }

                // Optionally, populate the AutoCompleteTextView with street names
                if (!streetList.isEmpty()) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                            R.layout.dropdown_item, streetList);  // Use your custom layout
                    streetSpinner.setAdapter(adapter); // Set the adapter for streetSpinner

                    // Set the OnItemClickListener after populating the streetSpinner
                    streetSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                            // Get the selected street name
                            String selectedStreet = streetList.get(position);
                            // Get the corresponding street ID from the map
                            selectedStreetId = streetIdMap.get(selectedStreet);

                            // Now you can use the selectedStreetId as needed
                        }
                    });
                } else {
                    // Handle the case where no streets are found for the given barangay
                    Log.d("Fetch Streets", "No streets found for Barangay ID: " + barangayId);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors
                Log.e("Firebase", "Error fetching street data", databaseError.toException());
            }
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

    private String validateInputs(String lastname, String firstname, String middleInitial, String licenseNumber,
                                  String vehicleType, String plateNumber, String gender, String namebadgeorg, String nationality) {
        // Check for null or empty fields
        if (isNullOrEmpty(lastname) || isNullOrEmpty(firstname) || isNullOrEmpty(licenseNumber) ||
                isNullOrEmpty(vehicleType) || isNullOrEmpty(plateNumber) || isNullOrEmpty(gender) ||
                isNullOrEmpty(namebadgeorg) || isNullOrEmpty(nationality)) {
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
            return "Enter a valid license number.";
        }

        // Validate plate number
        if (!isValidPlateNumber(plateNumber)) {
            return "Invalid plate number format.";
        }

        // Validate sex
        if (!isValidSex(gender)) {
            return "Sex must be 'M' or 'F'.";
        }

        // Validate name badge organization
        if (!isValidNameBadgeOrg(namebadgeorg)) {
            return "Name badge organization should not contain special characters";
        }

        // Validate nationality
        if (!isValidNationality(nationality)) {
            return "Nationality must be 3 to 4 letters only.";
        }

        return null; // No validation errors
    }

    // Helper method to check if a string is null or empty
    private boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    // Helper method to validate names (no numbers or special characters)
    private boolean isValidName(String name) {
        return name != null && name.matches("[a-zA-Z\\s]*"); // Returns true if the name is valid (no numbers or special characters)
    }

    // Helper method to validate name badge organization
    private boolean isValidNameBadgeOrg(String namebadgeorg) {
        return namebadgeorg != null && namebadgeorg.matches("[a-zA-Z0-9\\s]*");
    }

    // Helper method to validate license number
    private boolean isValidLicenseNumber(String licenseNumber) {
        return licenseNumber != null && licenseNumber.matches("^[A-Z]{1}\\d{2}-\\d{2}-[A-Z0-9]{1}\\d{5}$");
    }

    // Helper method to validate plate number
    private boolean isValidPlateNumber(String plateNumber) {
        return plateNumber != null && (plateNumber.matches("^[A-Z]{3}\\s?\\d{3,4}$") ||
                plateNumber.matches("^[A-Z]{2}\\s?\\d{5}$") ||
                plateNumber.matches("^\\d{3}\\s?[A-Z]{3}$") ||
                plateNumber.matches("^\\d{5}\\s?[A-Z]{2}$") ||
                plateNumber.matches("^\\d{4}\\s?[A-Z]{2}$") ||
                plateNumber.matches("^[A-Z]{2}\\s?\\d{4}$") ||
                plateNumber.matches("^[A-Z]\\s?\\d{3}\\s?[A-Z]{2}$"));
    }

    // Helper method to validate sex
    private boolean isValidSex(String sex) {
        return sex != null && sex.length() == 1 && (sex.equalsIgnoreCase("M") || sex.equalsIgnoreCase("F"));
    }

    // Helper method to validate nationality
    private boolean isValidNationality(String nationality) {
        return nationality != null && nationality.matches("^[a-zA-Z]{3,4}$"); // Allows only 3 to 4 letters
    }

}
