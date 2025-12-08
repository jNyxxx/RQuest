package com.example.ridequest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class CarDetailActivity extends AppCompatActivity {

    private static final String TAG = "CarDetailActivity";
    private CarRentalData db;
    private int vehicleId;
    private int userId;

    private ImageView ivCar, btnFavorite;
    private TextView tvName, tvPrice, tvCategory, tvColor;
    private TextView tvTransmission, tvSeats, tvFuel, tvType;
    private MaterialButton btnBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_detail);

        db = new CarRentalData(this);

        // Gets Vehicle ID
        vehicleId = getIntent().getIntExtra("VEHICLE_ID", -1);
        if (vehicleId == -1) {
            vehicleId = getIntent().getIntExtra("VID", -1);
        }

        Log.d(TAG, "=== CarDetailActivity Started ===");
        Log.d(TAG, "Received VEHICLE_ID: " + vehicleId);
        Log.d(TAG, "All Intent Extras:");
        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Log.d(TAG, "  " + key + " = " + getIntent().getExtras().get(key));
            }
        }

        // User Session
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        userId = prefs.getInt("UID", -1);

        if (vehicleId == -1) {
            Toast.makeText(this, "Vehicle not found", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "ERROR: No valid vehicle ID provided!");
            finish();
            return;
        }

        //  Initialize Viewsw
        ivCar = findViewById(R.id.ivDetailCar);
        tvName = findViewById(R.id.tvDetailTitle);
        tvPrice = findViewById(R.id.tvDetailPrice);
        tvCategory = findViewById(R.id.tvCategory);
        tvColor = findViewById(R.id.tvColor);

        tvTransmission = findViewById(R.id.tvTransmission);
        tvSeats = findViewById(R.id.tvSeats);
        tvFuel = findViewById(R.id.tvFuelType);
        tvType = findViewById(R.id.tvType);

        btnBook = findViewById(R.id.btnBookNow);
        btnFavorite = findViewById(R.id.btnFavorite);

        if (findViewById(R.id.btnBack) != null) {
            findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        }

        // loads data from db
        loadVehicleData();

        // Favorite Logic
        if (userId != -1 && btnFavorite != null) {
            boolean isFav = db.isVehicleFavorite(userId, vehicleId);
            updateFavoriteIcon(isFav);

            btnFavorite.setOnClickListener(v -> {
                boolean nowFav = db.toggleFavorite(userId, vehicleId);
                updateFavoriteIcon(nowFav);

                if (nowFav) Toast.makeText(this, "Added to Favorites", Toast.LENGTH_SHORT).show();
                else Toast.makeText(this, "Removed from Favorites", Toast.LENGTH_SHORT).show();
            });
        } else if (btnFavorite != null) {
            btnFavorite.setVisibility(View.GONE);
        }
    }

    private void loadVehicleData() {
        Log.d(TAG, "Loading vehicle data for ID: " + vehicleId);

        // Fetch from DB
        CarRentalData.VehicleItem vehicle = db.getVehicle(vehicleId);

        if (vehicle == null) {
            Toast.makeText(this, "Error loading vehicle data", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "ERROR: Vehicle not found in database!");
            return;
        }

        Log.d(TAG, "=== Vehicle Data Loaded ===");
        Log.d(TAG, "Title: " + vehicle.title);
        Log.d(TAG, "Price: $" + vehicle.price);
        Log.d(TAG, "Type: " + vehicle.type);
        Log.d(TAG, "Transmission: " + vehicle.transmission);
        Log.d(TAG, "Seats: " + vehicle.seats);
        Log.d(TAG, "Fuel: " + vehicle.fuelType);
        Log.d(TAG, "Category: " + vehicle.category);
        Log.d(TAG, "Color: " + vehicle.color);
        Log.d(TAG, "Status: " + vehicle.status);

        // setsall the data
        if (tvName != null) {
            tvName.setText(vehicle.title);
            Log.d(TAG, "✓ Set vehicle name");
        }

        if (tvPrice != null) {
            tvPrice.setText("$" + vehicle.price);
            Log.d(TAG, "✓ Set price");
        }

        if (tvCategory != null && vehicle.category != null) {
            tvCategory.setText(vehicle.category);
            Log.d(TAG, "✓ Set category");
        }

        if (tvColor != null && vehicle.color != null) {
            tvColor.setText(vehicle.color);
            Log.d(TAG, "✓ Set color");
        }

        if (tvTransmission != null) {
            tvTransmission.setText(vehicle.transmission != null ? vehicle.transmission : "Manual");
            Log.d(TAG, "✓ Set transmission");
        }

        if (tvSeats != null) {
            tvSeats.setText(vehicle.seats + " Seats");
            Log.d(TAG, "✓ Set seats");
        }

        if (tvFuel != null) {
            tvFuel.setText(vehicle.fuelType != null ? vehicle.fuelType : "Gasoline");
            Log.d(TAG, "✓ Set fuel type");
        }

        if (tvType != null) {
            tvType.setText(vehicle.type);
            Log.d(TAG, "✓ Set vehicle type");
        }

        if (ivCar != null) {
            String imageRes = vehicle.imageRes;
            if (imageRes != null && !imageRes.isEmpty()) {
                try {
                    if (imageRes.length() > 20) {
                        byte[] decodedBytes = Base64.decode(imageRes, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                        ivCar.setImageBitmap(bitmap);
                        Log.d(TAG, "✓ Loaded Base64 image");
                    } else {
                        int resId = getResources().getIdentifier(imageRes, "drawable", getPackageName());
                        ivCar.setImageResource(resId != 0 ? resId : R.drawable.ic_launcher_background);
                        Log.d(TAG, "✓ Loaded drawable image");
                    }
                } catch (Exception e) {
                    ivCar.setImageResource(R.drawable.ic_launcher_background);
                    Log.e(TAG, "Error loading image: " + e.getMessage());
                }
            } else {
                ivCar.setImageResource(R.drawable.ic_launcher_background);
                Log.d(TAG, "No image available, using default");
            }
        }

        // Book Button Logic
        if (btnBook != null) {
            btnBook.setOnClickListener(v -> {
                if (userId == -1) {
                    Toast.makeText(this, "Please login to book", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, LoginActivity.class));
                } else {
                    Intent i = new Intent(this, BookingActivity.class);
                    i.putExtra("VID", vehicle.id);
                    i.putExtra("NAME", vehicle.title);
                    i.putExtra("PRICE", vehicle.price);
                    i.putExtra("IMG_RES", vehicle.imageRes);
                    i.putExtra("TRANSMISSION", vehicle.transmission);
                    i.putExtra("SEATS", vehicle.seats);
                    Log.d(TAG, "Starting BookingActivity for vehicle: " + vehicle.title);
                    startActivity(i);
                }
            });
        }

        Log.d(TAG, "=== Vehicle Data Display Complete ===");
    }

    private void updateFavoriteIcon(boolean isFavorite) {
        if (btnFavorite == null) return;

        if (isFavorite) {
            btnFavorite.setImageResource(R.drawable.ic_star_filled);
        } else {
            btnFavorite.setImageResource(R.drawable.ic_star_outline);
        }
        btnFavorite.clearColorFilter();
    }
}