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
    private String userRole;

    public interface BookingActionListener {
        void onApprove(CarRentalData.AdminBookingItem booking);
        void onCancel(CarRentalData.AdminBookingItem booking);
        void onReturn(CarRentalData.AdminBookingItem booking);
        void onViewDetails(CarRentalData.AdminBookingItem booking, String inspectionType);
    }

    public AdminBookingAdapter(Context context, List<CarRentalData.AdminBookingItem> bookings, String userRole, BookingActionListener listener) {
        this.context = context;
        this.bookings = bookings;
        this.userRole = userRole;
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

        holder.btnApprove.setVisibility(View.GONE);
        holder.btnCancel.setVisibility(View.GONE);
        holder.btnReturn.setVisibility(View.GONE);
        holder.tvStatus.setOnClickListener(null);

        switch (booking.status) {
            case "Pending":
                holder.tvStatus.setText("Pending Approval");
                holder.tvStatus.setTextColor(context.getResources().getColor(R.color.rq_orange));
                if (userRole.equals("Manager")) {
                    holder.btnApprove.setVisibility(View.VISIBLE);
                    holder.btnCancel.setVisibility(View.VISIBLE);
                    holder.btnApprove.setText("Approve");
                    holder.btnApprove.setOnClickListener(v -> listener.onApprove(booking));
                }
                break;

            case "Confirmed": // ready for pickup from inspector agent
                holder.tvStatus.setText("Ready for Pickup");
                holder.tvStatus.setTextColor(Color.parseColor("#008000"));

                if (userRole.equals("Inspection Agent")) {
                    holder.btnApprove.setText("Start Pickup");
                    holder.btnApprove.setVisibility(View.VISIBLE);
                    holder.btnApprove.setOnClickListener(v -> listener.onViewDetails(booking, "Pickup"));
                }
                break;

            case "Rented": // THIS IS READY FOR RETURN
                holder.tvStatus.setText("Ready for Return");
                holder.tvStatus.setTextColor(Color.BLUE);

                if (userRole.equals("Inspection Agent")) {
                    holder.btnApprove.setText("Start Return");
                    holder.btnApprove.setVisibility(View.VISIBLE);
                    holder.btnApprove.setOnClickListener(v -> listener.onViewDetails(booking, "Return"));
                } else {
                    holder.tvStatus.setText("On Rental");
                }
                break;

            case "Inspected":
                holder.tvStatus.setText("Inspection Complete");
                holder.tvStatus.setTextColor(Color.parseColor("#800080"));
                if (userRole.equals("Manager")) {
                    holder.btnReturn.setVisibility(View.VISIBLE);
                    holder.btnReturn.setText("Finalize Return");
                    holder.btnReturn.setOnClickListener(v -> listener.onReturn(booking));
                }
                break;

            default:
                holder.tvStatus.setText(booking.status);
                holder.tvStatus.setTextColor(Color.GRAY);
                break;
        }

        holder.btnCancel.setOnClickListener(v -> listener.onCancel(booking));
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