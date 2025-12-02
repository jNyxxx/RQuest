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
        public String address;

        public LocationItem(int id, String name, String address) {
            this.id = id;
            this.name = name;
            this.address = address;
        }
    }

    public List<LocationItem> getAllLocations() {
        List<LocationItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try {
            Cursor c = db.rawQuery("SELECT location_id, location_name, address FROM Location", null);
            if (c.moveToFirst()) {
                do {
                    list.add(new LocationItem(c.getInt(0), c.getString(1), c.getString(2)));
                } while (c.moveToNext());
            }
            c.close();
        } catch (Exception e) {
            Log.e(TAG, "Error fetching locations: " + e.getMessage(), e);
        }
        db.close();
        return list;
    }

    public String getLocationAddress(int locationId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String address = "Unknown Location";
        try {
            Cursor c = db.rawQuery("SELECT address FROM Location WHERE location_id=?",
                    new String[]{String.valueOf(locationId)});
            if(c.moveToFirst()) {
                address = c.getString(0);
            }
            c.close();
        } catch(Exception e) {
            Log.e(TAG, "Error fetching location address: " + e.getMessage(), e);
        }
        db.close();
        return address;
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

    // --- CREATE PENDING BOOKING ---
    public boolean createPendingBooking(int uid, int vid, String carName,
                                        String pickupDate, String returnDate,
                                        String pickupTime, String returnTime,
                                        String pickupAddress, String returnAddress,
                                        double totalCost, String paymentMethod,
                                        String bookingId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            ContentValues r = new ContentValues();
            r.put("customer_num", uid);
            r.put("vehicle_id", vid);
            r.put("pickup_date", pickupDate);
            r.put("return_date", returnDate);
            r.put("pickup_time", pickupTime);
            r.put("return_time", returnTime);
            r.put("status", "Pending");
            r.put("total_cost", totalCost);
            r.put("booking_reference", bookingId);
            r.put("pickup_address", pickupAddress);
            r.put("return_address", returnAddress);
            r.put("payment_method", paymentMethod);

            long bid = db.insert("Reservation", null, r);
            if(bid == -1) throw new Exception("Failed to insert reservation");

            db.setTransactionSuccessful();
            Log.d(TAG, "✓ Pending booking created: " + bookingId);
            return true;

        } catch(Exception ex) {
            Log.e(TAG, "✗ Pending booking failed: " + ex.getMessage(), ex);
            return false;
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    // --- APPROVE BOOKING (ADMIN ACTION) ---
    public boolean approveBooking(int bookingId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("status", "Confirmed");
        int rows = db.update("Reservation", v, "booking_id=?", new String[]{String.valueOf(bookingId)});
        db.close();

        Log.d(TAG, rows > 0 ? "✓ Booking approved: " + bookingId : "✗ Approval failed");
        return rows > 0;
    }

    // --- CANCEL BOOKING (ADMIN ACTION) ---
    public boolean cancelBooking(int bookingId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("status", "Cancelled");
        int rows = db.update("Reservation", v, "booking_id=?", new String[]{String.valueOf(bookingId)});
        db.close();

        Log.d(TAG, rows > 0 ? "✓ Booking cancelled: " + bookingId : "✗ Cancellation failed");
        return rows > 0;
    }

    // --- ADMIN BOOKING ITEM ---
    public static class AdminBookingItem {
        public int id;
        public String bookingReference;
        public String customerName;
        public String customerEmail;
        public String carName;
        public String pickupDate;
        public String pickupTime;
        public String pickupAddress;
        public String returnDate;
        public String returnTime;
        public String returnAddress;
        public String status;
        public String paymentMethod;
        public double totalCost;

        public AdminBookingItem(int id, String ref, String custName, String custEmail,
                                String car, String pDate, String pTime, String pAddr,
                                String rDate, String rTime, String rAddr,
                                String stat, String payment, double cost) {
            this.id = id;
            this.bookingReference = ref;
            this.customerName = custName;
            this.customerEmail = custEmail;
            this.carName = car;
            this.pickupDate = pDate;
            this.pickupTime = pTime;
            this.pickupAddress = pAddr;
            this.returnDate = rDate;
            this.returnTime = rTime;
            this.returnAddress = rAddr;
            this.status = stat;
            this.paymentMethod = payment;
            this.totalCost = cost;
        }
    }

    // --- GET ALL BOOKINGS FOR ADMIN ---
    public List<AdminBookingItem> getAllBookingsForAdmin() {
        List<AdminBookingItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT r.booking_id, r.booking_reference, " +
                "c.first_name || ' ' || c.last_name as customer_name, c.email, " +
                "mk.make_name || ' ' || vm.model_name as car_name, " +
                "r.pickup_date, r.pickup_time, r.pickup_address, " +
                "r.return_date, r.return_time, r.return_address, " +
                "r.status, r.payment_method, r.total_cost " +
                "FROM Reservation r " +
                "JOIN Customer c ON r.customer_num = c.customer_id " +
                "JOIN Vehicle v ON r.vehicle_id = v.vehicle_id " +
                "JOIN VehicleModel vm ON v.model_id = vm.model_id " +
                "JOIN Make mk ON vm.make_id = mk.make_id " +
                "ORDER BY r.booking_id DESC";

        try {
            Cursor c = db.rawQuery(query, null);
            if(c.moveToFirst()) {
                do {
                    list.add(new AdminBookingItem(
                            c.getInt(0),      // booking_id
                            c.getString(1),   // booking_reference
                            c.getString(2),   // customer_name
                            c.getString(3),   // email
                            c.getString(4),   // car_name
                            c.getString(5),   // pickup_date
                            c.getString(6),   // pickup_time
                            c.getString(7),   // pickup_address
                            c.getString(8),   // return_date
                            c.getString(9),   // return_time
                            c.getString(10),  // return_address
                            c.getString(11),  // status
                            c.getString(12),  // payment_method
                            c.getDouble(13)   // total_cost
                    ));
                } while(c.moveToNext());
            }
            c.close();
        } catch(Exception e) {
            Log.e(TAG, "Error fetching admin bookings: " + e.getMessage(), e);
        }
        db.close();
        return list;
    }

    // --- BOOKING ITEM (Simple version for non-admin) ---
    public static class BookingItem {
        public int id;
        public String customerName;
        public String carName;
        public String dates;
        public String status;
        public double total;

        public BookingItem(int id, String custName, String car, String dates, String status, double total) {
            this.id = id;
            this.customerName = custName;
            this.carName = car;
            this.dates = dates;
            this.status = status;
            this.total = total;
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