package com.example.ridequest;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MaintenanceLogAdapter extends RecyclerView.Adapter<MaintenanceLogAdapter.ViewHolder> {

    private Context context;
    private List<CarRentalData.MaintenanceLogItem> logs;

    public MaintenanceLogAdapter(List<CarRentalData.MaintenanceLogItem> logs) {
        this.logs = logs;
    }

    // Constructor with Context for Intent navigation
    public MaintenanceLogAdapter(Context context, List<CarRentalData.MaintenanceLogItem> logs) {
        this.context = context;
        this.logs = logs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context == null) {
            context = parent.getContext();
        }
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_maintenance_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CarRentalData.MaintenanceLogItem log = logs.get(position);

        holder.tvLogId.setText(String.valueOf(log.id));
        holder.tvCarName.setText(log.carName);
        holder.tvDescription.setText(log.description);
        holder.tvMechanic.setText(log.mechanic);
        holder.tvDate.setText("Date: " + log.date);
        holder.tvCost.setText(String.format("₱%.2f", log.cost));

        // Click to view details
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MaintenanceDetailsActivity.class);
            intent.putExtra("ID", log.id);
            intent.putExtra("CAR_NAME", log.carName);
            intent.putExtra("MECHANIC", log.mechanic);
            intent.putExtra("DATE", log.date);
            intent.putExtra("COST", log.cost);
            intent.putExtra("DESCRIPTION", log.description);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLogId, tvCarName, tvDescription, tvMechanic, tvDate, tvCost;

        ViewHolder(View itemView) {
            super(itemView);
            tvLogId = itemView.findViewById(R.id.tvLogId);
            tvCarName = itemView.findViewById(R.id.tvCarName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvMechanic = itemView.findViewById(R.id.tvMechanic);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvCost = itemView.findViewById(R.id.tvCost);
        }
    }
}