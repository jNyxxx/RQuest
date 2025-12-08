package com.example.ridequest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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

    private CarRentalData dbHelper;

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
        this.dbHelper = new CarRentalData(ctx);
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
        if (holder.price != null) holder.price.setText("$" + v.price + "/day");

        // --- STATUS LOGIC ---
        if (holder.status != null) {
            holder.status.setText(v.status);
            if (v.status != null && v.status.equalsIgnoreCase("Rented")) {
                holder.status.setTextColor(Color.RED);
                if (!isAdmin) {
                    holder.btnDetails.setText("Rented");
                    holder.btnDetails.setEnabled(false);
                    holder.btnDetails.setBackgroundColor(Color.GRAY);
                }
            } else if (v.status != null && v.status.equalsIgnoreCase("Pending")) {
                holder.status.setTextColor(Color.parseColor("#FFA500"));
                if (!isAdmin) {
                    holder.btnDetails.setText("Pending");
                    holder.btnDetails.setEnabled(false);
                    holder.btnDetails.setBackgroundColor(Color.GRAY);
                }
            } else {
                holder.status.setTextColor(Color.parseColor("#4CAF50"));
                if (!isAdmin) {
                    holder.btnDetails.setText("View Details");
                    holder.btnDetails.setEnabled(true);
                    holder.btnDetails.setBackgroundColor(context.getResources().getColor(R.color.rq_orange));
                }
            }
        }

        // --- IMAGE LOADING ---
        if (holder.img != null) {
            if(v.imageRes != null && !v.imageRes.isEmpty()) {
                try {
                    if (v.imageRes.length() > 20) {
                        byte[] decodedBytes = android.util.Base64.decode(v.imageRes, android.util.Base64.DEFAULT);
                        android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                        if (bitmap != null) holder.img.setImageBitmap(bitmap);
                    } else {
                        int resId = context.getResources().getIdentifier(v.imageRes, "drawable", context.getPackageName());
                        if(resId != 0) holder.img.setImageResource(resId);
                        else holder.img.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                } catch (Exception e) {
                    holder.img.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            } else {
                holder.img.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }

        // --- FAVORITE LOGIC ---
        int currentUserId = getUserId();

        if (!isAdmin && currentUserId != -1) {
            holder.btnFavorite.setVisibility(View.VISIBLE);

            // FIX: Changed method name to match your CarRentalData code (isVehicleFavorite)
            boolean isFav = dbHelper.isVehicleFavorite(currentUserId, v.id);
            updateFavoriteIcon(holder.btnFavorite, isFav);

            holder.btnFavorite.setOnClickListener(view -> {
                boolean isNowFav = dbHelper.toggleFavorite(currentUserId, v.id);
                updateFavoriteIcon(holder.btnFavorite, isNowFav);

                String msg = isNowFav ? "Added to Favorites" : "Removed from Favorites";
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            });
        } else {
            holder.btnFavorite.setVisibility(View.GONE);
        }

        // --- CLICK LISTENERS ---
        View.OnClickListener openDetails = view -> {
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
                i.putExtra("VEHICLE_ID", v.id);
                i.putExtra("PRICE", v.price);
                i.putExtra("NAME", v.title);
                i.putExtra("IMAGE", v.imageRes);
                // Add any other fields you need for details
                context.startActivity(i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        if (isAdmin) {
            if (holder.btnDetails != null) holder.btnDetails.setVisibility(View.GONE);

            if (holder.btnEdit != null) {
                holder.btnEdit.setVisibility(View.VISIBLE);
                holder.btnEdit.setOnClickListener(x -> {
                    Intent intent = new Intent(context, EditVehicleActivity.class);
                    intent.putExtra("VEHICLE_ID", v.id);
                    context.startActivity(intent);
                });
            }
            if (holder.btnDelete != null) {
                holder.btnDelete.setVisibility(View.VISIBLE);
                holder.btnDelete.setOnClickListener(x -> {
                    if (deleteListener != null) deleteListener.onDelete(v.id);
                });
            }
        } else {
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

    private void updateFavoriteIcon(ImageView view, boolean isFavorite) {
        // Switch between your custom outline/filled icons
        if (isFavorite) {
            view.setImageResource(R.drawable.ic_star_filled);
        } else {
            view.setImageResource(R.drawable.ic_star_outline);
        }
        view.clearColorFilter();
    }

    private int getUserId() {
        SharedPreferences prefs = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        return prefs.getInt("UID", -1);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, type, price, status;
        ImageView img, btnFavorite;
        Button btnDelete, btnDetails, btnEdit;

        public ViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.tvCarName);
            type = v.findViewById(R.id.tvType);
            price = v.findViewById(R.id.tvPrice);
            status = v.findViewById(R.id.tvStatus);
            img = v.findViewById(R.id.ivCar);

            btnFavorite = v.findViewById(R.id.btnFavorite); // Ensure this ID is in XML

            btnDelete = v.findViewById(R.id.btnDelete);
            btnDetails = v.findViewById(R.id.btnDetails);
            btnEdit = v.findViewById(R.id.btnEdit);
        }
    }
}