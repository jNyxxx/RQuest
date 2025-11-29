package com.example.ridequest;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AddVehicleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vehicle);

        CarRentalData db = new CarRentalData(this);

        EditText etMake = findViewById(R.id.etMake);
        EditText etModel = findViewById(R.id.etModel);
        EditText etType = findViewById(R.id.etType);
        EditText etYear = findViewById(R.id.etYear);
        EditText etPrice = findViewById(R.id.etPrice);
        EditText etPlate = findViewById(R.id.etPlate);
        Spinner spImage = findViewById(R.id.spImage);
        ImageView ivPreview = findViewById(R.id.ivPreview);
        Button btnAdd = findViewById(R.id.btnAddCar);
        Button btnPreview = findViewById(R.id.btnPreview);

        String[] images = {"car_wigo", "car_tesla", "car_explorer"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, images);
        spImage.setAdapter(adapter);

        btnPreview.setOnClickListener(v -> {
            try {
                String selected = spImage.getSelectedItem().toString();
                int resId = getResources().getIdentifier(selected, "drawable", getPackageName());
                if (resId != 0) ivPreview.setImageResource(resId);
            } catch (Exception e) {}
        });

        btnAdd.setOnClickListener(v -> {
            try {
                String make = etMake.getText().toString().trim();
                String model = etModel.getText().toString().trim();
                String type = etType.getText().toString().trim();
                String yearStr = etYear.getText().toString().trim();
                String priceStr = etPrice.getText().toString().trim();
                String plate = etPlate.getText().toString().trim();
                String img = spImage.getSelectedItem().toString();

                if (make.isEmpty() || model.isEmpty() || type.isEmpty() || yearStr.isEmpty() || priceStr.isEmpty() || plate.isEmpty()) {
                    Toast.makeText(this, "Please fill ALL fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                int year = Integer.parseInt(yearStr);
                double price = Double.parseDouble(priceStr);

                if (db.addNewCarComplete(make, model, type, year, price, plate, img)) {
                    Toast.makeText(this, "Vehicle Added!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(this, "Failed to Add", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}