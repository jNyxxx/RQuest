package com.example.ridequest;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class BookingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        int vid = getIntent().getIntExtra("VID", -1);
        double price = getIntent().getDoubleExtra("PRICE", 0.0);
        String name = getIntent().getStringExtra("NAME");
        String imgRes = getIntent().getStringExtra("IMG_RES");

        TextView tvName = findViewById(R.id.tvBookingCarName);
        TextView tvPrice = findViewById(R.id.tvBookingPrice);
        ImageView ivCar = findViewById(R.id.ivBookingCar);
        EditText etPickup = findViewById(R.id.etPickupDate);
        EditText etReturn = findViewById(R.id.etReturnDate);
        Spinner spPickup = findViewById(R.id.spPickupLoc);
        Spinner spReturn = findViewById(R.id.spReturnLoc);

        if(tvName != null) tvName.setText(name);
        if(tvPrice != null) tvPrice.setText("$" + price + " per day");

        if(ivCar != null && imgRes != null) {
            int resId = getResources().getIdentifier(imgRes, "drawable", getPackageName());
            if(resId != 0) ivCar.setImageResource(resId);
        }

        String[] locations = {"Cebu City Center", "Mactan Airport", "Ayala Center"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, locations);
        if(spPickup != null) spPickup.setAdapter(adapter);
        if(spReturn != null) spReturn.setAdapter(adapter);

        findViewById(R.id.btnContinue).setOnClickListener(v -> {
            String start = etPickup.getText().toString();
            String end = etReturn.getText().toString();

            if(start.isEmpty() || end.isEmpty()) {
                Toast.makeText(this, "Please select dates", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent i = new Intent(this, PaymentActivity.class);
            i.putExtra("VID", vid);
            i.putExtra("PRICE", price);
            i.putExtra("NAME", name);
            i.putExtra("START_DATE", start);
            i.putExtra("END_DATE", end);
            startActivity(i);
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}