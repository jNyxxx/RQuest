package com.example.ridequest;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class InspectionHistoryAdapter extends RecyclerView.Adapter<InspectionHistoryAdapter.ViewHolder> {

    private Context context;
    private List<CarRentalData.InspectionLogItem> list;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CarRentalData.InspectionLogItem item);
    }

    public InspectionHistoryAdapter(Context context, List<CarRentalData.InspectionLogItem> list, OnItemClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_admin_booking, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CarRentalData.InspectionLogItem item = list.get(position);

        holder.tvRef.setText("ID: " + item.inspectionId);
        holder.tvCar.setText(item.carName);
        holder.tvCustomer.setText(item.type + " Inspection");
        holder.tvDates.setText("Date: " + item.date);

        // Hide unused buttons
        holder.btnApprove.setVisibility(View.GONE);
        holder.btnCancel.setVisibility(View.GONE);
        holder.btnReturn.setVisibility(View.GONE);

        // Style the "Status" to look like a link/button
        holder.tvStatus.setText("View Report >");
        holder.tvStatus.setTextColor(Color.parseColor("#E65100")); // RQ Orange
        holder.tvStatus.setBackground(null);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRef, tvCar, tvCustomer, tvDates, tvStatus;
        View btnApprove, btnReturn, btnCancel;

        public ViewHolder(View v) {
            super(v);
            tvRef = v.findViewById(R.id.tvBookingId);
            tvCar = v.findViewById(R.id.tvCar);
            tvCustomer = v.findViewById(R.id.tvCustomer);
            tvDates = v.findViewById(R.id.tvDates);
            tvStatus = v.findViewById(R.id.tvStatus);
            btnApprove = v.findViewById(R.id.btnApprove);
            btnCancel = v.findViewById(R.id.btnCancel);
            btnReturn = v.findViewById(R.id.btnReturn);
        }
    }
}