package com.example.ticketingsystem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private EditText usernameet, passwordet;
    private Button loginbtn;
    private TextView forgotPasswordText; // Added for forgot password
    private FirebaseAuth mAuth;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        usernameet = findViewById(R.id.usernameet);
        passwordet = findViewById(R.id.passwordet);
        loginbtn = findViewById(R.id.loginbtn);
        forgotPasswordText = findViewById(R.id.forgotPasswordText); // Initialize forgot password text view

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference("tbl_users");

        // Set onClickListener on the login button
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        // Set onClickListener for the forgot password text
        forgotPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ForgotPassword.class); // Open ForgotPasswordActivity
                startActivity(intent);
            }
        });
    }

    private void login() {
        String username = usernameet.getText().toString().trim();
        String password = passwordet.getText().toString().trim();

        // Validate username and password
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(MainActivity.this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(MainActivity.this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Query the Firebase Realtime Database to check credentials in tbl_users
        database.orderByChild("USERNAME").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Loop through users that match the username
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String storedPassword = userSnapshot.child("PASSWORD").getValue(String.class);

                        // Check if the provided password matches the stored password
                        if (storedPassword != null && storedPassword.equals(password)) {
                            // Login success, get the role and redirect accordingly
                            String role = userSnapshot.child("ROLE").getValue(String.class);
                            String firstName = userSnapshot.child("FIRSTNAME").getValue(String.class);
                            String lastName = userSnapshot.child("LASTNAME").getValue(String.class);
                            String userId = userSnapshot.getKey();

                            // Save user data in SharedPreferences
                            saveUserDataToPreferences(userId, firstName, lastName, role);

                            // Navigate based on role
                            navigateBasedOnRole(role,userId);
                        } else {
                            // Invalid password
                            Toast.makeText(MainActivity.this, "Invalid password. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    // No match in tbl_users, check tbl_drivers
                    checkDriverTable(username, password);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", error.getMessage());
                Toast.makeText(MainActivity.this, "Error fetching user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkDriverTable(String username, String password) {
        // Query the Firebase Realtime Database to check credentials in tbl_drivers
        DatabaseReference driversDatabase = FirebaseDatabase.getInstance().getReference("tbl_drivers");
        driversDatabase.orderByChild("USERNAME").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Loop through drivers that match the username
                    for (DataSnapshot driverSnapshot : snapshot.getChildren()) {
                        String storedPassword = driverSnapshot.child("PASSWORD").getValue(String.class);

                        // Check if the provided password matches the stored password
                        if (storedPassword != null && storedPassword.equals(password)) {
                            // Login success, get the role and redirect accordingly
                            String role = driverSnapshot.child("ROLE").getValue(String.class);
                            String firstName = driverSnapshot.child("FIRSTNAME").getValue(String.class);
                            String lastName = driverSnapshot.child("LASTNAME").getValue(String.class);
                            String driverId = driverSnapshot.getKey();

                            // Save driver data in SharedPreferences
                            saveUserDataToPreferences(driverId, firstName, lastName, role);

                            // Navigate based on role
                            navigateBasedOnRole(role,driverId);
                        } else {
                            // Invalid password
                            Toast.makeText(MainActivity.this, "Invalid password. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    // Username not found in tbl_drivers either
                    Toast.makeText(MainActivity.this, "Username not found. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", error.getMessage());
                Toast.makeText(MainActivity.this, "Error fetching driver data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to navigate based on role
    private void navigateBasedOnRole(String role,String userKey) {
        if ("STAFF".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role)) {
            Intent intent = new Intent(MainActivity.this, HomeTicketer.class);
            intent.putExtra("userKey", userKey);
            startActivity(intent);
            finish();
        } else if ("DRIVER".equalsIgnoreCase(role)) {
            Intent intent = new Intent(MainActivity.this, HomeUsers.class);
            intent.putExtra("userKey", userKey);
            startActivity(intent);
            finish();
        }
    }


    // Function to save User data in SharedPreferences
    private void saveUserDataToPreferences(String userId, String firstName, String lastName, String role) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("user_id", userId);
        editor.putString("firstName", firstName);
        editor.putString("lastName", lastName);
        editor.putString("role", role);

        editor.apply();
    }
}
