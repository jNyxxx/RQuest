package com.example.ridequest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AdminDashboardActivity extends AppCompatActivity {

    private static final String TAG = "AdminDashboard";
    private CarRentalData db;
    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private boolean showingVehicles = true;

    private TextView btnVehicles, btnBookings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        db = new CarRentalData(this);
        recyclerView = findViewById(R.id.rvAdmin);
        fab = findViewById(R.id.fabAdd);
        ImageView btnProfile = findViewById(R.id.btnAdminProfile);

        btnVehicles = findViewById(R.id.btnViewVehicles);
        btnBookings = findViewById(R.id.btnViewBookings);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Tab Selection
        btnVehicles.setOnClickListener(v -> {
            showingVehicles = true;
            updateTabStyles();
            loadVehicles();
        });

        btnBookings.setOnClickListener(v -> {
            showingVehicles = false;
            updateTabStyles();
            loadBookings();
        });

        // Logout Logic - UPDATED to go directly to Login
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(AdminDashboardActivity.this, btnProfile);
                popup.getMenu().add(0, 1, 0, "Log Out");

                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == 1) {
                        // Go directly to LoginActivity instead of LandingActivity
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

        // FAB for adding vehicles
        fab.setOnClickListener(v -> {
            if (showingVehicles) {
                startActivity(new Intent(AdminDashboardActivity.this, AddVehicleActivity.class));
            } else {
                Toast.makeText(this, "Switch to Vehicles tab to add cars", Toast.LENGTH_SHORT).show();
            }
        });

        // Start with vehicles view
        updateTabStyles();
        loadVehicles();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (showingVehicles) loadVehicles();
        else loadBookings();
    }

    private void updateTabStyles() {
        if(showingVehicles) {
            btnVehicles.setBackgroundResource(R.drawable.bg_button_orange);
            btnVehicles.setTextColor(getResources().getColor(R.color.white));

            btnBookings.setBackgroundResource(R.drawable.bg_input_field);
            btnBookings.setTextColor(getResources().getColor(R.color.black));

            fab.show();
        } else {
            btnBookings.setBackgroundResource(R.drawable.bg_button_orange);
            btnBookings.setTextColor(getResources().getColor(R.color.white));

            btnVehicles.setBackgroundResource(R.drawable.bg_input_field);
            btnVehicles.setTextColor(getResources().getColor(R.color.black));

            fab.hide();
        }
    }

    private void loadVehicles() {
        Log.d(TAG, "Loading vehicles...");
        fab.show();

        // UPDATED: Added onEdit callback for editing vehicles
        recyclerView.setAdapter(new VehicleAdapter(this, db.getAllVehicles(), true,
                new VehicleAdapter.OnDeleteListener() {
                    @Override
                    public void onDelete(int id) {
                        db.deleteVehicle(id);
                        loadVehicles();
                        Toast.makeText(AdminDashboardActivity.this, "Vehicle Deleted", Toast.LENGTH_SHORT).show();
                    }
                },
                new VehicleAdapter.OnEditListener() {
                    @Override
                    public void onEdit(CarRentalData.VehicleItem vehicle) {
                        // Open EditVehicleActivity with vehicle data
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

    private void loadBookings() {
        Log.d(TAG, "Loading bookings...");
        fab.hide();

        AdminBookingAdapter adapter = new AdminBookingAdapter(
                this,
                db.getAllBookingsForAdmin(),
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
                    public void onViewDetails(CarRentalData.AdminBookingItem booking) {
                        viewBookingDetails(booking);
                    }
                }
        );

        recyclerView.setAdapter(adapter);
    }

    private void approveBooking(CarRentalData.AdminBookingItem booking) {
        Log.d(TAG, "Approving booking: " + booking.id);

        if(db.approveBooking(booking.id)) {
            String receipt = generateReceipt(booking);
            Toast.makeText(this, "Booking Approved!\n\n" + receipt, Toast.LENGTH_LONG).show();
            sendApprovalEmailToCustomer(booking, receipt);
            loadBookings();
        } else {
            Toast.makeText(this, "Failed to approve booking", Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelBooking(CarRentalData.AdminBookingItem booking) {
        Log.d(TAG, "Cancelling booking: " + booking.id);

        if(db.cancelBooking(booking.id, true)) {
            Toast.makeText(this, "Booking Cancelled", Toast.LENGTH_SHORT).show();
            sendCancellationEmailToCustomer(booking);
            loadBookings();
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
        String subject = "✅ Booking Confirmed - " + booking.bookingReference;

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
        String subject = "❌ Booking Cancelled - " + booking.bookingReference;

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