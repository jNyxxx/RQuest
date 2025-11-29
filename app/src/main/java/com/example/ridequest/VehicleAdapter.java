package com.example.ridequest;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.ViewHolder> {
    private List<CarRentalData.VehicleItem> vehicles;
    private Context context;
    private boolean isAdmin;
    private OnDeleteListener deleteListener;

    public interface OnDeleteListener { void onDelete(int id); }

    public VehicleAdapter(Context ctx, List<CarRentalData.VehicleItem> list, boolean admin, OnDeleteListener listener) {
        this.context = ctx; this.vehicles = list; this.isAdmin = admin; this.deleteListener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_vehicle, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CarRentalData.VehicleItem v = vehicles.get(position);

        // Set Text Data
        if (holder.name != null) holder.name.setText(v.title);
        if (holder.type != null) holder.type.setText(v.type);
        if (holder.price != null) holder.price.setText("$" + v.price);

        // Load Image Safely
        if (holder.img != null) {
            int resId = 0;
            if(v.imageRes != null) {
                resId = context.getResources().getIdentifier(v.imageRes, "drawable", context.getPackageName());
            }
            if(resId != 0) holder.img.setImageResource(resId);
            else holder.img.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // CLICK LISTENER Logic for "View Details"
        View.OnClickListener openDetails = view -> {
            try {
                Intent i = new Intent(context, CarDetailActivity.class);
                i.putExtra("VID", v.id);
                i.putExtra("PRICE", v.price);
                i.putExtra("NAME", v.title);
                i.putExtra("IMG_RES", v.imageRes);
                context.startActivity(i);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        if (isAdmin) {
            if (holder.btnDetails != null) holder.btnDetails.setVisibility(View.GONE);
            if (holder.btnDelete != null) {
                holder.btnDelete.setVisibility(View.VISIBLE);
                holder.btnDelete.setOnClickListener(x -> deleteListener.onDelete(v.id));
            }
        } else {
            if (holder.btnDelete != null) holder.btnDelete.setVisibility(View.GONE);

            // Attach click to "View Details" button
            if (holder.btnDetails != null) {
                holder.btnDetails.setVisibility(View.VISIBLE);
                holder.btnDetails.setOnClickListener(openDetails);
            }
            // Also attach click to the whole card for better UX
            holder.itemView.setOnClickListener(openDetails);
        }
    }

    @Override public int getItemCount() { return vehicles.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, type, price;
        ImageView img;
        Button btnDelete, btnDetails;

        public ViewHolder(View v) {
            super(v);
            // Find Views by ID matching item_vehicle.xml
            name = v.findViewById(R.id.tvCarName);
            type = v.findViewById(R.id.tvType);
            price = v.findViewById(R.id.tvPrice);
            img = v.findViewById(R.id.ivCar);
            btnDelete = v.findViewById(R.id.btnDelete);
            btnDetails = v.findViewById(R.id.btnDetails);
        }
    }
}