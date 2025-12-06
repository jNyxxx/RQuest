package com.example.ridequest;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat; // Added for color handling
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private static final String TAG = "AdminDashboard";
    private CarRentalData db;
    private RecyclerView recyclerView;
    private ExtendedFloatingActionButton fab;
    private LinearLayout layoutEmpty, layoutStats;
    private TextView tvSectionTitle, tvItemCount, tvTotalVehicles, tvActiveBookings, tvAdminSubtitle;

    private boolean showingVehicles = true;

    private MaterialButton btnVehicles, btnBookings, btnMaintenance, btnInspections;
    private MaterialCardView cardAdminProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        db = new CarRentalData(this);

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

        btnVehicles.setOnClickListener(v -> selectTab((MaterialButton) v));
        btnBookings.setOnClickListener(v -> selectTab((MaterialButton) v));
        btnMaintenance.setOnClickListener(v -> selectTab((MaterialButton) v));
        btnInspections.setOnClickListener(v -> selectTab((MaterialButton) v));

        // profile menu
        if (cardAdminProfile != null) {
            cardAdminProfile.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(AdminDashboardActivity.this, cardAdminProfile);
                popup.getMenu().add(0, 1, 0, "Profile");
                popup.getMenu().add(0, 2, 0, "Settings");
                popup.getMenu().add(0, 3, 0, "Log Out");

                popup.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case 1:
                            Toast.makeText(this, "Profile coming soon", Toast.LENGTH_SHORT).show();
                            return true;
                        case 2:
                            Toast.makeText(this, "Settings coming soon", Toast.LENGTH_SHORT).show();
                            return true;
                        case 3:
                            Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                            return true;
                    }
                    return false;
                });
                popup.show();
            });
        }

        // add vehicles
        fab.setOnClickListener(v -> {
            if (showingVehicles) {
                startActivity(new Intent(AdminDashboardActivity.this, AddVehicleActivity.class));
            } else {
                Toast.makeText(this, "Action not available in this tab", Toast.LENGTH_SHORT).show();
            }
        });

        // scroll
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && fab.isExtended()) {
                    fab.shrink();
                } else if (dy < 0 && !fab.isExtended()) {
                    fab.extend();
                }
            }
        });

        selectTab(btnVehicles);
        updateStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (showingVehicles) loadVehicles();
        else if (btnBookings.getText().toString().equals("Bookings")) loadBookings();
        updateStats();
    }

    private void selectTab(MaterialButton selectedButton) {
        int orangeColor = ContextCompat.getColor(this, R.color.rq_orange);
        int whiteColor = ContextCompat.getColor(this, android.R.color.white);
        int transparentColor = ContextCompat.getColor(this, android.R.color.transparent);
        int strokeWidthPx = (int) (2 * getResources().getDisplayMetrics().density); // 2dp

        resetButtonStyle(btnVehicles, "V", orangeColor, transparentColor, strokeWidthPx);
        resetButtonStyle(btnBookings, "B", orangeColor, transparentColor, strokeWidthPx);
        resetButtonStyle(btnMaintenance, "M", orangeColor, transparentColor, strokeWidthPx);
        resetButtonStyle(btnInspections, "I", orangeColor, transparentColor, strokeWidthPx);

        selectedButton.setBackgroundTintList(ColorStateList.valueOf(orangeColor));
        selectedButton.setTextColor(whiteColor);
        selectedButton.setStrokeWidth(0);

        int id = selectedButton.getId();

        if (id == R.id.btnViewVehicles) {
            selectedButton.setText("Vehicles");
            showingVehicles = true;
            tvSectionTitle.setText("All Vehicles");
            tvAdminSubtitle.setText("Manage your fleet");
            fab.setText("Add Vehicle");
            fab.setVisibility(View.VISIBLE);
            loadVehicles();

        } else if (id == R.id.btnViewBookings) {
            selectedButton.setText("Bookings");
            showingVehicles = false;
            tvSectionTitle.setText("All Bookings");
            tvAdminSubtitle.setText("Manage reservations");
            fab.setVisibility(View.GONE);
            loadBookings();

        } else if (id == R.id.btnViewMaintenance) {
            selectedButton.setText("Maintenance");
            showingVehicles = false;
            tvSectionTitle.setText("Maintenance");
            tvAdminSubtitle.setText("Vehicle upkeep status");
            fab.setVisibility(View.GONE);
            showEmptyState("No maintenance records", "Maintenance tracking coming soon");
            tvItemCount.setText("0 items");

        } else if (id == R.id.btnViewInspections) {
            selectedButton.setText("Inspections");
            showingVehicles = false;
            tvSectionTitle.setText("Inspections");
            tvAdminSubtitle.setText("Quality control checks");
            fab.setVisibility(View.GONE);
            showEmptyState("No inspections found", "Inspection logs coming soon");
            tvItemCount.setText("0 items");
        }
    }

    private void resetButtonStyle(MaterialButton btn, String abbrText, int orange, int transparent, int stroke) {
        btn.setText(abbrText);
        btn.setBackgroundTintList(ColorStateList.valueOf(transparent));
        btn.setStrokeColor(ColorStateList.valueOf(orange));
        btn.setStrokeWidth(stroke);
        btn.setTextColor(orange);
    }

    private void updateStats() {
        // Get counts from database
        List<CarRentalData.VehicleItem> vehicles = db.getAllVehicles();
        List<CarRentalData.AdminBookingItem> bookings = db.getAllBookingsForAdmin();

        int vehicleCount = vehicles.size();
        int bookingCount = bookings.size();

        tvTotalVehicles.setText(String.valueOf(vehicleCount));
        tvActiveBookings.setText(String.valueOf(bookingCount));

        layoutStats.setVisibility(View.VISIBLE);
    }

    private void loadVehicles() {
        Log.d(TAG, "Loading vehicles...");
        List<CarRentalData.VehicleItem> vehicles = db.getAllVehicles();

        if (vehicles.isEmpty()) {
            showEmptyState("No vehicles yet", "Tap the + button to add your first vehicle");
        } else {
            hideEmptyState();
            tvItemCount.setText(vehicles.size() + " items");

            recyclerView.setAdapter(new VehicleAdapter(this, vehicles, true,
                    new VehicleAdapter.OnDeleteListener() {
                        @Override
                        public void onDelete(int id) {
                            db.deleteVehicle(id);
                            loadVehicles();
                            updateStats();
                            Toast.makeText(AdminDashboardActivity.this, "Vehicle Deleted ✓", Toast.LENGTH_SHORT).show();
                        }
                    },
                    new VehicleAdapter.OnEditListener() {
                        @Override
                        public void onEdit(CarRentalData.VehicleItem vehicle) {
                            Intent intent = new Intent(AdminDashboardActivity.this, EditVehicleActivity.class);
                            intent.putExtra("VEHICLE_ID", vehicle.id);
                            intent.putExtra("MAKE_MODEL", vehicle.title);
                            intent.putExtra("TYPE", vehicle.type);
                            intent.putExtra("PRICE", vehicle.price);
                            intent.putExtra("IMAGE_RES", vehicle.imageRes);
                            intent.putExtra("TRANSMISSION", vehicle.transmission);
                            intent.putExtra("SEATS", vehicle.seats);
                            startActivity(intent);
                        }
                    }
            ));
        }
    }

    private void loadBookings() {
        Log.d(TAG, "Loading bookings...");
        List<CarRentalData.AdminBookingItem> bookings = db.getAllBookingsForAdmin();

        if (bookings.isEmpty()) {
            showEmptyState("No bookings yet", "Bookings will appear here when customers make reservations");
        } else {
            hideEmptyState();
            tvItemCount.setText(bookings.size() + " items");

            AdminBookingAdapter adapter = new AdminBookingAdapter(
                    this,
                    bookings,
                    new AdminBookingAdapter.BookingActionListener() {
                        @Override
                        public void onApprove(CarRentalData.AdminBookingItem booking) {
                            approveBooking(booking);
                        }

                        @Override
                        public void onCancel(CarRentalData.AdminBookingItem booking) {
                            cancelBooking(booking);
                        }

                        @Override
                        public void onReturn(CarRentalData.AdminBookingItem booking) {
                            if (db.markBookingAsReturned(booking.id)) {
                                Toast.makeText(AdminDashboardActivity.this, "Vehicle Returned & Available ✓", Toast.LENGTH_SHORT).show();
                                loadBookings();
                                updateStats();
                            } else {
                                Toast.makeText(AdminDashboardActivity.this, "Failed to update status", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onViewDetails(CarRentalData.AdminBookingItem booking) {
                            viewBookingDetails(booking);
                        }
                    }
            );
            recyclerView.setAdapter(adapter);
        }
    }

    private void showEmptyState(String title, String message) {
        recyclerView.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);

        TextView tvEmptyTitle = layoutEmpty.findViewById(R.id.tvEmptyTitle);
        TextView tvEmptyMessage = layoutEmpty.findViewById(R.id.tvEmptyMessage);

        if (tvEmptyTitle != null) tvEmptyTitle.setText(title);
        if (tvEmptyMessage != null) tvEmptyMessage.setText(message);
    }

    private void hideEmptyState() {
        recyclerView.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
    }

    private void approveBooking(CarRentalData.AdminBookingItem booking) {
        if (db.approveBooking(booking.id)) {
            String receipt = generateReceipt(booking);
            Toast.makeText(this, "Booking Approved! ✓", Toast.LENGTH_LONG).show();
            sendApprovalEmailToCustomer(booking, receipt);
            loadBookings();
            updateStats();
        } else {
            Toast.makeText(this, "Failed to approve booking", Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelBooking(CarRentalData.AdminBookingItem booking) {
        if (db.cancelBooking(booking.id, true)) {
            Toast.makeText(this, "Booking Cancelled ✓", Toast.LENGTH_SHORT).show();
            sendCancellationEmailToCustomer(booking);
            loadBookings();
            updateStats();
        } else {
            Toast.makeText(this, "Failed to cancel booking", Toast.LENGTH_SHORT).show();
        }
    }

    private void viewBookingDetails(CarRentalData.AdminBookingItem booking) {
        Intent intent = new Intent(this, BookingDetailsActivity.class);
        intent.putExtra("BOOKING_ID", booking.id);
        startActivity(intent);
    }

    private String generateReceipt(CarRentalData.AdminBookingItem booking) {
        return "━━━━━━━━━━━━━━━━━━━━\n" +
                "   RIDEQUEST RECEIPT\n" +
                "━━━━━━━━━━━━━━━━━━━━\n\n" +
                "Receipt #: " + booking.bookingReference + "\n" +
                "Date: " + new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm", java.util.Locale.US).format(new java.util.Date()) + "\n\n" +
                "Customer: " + booking.customerName + "\n" +
                "Vehicle: " + booking.carName + "\n\n" +
                "Pickup: " + booking.pickupDate + " " + booking.pickupTime + "\n" +
                "Return: " + booking.returnDate + " " + booking.returnTime + "\n\n" +
                "TOTAL: $" + String.format("%.2f", booking.totalCost) + "\n" +
                "Payment: " + booking.paymentMethod + "\n\n" +
                "Status: CONFIRMED\n" +
                "━━━━━━━━━━━━━━━━━━━━\n" +
                "Thank you for choosing\n" +
                "RideQuest!";
    }

    private void sendApprovalEmailToCustomer(CarRentalData.AdminBookingItem booking, String receipt) {
        String subject = "Booking Confirmed - " + booking.bookingReference;
        String body = "Dear " + booking.customerName + ",\n\n" +
                "Great news! Your booking has been APPROVED.\n\n" +
                receipt + "\n\n" +
                "Pickup Address: " + booking.pickupAddress + "\n" +
                "Return Address: " + booking.returnAddress + "\n\n" +
                "Please arrive on time and bring a valid ID.\n\n" +
                "Safe travels!\n" +
                "RideQuest Team";

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{booking.customerEmail});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send confirmation via..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Log.e(TAG, "No email app found");
        }
    }

    private void sendCancellationEmailToCustomer(CarRentalData.AdminBookingItem booking) {
        String subject = "Booking Cancelled - " + booking.bookingReference;
        String body = "Dear " + booking.customerName + ",\n\n" +
                "Unfortunately, we had to cancel your booking.\n\n" +
                "Booking ID: " + booking.bookingReference + "\n" +
                "Vehicle: " + booking.carName + "\n" +
                "Total: $" + String.format("%.2f", booking.totalCost) + "\n\n" +
                "Reason: Payment not verified\n\n" +
                "If you believe this is an error, please contact us.\n\n" +
                "RideQuest Team";

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{booking.customerEmail});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send cancellation via..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Log.e(TAG, "No email app found");
        }
    }
}