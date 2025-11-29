package com.example.ridequest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {
    private List<CarRentalData.BookingItem> bookings;

    public BookingAdapter(List<CarRentalData.BookingItem> bookings) {
        this.bookings = bookings;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CarRentalData.BookingItem b = bookings.get(position);
        holder.text1.setText("Booking #" + b.id + " • " + b.customerName + " (" + b.status + ")");
        holder.text2.setText(b.carName + "\n" + b.dates + " • ₱" + b.total);
    }

    @Override public int getItemCount() { return bookings.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView text1, text2;
        public ViewHolder(View v) {
            super(v);
            text1 = v.findViewById(android.R.id.text1);
            text2 = v.findViewById(android.R.id.text2);
        }
    }
}