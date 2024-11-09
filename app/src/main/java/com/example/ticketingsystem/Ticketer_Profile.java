package com.example.ticketingsystem;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Ticketer_Profile extends AppCompatActivity {
    private TextView fnametxt, lnametxt, mitxt, emailtxt, username;
    private ImageView userImage;
    private TextView titleTextView; // Declare the TextView

    DrawerLayout drawerLayout;
    ImageButton buttonDrawerToggle;
    NavigationView navigationView;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticketer_profile);
        titleTextView = findViewById(R.id.titletextview); // Initialize the TextView
        // Initialize views
        fnametxt = findViewById(R.id.fnametxt);
        lnametxt = findViewById(R.id.lnametxt);
        mitxt = findViewById(R.id.mitxt);
        emailtxt = findViewById(R.id.Email);
        userImage = findViewById(R.id.user_image);
        username = findViewById(R.id.username);

        drawerLayout = findViewById(R.id.drawerLayout);
        buttonDrawerToggle = findViewById(R.id.buttonDrawerToggle);
        navigationView = findViewById(R.id.navigationView);

        Button UsernameButton = findViewById(R.id.EditUsernameButton);
        Button editPasswordButton = findViewById(R.id.buttonEditPassword);

        String userKey = getIntent().getStringExtra("userKey");
        fetchUserData(userKey);  // Fetch user data from Firebase

        // Edit Username Dialog
        UsernameButton.setOnClickListener(v -> {
            Dialog dialog = new Dialog(Ticketer_Profile.this);
            dialog.setContentView(R.layout.dialog_edit_username);

            EditText newUsername = dialog.findViewById(R.id.editTextNewUsername);
            EditText confirmUsername = dialog.findViewById(R.id.editTextConfirmUsername);
            Button saveButton = dialog.findViewById(R.id.buttonSaveUsername);
            ImageButton closeButton = dialog.findViewById(R.id.buttonClose);

            closeButton.setOnClickListener(v1 -> dialog.dismiss());

            saveButton.setOnClickListener(v12 -> {
                String username = newUsername.getText().toString().trim();
                String confirm = confirmUsername.getText().toString().trim();

                if (username.isEmpty() || confirm.isEmpty()) {
                    Toast.makeText(Ticketer_Profile.this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                } else if (username.length() < 4) {
                    Toast.makeText(Ticketer_Profile.this, "Username must be at least 4 characters", Toast.LENGTH_SHORT).show();
                } else if (!username.equals(confirm)) {
                    Toast.makeText(Ticketer_Profile.this, "Usernames do not match", Toast.LENGTH_SHORT).show();
                } else {
                    // Confirmation dialog
                    new AlertDialog.Builder(Ticketer_Profile.this)
                            .setTitle("Confirm Changes")
                            .setMessage("Are you sure you want to change your username?")
                            .setPositiveButton("Yes", (dialogInterface, i) -> {
                                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("tbl_users").child(userKey);
                                userRef.child("USERNAME").setValue(username)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(Ticketer_Profile.this, "Username updated successfully", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                            Intent intent = getIntent();
                                            finish();
                                            startActivity(intent);
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(Ticketer_Profile.this, "Failed to update username", Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss())
                            .show();
                }
            });

            dialog.show();
        });

        // Edit Password Dialog
        editPasswordButton.setOnClickListener(v -> {
            Dialog dialog = new Dialog(Ticketer_Profile.this);
            dialog.setContentView(R.layout.dialog_edit_password_ticketer);

            EditText newPassword = dialog.findViewById(R.id.editTextNewPassword);
            EditText confirmPassword = dialog.findViewById(R.id.editTextConfirmPassword);
            Button saveButton = dialog.findViewById(R.id.buttonSavePassword);
            ImageButton closeButton = dialog.findViewById(R.id.buttonClose);

            closeButton.setOnClickListener(v1 -> dialog.dismiss());

            saveButton.setOnClickListener(v12 -> {
                String password = newPassword.getText().toString().trim();
                String confirm = confirmPassword.getText().toString().trim();

                if (password.isEmpty() || confirm.isEmpty()) {
                    Toast.makeText(Ticketer_Profile.this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                } else if (password.length() < 8) {
                    Toast.makeText(Ticketer_Profile.this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
                } else if (!password.equals(confirm)) {
                    Toast.makeText(Ticketer_Profile.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                } else {
                    // Confirmation dialog
                    new AlertDialog.Builder(Ticketer_Profile.this)
                            .setTitle("Confirm Changes")
                            .setMessage("Are you sure you want to change your password?")
                            .setPositiveButton("Yes", (dialogInterface, i) -> {
                                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("tbl_users").child(userKey);
                                userRef.child("PASSWORD").setValue(password)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(Ticketer_Profile.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(Ticketer_Profile.this, "Failed to update password", Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss())
                            .show();
                }
            });

            dialog.show();
        });

        // Drawer toggle listener
       // buttonDrawerToggle.setOnClickListener(v -> drawerLayout.open());

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
                Intent intent = new Intent(Ticketer_Profile.this, HomeTicketer.class);
                intent.putExtra("userKey", userKey);
                startActivity(intent);
            } else if (itemId == R.id.profile) {
                Intent intent = new Intent(Ticketer_Profile.this, Ticketer_Profile.class);
                intent.putExtra("userKey", userKey);
                startActivity(intent);
            } else if (itemId == R.id.issuetickets) {
                Intent intent = new Intent(Ticketer_Profile.this, IssuesTickets.class);
                intent.putExtra("userKey", userKey);
                startActivity(intent);
            } else if (itemId == R.id.mytickets) {
                Intent intent = new Intent(Ticketer_Profile.this, TicketerTicketsViewing.class);
                intent.putExtra("userKey", userKey);
                startActivity(intent);
            } else if (itemId == R.id.logout) {
                Intent intent = new Intent(Ticketer_Profile.this, MainActivity.class);
                startActivity(intent);
            }

            drawerLayout.close();
            return true;
        });
    }

    private void fetchUserData(String userKey) {
        DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference("tbl_users").child(userKey);

        userDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String firstName = snapshot.child("FIRSTNAME").getValue(String.class);
                    String lastName = snapshot.child("LASTNAME").getValue(String.class);
                    String middleInitial = snapshot.child("MI").getValue(String.class);
                    String email = snapshot.child("EMAIL").getValue(String.class);
                    String imageUrl = snapshot.child("PROFILE").getValue(String.class);
                    String uname = snapshot.child("USERNAME").getValue(String.class);

                    // Set the text views
                    fnametxt.setText("First Name: " + firstName);
                    lnametxt.setText("Last Name: " + lastName);
                    mitxt.setText("Middle Initial: " + middleInitial);
                    emailtxt.setText("Email: " + email);
                    username.setText(uname);

                    // Decode and display the base64 image if available
                    if (!TextUtils.isEmpty(imageUrl)) {
                        // Use a library like Glide or Picasso to load the image
                        Glide.with(Ticketer_Profile.this)
                                .load(imageUrl)
                                .into(userImage);
                    }
                } else {
                    Toast.makeText(Ticketer_Profile.this, "User not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Ticketer_Profile", "Error fetching user data: " + error.getMessage());
                Toast.makeText(Ticketer_Profile.this, "Error loading user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
