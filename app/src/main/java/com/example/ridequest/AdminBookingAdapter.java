package com.example.ridequest;

import android.content.Context;
import android.graphics.Color;
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
    private String userRole; // ADDED: To distinguish between Manager and Agent views

    public interface BookingActionListener {
        void onApprove(CarRentalData.AdminBookingItem booking);
        void onCancel(CarRentalData.AdminBookingItem booking);
        void onReturn(CarRentalData.AdminBookingItem booking); // Manager Action
        void onViewDetails(CarRentalData.AdminBookingItem booking); // Agent Action (Inspect)
    }

    // UPDATED CONSTRUCTOR
    public AdminBookingAdapter(Context context, List<CarRentalData.AdminBookingItem> bookings, String userRole, BookingActionListener listener) {
        this.context = context;
        this.bookings = bookings;
        this.userRole = userRole; // Store the role
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
        holder.tvCustomer.setText(booking.customerName);
        holder.tvCar.setText(booking.carName);
        holder.tvDates.setText(booking.pickupDate + " → " + booking.returnDate);
        holder.tvTotal.setText("$" + String.format("%.2f", booking.totalCost));

        // 1. RESET BUTTONS (Default to GONE)
        holder.btnApprove.setVisibility(View.GONE);
        holder.btnCancel.setVisibility(View.GONE);
        holder.btnReturn.setVisibility(View.GONE);
        holder.tvStatus.setOnClickListener(null); // Reset click listeners on status

        // 2. LOGIC BASED ON STATUS
        switch (booking.status) {
            case "Pending":
                holder.tvStatus.setText("Pending Approval");
                holder.tvStatus.setTextColor(context.getResources().getColor(R.color.rq_orange));
                // Only Manager can approve/cancel
                if (userRole.equals("Manager")) {
                    holder.btnApprove.setVisibility(View.VISIBLE); // "Approve"
                    holder.btnCancel.setVisibility(View.VISIBLE);
                }
                break;

            case "Confirmed": // Ready for Pickup
                holder.tvStatus.setText("Ready for Pickup");
                holder.tvStatus.setTextColor(Color.parseColor("#008000")); // Green

                // If Agent, this is where they start the Pickup Inspection
                if (userRole.equals("Inspection Agent")) {
                    holder.btnApprove.setText("Start Pickup"); // Repurpose button
                    holder.btnApprove.setVisibility(View.VISIBLE);
                    holder.btnApprove.setOnClickListener(v -> listener.onViewDetails(booking));
                }
                break;

            case "Rented": // Car is out with client
                holder.tvStatus.setText("On Rental");
                holder.tvStatus.setTextColor(Color.BLUE);

                if (userRole.equals("Inspection Agent")) {
                    // AGENT VIEW: Needs to Inspect Return
                    holder.btnApprove.setText("Inspect Return"); // Repurpose button
                    holder.btnApprove.setVisibility(View.VISIBLE);
                    holder.btnApprove.setOnClickListener(v -> listener.onViewDetails(booking));
                } else {
                    // MANAGER VIEW: Shows nothing. Must wait for inspection.
                    holder.tvStatus.setText("Waiting for Inspection");
                }
                break;

            case "Inspected": // Agent is done.
                holder.tvStatus.setText("Inspection Complete");
                holder.tvStatus.setTextColor(Color.parseColor("#800080")); // Purple

                if (userRole.equals("Manager")) {
                    // MANAGER VIEW: NOW they see the Return button
                    holder.btnReturn.setVisibility(View.VISIBLE);
                    holder.btnReturn.setText("Finalize Return");
                } else {
                    // AGENT VIEW: They are done.
                    holder.tvStatus.setText("Sent to Manager");
                }
                break;

            case "Completed":
                holder.tvStatus.setText("Returned");
                holder.tvStatus.setTextColor(Color.GRAY);
                break;

            case "Cancelled":
                holder.tvStatus.setText("Cancelled");
                holder.tvStatus.setTextColor(Color.RED);
                break;
        }

        // Standard Button Listeners
        // We set specific listeners inside the switch for "Approve" repurposing,
        // but these are the defaults for Manager actions.
        if (holder.btnApprove.getVisibility() == View.VISIBLE && holder.btnApprove.getText().toString().equals("Approve")) {
            holder.btnApprove.setOnClickListener(v -> listener.onApprove(booking));
        }

        holder.btnCancel.setOnClickListener(v -> listener.onCancel(booking));
        holder.btnReturn.setOnClickListener(v -> listener.onReturn(booking));

        // Whole row click always views details/starts inspection
        holder.itemView.setOnClickListener(v -> listener.onViewDetails(booking));
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