package com.example.ridequest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class BookingDetailsActivity extends AppCompatActivity {

    private static final String TAG = "BookingDetails";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_details);

        int bookingId = getIntent().getIntExtra("BOOKING_ID", -1);

        if (bookingId == -1) {
            Log.e(TAG, "No Booking ID passed");
            finish();
            return;
        }

        CarRentalData db = new CarRentalData(this);
        CarRentalData.BookingDetailItem booking = db.getBookingDetails(bookingId);

        if (booking == null) {
            Log.e(TAG, "Booking not found in DB");
            finish();
            return;
        }

        // Initialize UI
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvBookingRef = findViewById(R.id.tvBookingRef);
        TextView tvStatus = findViewById(R.id.tvStatus);

        TextView tvCustomerName = findViewById(R.id.tvCustomerName);
        TextView tvCustomerEmail = findViewById(R.id.tvCustomerEmail);
        TextView tvCustomerPhone = findViewById(R.id.tvCustomerPhone);

        TextView tvCarName = findViewById(R.id.tvCarName);
        ImageView ivCarImage = findViewById(R.id.ivCarImage);

        TextView tvPickupDate = findViewById(R.id.tvPickupDate);
        TextView tvPickupTime = findViewById(R.id.tvPickupTime);
        TextView tvReturnDate = findViewById(R.id.tvReturnDate);
        TextView tvReturnTime = findViewById(R.id.tvReturnTime);
        TextView tvPickupAddress = findViewById(R.id.tvPickupAddress);
        TextView tvReturnAddress = findViewById(R.id.tvReturnAddress);

        TextView tvTotalCost = findViewById(R.id.tvTotalCost);
        TextView tvDownpayment = findViewById(R.id.tvDownpayment);
        TextView tvBalance = findViewById(R.id.tvBalance);
        TextView tvPaymentMethod = findViewById(R.id.tvPaymentMethod);

        // Correct ID from XML
        ImageView ivReceipt = findViewById(R.id.ivReceipt);

        // Set Texts
        if (tvBookingRef != null) tvBookingRef.setText(booking.bookingReference);

        if (tvStatus != null) {
            tvStatus.setText(booking.status);
            if(booking.status.equals("Pending")) tvStatus.setTextColor(getResources().getColor(R.color.rq_orange));
            else if(booking.status.equals("Confirmed")) tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            else if(booking.status.equals("Cancelled")) tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }

        if (tvCustomerName != null) tvCustomerName.setText(booking.customerName);
        if (tvCustomerEmail != null) tvCustomerEmail.setText(booking.customerEmail);
        if (tvCustomerPhone != null) tvCustomerPhone.setText(booking.customerPhone);
        if (tvCarName != null) tvCarName.setText(booking.carName);

        if (tvPickupDate != null) tvPickupDate.setText(booking.pickupDate);
        if (tvPickupTime != null) tvPickupTime.setText(booking.pickupTime);
        if (tvReturnDate != null) tvReturnDate.setText(booking.returnDate);
        if (tvReturnTime != null) tvReturnTime.setText(booking.returnTime);
        if (tvPickupAddress != null) tvPickupAddress.setText(booking.pickupAddress);
        if (tvReturnAddress != null) tvReturnAddress.setText(booking.returnAddress);

        double downpayment = booking.totalCost * 0.30;
        double balance = booking.totalCost - downpayment;

        if (tvTotalCost != null) tvTotalCost.setText("$" + String.format("%.2f", booking.totalCost));
        if (tvDownpayment != null) tvDownpayment.setText("$" + String.format("%.2f", downpayment));
        if (tvBalance != null) tvBalance.setText("$" + String.format("%.2f", balance));
        if (tvPaymentMethod != null) tvPaymentMethod.setText(booking.paymentMethod);

        // ==========================================
        // ROBUST IMAGE LOADING (Handles Base64 & URI)
        // ==========================================

        // 1. Load Car Image
        if (ivCarImage != null) {
            loadSmartImage(ivCarImage, booking.carImage, "CarImage");
        }

        // 2. Load Receipt Image
        if (ivReceipt != null) {
            loadSmartImage(ivReceipt, booking.paymentReceipt, "PaymentReceipt");
        }

        // Back button
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    // --- SMART IMAGE LOADER HELPER ---
    private void loadSmartImage(ImageView imageView, String imageData, String tag) {
        if (imageData == null || imageData.isEmpty()) {
            Log.e(TAG, tag + ": Data is NULL or EMPTY");
            imageView.setVisibility(View.GONE);
            return;
        }

        // Log the length to verify data exists
        Log.d(TAG, tag + ": Data Length = " + imageData.length());

        try {
            // OPTION A: Try Base64 Decoding
            if (imageData.length() > 100) { // Base64 is usually long
                byte[] decodedBytes = Base64.decode(imageData, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    imageView.setVisibility(View.VISIBLE);
                    Log.d(TAG, tag + ": Loaded as Base64 Bitmap");
                    return;
                }
            }
        } catch (Exception e) {
            Log.w(TAG, tag + ": Not a valid Base64 string, trying URI...");
        }

        try {
            // OPTION B: Try as File Path / URI (Fallback)
            // This catches cases where you saved "content://..." instead of the image data
            imageView.setImageURI(Uri.parse(imageData));
            imageView.setVisibility(View.VISIBLE);
            Log.d(TAG, tag + ": Loaded as URI");
        } catch (Exception e) {
            Log.e(TAG, tag + ": Failed to load image. " + e.getMessage());
            imageView.setVisibility(View.GONE);
        }
    }
}