package com.example.ridequest;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private CarRentalData db;
    private RecyclerView recyclerView;
    private ExtendedFloatingActionButton fab;
    private LinearLayout layoutEmpty, layoutStats;
    private TextView tvSectionTitle, tvItemCount, tvTotalVehicles, tvActiveBookings, tvAdminSubtitle;
    private MaterialButton btnVehicles, btnBookings, btnMaintenance, btnInspections;
    private MaterialCardView cardAdminProfile;

    private String currentUserRole = "Manager"; // Default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        db = new CarRentalData(this);

        // Get Role from Intent
        currentUserRole = getIntent().getStringExtra("USER_ROLE");
        if(currentUserRole == null) currentUserRole = "Manager";

        // Initialize Views
        recyclerView = findViewById(R.id.rvAdmin);
        fab = findViewById(R.id.fabAdd);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        layoutStats = findViewById(R.id.layoutStats);
        tvSectionTitle = findViewById(R.id.tvSectionTitle);
        tvItemCount = findViewById(R.id.tvItemCount);
        tvTotalVehicles = findViewById(R.id.tvTotalVehicles);
        tvActiveBookings = findViewById(R.id.tvActiveBookings);
        tvAdminSubtitle = findViewById(R.id.tvAdminSubtitle);
        cardAdminProfile = findViewById(R.id.cardAdminProfile);

        btnVehicles = findViewById(R.id.btnViewVehicles);
        btnBookings = findViewById(R.id.btnViewBookings);
        btnMaintenance = findViewById(R.id.btnViewMaintenance);
        btnInspections = findViewById(R.id.btnViewInspections);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        setupRoleUI();

        // Tab Click Listeners
        btnVehicles.setOnClickListener(v -> loadVehicles());
        btnBookings.setOnClickListener(v -> loadBookings());
        btnMaintenance.setOnClickListener(v -> loadMaintenance());
        btnInspections.setOnClickListener(v -> loadInspections());

        // Profile Menu
        cardAdminProfile.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, cardAdminProfile);
            popup.getMenu().add(0, 1, 0, "Log Out");
            popup.setOnMenuItemClickListener(item -> {
                if(item.getItemId() == 1) {
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                }
                return true;
            });
            popup.show();
        });

        // FAB for Adding Vehicles
        fab.setOnClickListener(v -> startActivity(new Intent(this, AddVehicleActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentUserRole.equals("Inspection Agent")) loadInspections();
        else if (currentUserRole.equals("Mechanic Agent")) loadMaintenance();
        else if (btnVehicles.getStrokeWidth() == 0 && currentUserRole.equals("Manager")) loadVehicles();
        else if (btnBookings.getStrokeWidth() == 0 && currentUserRole.equals("Manager")) loadBookings();
        updateStats();
    }

    private void setupRoleUI() {
        if (currentUserRole.equalsIgnoreCase("Inspection Agent")) {
            tvAdminSubtitle.setText("Inspection Agent");
            fab.setVisibility(View.GONE);
            layoutStats.setVisibility(View.GONE);
            btnVehicles.setVisibility(View.GONE);
            btnBookings.setVisibility(View.GONE);
            btnMaintenance.setVisibility(View.GONE);
            highlightTab(btnInspections);
            loadInspections();
        } else if (currentUserRole.equalsIgnoreCase("Mechanic Agent")) {
            tvAdminSubtitle.setText("Mechanic Agent");
            fab.setVisibility(View.GONE);
            layoutStats.setVisibility(View.GONE);
            btnVehicles.setVisibility(View.GONE);
            btnBookings.setVisibility(View.GONE);
            btnInspections.setVisibility(View.GONE);
            highlightTab(btnMaintenance);
            loadMaintenance();
        } else {
            tvAdminSubtitle.setText("Manager");
            fab.setVisibility(View.VISIBLE);
            layoutStats.setVisibility(View.VISIBLE);
            highlightTab(btnVehicles);
            loadVehicles();
        }
    }

    private void highlightTab(MaterialButton selected) {
        int orange = ContextCompat.getColor(this, R.color.rq_orange);
        int transparent = ContextCompat.getColor(this, android.R.color.transparent);
        int white = ContextCompat.getColor(this, android.R.color.white);

        resetTab(btnVehicles, "V", orange, transparent);
        resetTab(btnBookings, "B", orange, transparent);
        resetTab(btnMaintenance, "M", orange, transparent);
        resetTab(btnInspections, "I", orange, transparent);

        selected.setBackgroundTintList(ColorStateList.valueOf(orange));
        selected.setTextColor(white);
        selected.setStrokeWidth(0);

        if(selected == btnVehicles) selected.setText("Vehicles");
        if(selected == btnBookings) selected.setText("Bookings");
        if(selected == btnMaintenance) selected.setText("Maintenance");
        if(selected == btnInspections) selected.setText("Inspections");
    }

    private void resetTab(MaterialButton btn, String txt, int orange, int trans) {
        btn.setText(txt);
        btn.setBackgroundTintList(ColorStateList.valueOf(trans));
        btn.setTextColor(orange);
        btn.setStrokeColor(ColorStateList.valueOf(orange));
        btn.setStrokeWidth(2);
    }

    // ========== VEHICLES TAB ==========
    private void loadVehicles() {
        if(!currentUserRole.equals("Manager")) return;
        highlightTab(btnVehicles);
        tvSectionTitle.setText("Fleet Management");

        List<CarRentalData.VehicleItem> list = db.getAllVehicles();
        tvItemCount.setText(list.size() + " vehicles");

        recyclerView.setAdapter(new VehicleAdapter(this, list, true,
                id -> {
                    db.deleteVehicle(id);
                    loadVehicles();
                },
                v -> {
                    Intent i = new Intent(this, EditVehicleActivity.class);
                    i.putExtra("VEHICLE_ID", v.id);
                    startActivity(i);
                }
        ));

        fab.setVisibility(View.VISIBLE);
        fab.setText("Add Vehicle");
    }

    // ========== BOOKINGS TAB ==========
    private void loadBookings() {
        if(!currentUserRole.equals("Manager")) return;
        highlightTab(btnBookings);
        tvSectionTitle.setText("Reservations");

        List<CarRentalData.AdminBookingItem> list = db.getAllBookingsForAdmin();
        tvItemCount.setText(list.size() + " bookings");

        AdminBookingAdapter adapter = new AdminBookingAdapter(this, list, "Manager",
                new AdminBookingAdapter.BookingActionListener() {
                    @Override
                    public void onApprove(CarRentalData.AdminBookingItem b) {
                        int empId = db.getCurrentEmployeeId(AdminDashboardActivity.this);
                        if(db.approveBooking(b.id, empId)) {
                            Toast.makeText(AdminDashboardActivity.this, "Booking Approved!", Toast.LENGTH_SHORT).show();
                            loadBookings();
                        }
                    }

                    @Override
                    public void onCancel(CarRentalData.AdminBookingItem b) {
                        if(db.cancelBooking(b.id, true)) {
                            Toast.makeText(AdminDashboardActivity.this, "Booking Cancelled", Toast.LENGTH_SHORT).show();
                            loadBookings();
                        }
                    }

                    @Override
                    public void onReturn(CarRentalData.AdminBookingItem b) {
                        if(db.markBookingAsReturned(b.id)) {
                            Toast.makeText(AdminDashboardActivity.this, "Vehicle Returned!", Toast.LENGTH_SHORT).show();
                            loadBookings();
                        } else {
                            Toast.makeText(AdminDashboardActivity.this, "Error returning vehicle", Toast.LENGTH_SHORT).show();
                        }
                    }

                    // --- FIX HERE: Added 'String inspectionType' ---
                    @Override
                    public void onViewDetails(CarRentalData.AdminBookingItem b, String inspectionType) {
                        // Manager just views details, ignores inspection type
                        Intent i = new Intent(AdminDashboardActivity.this, BookingDetailsActivity.class);
                        i.putExtra("BOOKING_ID", b.id);
                        startActivity(i);
                    }
                });
        recyclerView.setAdapter(adapter);
        fab.setVisibility(View.GONE);
    }

    // ========== MAINTENANCE TAB ==========
    private void loadMaintenance() {
        highlightTab(btnMaintenance);

        if (currentUserRole.equalsIgnoreCase("Mechanic Agent")) {
            // MECHANIC VIEW: Show all vehicles to log service
            tvSectionTitle.setText("Vehicle Maintenance");

            List<CarRentalData.VehicleMaintenanceItem> vehicles =
                    db.getAllVehiclesForMaintenance();
            tvItemCount.setText(vehicles.size() + " vehicles");

            MaintenanceVehicleAdapter adapter = new MaintenanceVehicleAdapter(vehicles);
            recyclerView.setAdapter(adapter);

        } else {
            // MANAGER VIEW: Show maintenance records/logs
            tvSectionTitle.setText("Maintenance Records");

            List<CarRentalData.MaintenanceLogItem> logs = db.getAllMaintenanceLogs();
            tvItemCount.setText(logs.size() + " records");

            MaintenanceLogAdapter adapter = new MaintenanceLogAdapter(this, logs);
            recyclerView.setAdapter(adapter);
        }

        fab.setVisibility(View.GONE);
    }

    // ========== INNER ADAPTER FOR MECHANIC VEHICLE LIST ==========
    private class MaintenanceVehicleAdapter extends
            RecyclerView.Adapter<MaintenanceVehicleAdapter.VehicleViewHolder> {

        private List<CarRentalData.VehicleMaintenanceItem> vehicles;

        MaintenanceVehicleAdapter(List<CarRentalData.VehicleMaintenanceItem> vehicles) {
            this.vehicles = vehicles;
        }

        @NonNull
        @Override
        public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_vehicle, parent, false);
            return new VehicleViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position) {
            CarRentalData.VehicleMaintenanceItem vehicle = vehicles.get(position);

            holder.tvCarName.setText(vehicle.carName);
            holder.tvType.setText("Plate: " + vehicle.plate);
            holder.tvPrice.setText("Status: " + vehicle.status + " | " +
                    vehicle.maintenanceCount + " services");

            holder.btnEdit.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
            holder.btnDetails.setVisibility(View.VISIBLE);
            holder.btnDetails.setText("Log Service");

            holder.btnDetails.setOnClickListener(view -> {
                Intent intent = new Intent(AdminDashboardActivity.this,
                        MaintenanceActivity.class);
                intent.putExtra("VEHICLE_ID", vehicle.vehicleId);
                intent.putExtra("CAR_NAME", vehicle.carName);
                startActivity(intent);
            });

            // Load Image
            if (vehicle.imageRes != null && !vehicle.imageRes.isEmpty()) {
                try {
                    byte[] decodedBytes = Base64.decode(vehicle.imageRes, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0,
                            decodedBytes.length);
                    holder.imgCar.setImageBitmap(bitmap);
                } catch (Exception e) {
                    holder.imgCar.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            } else {
                holder.imgCar.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }

        @Override
        public int getItemCount() {
            return vehicles.size();
        }

        // ViewHolder for Maintenance Vehicle
        class VehicleViewHolder extends RecyclerView.ViewHolder {
            ImageView imgCar;
            TextView tvCarName, tvType, tvPrice;
            Button btnDetails, btnEdit, btnDelete;

            VehicleViewHolder(View itemView) {
                super(itemView);
                imgCar = itemView.findViewById(R.id.ivCar);
                tvCarName = itemView.findViewById(R.id.tvCarName);
                tvType = itemView.findViewById(R.id.tvType);
                tvPrice = itemView.findViewById(R.id.tvPrice);
                btnDetails = itemView.findViewById(R.id.btnDetails);
                btnEdit = itemView.findViewById(R.id.btnEdit);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            }
        }
    }

    // ========== INSPECTIONS TAB ==========
    private void loadInspections() {
        highlightTab(btnInspections);

        if (currentUserRole.equalsIgnoreCase("Inspection Agent")) {
            // AGENT VIEW: Inspection Tasks
            tvSectionTitle.setText("Inspection Tasks");

            List<CarRentalData.AdminBookingItem> list = db.getBookingsForInspection();
            tvItemCount.setText(list.size() + " tasks");

            AdminBookingAdapter adapter = new AdminBookingAdapter(this, list,
                    "Inspection Agent", new AdminBookingAdapter.BookingActionListener() {
                @Override public void onApprove(CarRentalData.AdminBookingItem b) {}
                @Override public void onCancel(CarRentalData.AdminBookingItem b) {}
                @Override public void onReturn(CarRentalData.AdminBookingItem b) {}

                // Added 'String inspectionType' ---
                @Override
                public void onViewDetails(CarRentalData.AdminBookingItem b, String inspectionType) {
                    Intent intent = new Intent(AdminDashboardActivity.this, InspectionActivity.class);
                    intent.putExtra("BOOKING_ID", b.id);
                    // Now we pass the correct type (Pickup or Return) to the activity
                    intent.putExtra("INSPECTION_TYPE", inspectionType);
                    startActivity(intent);
                }
            });
            recyclerView.setAdapter(adapter);

        } else {
            // Inspection History
            tvSectionTitle.setText("Inspection Logs");

            List<CarRentalData.InspectionLogItem> history = db.getInspectionHistory();
            tvItemCount.setText(history.size() + " records");

            InspectionHistoryAdapter historyAdapter = new InspectionHistoryAdapter(
                    this, history, item -> {
                Intent intent = new Intent(AdminDashboardActivity.this,
                        InspectionDetailsActivity.class);
                intent.putExtra("ID", item.inspectionId);
                intent.putExtra("TYPE", item.type);
                intent.putExtra("DATE", item.date);
                intent.putExtra("CAR", item.carName);
                intent.putExtra("CUSTOMER", item.customerName);
                intent.putExtra("INSPECTOR", item.inspectorName);
                intent.putExtra("FUEL", item.fuel);
                intent.putExtra("DAMAGE", item.damage);
                intent.putExtra("NOTES", item.notes);
                intent.putExtra("PHOTO", item.photos);
                startActivity(intent);
            });
            recyclerView.setAdapter(historyAdapter);
        }

        fab.setVisibility(View.GONE);
    }

    // ========== STATISTICS ==========
    private void updateStats() {
        if (db == null) return;

        List<CarRentalData.VehicleItem> vehicles = db.getAllVehicles();
        List<CarRentalData.AdminBookingItem> bookings = db.getAllBookingsForAdmin();

        if(vehicles != null) {
            tvTotalVehicles.setText(String.valueOf(vehicles.size()));
        }

        if(bookings != null) {
            // Count only active bookings Pending, Confirmed, Rented
            int activeCount = 0;
            for(CarRentalData.AdminBookingItem booking : bookings) {
                if(booking.status.equals("Pending") ||
                        booking.status.equals("Confirmed") ||
                        booking.status.equals("Rented")) {
                    activeCount++;
                }
            }
            tvActiveBookings.setText(String.valueOf(activeCount));
        }
    }
}