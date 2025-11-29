package com.example.ridequest;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        EditText etFirst = findViewById(R.id.etFirstName);
        EditText etLast = findViewById(R.id.etLastName);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPass = findViewById(R.id.etPassword);

        findViewById(R.id.btnSignup).setOnClickListener(v -> {
            if (new CarRentalData(this).registerCustomer(
                    etFirst.getText().toString(), etLast.getText().toString(),
                    etEmail.getText().toString(), etPass.getText().toString(), "0000")) {
                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}