package com.example.ridequest;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class EditProfileActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        CarRentalData db = new CarRentalData(this);
        int uid = getSharedPreferences("UserSession", MODE_PRIVATE).getInt("UID", 1);

        EditText f = findViewById(R.id.etFirst), l = findViewById(R.id.etLast), e = findViewById(R.id.etEmail), p = findViewById(R.id.etPhone);
        CarRentalData.Customer c = db.getCustomer(uid);
        if(c!=null) { f.setText(c.firstName); l.setText(c.lastName); e.setText(c.email); p.setText(c.phone); }

        findViewById(R.id.btnSave).setOnClickListener(v -> {
            if(db.updateCustomer(uid, f.getText().toString(), l.getText().toString(), e.getText().toString(), p.getText().toString())) {
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show(); finish();
            }
        });
    }
}