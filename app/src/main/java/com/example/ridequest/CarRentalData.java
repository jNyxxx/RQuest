package com.example.ridequest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class CarRentalData {
    private static final String TAG = "CarRentalData";
    private DatabaseHelper dbHelper;

    public CarRentalData(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // --- ADMIN: ADD VEHICLE ---
    public boolean addNewCarComplete(String make, String model, String type, int year, double price, String plate, String imageRes) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long makeId = -1, typeId = -1, modelId = -1;

        db.beginTransaction();
        try {
            Cursor cMake = db.rawQuery("SELECT make_id FROM Make WHERE make_name=?", new String[]{make});
            if (cMake.moveToFirst()) makeId = cMake.getInt(0);
            else {
                ContentValues v = new ContentValues();
                v.put("make_name", make);
                makeId = db.insert("Make", null, v);
            }
            cMake.close();

            Cursor cType = db.rawQuery("SELECT type_id FROM Type WHERE type_name=?", new String[]{type});
            if (cType.moveToFirst()) typeId = cType.getInt(0);
            else {
                ContentValues v = new ContentValues();
                v.put("type_name", type);
                typeId = db.insert("Type", null, v);
            }
            cType.close();

            ContentValues vModel = new ContentValues();
            vModel.put("make_id", makeId);
            vModel.put("type_id", typeId);
            vModel.put("model_name", model);
            vModel.put("year", year);
            vModel.put("daily_rate", price);
            modelId = db.insert("VehicleModel", null, vModel);

            ContentValues vCar = new ContentValues();
            vCar.put("model_id", modelId);
            vCar.put("location_id", 1);
            vCar.put("plt_number", plate);
            vCar.put("status", "Available");
            vCar.put("image_res_name", imageRes);
            db.insert("Vehicle", null, vCar);

            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error adding vehicle: " + e.getMessage(), e);
            return false;
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    // --- FETCH CARS ---
    public static class VehicleItem {
        public int id;
        public String title, type, status, imageRes;
        public double price;

        public VehicleItem(int id, String t, String ty, double p, String s, String img) {
            this.id = id;
            title = t;
            type = ty;
            price = p;
            status = s;
            imageRes = img;
        }
    }

    public List<VehicleItem> getAllVehicles() {
        List<VehicleItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT v.vehicle_id, mk.make_name, vm.model_name, t.type_name, vm.daily_rate, v.status, v.image_res_name " +
                "FROM Vehicle v " +
                "JOIN VehicleModel vm ON v.model_id = vm.model_id " +
                "JOIN Make mk ON vm.make_id = mk.make_id " +
                "JOIN Type t ON vm.type_id = t.type_id";
        try {
            Cursor c = db.rawQuery(query, null);
            if (c.moveToFirst()) {
                do {
                    String title = c.getString(1) + " " + c.getString(2);
                    list.add(new VehicleItem(
                            c.getInt(0),
                            title,
                            c.getString(3),
                            c.getDouble(4),
                            c.getString(5),
                            c.getString(6)
                    ));
                } while (c.moveToNext());
            }
            c.close();
        } catch (Exception e) {
            Log.e(TAG, "Error fetching vehicles: " + e.getMessage(), e);
        }
        db.close();
        return list;
    }

    // --- LOCATIONS ---
    public static class LocationItem {
        public int id;
        public String name;

        public LocationItem(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public List<LocationItem> getAllLocations() {
        List<LocationItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try {
            Cursor c = db.rawQuery("SELECT location_id, location_name FROM Location", null);
            if (c.moveToFirst()) {
                do {
                    list.add(new LocationItem(c.getInt(0), c.getString(1)));
                } while (c.moveToNext());
            }
            c.close();
        } catch (Exception e) {
            Log.e(TAG, "Error fetching locations: " + e.getMessage(), e);
        }
        db.close();
        return list;
    }

    // --- AUTH ---
    public int loginCustomer(String email, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT customer_id FROM Customer WHERE email=? AND password=?",
                new String[]{email, password});
        int id = -1;
        if(c.moveToFirst()) id = c.getInt(0);
        c.close();
        db.close();
        return id;
    }

    public boolean registerCustomer(String f, String l, String e, String p, String ph) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("first_name", f);
        v.put("last_name", l);
        v.put("email", e);
        v.put("password", p);
        v.put("phone", ph);
        long res = db.insert("Customer", null, v);
        db.close();
        return res != -1;
    }

    public boolean checkAdmin(String email, String password) {
        return email.equals("admin") && password.equals("admin123");
    }

    public void deleteVehicle(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("Vehicle", "vehicle_id=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // --- PROFILE ---
    public static class Customer {
        public int id;
        public String firstName, lastName, email, phone;

        public Customer(int id, String f, String l, String e, String p){
            this.id = id;
            firstName = f;
            lastName = l;
            email = e;
            phone = p;
        }
    }

    public Customer getCustomer(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM Customer WHERE customer_id=?",
                new String[]{String.valueOf(id)});
        Customer cust = null;
        if(c.moveToFirst()) {
            cust = new Customer(
                    c.getInt(0),
                    c.getString(1),
                    c.getString(2),
                    c.getString(3),
                    c.getString(5)
            );
        }
        c.close();
        db.close();
        return cust;
    }

    public boolean updateCustomer(int id, String f, String l, String e, String p) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("first_name", f);
        v.put("last_name", l);
        v.put("email", e);
        v.put("phone", p);
        int rows = db.update("Customer", v, "customer_id=?", new String[]{String.valueOf(id)});
        db.close();
        return rows > 0;
    }

    // --- PAYMENT & BOOKING (UPDATED WITH NEW PARAMETERS) ---
    public boolean processPaymentAndBooking(int uid, int vid,
                                            String pickupDate, String returnDate,
                                            String pickupTime, String returnTime,
                                            int pickupLocId, int returnLocId,
                                            double totalCost, String paymentMethod) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        Log.d(TAG, "Processing booking:");
        Log.d(TAG, "  Customer ID: " + uid);
        Log.d(TAG, "  Vehicle ID: " + vid);
        Log.d(TAG, "  Pickup: " + pickupDate + " " + pickupTime);
        Log.d(TAG, "  Return: " + returnDate + " " + returnTime);
        Log.d(TAG, "  Locations: " + pickupLocId + " -> " + returnLocId);
        Log.d(TAG, "  Total Cost: $" + totalCost);
        Log.d(TAG, "  Payment: " + paymentMethod);

        try {
            // Insert Reservation with all fields
            ContentValues r = new ContentValues();
            r.put("customer_num", uid);
            r.put("vehicle_id", vid);
            r.put("pickup_date", pickupDate);
            r.put("return_date", returnDate);
            r.put("pickup_time", pickupTime);
            r.put("return_time", returnTime);
            r.put("pickup_loc_id", pickupLocId);
            r.put("return_loc_id", returnLocId);
            r.put("status", "Confirmed");
            r.put("total_cost", totalCost);

            long bid = db.insert("Reservation", null, r);
            if(bid == -1) throw new Exception("Failed to insert reservation");
            Log.d(TAG, "✓ Reservation created: ID " + bid);

            // Insert Rental
            ContentValues ren = new ContentValues();
            ren.put("reservation_id", bid);
            ren.put("total_amount", totalCost);
            long rid = db.insert("Rental", null, ren);
            if(rid == -1) throw new Exception("Failed to insert rental");
            Log.d(TAG, "✓ Rental created: ID " + rid);

            // Insert Payment
            ContentValues p = new ContentValues();
            p.put("rental_id", rid);
            p.put("payment_date", "NOW");
            p.put("amount", totalCost);
            p.put("payment_mthd", paymentMethod);
            long pid = db.insert("Payment", null, p);
            if(pid == -1) throw new Exception("Failed to insert payment");
            Log.d(TAG, "✓ Payment created: ID " + pid);

            // Update Vehicle status
            ContentValues v = new ContentValues();
            v.put("status", "Rented");
            int updated = db.update("Vehicle", v, "vehicle_id=?", new String[]{String.valueOf(vid)});
            if(updated == 0) throw new Exception("Failed to update vehicle status");
            Log.d(TAG, "✓ Vehicle status updated to Rented");

            db.setTransactionSuccessful();
            Log.d(TAG, "✓✓✓ Booking completed successfully!");
            return true;

        } catch(Exception ex) {
            Log.e(TAG, "✗✗✗ Booking failed: " + ex.getMessage(), ex);
            return false;
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    // --- BOOKINGS ---
    public static class BookingItem {
        public int id;
        public String customerName, carName, dates, status;
        public double total;

        public BookingItem(int id, String c, String v, String d, String s, double t){
            this.id = id;
            customerName = c;
            carName = v;
            dates = d;
            status = s;
            total = t;
        }
    }

    public List<BookingItem> getAllBookings() {
        List<BookingItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT r.booking_id, c.last_name, mk.make_name, vm.model_name, " +
                "r.pickup_date, r.return_date, r.status, r.total_cost " +
                "FROM Reservation r " +
                "JOIN Customer c ON r.customer_num = c.customer_id " +
                "JOIN Vehicle v ON r.vehicle_id = v.vehicle_id " +
                "JOIN VehicleModel vm ON v.model_id = vm.model_id " +
                "JOIN Make mk ON vm.make_id = mk.make_id";
        try {
            Cursor c = db.rawQuery(query, null);
            if(c.moveToFirst()) {
                do {
                    String car = c.getString(2) + " " + c.getString(3);
                    String dates = c.getString(4) + " to " + c.getString(5);
                    list.add(new BookingItem(
                            c.getInt(0),
                            c.getString(1),
                            car,
                            dates,
                            c.getString(6),
                            c.getDouble(7)
                    ));
                } while(c.moveToNext());
            }
            c.close();
        } catch(Exception e) {
            Log.e(TAG, "Error fetching bookings: " + e.getMessage(), e);
        }
        db.close();
        return list;
    }
}