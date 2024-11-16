package com.lbtmo.ticketingsystem;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TicketViewing extends AppCompatActivity {

    // Declare TextView fields
    private TextView lastnameTv, firstnameTv, miTv, licenseTv, vehicleTypeTv,
            plateNumberTv, regNumberTv, vehicleNumberTv, dateTv, addressTv, violationTv, barangayTv, streetTv, confiscatedLicenseTv, ticketIdTv;

    private static final int EDIT_REQUEST_CODE = 2; // Request code for editing
    private String namebadgeorg = "";
    private String barangayId;
    private String description;
    private String streetId;
    private String age;
    private String gender;
    private String ticketId;
    private String nationality;
    private String fullname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_viewing);

        // Initialize TextView fields
        lastnameTv = findViewById(R.id.lastnameTv);
        firstnameTv = findViewById(R.id.firstnameTv);
        miTv = findViewById(R.id.miTv);
        licenseTv = findViewById(R.id.licenseTv);
        vehicleTypeTv = findViewById(R.id.vehicleTypeTv);
        plateNumberTv = findViewById(R.id.plateNumberTv);
        dateTv = findViewById(R.id.dateTv);
        addressTv = findViewById(R.id.addressTv);
        violationTv = findViewById(R.id.violationTv);
        barangayTv = findViewById(R.id.barangayTv);
        streetTv = findViewById(R.id.streetTv);
        confiscatedLicenseTv = findViewById(R.id.checkboxOption1);
        ticketIdTv = findViewById(R.id.ticketIdTv);

        // Retrieve the data passed from IssuesTickets activity
        Intent intent = getIntent();
        String lastname = intent.getStringExtra("lastname");
        String firstname = intent.getStringExtra("firstname");
        String middleInitial = intent.getStringExtra("middleInitial");
        String birthday = intent.getStringExtra("birthday");
        String licenseNumber = intent.getStringExtra("licenseNumber");
        String vehicleType = intent.getStringExtra("vehicleType");
        String plateNumber = intent.getStringExtra("plateNumber");
        String address = intent.getStringExtra("address");
        String barangay = intent.getStringExtra("barangay");
        String street = intent.getStringExtra("street");
        String confiscatedLicense = intent.getStringExtra("confiscatedLicense");
        ticketId = intent.getStringExtra("ticketId");
        age = calculateAge(birthday);  // Calculate age here
        gender = intent.getStringExtra("gender");
        description = intent.getStringExtra("description");
        barangayId = getIntent().getStringExtra("barangayId");
        streetId = intent.getStringExtra("streetId");
        namebadgeorg = intent.getStringExtra("namebadgeorg");
        nationality = intent.getStringExtra("nationality");
        String userKey = getIntent().getStringExtra("userKey");

        fetchUserData(userKey);

        // Retrieve the selected violation codes and titles
        ArrayList<String> selectedViolationCodes = intent.getStringArrayListExtra("selectedViolationCodes");
        ArrayList<String> selectedViolationTitles = intent.getStringArrayListExtra("selectedViolationTitles");
        ArrayList<String> selectedViolationIds = intent.getStringArrayListExtra("selectedViolationIds");

        // Prepare the violation text to display
        StringBuilder violationText = new StringBuilder();
        if (selectedViolationCodes != null && !selectedViolationCodes.isEmpty()) {
            for (String violation : selectedViolationCodes) {
                violationText.append(violation).append("\n");
            }
        } else {
            violationText.append("N/A");
        }
        StringBuilder violationTitles = new StringBuilder();
        if (selectedViolationTitles != null && !selectedViolationTitles.isEmpty()) {
            for (String violation : selectedViolationTitles) {
                violationTitles.append(violation).append("\n");
            }
        } else {
            violationTitles.append("N/A");
        }
        StringBuilder violationId = new StringBuilder();
        if (selectedViolationIds != null && !selectedViolationIds.isEmpty()) {
            for (String violation : selectedViolationIds) {
                violationId.append(violation).append("\n");
            }
        } else {
            violationId.append("N/A");
        }

        // Set the retrieved data to the TextView fields
        lastnameTv.setText(lastname != null ? lastname : "N/A");
        firstnameTv.setText(firstname != null ? firstname : "N/A");
        miTv.setText(middleInitial != null ? middleInitial : "N/A");
        licenseTv.setText(licenseNumber != null ? licenseNumber : "N/A");
        vehicleTypeTv.setText(vehicleType != null ? vehicleType : "N/A");
        plateNumberTv.setText(plateNumber != null ? plateNumber : "N/A");
        dateTv.setText(birthday != null ? birthday : "N/A");
        addressTv.setText(address != null ? address : "N/A");
        barangayTv.setText(barangay != null ? barangay : "N/A");
        streetTv.setText(street != null ? street : "N/A");
        confiscatedLicenseTv.setText(confiscatedLicense != null ? confiscatedLicense : "N/A");
        ticketIdTv.setText("Ticket ID: " + (ticketId != null ? ticketId : "N/A"));


        // Set the violation text to the violationTv TextView
        violationTv.setText(violationText.toString());

        Button goBackButton = findViewById(R.id.goBackButton);
        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passDataBackToIssuesTickets();
            }
        });

        Button printTicketButton = findViewById(R.id.printTicketButton);
        printTicketButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Temporarily hide the button
                printTicketButton.setVisibility(View.INVISIBLE);
                goBackButton.setVisibility(View.INVISIBLE);
                // Capture screenshot
                View rootView = getWindow().getDecorView().getRootView();
                rootView.setDrawingCacheEnabled(true);
                Bitmap bitmap = Bitmap.createBitmap(rootView.getDrawingCache());
                rootView.setDrawingCacheEnabled(false);

                // Save bitmap to gallery
                saveImageToGallery(bitmap);

                // Make the button visible again
                printTicketButton.setVisibility(View.VISIBLE);
                goBackButton.setVisibility(View.VISIBLE);

                // Action to be taken when "Print Ticket" is clicked
                fetchDriver();

                // Navigate to HomeTicketer after successful ticket processing
                Intent intent = new Intent(TicketViewing.this, HomeTicketer.class);
                startActivity(intent);
                finish(); // Optional: Close the current activity to prevent returning back
            }
        });
    }
    private void saveImageToGallery(Bitmap bitmap) {
        String filename = "ticket_screenshot_" + System.currentTimeMillis() + ".jpg";
        OutputStream fos;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            ContentResolver resolver = getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/TicketScreenshots");
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

            try {
                fos = resolver.openOutputStream(imageUri);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
                Toast.makeText(this, "Saved to Gallery", Toast.LENGTH_SHORT).show();

                // Show success alert
                showAlert("Success", "Image has been successfully saved to your gallery!");
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show();
                showAlert("Error", "Failed to save the image to the gallery.");
            }
        } else {
            String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/TicketScreenshots";
            File file = new File(imagesDir);
            if (!file.exists()) {
                file.mkdirs();
            }
            File image = new File(imagesDir, filename);
            try {
                fos = new FileOutputStream(image);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
                // Notify gallery about new file
                MediaScannerConnection.scanFile(this, new String[]{image.toString()}, null, null);
                Toast.makeText(this, "Saved to Gallery", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showAlert(String title, String message) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(title)         // Use 'title' parameter
                .setMessage(message)     // Use 'message' parameter
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }


    private void fetchDriver() {
        String lname = lastnameTv.getText().toString().toUpperCase();
        String fname = firstnameTv.getText().toString().toUpperCase();
        String mi = miTv.getText().toString().toUpperCase();
        DatabaseReference driversRef = FirebaseDatabase.getInstance().getReference("tbl_drivers");

        driversRef.orderByChild("LNAME").equalTo(lname).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean driverExists = false;
                String driverId = "";

                for (DataSnapshot driverSnapshot : dataSnapshot.getChildren()) {
                    String dbFname = driverSnapshot.child("FNAME").getValue(String.class);
                    String dbMi = driverSnapshot.child("MI").getValue(String.class);

                    if (dbFname != null && dbFname.equals(fname) && dbMi != null && dbMi.equals(mi)) {
                        driverExists = true;
                        driverId = driverSnapshot.getKey();
                        break;
                    }
                }

                if (!driverExists) {
                    // Create a new driver account
                    driverId = driversRef.push().getKey();
                    Map<String, Object> newDriverData = new HashMap<>();
                    newDriverData.put("AGE", age);
                    newDriverData.put("DOB", dateTv.getText().toString());
                    newDriverData.put("DRIVER_LICENSE", licenseTv.getText().toString().toUpperCase());
                    newDriverData.put("FNAME", firstnameTv.getText().toString().toUpperCase());
                    newDriverData.put("GENDER", gender.toUpperCase());
                    newDriverData.put("LNAME", lastnameTv.getText().toString().toUpperCase());
                    newDriverData.put("MI", miTv.getText().toString().toUpperCase());
                    newDriverData.put("NATIONALITY", nationality.toUpperCase());
                    newDriverData.put("PASSWORD", dateTv.getText().toString());
                    newDriverData.put("PHOTO", "");
                    newDriverData.put("PRESENT_ADDRESS", addressTv.getText().toString().toUpperCase());
                    newDriverData.put("ROLE", "DRIVER");
                    newDriverData.put("USERNAME", licenseTv.getText().toString().toUpperCase());

                    driversRef.child(driverId).setValue(newDriverData)
                            .addOnSuccessListener(aVoid -> Toast.makeText(getApplicationContext(), "New driver account created successfully", Toast.LENGTH_LONG).show())
                            .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Failed to create driver account: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
                saveTicketAndViolations(driverId);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("fetchDriver", "Database error: " + databaseError.getMessage());
            }
        });
    }

    private void fetchUserData(String userKey) {
        DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference("tbl_users").child(userKey);

        userDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String TicketerfName = snapshot.child("FIRSTNAME").getValue(String.class);
                    String TicketerlName = snapshot.child("LASTNAME").getValue(String.class);

                    fullname = String.format("%s, %s", TicketerlName, TicketerfName);
                    Log.d("Fullname", "onDataChange: " + fullname);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void saveTicketAndViolations(String driverId) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("tbl_violation");
        String ticketkey = databaseReference.push().getKey();
        Map<String, Object> ticketData = new HashMap<>();
        ticketData.put("TICKET_NO", ticketId);
        ticketData.put("DRIVER_ID", driverId);
        ticketData.put("PLATE_NUMBER", plateNumberTv.getText().toString().toUpperCase());
        ticketData.put("VEHICLE_TYPE", vehicleTypeTv.getText().toString().toUpperCase());
        ticketData.put("BARANGAY", barangayId.toUpperCase());
        ticketData.put("STREET", streetId);
        ticketData.put("DATE_VIOLATED", now());
        ticketData.put("DESCRIPTION", description.toUpperCase());
        ticketData.put("REPORTING_OFFICER", fullname.toUpperCase());
        ticketData.put("OFFICER_ID", namebadgeorg);
        ticketData.put("STATUS", "PENDING");

        databaseReference.child(ticketkey).setValue(ticketData)
                .addOnSuccessListener(aVoid -> Toast.makeText(getApplicationContext(), "Ticket information saved successfully", Toast.LENGTH_LONG).show())
                .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Failed to save ticket: " + e.getMessage(), Toast.LENGTH_LONG).show());

        DatabaseReference violationListRef = FirebaseDatabase.getInstance().getReference("tbl_violation_list");
        ArrayList<String> selectedViolationIds = getIntent().getStringArrayListExtra("selectedViolationIds");

        if (selectedViolationIds != null) {
            for (String offenseId : selectedViolationIds) {
                Map<String, Object> offenseData = new HashMap<>();
                offenseData.put("TICKET_NO", ticketId);
                offenseData.put("OFFENSE_ID", offenseId);
                offenseData.put("DRIVER_ID", driverId);

                violationListRef.push().setValue(offenseData);
            }
        }
    }


    private String now() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Format: yyyy-MM-dd
        return dateFormat.format(calendar.getTime());
    }

    // Function to calculate the age based on the birthday
    private String calculateAge(String birthday) {
        // Assuming the birthday is in the format "yyyy-MM-dd"
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date birthDate = sdf.parse(birthday);
            Calendar birthCalendar = Calendar.getInstance();
            birthCalendar.setTime(birthDate);

            Calendar currentCalendar = Calendar.getInstance();
            int age = currentCalendar.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR);

            // Adjust the age if the birthday hasn't occurred yet this year
            if (currentCalendar.get(Calendar.MONTH) < birthCalendar.get(Calendar.MONTH) ||
                    (currentCalendar.get(Calendar.MONTH) == birthCalendar.get(Calendar.MONTH) &&
                            currentCalendar.get(Calendar.DAY_OF_MONTH) < birthCalendar.get(Calendar.DAY_OF_MONTH))) {
                age--;
            }
            return String.valueOf(age);
        } catch (ParseException e) {
            e.printStackTrace();
            return "N/A";  // Return "N/A" in case of an error
        }
    }

    // Function to pass data back to IssuesTickets activity
    private void passDataBackToIssuesTickets() {
        Intent intent = new Intent(TicketViewing.this, IssuesTickets.class);

        // Pass all data fields back
        intent.putExtra("lastname", lastnameTv.getText().toString());
        intent.putExtra("firstname", firstnameTv.getText().toString());
        intent.putExtra("middleInitial", miTv.getText().toString());
        intent.putExtra("licenseNumber", licenseTv.getText().toString());
        intent.putExtra("vehicleType", vehicleTypeTv.getText().toString());
        intent.putExtra("plateNumber", plateNumberTv.getText().toString());
        intent.putExtra("address", addressTv.getText().toString());
        intent.putExtra("barangay", barangayTv.getText().toString());
        intent.putExtra("street", streetTv.getText().toString());
        intent.putExtra("violation", violationTv.getText().toString());
        intent.putExtra("confiscatedLicense", confiscatedLicenseTv.getText().toString());
        intent.putExtra("ticketId", ticketIdTv.getText().toString().replace("Ticket ID: ", ""));
        intent.putExtra("gender", getIntent().getStringExtra("gender"));
        intent.putExtra("description", getIntent().getStringExtra("description"));
        intent.putExtra("nationality", getIntent().getStringExtra("nationality"));

        setResult(RESULT_OK, intent);
        finish(); // Close TicketViewing activity and return to IssuesTickets
    }
}