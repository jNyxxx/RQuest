package com.example.ridequest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;

public class InspectionDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection_details);

        // Retrieve Data
        int id = getIntent().getIntExtra("ID", -1);
        String type = getIntent().getStringExtra("TYPE");
        String date = getIntent().getStringExtra("DATE");
        String car = getIntent().getStringExtra("CAR");
        String customer = getIntent().getStringExtra("CUSTOMER");
        String inspector = getIntent().getStringExtra("INSPECTOR");
        String fuel = getIntent().getStringExtra("FUEL");
        String damage = getIntent().getStringExtra("DAMAGE");
        String notes = getIntent().getStringExtra("NOTES");
        String photoData = getIntent().getStringExtra("PHOTO");

        // Bind Text Views
        ((TextView) findViewById(R.id.tvHeaderTitle)).setText("Inspection #" + id);
        ((TextView) findViewById(R.id.tvCarName)).setText(car);
        ((TextView) findViewById(R.id.tvDate)).setText(date);

        ((TextView) findViewById(R.id.tvCustomer)).setText(customer);
        ((TextView) findViewById(R.id.tvInspector)).setText(inspector);

        ((TextView) findViewById(R.id.tvInspectionType)).setText(type);
        ((TextView) findViewById(R.id.tvFuelLevel)).setText(fuel);
        ((TextView) findViewById(R.id.tvConditionNotes)).setText(notes);
        ((TextView) findViewById(R.id.tvDamageReport)).setText(damage);

        // --- PHOTO LOGIC ---
        ImageView ivPhoto = findViewById(R.id.ivDamagePhoto);
        TextView tvLabel = findViewById(R.id.tvPhotoLabel);

        if (photoData != null && !photoData.isEmpty()) {
            try {
                // Try 1: Check if it's a File URI/Path
                File imgFile = new File(photoData);
                if (imgFile.exists()) {
                    ivPhoto.setImageURI(Uri.fromFile(imgFile));
                    ivPhoto.setVisibility(View.VISIBLE);
                    tvLabel.setVisibility(View.VISIBLE);
                }
                // Try 2: Check if it's a Base64 String
                else {
                    byte[] decodedString = Base64.decode(photoData, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    ivPhoto.setImageBitmap(decodedByte);
                    ivPhoto.setVisibility(View.VISIBLE);
                    tvLabel.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                ivPhoto.setVisibility(View.GONE);
                tvLabel.setVisibility(View.GONE);
            }
        } else {
            ivPhoto.setVisibility(View.GONE);
            tvLabel.setVisibility(View.GONE);
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}