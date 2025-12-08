package com.example.ridequest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MaintenanceDashboardActivity extends AppCompatActivity {

    private CarRentalData db;
    private RecyclerView recyclerView;
    private TextView tvSectionTitle, tvItemCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance_dashboard);

        db = new CarRentalData(this);

        recyclerView = findViewById(R.id.rvMaintenanceVehicles);
        tvSectionTitle = findViewById(R.id.tvSectionTitle);
        tvItemCount = findViewById(R.id.tvItemCount);

        tvSectionTitle.setText("Vehicle Maintenance");
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        loadVehicles();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadVehicles();
    }

    private void loadVehicles() {
        List<CarRentalData.VehicleMaintenanceItem> vehicles =
                db.getAllVehiclesForMaintenance();

        tvItemCount.setText(vehicles.size() + " vehicles");
        recyclerView.setAdapter(new MaintenanceVehicleAdapter(vehicles));
    }

    private class MaintenanceVehicleAdapter extends
            RecyclerView.Adapter<MaintenanceVehicleAdapter.ViewHolder> {

        private List<CarRentalData.VehicleMaintenanceItem> vehicles;

        MaintenanceVehicleAdapter(List<CarRentalData.VehicleMaintenanceItem> vehicles) {
            this.vehicles = vehicles;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_maintenance_vehicle, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CarRentalData.VehicleMaintenanceItem vehicle = vehicles.get(position);

            holder.tvCarName.setText(vehicle.carName);
            holder.tvPlate.setText("Plate: " + vehicle.plate);
            holder.tvStatus.setText("Status: " + vehicle.status);
            holder.tvLastService.setText("Last Service: " + vehicle.lastService);
            holder.tvServiceCount.setText(vehicle.maintenanceCount + " services");

            if (vehicle.imageRes != null && !vehicle.imageRes.isEmpty()) {
                try {
                    byte[] decoded = Base64.decode(vehicle.imageRes, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                    holder.imgCar.setImageBitmap(bitmap);
                } catch (Exception e) {
                    holder.imgCar.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            } else {
                holder.imgCar.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            holder.btnLogService.setOnClickListener(v -> {
                Intent intent = new Intent(MaintenanceDashboardActivity.this,
                        MaintenanceActivity.class);
                intent.putExtra("VEHICLE_ID", vehicle.vehicleId);
                intent.putExtra("CAR_NAME", vehicle.carName);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return vehicles.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imgCar;
            TextView tvCarName, tvPlate, tvStatus, tvLastService, tvServiceCount;
            Button btnLogService;

            ViewHolder(View itemView) {
                super(itemView);
                imgCar = itemView.findViewById(R.id.imgCar);
                tvCarName = itemView.findViewById(R.id.tvCarName);
                tvPlate = itemView.findViewById(R.id.tvPlate);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                tvLastService = itemView.findViewById(R.id.tvLastService);
                tvServiceCount = itemView.findViewById(R.id.tvServiceCount);
                btnLogService = itemView.findViewById(R.id.btnLogService);
            }
        }
    }
}