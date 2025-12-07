package com.example.ridequest;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
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

        btnVehicles.setOnClickListener(v -> loadVehicles());
        btnBookings.setOnClickListener(v -> loadBookings());
        btnMaintenance.setOnClickListener(v -> loadMaintenance());
        btnInspections.setOnClickListener(v -> loadInspections());

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

        fab.setOnClickListener(v -> startActivity(new Intent(this, AddVehicleActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentUserRole.equals("Inspection Agent")) loadInspections();
        else if (currentUserRole.equals("Mechanic")) loadMaintenance();
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
        } else if (currentUserRole.equalsIgnoreCase("Mechanic")) {
            tvAdminSubtitle.setText("Mechanic");
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

    private void loadVehicles() {
        if(!currentUserRole.equals("Manager")) return;
        highlightTab(btnVehicles);
        tvSectionTitle.setText("Fleet Management");

        List<CarRentalData.VehicleItem> list = db.getAllVehicles();
        tvItemCount.setText(list.size() + " vehicles");

        recyclerView.setAdapter(new VehicleAdapter(this, list, true,
                id -> { db.deleteVehicle(id); loadVehicles(); },
                v -> {
                    Intent i = new Intent(this, EditVehicleActivity.class);
                    i.putExtra("VEHICLE_ID", v.id);
                    startActivity(i);
                }
        ));
    }

    private void loadBookings() {
        if(!currentUserRole.equals("Manager")) return;
        highlightTab(btnBookings);
        tvSectionTitle.setText("Reservations");

        List<CarRentalData.AdminBookingItem> list = db.getAllBookingsForAdmin();
        tvItemCount.setText(list.size() + " bookings");

        AdminBookingAdapter adapter = new AdminBookingAdapter(this, list, "Manager", new AdminBookingAdapter.BookingActionListener() {
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
                if(db.cancelBooking(b.id, true)) loadBookings();
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

            @Override
            public void onViewDetails(CarRentalData.AdminBookingItem b) {
                Intent i = new Intent(AdminDashboardActivity.this, BookingDetailsActivity.class);
                i.putExtra("BOOKING_ID", b.id);
                startActivity(i);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void loadMaintenance() {
        highlightTab(btnMaintenance);
        tvSectionTitle.setText("Maintenance Records");

        List<CarRentalData.VehicleItem> vehicles = db.getAllVehicles();
        tvItemCount.setText(vehicles.size() + " vehicles");

        recyclerView.setAdapter(new RecyclerView.Adapter<VehicleAdapter.ViewHolder>() {
            @Override
            public VehicleAdapter.ViewHolder onCreateViewHolder(android.view.ViewGroup p, int t) {
                return new VehicleAdapter.ViewHolder(getLayoutInflater().inflate(R.layout.item_vehicle, p, false));
            }

            @Override
            public void onBindViewHolder(VehicleAdapter.ViewHolder h, int pos) {
                CarRentalData.VehicleItem v = vehicles.get(pos);
                h.name.setText(v.title);
                h.type.setText(v.type);
                h.price.setText("Status: " + v.status);
                h.btnEdit.setVisibility(View.GONE);
                h.btnDelete.setVisibility(View.GONE);
                h.btnDetails.setVisibility(View.VISIBLE);

                if (currentUserRole.equals("Mechanic")) {
                    h.btnDetails.setText("Log Service");
                    h.btnDetails.setOnClickListener(view -> {
                        Intent i = new Intent(AdminDashboardActivity.this, MaintenanceActivity.class);
                        i.putExtra("VEHICLE_ID", v.id);
                        startActivity(i);
                    });
                } else {
                    h.btnDetails.setText("View Logs");
                    h.btnDetails.setOnClickListener(view -> {
                        Toast.makeText(AdminDashboardActivity.this, "Viewing logs for " + v.title, Toast.LENGTH_SHORT).show();
                    });
                }

                if (v.imageRes != null && !v.imageRes.isEmpty()) {
                    try {
                        byte[] b = android.util.Base64.decode(v.imageRes, android.util.Base64.DEFAULT);
                        h.img.setImageBitmap(android.graphics.BitmapFactory.decodeByteArray(b, 0, b.length));
                    } catch (Exception e) {}
                }
            }
            @Override public int getItemCount() { return vehicles.size(); }
        });
    }

    private void loadInspections() {
        highlightTab(btnInspections);
        tvSectionTitle.setText("Inspections");

        if (currentUserRole.equals("Inspection Agent")) {
            // AGENT VIEW: Tasks
            List<CarRentalData.AdminBookingItem> list = db.getBookingsForInspection();
            tvItemCount.setText(list.size() + " tasks");

            AdminBookingAdapter adapter = new AdminBookingAdapter(this, list, "Inspection Agent", new AdminBookingAdapter.BookingActionListener() {
                @Override public void onApprove(CarRentalData.AdminBookingItem b) {}
                @Override public void onCancel(CarRentalData.AdminBookingItem b) {}
                @Override public void onReturn(CarRentalData.AdminBookingItem b) {}
                @Override public void onViewDetails(CarRentalData.AdminBookingItem b) {
                    Intent i = new Intent(AdminDashboardActivity.this, InspectionActivity.class);
                    i.putExtra("BOOKING_ID", b.id);
                    startActivity(i);
                }
            });
            recyclerView.setAdapter(adapter);
        } else {
            // MANAGER VIEW: History
            tvSectionTitle.setText("Inspection Logs");
            List<CarRentalData.InspectionLogItem> history = db.getInspectionHistory();
            tvItemCount.setText(history.size() + " records");

            InspectionHistoryAdapter historyAdapter = new InspectionHistoryAdapter(this, history, item -> {
                Intent i = new Intent(AdminDashboardActivity.this, InspectionDetailsActivity.class);
                i.putExtra("ID", item.inspectionId);
                i.putExtra("TYPE", item.type);
                i.putExtra("DATE", item.date);
                i.putExtra("CAR", item.carName);
                i.putExtra("CUSTOMER", item.customerName);
                i.putExtra("INSPECTOR", item.inspectorName);
                i.putExtra("FUEL", item.fuel);
                i.putExtra("DAMAGE", item.damage);
                i.putExtra("NOTES", item.notes);
                i.putExtra("PHOTO", item.photos);
                startActivity(i);
            });
            recyclerView.setAdapter(historyAdapter);
        }
    }

    private void updateStats() {
        if (db == null) return;
        List<CarRentalData.VehicleItem> v = db.getAllVehicles();
        List<CarRentalData.AdminBookingItem> b = db.getAllBookingsForAdmin();
        if(v != null) tvTotalVehicles.setText(String.valueOf(v.size()));
        if(b != null) tvActiveBookings.setText(String.valueOf(b.size()));
    }
}