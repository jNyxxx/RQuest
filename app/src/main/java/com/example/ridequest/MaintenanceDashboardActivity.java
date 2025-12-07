package com.example.ridequest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import java.util.List;

public class MaintenanceDashboardActivity extends AppCompatActivity {

    private CarRentalData db;
    private RecyclerView recyclerView;
    private TextView tvSectionTitle, tvItemCount;
    private ExtendedFloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        db = new CarRentalData(this);

        // Bind Views
        recyclerView = findViewById(R.id.rvAdmin);
        tvSectionTitle = findViewById(R.id.tvSectionTitle);
        tvItemCount = findViewById(R.id.tvItemCount);
        fab = findViewById(R.id.fabAdd);

        // Hide Admin-specific elements we don't need for the Mechanic view
        View profileCard = findViewById(R.id.cardAdminProfile);
        if (profileCard != null) profileCard.setVisibility(View.GONE);

        View statsLayout = findViewById(R.id.layoutStats);
        if (statsLayout != null) statsLayout.setVisibility(View.GONE);

        // hide the tabs using the new ID
        View tabContainer = findViewById(R.id.cardTabContainer);
        if (tabContainer != null) {
            tabContainer.setVisibility(View.GONE);
        }

        // Customize UI for Mechanic
        tvSectionTitle.setText("Vehicle Maintenance");
        fab.setText("Log Service");
        fab.setIconResource(android.R.drawable.ic_menu_manage);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // FAB Click -> Go to Log Maintenance Screen (Generic vehicle selection)
        fab.setOnClickListener(v -> {
            startActivity(new Intent(MaintenanceDashboardActivity.this, MaintenanceActivity.class));
        });

        loadVehicles();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadVehicles();
    }

    private void loadVehicles() {
        List<CarRentalData.VehicleItem> vehicles = db.getAllVehicles();
        tvItemCount.setText(vehicles.size() + " vehicles");

        // We use a custom adapter implementation here to change behavior
        // Instead of "View Details", we want "Log Service"
        recyclerView.setAdapter(new RecyclerView.Adapter<VehicleAdapter.ViewHolder>() {

            @Override
            public VehicleAdapter.ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
                View view = android.view.LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_vehicle, parent, false);
                return new VehicleAdapter.ViewHolder(view);
            }

            @Override
            public void onBindViewHolder(VehicleAdapter.ViewHolder holder, int position) {
                CarRentalData.VehicleItem v = vehicles.get(position);

                holder.name.setText(v.title);
                holder.type.setText(v.type);
                holder.price.setText("Status: " + v.status);

                holder.btnEdit.setVisibility(View.GONE);
                holder.btnDelete.setVisibility(View.GONE);

                holder.btnDetails.setVisibility(View.VISIBLE);
                holder.btnDetails.setText("Log Service");
                holder.btnDetails.setOnClickListener(view -> {
                    Intent i = new Intent(MaintenanceDashboardActivity.this, MaintenanceActivity.class);
                    i.putExtra("VEHICLE_ID", v.id);
                    startActivity(i);
                });


                if (v.imageRes != null && !v.imageRes.isEmpty()) {
                    try {
                        byte[] decodedString = android.util.Base64.decode(v.imageRes, android.util.Base64.DEFAULT);
                        android.graphics.Bitmap decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        holder.img.setImageBitmap(decodedByte);
                    } catch (Exception e) {
                        holder.img.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                } else {
                    holder.img.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            }

            @Override
            public int getItemCount() {
                return vehicles.size();
            }
        });
    }
}