package com.example.ridequest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private boolean isAdmin = false;
    private CarRentalData db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new CarRentalData(this);

        // List all customers on app start
        Log.d(TAG, "=== LoginActivity Started ===");
        db.debugListAllCustomers();

        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPass = findViewById(R.id.etPassword);
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvSwitch = findViewById(R.id.tvAdminLogin);

        tvSwitch.setOnClickListener(v -> {
            isAdmin = !isAdmin;
            tvTitle.setText(isAdmin ? "Admin Login" : "Sign In");
            tvSwitch.setText(isAdmin ? "Customer? Login Here" : "Are you an Admin? Login Here");
            etEmail.setHint(isAdmin ? "admin" : "example@gmail.com");
        });

        findViewById(R.id.btnLogin).setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPass.getText().toString();

            // Validate input
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, "Login attempt - Email: " + email + " | Admin mode: " + isAdmin);

            if (isAdmin) {
                // Admin login
                if (db.checkAdmin(email, password)) {
                    Log.d(TAG, "✓ Admin login successful");
                    Toast.makeText(this, "Welcome Admin!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, AdminDashboardActivity.class));
                    finish();
                } else {
                    Log.e(TAG, "✗ Admin login failed");
                    Toast.makeText(this, "Invalid admin credentials", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Customer login
                int uid = db.loginCustomer(email, password);

                if (uid != -1) {
                    Log.d(TAG, "✓ Customer login successful - UID: " + uid);

                    // Save session
                    getSharedPreferences("UserSession", MODE_PRIVATE)
                            .edit()
                            .putInt("UID", uid)
                            .apply();

                    Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                } else {
                    Log.e(TAG, "✗ Customer login failed for: " + email);
                    Toast.makeText(this, "Login Failed\n\nPlease check your email and password", Toast.LENGTH_LONG).show();
                }
            }
        });

        findViewById(R.id.tvSignUp).setOnClickListener(v -> {
            Log.d(TAG, "Navigate to signup");
            startActivity(new Intent(this, SignupActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh customer list when returning from signup
        Log.d(TAG, "=== Returned to LoginActivity ===");
        db.debugListAllCustomers();
    }
}