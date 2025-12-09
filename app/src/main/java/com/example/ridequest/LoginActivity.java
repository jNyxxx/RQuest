package com.example.ridequest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPass = findViewById(R.id.etPassword);
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvSwitch = findViewById(R.id.tvAdminLogin);

        // Toggle Customer and Admin/Staff login
        tvSwitch.setOnClickListener(v -> {
            isAdmin = !isAdmin;
            tvTitle.setText(isAdmin ? "Staff Login" : "Sign In");
            tvSwitch.setText(isAdmin ? "Customer? Login Here" : "Are you an Admin/Agent? Login Here");
            etEmail.setHint(isAdmin ? "admin or agent email" : "example@gmail.com");
        });

        findViewById(R.id.btnLogin).setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPass.getText().toString();

            if (isAdmin) {

                // Check if it is the Manager
                if (db.checkAdmin(email, password)) {
                    Log.d(TAG, "✓ Admin login successful");
                    Toast.makeText(this, "Welcome Manager!", Toast.LENGTH_SHORT).show();
                    launchStaffDashboard("Manager", 1);
                    return;
                }

                // Check if it is Employee inspector/Mmechanic
                CarRentalData.Employee emp = db.loginEmployee(email, password);
                if (emp != null) {
                    Log.d(TAG, "✓ Employee login successful: " + emp.role);
                    Toast.makeText(this, "Welcome " + emp.role + "!", Toast.LENGTH_SHORT).show();

                    launchStaffDashboard(emp.role, emp.id);
                    return;
                }

                Log.e(TAG, "✗ Staff login failed");
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();

            } else {
                // customer login
                int uid = db.loginCustomer(email, password);
                if (uid != -1) {
                    getSharedPreferences("UserSession", MODE_PRIVATE).edit()
                            .putInt("UID", uid)
                            .apply();

                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.tvSignUp).setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
    }

    private void launchStaffDashboard(String role, int employeeId) {
        SharedPreferences prefs = getSharedPreferences("EmployeeSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt("EMPLOYEE_ID", employeeId);
        editor.putString("ROLE", role);
        editor.apply();

        Log.d(TAG, "Session Saved -> ID: " + employeeId + " | Role: " + role);

        Intent intent;

        // Check Role
        if (role.equalsIgnoreCase("Mechanic Agent")) {
            intent = new Intent(this, MaintenanceDashboardActivity.class);
        } else if (role.equalsIgnoreCase("Inspection Agent")) {
            intent = new Intent(this, InspectionDashboardActivity.class);
        } else {
            // Manager/Admin
            intent = new Intent(this, AdminDashboardActivity.class);
        }

        intent.putExtra("USER_ROLE", role);
        startActivity(intent);
        finish();
    }
}