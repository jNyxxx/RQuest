package com.example.ridequest;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
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

        tvSwitch.setOnClickListener(v -> {
            isAdmin = !isAdmin;
            tvTitle.setText(isAdmin ? "Admin Login" : "Sign In");
            tvSwitch.setText(isAdmin ? "Customer? Login Here" : "Admin? Login Here");
            etEmail.setHint(isAdmin ? "admin" : "email");
        });

        findViewById(R.id.btnLogin).setOnClickListener(v -> {
            String e = etEmail.getText().toString();
            String p = etPass.getText().toString();
            if (isAdmin) {
                if (db.checkAdmin(e, p)) {
                    startActivity(new Intent(this, AdminDashboardActivity.class));
                    finish();
                } else Toast.makeText(this, "Bad Admin Creds", Toast.LENGTH_SHORT).show();
            } else {
                int uid = db.loginCustomer(e, p);
                if (uid != -1) {
                    getSharedPreferences("UserSession", MODE_PRIVATE).edit().putInt("UID", uid).apply();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                } else Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.tvSignUp).setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
    }
}