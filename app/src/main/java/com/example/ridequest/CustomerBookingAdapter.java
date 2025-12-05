package com.example.ridequest;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CustomerBookingAdapter extends RecyclerView.Adapter<CustomerBookingAdapter.ViewHolder> {
    private Context context;
    private List<CarRentalData.CustomerBookingItem> bookings;
    private OnBookingActionListener listener;

    public interface OnBookingActionListener {
        void onViewDetails(int bookingId);
        void onCancelBooking(int bookingId);
    }

    public CustomerBookingAdapter(Context context, List<CarRentalData.CustomerBookingItem> bookings, OnBookingActionListener listener) {
        this.context = context;
        this.bookings = bookings;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_customer_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CarRentalData.CustomerBookingItem booking = bookings.get(position);

        holder.tvBookingRef.setText("Ref: " + booking.bookingReference);
        holder.tvCarName.setText(booking.carName);
        holder.tvDates.setText(booking.pickupDate + " → " + booking.returnDate);
        holder.tvTotalCost.setText(String.format(Locale.US, "$%.2f", booking.totalCost));

        int resId = context.getResources().getIdentifier(booking.carImage, "drawable", context.getPackageName());
        if (resId != 0) {
            holder.ivCarImage.setImageResource(resId);
        } else {
            holder.ivCarImage.setImageResource(R.drawable.ic_car_placeholder);
        }

        holder.tvStatus.setText(booking.status);
        setStatusStyle(holder.tvStatus, booking.status);

        if (booking.status.equals("Cancelled")) {
            holder.btnCancel.setVisibility(View.GONE);
        } else if (booking.status.equals("Pending") || booking.status.equals("Confirmed")) {
            holder.btnCancel.setVisibility(View.VISIBLE);
        } else {
            holder.btnCancel.setVisibility(View.GONE);
        }

        holder.btnViewDetails.setOnClickListener(v -> listener.onViewDetails(booking.id));

        holder.btnCancel.setOnClickListener(v -> {
            boolean isWithin24Hours = false;
            double cancellationFee = 0;

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy", Locale.US);
                Date pickupDate = sdf.parse(booking.pickupDate);
                Date now = new Date();

                if (pickupDate != null) {
                    long diff = pickupDate.getTime() - now.getTime();
                    long hoursUntilPickup = TimeUnit.MILLISECONDS.toHours(diff);

                    if (hoursUntilPickup < 24) {
                        isWithin24Hours = true;
                        cancellationFee = booking.totalCost * 0.20;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            String message = "Are you sure you want to cancel this booking?";
            if (isWithin24Hours) {
                message += "\n\n Cancellation within 24 hours of pickup.\nA 20% cancellation fee of $"
                        + String.format(Locale.US, "%.2f", cancellationFee) + " will be applied.";
            } else {
                message += "\n\nYou will receive a full refund.";
            }

            new AlertDialog.Builder(context)
                    .setTitle("Cancel Booking")
                    .setMessage(message)
                    .setPositiveButton("Yes, Cancel", (dialog, which) -> listener.onCancelBooking(booking.id))
                    .setNegativeButton("No, Keep It", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    private void setStatusStyle(TextView tvStatus, String status) {
        switch (status) {
            case "Pending":
                tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
                tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
                break;
            case "Confirmed":
                tvStatus.setBackgroundResource(R.drawable.bg_status_confirmed);
                tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                break;
            case "Cancelled":
                tvStatus.setBackgroundResource(R.drawable.bg_status_cancelled);
                tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                break;
            default:
                tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
                tvStatus.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
                break;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCarImage;
        TextView tvBookingRef, tvCarName, tvDates, tvTotalCost, tvStatus;
        Button btnCancel, btnViewDetails;

        ViewHolder(View itemView) {
            super(itemView);
            ivCarImage = itemView.findViewById(R.id.ivCarImage);
            tvBookingRef = itemView.findViewById(R.id.tvBookingRef);
            tvCarName = itemView.findViewById(R.id.tvCarName);
            tvDates = itemView.findViewById(R.id.tvDates);
            tvTotalCost = itemView.findViewById(R.id.tvTotalCost);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnCancel = itemView.findViewById(R.id.btnCancel);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
        }
    }
}