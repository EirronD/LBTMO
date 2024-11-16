package com.lbtmo.ticketingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class ForgotPassword extends AppCompatActivity {



    private EditText emailInput, otpInput, newPasswordInput, confirmPasswordInput;
    private Button verifyButton, submitButton;
    private TextView timerText;
    private CountDownTimer countDownTimer;
    private static final long OTP_TIMEOUT = 300000;
    private TextInputLayout otpInputLayout,newPassInputLayout,confirmPassInputLayout;// 5 minutes

    private FirebaseDatabase database;
    private DatabaseReference reference;
    private FirebaseAuth firebaseAuth;
    private String generatedOtp;
    private Executor executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        otpInputLayout = findViewById(R.id.otpInputLayout);
        confirmPassInputLayout = findViewById(R.id.confirmPassInputLayout);
        newPassInputLayout = findViewById(R.id.newPassInputLayout);
        emailInput = findViewById(R.id.emailInput);
        otpInput = findViewById(R.id.otpInput);
        newPasswordInput = findViewById(R.id.newPasswordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        verifyButton = findViewById(R.id.verifyButton);
        submitButton = findViewById(R.id.submitButton);
        timerText = findViewById(R.id.timerText);

        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        executor = Executors.newSingleThreadExecutor(); // For sending email in the background

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEmailValid()) {
                    sendOtp();
                }
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPasswordReset();
            }
        });


        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the MainActivity
                Intent intent = new Intent(ForgotPassword.this, MainActivity.class);
                startActivity(intent);
                // Optional: Close the current activity
                finish();
            }
        });
    }

    // Email validation
    private boolean isEmailValid() {
        String email = emailInput.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // Generate a random 6-digit OTP
    private String generateOtp() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(999999));
    }

    // Send OTP to user's email using Gmail
    private void sendOtp() {
        String email = emailInput.getText().toString();
        generatedOtp = generateOtp();

        // Save OTP to Firebase
        Query resetemail = database.getReference("tbl_users").orderByChild("EMAIL").equalTo(email);

        resetemail.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    if (userId != null) {
                        database.getReference("tbl_users").child(userId).child("otp").setValue(generatedOtp);
                    }
                }
                String subject = "Your OTP for Password Reset";
                String message = "Your OTP is: " + generatedOtp + ". It will expire in 5 minutes.";

                executor.execute(() -> sendEmail(email, subject, message));

                Toast.makeText(ForgotPassword.this, "OTP sent to email", Toast.LENGTH_SHORT).show();
                confirmPassInputLayout.setVisibility(View.VISIBLE);
                newPassInputLayout.setVisibility(View.VISIBLE);
                otpInputLayout.setVisibility(View.VISIBLE);

                otpInput.setVisibility(View.VISIBLE);
                newPasswordInput.setVisibility(View.VISIBLE);
                confirmPasswordInput.setVisibility(View.VISIBLE);
                submitButton.setVisibility(View.VISIBLE);
                timerText.setVisibility(View.VISIBLE);

                startOtpCountdown();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", error.getMessage());
            }
        });
    }

    private void sendEmail(String recipient, String subject, String message) {
        final String username = "lbtmoticketsystem@gmail.com"; // Your email
        final String password = "eabc ecsu gmss aywj"; // Use an App-Specific Password (highly recommended)

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(username));
            mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            mimeMessage.setSubject(subject);
            mimeMessage.setText(message);

            Transport.send(mimeMessage);

            runOnUiThread(() -> Toast.makeText(ForgotPassword.this, "OTP sent to email", Toast.LENGTH_SHORT).show());
            Log.d("Email", "OTP sent successfully.");
        } catch (Exception e) {
            Log.e("EmailError", "Error sending email: " + e.getMessage());
            runOnUiThread(() -> Toast.makeText(ForgotPassword.this, "Failed to send email. Please try again.", Toast.LENGTH_SHORT).show());
        }
    }


    private void startOtpCountdown() {
        countDownTimer = new CountDownTimer(OTP_TIMEOUT, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                timerText.setText(String.format("%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                timerText.setText("00:00");
                otpInput.setEnabled(false);
                submitButton.setEnabled(false);
                Toast.makeText(ForgotPassword.this, "OTP expired", Toast.LENGTH_SHORT).show();
            }
        };
        countDownTimer.start();
    }

    private void submitPasswordReset() {
        String otp = otpInput.getText().toString();
        String newPassword = newPasswordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();

        if (!isOtpValid()) {
            return;
        }

        String email = emailInput.getText().toString().trim(); // Get the user's email

        // Check in tbl_users first
        Query userQuery = database.getReference("tbl_users").orderByChild("EMAIL").equalTo(email);

        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Email found in tbl_users
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        handleOtpVerification(userSnapshot, otp, newPassword, confirmPassword);
                    }
                } else {
                    // If not found in tbl_users, check in tbl_drivers
                    checkInDriversTable(email, otp, newPassword, confirmPassword);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("FirebaseError", "Failed to read value.", error.toException());
                Toast.makeText(ForgotPassword.this, "Error reading from database. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void checkInDriversTable(String email, String otp, String newPassword, String confirmPassword) {
        Query driverQuery = database.getReference("tbl_drivers").orderByChild("EMAIL").equalTo(email);

        driverQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Email found in tbl_drivers
                    for (DataSnapshot driverSnapshot : snapshot.getChildren()) {
                        handleOtpVerification(driverSnapshot, otp, newPassword, confirmPassword);
                    }
                } else {
                    // If not found in both tables
                    Toast.makeText(ForgotPassword.this, "No user found with this email.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("FirebaseError", "Failed to read value.", error.toException());
                Toast.makeText(ForgotPassword.this, "Error reading from database. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Reusable method to handle OTP verification and password reset
    private void handleOtpVerification(DataSnapshot userSnapshot, String otp, String newPassword, String confirmPassword) {
        String storedOtp = userSnapshot.child("otp").getValue(String.class);
        String userId = userSnapshot.getKey();

        if (storedOtp != null && storedOtp.equals(otp)) {
            if (arePasswordsValid(newPassword, confirmPassword)) {
                updatePassword(newPassword, userId, userSnapshot.getRef().getParent().getKey()); // Pass userId and table reference
            }
        } else {
            Toast.makeText(ForgotPassword.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
        }
    }
    private boolean arePasswordsValid(String newPassword, String confirmPassword) {
        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please enter the new password and confirm it", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    // Update password for the user in the corresponding table
    private void updatePassword(String newPassword, String userId, String tableName) {
        if (userId != null && tableName != null) {
            database.getReference(tableName).child(userId).child("PASSWORD").setValue(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ForgotPassword.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                            finish(); // Close the activity
                        } else {
                            Toast.makeText(ForgotPassword.this, "Failed to update password. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(ForgotPassword.this, "Unable to update password.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isOtpValid() {
        String otp = otpInput.getText().toString().trim();
        if (otp.isEmpty()) {
            Toast.makeText(this, "Please enter OTP", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean arePasswordsValid() {
        String newPassword = newPasswordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please enter the new password and confirm it", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void updatePassword(String newPassword, String userId) {
        // Check if the new password is valid
        if (newPassword.isEmpty()) {
            Toast.makeText(ForgotPassword.this, "Please enter your new password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Optional: Hash the password before saving (consider implementing a hashing function)

        // Update the password in the database
        if (userId != null) {
            database.getReference("tbl_users").child(userId).child("PASSWORD").setValue(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ForgotPassword.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                            // Cancel OTP timer on success
                            if (countDownTimer != null) {
                                countDownTimer.cancel();
                            }
                            finish(); // Close the activity
                        } else {
                            Toast.makeText(ForgotPassword.this, "Failed to update password. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(ForgotPassword.this, "User ID is null. Unable to update password.", Toast.LENGTH_SHORT).show();
        }
    }

}
