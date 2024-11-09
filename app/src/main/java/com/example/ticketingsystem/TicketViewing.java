package com.example.ticketingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class TicketViewing extends AppCompatActivity {

    // Declare TextView fields
    private TextView lastnameTv, firstnameTv, miTv, licenseTv, vehicleTypeTv,
            plateNumberTv, regNumberTv, vehicleNumberTv, dateTv, addressTv,violationTv, barangayTv, streetTv;

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
        regNumberTv = findViewById(R.id.regNumberTv);
        vehicleNumberTv = findViewById(R.id.vehicleNumberTv);
        dateTv = findViewById(R.id.dateTv);
        addressTv = findViewById(R.id.addressTv);
        violationTv = findViewById(R.id.violationTv);
        barangayTv = findViewById(R.id.barangayTv);
        streetTv = findViewById(R.id.streetTv);

        Intent intent = getIntent();

        if (intent != null) {
            String lastname = intent.getStringExtra("klastName");
            String firstname = intent.getStringExtra("kfirstName");
            String middleInitial = intent.getStringExtra("kmiddleName");
            String birthday = intent.getStringExtra("kbirthDate");
            String licenseNumber = intent.getStringExtra("klicenseNo");
            String vehicleType = intent.getStringExtra("kvehicleType");
            String plateNumber = intent.getStringExtra("kplateNumber");
            String registrationNumber = intent.getStringExtra("kregistrationNumber");
            String vehicleNumber = intent.getStringExtra("kvehicleNumber");
            String address = intent.getStringExtra("kuserAddress");
            String violation = intent.getStringExtra("kviolation_id");
            String barangay = intent.getStringExtra("kbarangay_name");
            String street = intent.getStringExtra("kstreet_name");

            // Set the retrieved data to the TextView fields
            lastnameTv.setText(lastname != null ? lastname : "N/A");
            firstnameTv.setText(firstname != null ? firstname : "N/A");
            miTv.setText(middleInitial != null ? middleInitial : "N/A");
            licenseTv.setText(licenseNumber != null ? licenseNumber : "N/A");
            vehicleTypeTv.setText(vehicleType != null ? vehicleType : "N/A");
            plateNumberTv.setText(plateNumber != null ? plateNumber : "N/A");
            regNumberTv.setText(registrationNumber != null ? registrationNumber : "N/A");
            vehicleNumberTv.setText(vehicleNumber != null ? vehicleNumber : "N/A");
            dateTv.setText(birthday != null ? birthday : "N/A");
            addressTv.setText(address != null ? address : "N/A");
            violationTv.setText(violation != null ? violation : "N/A");
            barangayTv.setText(barangay != null ? barangay : "N/A");
            streetTv.setText(street != null ? street : "N/A");
        } else {
            Log.e("IntentError", "No Intent data received");
        }
    }
}
