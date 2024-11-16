package com.lbtmo.ticketingsystem;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class UserProfile extends AppCompatActivity {

    private TextView fnametxt, lnametxt, mitxt, sextxt, contacttxt, emailtxt, civilstatus, DateOfBirthday, DriverLicenseNo, LicenseType, Nationality, PermanentAddress, PresentAddress;
    private ImageView userImage;
    private Button actionButton;
    private DrawerLayout drawerLayout;
    private ImageButton buttonDrawerToggle;
    private NavigationView navigationView;
    private TextView titleTextView;

    private ProgressBar progressBar;
    private LinearLayout mainLayout;

    private LottieAnimationView noInternetAnimation;
    private TextView noInternetMessage;


    private String userKey;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference reference;
    private StorageReference storageReference;

    private static final int REQUEST_GALLERY = 1;
    private static final int REQUEST_CAMERA = 2;

    private String generatedOTP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        titleTextView = findViewById(R.id.titletextview); // Initialize the TextView

        // Initialize Firebase components
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize UI components
        userImage = findViewById(R.id.user_image);
        fnametxt = findViewById(R.id.fnametxt);
        lnametxt = findViewById(R.id.lnametxt);
        mitxt = findViewById(R.id.mitxt);
        DateOfBirthday = findViewById(R.id.DateOfBirthday);
        DriverLicenseNo = findViewById(R.id.DriverLicenseNo);
        Nationality = findViewById(R.id.Nationality);
        PresentAddress = findViewById(R.id.PresentAddress);
        sextxt = findViewById(R.id.sextxt);
        emailtxt = findViewById(R.id.emailtxt);

        drawerLayout = findViewById(R.id.drawerLayout);
        buttonDrawerToggle = findViewById(R.id.buttonDrawerToggle);
        navigationView = findViewById(R.id.navigationView);

        Button editPasswordButton = findViewById(R.id.buttonEditPassword);
        Button changeProfileButton = findViewById(R.id.buttonEditProfile);
        Button buttonAddEmail = findViewById(R.id.buttonAddEmail);

        progressBar = findViewById(R.id.loadingIndicator);
        mainLayout = findViewById(R.id.main);

        // Initialize Lottie animation view
        noInternetAnimation = findViewById(R.id.noInternetAnimation);
        noInternetMessage = findViewById(R.id.noInternetMessage);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("tbl_drivers");


        userKey = getIntent().getStringExtra("userKey");

        changeProfileButton.setOnClickListener(view -> openEditProfilePictureDialog());
        // Call function to check internet and show animation if necessary
        checkInternetAndShowAnimation();


        if (userKey != null) {
            retrieveDriverInfo(userKey);
        } else {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
        }

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
        buttonAddEmail.setOnClickListener(v -> {
            // Initialize the dialog
            Dialog dialog = new Dialog(UserProfile.this);
            dialog.setContentView(R.layout.add_email_userprofile);

            // Find dialog views
            ImageButton closeButton = dialog.findViewById(R.id.buttonClose);
            EditText editTextAddGmail = dialog.findViewById(R.id.editTextAddGmail);
            EditText editTextConfirmOTP = dialog.findViewById(R.id.editTextConfirmOTP);
            Button buttonOTP = dialog.findViewById(R.id.buttonOTP);
            Button buttonAddEmailDialog = dialog.findViewById(R.id.buttonAddEmail);

            // Close button to dismiss the dialog
            closeButton.setOnClickListener(v1 -> dialog.dismiss());

            // Set up "Send OTP" button click listener
            buttonOTP.setOnClickListener(v2 -> {
                String email = editTextAddGmail.getText().toString().trim();

                // Check if email field is empty
                if (email.isEmpty()) {
                    editTextAddGmail.setError("Please enter your email");
                } else {
                    // Code to send OTP to the entered email
                    sendOTP(email); // This function should handle OTP sending logic
                }
            });

            // Set up "Add Email" button click listener
            buttonAddEmailDialog.setOnClickListener(v3 -> {
                String email = editTextAddGmail.getText().toString().trim();
                String otp = editTextConfirmOTP.getText().toString().trim();

                if (email.isEmpty()) {
                    editTextAddGmail.setError("Please enter your email");
                } else if (otp.isEmpty()) {
                    editTextConfirmOTP.setError("Please enter the OTP");
                } else {
                    // Code to verify OTP and add the email
                    verifyOTPAndAddEmail(email, otp); // Define this function as per your app logic
                    dialog.dismiss(); // Close the dialog after adding the email
                }
            });

            // Show the dialog
            dialog.show();
        });


        editPasswordButton.setOnClickListener(v -> {
            Dialog dialog = new Dialog(UserProfile.this);
            dialog.setContentView(R.layout.dialog_edit_password_user);

            EditText newPassword = dialog.findViewById(R.id.editTextNewPassword);
            EditText confirmPassword = dialog.findViewById(R.id.editTextConfirmPassword);
            Button saveButton = dialog.findViewById(R.id.buttonSavePassword);
            ImageButton closeButton = dialog.findViewById(R.id.buttonClose);

            closeButton.setOnClickListener(v1 -> dialog.dismiss());

            saveButton.setOnClickListener(v12 -> {
                String password = newPassword.getText().toString().trim();
                String confirm = confirmPassword.getText().toString().trim();

                if (password.isEmpty() || confirm.isEmpty()) {
                    Toast.makeText(UserProfile.this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                } else if (password.length() < 8) {
                    Toast.makeText(UserProfile.this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
                } else if (!password.equals(confirm)) {
                    Toast.makeText(UserProfile.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                } else {
                    // Confirmation dialog
                    new AlertDialog.Builder(UserProfile.this)
                            .setTitle("Confirm Changes")
                            .setMessage("Are you sure you want to change your password?")
                            .setPositiveButton("Yes", (dialogInterface, i) -> {
                                // Reference to the user in Firebase
                                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("tbl_drivers").child(userKey);

                                // Update the password
                                userRef.child("PASSWORD").setValue(password)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(UserProfile.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(UserProfile.this, "Failed to update password", Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss())
                            .show();
                }
            });

            dialog.show();
        });
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();

                if (itemId == R.id.home) {
                    Intent intent = new Intent(UserProfile.this, HomeUsers.class);
                    intent.putExtra("userKey", userKey);
                    startActivity(intent);
                }
                if (itemId == R.id.profile) {
                    Intent intent = new Intent(UserProfile.this, UserProfile.class);
                    intent.putExtra("userKey", userKey);
                    startActivity(intent);
                }

                if (itemId == R.id.seeviolations) {
                    Intent intent = new Intent(UserProfile.this, UserViolation.class);
                    intent.putExtra("userKey", userKey);
                    startActivity(intent);
                }

                if (itemId == R.id.announcement) {
                    Intent intent = new Intent(UserProfile.this, Announcement.class);
                    intent.putExtra("userKey", userKey);
                    startActivity(intent);

                }

                if (itemId == R.id.logout) {
                    // Handle logout functionality
                    firebaseAuth.signOut();
                    Toast.makeText(UserProfile.this, "Logged out successfully.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(UserProfile.this, MainActivity.class));
                    finish(); // Close the HomeUser activity
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

    private void verifyOTPAndAddEmail(String email, String otp) {
        if (otp.equals(generatedOTP)) {
            // OTP is correct, proceed with adding the email
            addEmailToDatabase(email); // Define this method based on your app's logic
            Toast.makeText(UserProfile.this, "Email added successfully!", Toast.LENGTH_SHORT).show();
        } else {
            // OTP is incorrect
            Toast.makeText(UserProfile.this, "Invalid OTP, please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void addEmailToDatabase(String email) {
        // Add the email to the database its your time to shine Dagle
    }

    private void sendOTP(String email) {
        // Generate a random 6-digit OTP
        generatedOTP = String.format("%06d", new Random().nextInt(999999));

        // Email credentials
        final String senderEmail = "lbtmoticketsystem@gmail.com"; // Sender's email
        final String senderPassword = "eabc ecsu gmss aywj";     // Sender's email password

        // Set up properties for JavaMail
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Start a new thread for sending the email (network operations can't be done on the main thread)
        new Thread(() -> {
            try {
                // Create session and authenticate
                Session session = Session.getInstance(props,
                        new javax.mail.Authenticator() {
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(senderEmail, senderPassword);
                            }
                        });

                // Create the email message
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(senderEmail));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
                message.setSubject("Your OTP Code");
                message.setText("Your OTP code is: " + generatedOTP);

                // Send the email
                Transport.send(message);
                runOnUiThread(() -> Toast.makeText(UserProfile.this, "OTP sent to your email", Toast.LENGTH_SHORT).show());

            } catch (MessagingException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(UserProfile.this, "Failed to send OTP", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void openEditProfilePictureDialog() {
        Dialog dialog = new Dialog(UserProfile.this);
        dialog.setContentView(R.layout.dialog_edit_profile_picture);

        ImageView imageViewProfile = dialog.findViewById(R.id.imageViewProfile);
        Button buttonChooseFromGallery = dialog.findViewById(R.id.buttonChooseFromGallery);
        Button buttonTakePhoto = dialog.findViewById(R.id.buttonTakePhoto);
        ImageButton buttonCloseDialog = dialog.findViewById(R.id.buttonCloseDialog);

        // Set initial image in the dialog
        Glide.with(this).load(R.drawable.picture_placeholder).into(imageViewProfile);

        // Close button listener
        buttonCloseDialog.setOnClickListener(v -> dialog.dismiss());

        buttonChooseFromGallery.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, REQUEST_GALLERY);
            dialog.dismiss();
        });

        buttonTakePhoto.setOnClickListener(v -> {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, REQUEST_CAMERA);
            dialog.dismiss();
        });

        dialog.show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Uri imageUri = null;

            if (requestCode == REQUEST_GALLERY && data != null) {
                imageUri = data.getData();
                if (isValidImageFormat(imageUri)) {
                    uploadImageToFirebase(imageUri);
                } else {
                    Toast.makeText(this, "Invalid file format. Only JPG, JPEG, PNG, & GIF are allowed.", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == REQUEST_CAMERA && data != null && data.getExtras() != null) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                imageUri = getImageUri(this, bitmap);
                if (isValidImageFormat(imageUri)) {
                    uploadImageToFirebase(imageUri);
                } else {
                    Toast.makeText(this, "Invalid file format. Only JPG, JPEG, PNG, & GIF are allowed.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private boolean isValidImageFormat(Uri imageUri) {
        String[] allowedTypes = {"jpg", "jpeg", "png", "gif"};
        String extension = getContentResolver().getType(imageUri);
        for (String type : allowedTypes) {
            if (extension != null && extension.contains(type)) return true;
        }
        return false;
    }


    private void uploadImageToFirebase(Uri imageUri) {
        String DID = userKey; // replace with your actual driver ID
        String imageName = "drivers/" + DID + "/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(imageName);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    updateDriverPhotoUrl(DID, imageUrl);
                    Toast.makeText(UserProfile.this, "Photo uploaded successfully!", Toast.LENGTH_SHORT).show();
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(UserProfile.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void updateDriverPhotoUrl(String DID, String photoUrl) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("tbl_drivers").child(DID);
        dbRef.child("PHOTO").setValue(photoUrl).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(UserProfile.this, "Profile picture updated successfully!", Toast.LENGTH_SHORT).show();
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            } else {
                Toast.makeText(UserProfile.this, "Failed to update profile picture", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Uri getImageUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    private void retrieveDriverInfo(String userKey) {
        progressBar.setVisibility(View.VISIBLE);
        mainLayout.setVisibility(View.GONE);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("tbl_drivers").child(userKey);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
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
                    mainLayout.startAnimation(android.view.animation.AnimationUtils.loadAnimation(UserProfile.this, R.anim.fade_in));
                }, 3000); // 3-second delay
                if (snapshot.exists()) {
                    String firstName = snapshot.child("FNAME").getValue(String.class);
                    String lastName = snapshot.child("LNAME").getValue(String.class);
                    String middleInitial = snapshot.child("MI").getValue(String.class);
                    String gender = snapshot.child("GENDER").getValue(String.class);
                    String email = snapshot.child("EMAIL").getValue(String.class);
                    String dob = snapshot.child("DOB").getValue(String.class);
                    String driverLicenseNo = snapshot.child("DRIVER_LICENSE").getValue(String.class);
                    String nationality = snapshot.child("NATIONALITY").getValue(String.class);
                    String presentAddress = snapshot.child("PRESENT_ADDRESS").getValue(String.class);
                    String photoUrl = snapshot.child("PHOTO").getValue(String.class);

                    // Populate your TextViews with the retrieved data
                    fnametxt.setText("First Name: " + firstName);
                    lnametxt.setText("Last Name: " + lastName);
                    mitxt.setText("Middle Initial: " + middleInitial);
                    sextxt.setText("Gender: " + gender);
                    emailtxt.setText("Email: " + email);
                    DateOfBirthday.setText("Birthday: " + dob);
                    DriverLicenseNo.setText("Driver License No: " + driverLicenseNo);
                    Nationality.setText("Nationality: " + nationality);
                    PresentAddress.setText("Present Address: " + presentAddress);

                    // Load the profile image if available
                    if (photoUrl != null && !photoUrl.isEmpty()) {
                        // Use a library like Picasso or Glide to load the image
                        Glide.with(UserProfile.this).load(photoUrl).into(userImage);
                    }
                } else {
                    Toast.makeText(UserProfile.this, "No data found for the driver.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", error.getMessage());
                Toast.makeText(UserProfile.this, "Error retrieving data.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
