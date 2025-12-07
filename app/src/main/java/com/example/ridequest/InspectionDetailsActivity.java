package com.example.ridequest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class InspectionDetailsActivity extends AppCompatActivity {

    private TextView tvInspectionId, tvCarName, tvDate, tvCustomer, tvInspector;
    private TextView tvType, tvFuel, tvDamage, tvNotes;
    private ImageView imgEvidence;
    private View photoSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection_details);

        // Initialize Views
        tvInspectionId = findViewById(R.id.tvInspectionId);
        tvCarName = findViewById(R.id.tvCarName);
        tvDate = findViewById(R.id.tvDate);
        tvCustomer = findViewById(R.id.tvCustomer);
        tvInspector = findViewById(R.id.tvInspector);
        tvType = findViewById(R.id.tvType);
        tvFuel = findViewById(R.id.tvFuel);
        tvDamage = findViewById(R.id.tvDamage);
        tvNotes = findViewById(R.id.tvNotes);
        imgEvidence = findViewById(R.id.imgEvidence);
        photoSection = findViewById(R.id.photoSection);

        // Get Data from Intent
        int id = getIntent().getIntExtra("ID", -1);
        String type = getIntent().getStringExtra("TYPE");
        String date = getIntent().getStringExtra("DATE");
        String carName = getIntent().getStringExtra("CAR");
        String customer = getIntent().getStringExtra("CUSTOMER");
        String inspector = getIntent().getStringExtra("INSPECTOR");
        String fuel = getIntent().getStringExtra("FUEL");
        String damage = getIntent().getStringExtra("DAMAGE");
        String notes = getIntent().getStringExtra("NOTES");
        String photo = getIntent().getStringExtra("PHOTO");

        // Display Data
        tvInspectionId.setText("Inspection #" + id);
        tvCarName.setText(carName != null ? carName : "N/A");
        tvDate.setText(date != null ? date : "N/A");
        tvCustomer.setText(customer != null ? customer : "N/A");

        // FIXED: Show inspector name if available
        if (inspector != null && !inspector.isEmpty() && !inspector.equals("null")) {
            tvInspector.setText(inspector);
        } else {
            tvInspector.setText("N/A");
        }

        tvType.setText(type != null ? type : "N/A");
        tvFuel.setText(fuel != null ? fuel : "N/A");
        tvDamage.setText(damage != null && !damage.isEmpty() ? damage : "None");
        tvNotes.setText(notes != null && !notes.isEmpty() ? notes : "No additional notes");

        // Load Photo if available
        if (photo != null && !photo.isEmpty() && !photo.equals("null")) {
            try {
                byte[] decodedBytes = Base64.decode(photo, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                imgEvidence.setImageBitmap(bitmap);
                photoSection.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                photoSection.setVisibility(View.GONE);
            }
        } else {
            photoSection.setVisibility(View.GONE);
        }

        // Back Button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}