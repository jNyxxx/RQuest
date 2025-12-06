package com.example.ridequest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AdminBookingAdapter extends RecyclerView.Adapter<AdminBookingAdapter.ViewHolder> {

    private Context context;
    private List<CarRentalData.AdminBookingItem> bookings;
    private BookingActionListener listener;

    public interface BookingActionListener {
        void onApprove(CarRentalData.AdminBookingItem booking);
        void onCancel(CarRentalData.AdminBookingItem booking);
        void onReturn(CarRentalData.AdminBookingItem booking);
        void onViewDetails(CarRentalData.AdminBookingItem booking);
    }

    public AdminBookingAdapter(Context context, List<CarRentalData.AdminBookingItem> bookings, BookingActionListener listener) {
        this.context = context;
        this.bookings = bookings;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CarRentalData.AdminBookingItem booking = bookings.get(position);

        holder.tvBookingId.setText("Booking #" + booking.id);
        holder.tvCustomer.setText("Customer: " + booking.customerName);
        holder.tvCar.setText("Car: " + booking.carName);
        holder.tvDates.setText(booking.pickupDate + " → " + booking.returnDate);
        holder.tvTotal.setText("$" + String.format("%.2f", booking.totalCost));
        holder.tvStatus.setText(booking.status);

        // hides visibility
        holder.btnApprove.setVisibility(View.GONE);
        holder.btnCancel.setVisibility(View.GONE);
        holder.btnReturn.setVisibility(View.GONE);

        switch (booking.status) {
            case "Pending":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
                holder.tvStatus.setTextColor(context.getResources().getColor(R.color.rq_orange));
                holder.btnApprove.setVisibility(View.VISIBLE);
                holder.btnCancel.setVisibility(View.VISIBLE);
                break;

            case "Confirmed":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_confirmed);
                holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                holder.btnReturn.setVisibility(View.VISIBLE); // button visible after approving the booknig
                break;

            case "Cancelled":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_cancelled);
                holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                break;

            case "Completed":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_confirmed);
                holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
                break;
        }

        holder.btnApprove.setOnClickListener(v -> { if(listener != null) listener.onApprove(booking); });
        holder.btnCancel.setOnClickListener(v -> { if(listener != null) listener.onCancel(booking); });
        holder.btnReturn.setOnClickListener(v -> { if(listener != null) listener.onReturn(booking); });
        holder.itemView.setOnClickListener(v -> { if(listener != null) listener.onViewDetails(booking); });
    }

    @Override
    public int getItemCount() { return bookings.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookingId, tvCustomer, tvCar, tvDates, tvTotal, tvStatus;
        Button btnApprove, btnCancel, btnReturn;

        public ViewHolder(View view) {
            super(view);
            tvBookingId = view.findViewById(R.id.tvBookingId);
            tvCustomer = view.findViewById(R.id.tvCustomer);
            tvCar = view.findViewById(R.id.tvCar);
            tvDates = view.findViewById(R.id.tvDates);
            tvTotal = view.findViewById(R.id.tvTotal);
            tvStatus = view.findViewById(R.id.tvStatus);

            btnApprove = view.findViewById(R.id.btnApprove);
            btnCancel = view.findViewById(R.id.btnCancel);
            btnReturn = view.findViewById(R.id.btnReturn);
        }
    }
}