package com.example.ridequest;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class SignupActivity extends AppCompatActivity {

    private EditText etFirst, etLast, etEmail, etPass, etPhone, etDOB, etLicense, etAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etFirst = findViewById(R.id.etFirstName);
        etLast = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etPass = findViewById(R.id.etPassword);
        etPhone = findViewById(R.id.etPhone);
        etDOB = findViewById(R.id.etDateOfBirth);
        etLicense = findViewById(R.id.etDriversLicense);
        etAddress = findViewById(R.id.etAddress);

        // Date of Birth Picker
        etDOB.setFocusable(false);
        etDOB.setOnClickListener(v -> showDatePicker());

        // Show password requirements
        TextView tvPasswordReq = findViewById(R.id.tvPasswordRequirements);
        if (tvPasswordReq != null) {
            tvPasswordReq.setText("Password must be 8+ characters with letters and numbers");
        }

        findViewById(R.id.btnSignup).setOnClickListener(v -> {
            String firstName = etFirst.getText().toString().trim();
            String lastName = etLast.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPass.getText().toString();
            String phone = etPhone.getText().toString().trim();
            String dob = etDOB.getText().toString().trim();
            String license = etLicense.getText().toString().trim();
            String address = etAddress.getText().toString().trim();

            // Validate all fields
            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() ||
                    password.isEmpty() || phone.isEmpty() || dob.isEmpty() ||
                    license.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate age (18+)
            if (!CarRentalData.isAgeValid(dob)) {
                Toast.makeText(this, "You must be at least 18 years old", Toast.LENGTH_LONG).show();
                return;
            }

            // Validate password security
            if (!CarRentalData.isPasswordValid(password)) {
                Toast.makeText(this, "Password must be 8+ characters with letters and numbers", Toast.LENGTH_LONG).show();
                return;
            }

            // Register customer
            CarRentalData db = new CarRentalData(this);
            if (db.registerCustomer(firstName, lastName, email, password, phone, dob, license, address)) {
                Toast.makeText(this, "✓ Registration Successful!\nPlease bring your driver's license for verification at pickup.", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(this, "Registration Failed. Email or phone may already be registered.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR) - 18; // Default to 18 years ago
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String date = year1 + "-" + String.format("%02d", monthOfYear + 1) + "-" + String.format("%02d", dayOfMonth);
                    etDOB.setText(date);
                },
                year, month, day);

        // Set maximum date to 18 years ago
        c.add(Calendar.YEAR, -18);
        dpd.getDatePicker().setMaxDate(c.getTimeInMillis());

        dpd.show();
    }
}