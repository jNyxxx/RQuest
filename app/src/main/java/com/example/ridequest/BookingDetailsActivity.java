package com.example.ridequest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
            finish();
            return;
        }

        // getter of booking details from database
        CarRentalData db = new CarRentalData(this);
        CarRentalData.BookingDetailItem booking = db.getBookingDetails(bookingId);

        if (booking == null) {
            finish();
            return;
        }

        // UI elements
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
        ImageView ivReceipt = findViewById(R.id.ivReceipt);

        if (tvTitle != null) tvTitle.setText("Booking #" + booking.id);
        if (tvBookingRef != null) tvBookingRef.setText("Ref: " + booking.bookingReference);
        if (tvStatus != null) {
            tvStatus.setText(booking.status);
            // set status color
            switch (booking.status) {
                case "Pending":
                    tvStatus.setTextColor(getResources().getColor(R.color.rq_orange));
                    break;
                case "Confirmed":
                    tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    break;
                case "Cancelled":
                    tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    break;
            }
        }

        // customer details
        if (tvCustomerName != null) tvCustomerName.setText(booking.customerName);
        if (tvCustomerEmail != null) tvCustomerEmail.setText(booking.customerEmail);
        if (tvCustomerPhone != null) tvCustomerPhone.setText(booking.customerPhone);

        // car details
        if (tvCarName != null) tvCarName.setText(booking.carName);

        // car image
        if (ivCarImage != null && booking.carImage != null) {
            try {
                byte[] decodedBytes = Base64.decode(booking.carImage, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                if (bitmap != null) {
                    ivCarImage.setImageBitmap(bitmap);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading car image", e);
            }
        }

        // booking dates
        if (tvPickupDate != null) tvPickupDate.setText(booking.pickupDate);
        if (tvPickupTime != null) tvPickupTime.setText(booking.pickupTime);
        if (tvReturnDate != null) tvReturnDate.setText(booking.returnDate);
        if (tvReturnTime != null) tvReturnTime.setText(booking.returnTime);

        // addresses
        if (tvPickupAddress != null) tvPickupAddress.setText(booking.pickupAddress);
        if (tvReturnAddress != null) tvReturnAddress.setText(booking.returnAddress);

        // payment details
        double downpayment = booking.totalCost * 0.30;
        double balance = booking.totalCost - downpayment;

        if (tvTotalCost != null) tvTotalCost.setText("$" + String.format("%.2f", booking.totalCost));
        if (tvDownpayment != null) tvDownpayment.setText("$" + String.format("%.2f", downpayment));
        if (tvBalance != null) tvBalance.setText("$" + String.format("%.2f", balance));
        if (tvPaymentMethod != null) tvPaymentMethod.setText(booking.paymentMethod);

        // payment receipt
        if (ivReceipt != null && booking.paymentReceipt != null && !booking.paymentReceipt.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(booking.paymentReceipt, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                if (bitmap != null) {
                    ivReceipt.setImageBitmap(bitmap);
                    ivReceipt.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading receipt image", e);
                ivReceipt.setVisibility(View.GONE);
            }
        } else {
            if (ivReceipt != null) ivReceipt.setVisibility(View.GONE);
        }

        // back button
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }
}