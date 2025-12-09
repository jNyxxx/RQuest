package com.example.ridequest;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class EditProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        CarRentalData db = new CarRentalData(this);
        int uid = getSharedPreferences("UserSession", MODE_PRIVATE).getInt("UID", 1);

        EditText etFirst = findViewById(R.id.etFirst);
        EditText etLast = findViewById(R.id.etLast);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPhone = findViewById(R.id.etPhone);
        TextView tvName = findViewById(R.id.tvEditNameDisplay);

        CarRentalData.Customer c = db.getCustomer(uid);
        if(c != null) {
            etFirst.setText(c.firstName);
            etLast.setText(c.lastName);
            etEmail.setText(c.email);
            etPhone.setText(c.phone);
            tvName.setText(c.firstName + " " + c.lastName);
        }

        // Save
        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> {
            String f = etFirst.getText().toString();
            String l = etLast.getText().toString();
            String e = etEmail.getText().toString();
            String p = etPhone.getText().toString();

            if (db.updateCustomer(uid, f, l, e, p)) {
                Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show();
            }
        });

        // Back Button
        ImageView btnBack = findViewById(R.id.btnBackEdit);
        btnBack.setOnClickListener(v -> finish());
    }
}