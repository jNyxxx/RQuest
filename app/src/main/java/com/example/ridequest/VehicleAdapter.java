package com.example.ridequest;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color; // Import for colors
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
    private OnEditListener editListener;

    public interface OnDeleteListener {
        void onDelete(int id);
    }

    public interface OnEditListener {
        void onEdit(CarRentalData.VehicleItem vehicle);
    }

    public VehicleAdapter(Context ctx, List<CarRentalData.VehicleItem> list, boolean admin,
                          OnDeleteListener delListener, OnEditListener editListener) {
        this.context = ctx;
        this.vehicles = list;
        this.isAdmin = admin;
        this.deleteListener = delListener;
        this.editListener = editListener;
    }

    public VehicleAdapter(Context ctx, List<CarRentalData.VehicleItem> list, boolean admin, OnDeleteListener listener) {
        this(ctx, list, admin, listener, null);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_vehicle, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CarRentalData.VehicleItem v = vehicles.get(position);

        if (holder.name != null) holder.name.setText(v.title);
        if (holder.type != null) holder.type.setText(v.type);
        if (holder.price != null) holder.price.setText("$" + v.price);

        if (holder.status != null) {
            holder.status.setText(v.status);

            if (v.status != null && v.status.equalsIgnoreCase("Rented")) {
                // RENTED
                holder.status.setTextColor(Color.RED);

                if (!isAdmin) {
                    holder.btnDetails.setText("Rented");
                    holder.btnDetails.setEnabled(false);
                    holder.btnDetails.setBackgroundColor(Color.GRAY);
                }
            } else if (v.status != null && v.status.equalsIgnoreCase("Pending")) {
                // PENDING
                holder.status.setTextColor(Color.parseColor("#FFA500")); // Orange

                if (!isAdmin) {
                    holder.btnDetails.setText("Pending");
                    holder.btnDetails.setEnabled(false);
                    holder.btnDetails.setBackgroundColor(Color.GRAY);
                }
            } else {
                // AVAILABLE
                holder.status.setTextColor(Color.parseColor("#4CAF50")); // Green

                if (!isAdmin) {
                    holder.btnDetails.setText("View Details");
                    holder.btnDetails.setEnabled(true);
                    holder.btnDetails.setBackgroundColor(context.getResources().getColor(R.color.rq_orange));
                }
            }
        }

        // Load Image
        if (holder.img != null) {
            if(v.imageRes != null && !v.imageRes.isEmpty()) {
                try {
                    byte[] decodedBytes = android.util.Base64.decode(v.imageRes, android.util.Base64.DEFAULT);
                    android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    if (bitmap != null) {
                        holder.img.setImageBitmap(bitmap);
                    } else {
                        int resId = context.getResources().getIdentifier(v.imageRes, "drawable", context.getPackageName());
                        if(resId != 0) holder.img.setImageResource(resId);
                        else holder.img.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                } catch (Exception e) {
                    int resId = context.getResources().getIdentifier(v.imageRes, "drawable", context.getPackageName());
                    if(resId != 0) holder.img.setImageResource(resId);
                    else holder.img.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            } else {
                holder.img.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }

        // Customer: Open Details Logic
        View.OnClickListener openDetails = view -> {
            // Prevent opening details if rented or pending
            if (!isAdmin && v.status != null) {
                if (v.status.equalsIgnoreCase("Rented")) {
                    Toast.makeText(context, "This vehicle is currently rented.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (v.status.equalsIgnoreCase("Pending")) {
                    Toast.makeText(context, "This vehicle is pending approval.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            try {
                Intent i = new Intent(context, CarDetailActivity.class);
                i.putExtra("VID", v.id);
                i.putExtra("PRICE", v.price);
                i.putExtra("NAME", v.title);
                i.putExtra("IMG_RES", v.imageRes);
                i.putExtra("TRANSMISSION", v.transmission);
                i.putExtra("SEATS", v.seats);

                // PASS NEW DATA (Customer View)
                i.putExtra("COLOR", v.color);
                i.putExtra("CATEGORY", v.category);
                i.putExtra("FUEL", v.fuelType);
                i.putExtra("PLATE", v.plate);
                i.putExtra("TYPE", v.type); // Body Type

                context.startActivity(i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        if (isAdmin) {
            if (holder.btnDetails != null) holder.btnDetails.setVisibility(View.GONE);

            // Admin: Edit Logic
            if (holder.btnEdit != null) {
                holder.btnEdit.setVisibility(View.VISIBLE);
                holder.btnEdit.setOnClickListener(x -> {
                    // We can use the interface callback if available, OR start intent directly.
                    // Since we updated the Intent logic, we'll do it here directly to ensure params are passed.

                    Intent intent = new Intent(context, EditVehicleActivity.class);
                    intent.putExtra("VEHICLE_ID", v.id);
                    intent.putExtra("MAKE_MODEL", v.title);
                    intent.putExtra("TYPE", v.type);
                    intent.putExtra("PRICE", v.price);
                    intent.putExtra("IMAGE_RES", v.imageRes);
                    intent.putExtra("TRANSMISSION", v.transmission);
                    intent.putExtra("SEATS", v.seats);

                    // ⭐ PASS NEW DATA (Admin Edit View)
                    intent.putExtra("COLOR", v.color);
                    intent.putExtra("CATEGORY", v.category);
                    intent.putExtra("FUEL", v.fuelType);

                    context.startActivity(intent);
                });
            }

            // Admin: Delete Logic
            if (holder.btnDelete != null) {
                holder.btnDelete.setVisibility(View.VISIBLE);
                holder.btnDelete.setOnClickListener(x -> {
                    if (deleteListener != null) deleteListener.onDelete(v.id);
                });
            }
        } else {
            // Customer View
            if (holder.btnDelete != null) holder.btnDelete.setVisibility(View.GONE);
            if (holder.btnEdit != null) holder.btnEdit.setVisibility(View.GONE);

            if (holder.btnDetails != null) {
                holder.btnDetails.setVisibility(View.VISIBLE);
                holder.btnDetails.setOnClickListener(openDetails);
            }
            holder.itemView.setOnClickListener(openDetails);
        }
    }

    @Override
    public int getItemCount() {
        return vehicles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, type, price, status;
        ImageView img;
        Button btnDelete, btnDetails, btnEdit;

        public ViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.tvCarName);
            type = v.findViewById(R.id.tvType);
            price = v.findViewById(R.id.tvPrice);
            status = v.findViewById(R.id.tvStatus);

            img = v.findViewById(R.id.ivCar);
            btnDelete = v.findViewById(R.id.btnDelete);
            btnDetails = v.findViewById(R.id.btnDetails);
            btnEdit = v.findViewById(R.id.btnEdit);
        }
    }
}