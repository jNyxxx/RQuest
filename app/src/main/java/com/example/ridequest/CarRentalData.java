package com.example.ridequest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CarRentalData {
    private static final String TAG = "CarRentalData";
    private DatabaseHelper dbHelper;

    public CarRentalData(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // ===================== Security & Account Rules =====================

     //vValidates password meets minimum security standards (8+ characters)

    public static boolean isPasswordValid(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) hasLetter = true;
            if (Character.isDigit(c)) hasDigit = true;
        }
        return hasLetter && hasDigit;
    }


     // validates customer is at least 18 years old

    public static boolean isAgeValid(String dateOfBirth) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date birthDate = sdf.parse(dateOfBirth);
            if (birthDate == null) return false;

            Calendar today = Calendar.getInstance();
            Calendar birth = Calendar.getInstance();
            birth.setTime(birthDate);

            int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
            if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }

            return age >= 18;
        } catch (ParseException e) {
            return false;
        }
    }

    // ===================== AUTHENTICATION METHODS =====================

    public int loginCustomer(String email, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = null;
        int id = -1;

        try {
            Log.d(TAG, "Attempting login for email: " + email);

            c = db.rawQuery("SELECT customer_id, email, password FROM Customer WHERE email=? AND password=?",
                    new String[]{email, password});

            if (c.moveToFirst()) {
                id = c.getInt(0);
                Log.d(TAG, "✓ Login successful! Customer ID: " + id);
            } else {
                Log.e(TAG, "✗ Login failed - no matching email/password");

                // checks if email exists
                Cursor cDebug = db.rawQuery("SELECT customer_id, email FROM Customer WHERE email=?",
                        new String[]{email});
                if (cDebug.moveToFirst()) {
                    Log.d(TAG, "Email exists but password mismatch");
                } else {
                    Log.d(TAG, "Email does not exist in database");
                }
                cDebug.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Login error: " + e.getMessage(), e);
        } finally {
            if (c != null) c.close();
            db.close();
        }

        return id;
    }

      // hard coded admin credentials
    public boolean checkAdmin(String email, String password) {
        return email.equals("admin") && password.equals("admin123");
    }

    public boolean registerCustomer(String firstName, String lastName, String email,
                                    String password, String phone, String dateOfBirth,
                                    String driversLicense, String address) {

        Log.d(TAG, "=== Registration Request ===");
        Log.d(TAG, "Email: " + email);
        Log.d(TAG, "Phone: " + phone);
        Log.d(TAG, "DOB: " + dateOfBirth);

        // Validate age (18+)
        if (!isAgeValid(dateOfBirth)) {
            Log.e(TAG, "Registration failed: Age < 18");
            return false;
        }

        // Validate password security
        if (!isPasswordValid(password)) {
            Log.e(TAG, "Registration failed: Weak password");
            return false;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            // Check for duplicate email or phone
            Cursor c = db.rawQuery("SELECT customer_id FROM Customer WHERE email=? OR phone=?",
                    new String[]{email, phone});

            if (c.getCount() > 0) {
                c.close();
                Log.e(TAG, "Registration failed: Email or phone already exists");
                return false;
            }
            c.close();

            // inserts new customer
            ContentValues v = new ContentValues();
            v.put("first_name", firstName);
            v.put("last_name", lastName);
            v.put("email", email);
            v.put("password", password);
            v.put("phone", phone);
            v.put("date_of_birth", dateOfBirth);
            v.put("drivers_license", driversLicense);
            v.put("address", address);
            v.put("license_verified", 0);

            long res = db.insert("Customer", null, v);

            if (res != -1) {
                Log.d(TAG, "✓ Registration successful! Customer ID: " + res);

                // verify insertion
                Cursor cVerify = db.rawQuery("SELECT customer_id, email FROM Customer WHERE customer_id=?",
                        new String[]{String.valueOf(res)});
                if (cVerify.moveToFirst()) {
                    Log.d(TAG, "✓ Verified in database: " + cVerify.getString(1));
                }
                cVerify.close();

                return true;
            } else {
                Log.e(TAG, "✗ Registration failed: Database insert error");
                return false;
            }

        } catch (Exception e) {
            Log.e(TAG, "Registration error: " + e.getMessage(), e);
            return false;
        } finally {
            db.close();
        }
    }

    public void debugListAllCustomers() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT customer_id, email, first_name, last_name FROM Customer", null);

        Log.d(TAG, "=== All Customers in Database ===");
        if (c.moveToFirst()) {
            do {
                Log.d(TAG, "ID: " + c.getInt(0) + " | Email: " + c.getString(1) +
                        " | Name: " + c.getString(2) + " " + c.getString(3));
            } while (c.moveToNext());
        } else {
            Log.d(TAG, "No customers found");
        }
        c.close();
        db.close();
    }

    // ===================== Car Availability & Management =====================

    public boolean isCarAvailable(int vehicleId, String pickupDate, String returnDate) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cStatus = db.rawQuery("SELECT status FROM Vehicle WHERE vehicle_id=?",
                new String[]{String.valueOf(vehicleId)});
        if (!cStatus.moveToFirst() || !cStatus.getString(0).equals("Available")) {
            cStatus.close();
            db.close();
            return false;
        }
        cStatus.close();

        String query = "SELECT COUNT(*) FROM Reservation WHERE vehicle_id=? " +
                "AND status IN ('Confirmed', 'Pending') " +
                "AND NOT (return_date < ? OR pickup_date > ?)";

        Cursor c = db.rawQuery(query, new String[]{
                String.valueOf(vehicleId), pickupDate, returnDate
        });

        boolean available = true;
        if (c.moveToFirst()) {
            available = c.getInt(0) == 0;
        }

        c.close();
        db.close();
        return available;
    }

    // ===================== Booking & Reservation =====================

    private String generateBookingId() {
        return "RQ" + System.currentTimeMillis();
    }

    public boolean createPendingBooking(int customerId, int vehicleId, String carName,
                                        String pickupDate, String returnDate,
                                        String pickupTime, String returnTime,
                                        String pickupAddress, String returnAddress,
                                        double totalCost, String paymentMethod,
                                        String paymentId, int pickupLocId, int returnLocId) {

        if (paymentMethod == null || paymentMethod.isEmpty()) {
            Log.e(TAG, "Payment method required before booking");
            return false;
        }

        if (!isCarAvailable(vehicleId, pickupDate, returnDate)) {
            Log.e(TAG, "Car not available for selected dates");
            return false;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            String bookingRef = generateBookingId();

            ContentValues r = new ContentValues();
            r.put("customer_num", customerId);
            r.put("vehicle_id", vehicleId);
            r.put("pickup_date", pickupDate);
            r.put("return_date", returnDate);
            r.put("pickup_time", pickupTime);
            r.put("return_time", returnTime);
            r.put("pickup_loc_id", pickupLocId);
            r.put("return_loc_id", returnLocId);
            r.put("pickup_address", pickupAddress);
            r.put("return_address", returnAddress);
            r.put("status", "Pending");
            r.put("total_cost", totalCost);
            r.put("booking_reference", bookingRef);
            r.put("payment_method", paymentMethod);
            r.put("payment_id", paymentId);
            r.put("payment_status", "Pending");

            long bid = db.insert("Reservation", null, r);
            if (bid == -1) throw new Exception("Failed to insert reservation");

            db.setTransactionSuccessful();
            Log.d(TAG, "✓ Pending booking created: " + bookingRef);
            return true;

        } catch (Exception ex) {
            Log.e(TAG, "✗ Booking failed: " + ex.getMessage(), ex);
            return false;
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    // ===================== 24-Hour Cancellation Policy =====================

    public boolean cancelBooking(int bookingId, boolean isAdminCancellation) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            Cursor c = db.rawQuery("SELECT pickup_date, pickup_time, total_cost, status FROM Reservation WHERE booking_id=?",
                    new String[]{String.valueOf(bookingId)});

            if (!c.moveToFirst()) {
                c.close();
                return false;
            }

            String pickupDate = c.getString(0);
            String pickupTime = c.getString(1);
            double totalCost = c.getDouble(2);
            String currentStatus = c.getString(3);
            c.close();

            if (currentStatus.equals("Cancelled")) {
                return false;
            }

            double cancellationFee = 0;

            if (!isAdminCancellation) {
                SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy HH:mm", Locale.US);
                Date pickupDateTime = sdf.parse(pickupDate + " " + pickupTime);
                Date now = new Date();

                if (pickupDateTime != null) {
                    long diff = pickupDateTime.getTime() - now.getTime();
                    long hoursUntilPickup = TimeUnit.MILLISECONDS.toHours(diff);

                    if (hoursUntilPickup < 24) {
                        cancellationFee = totalCost * 0.20;
                        Log.d(TAG, "Cancellation fee applied: $" + cancellationFee);
                    }
                }
            }

            ContentValues v = new ContentValues();
            v.put("status", "Cancelled");
            v.put("cancellation_date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()));
            v.put("cancellation_fee", cancellationFee);

            int rows = db.update("Reservation", v, "booking_id=?",
                    new String[]{String.valueOf(bookingId)});

            return rows > 0;

        } catch (Exception e) {
            Log.e(TAG, "Error cancelling booking: " + e.getMessage(), e);
            return false;
        } finally {
            db.close();
        }
    }

    // ===================== Admin Approval =====================

    public boolean approveBooking(int bookingId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            Cursor c = db.rawQuery("SELECT vehicle_id FROM Reservation WHERE booking_id=?",
                    new String[]{String.valueOf(bookingId)});
            if (!c.moveToFirst()) {
                c.close();
                return false;
            }
            int vehicleId = c.getInt(0);
            c.close();

            ContentValues v = new ContentValues();
            v.put("status", "Confirmed");
            v.put("payment_status", "Paid");
            db.update("Reservation", v, "booking_id=?", new String[]{String.valueOf(bookingId)});

            ContentValues vCar = new ContentValues();
            vCar.put("status", "Rented");
            db.update("Vehicle", vCar, "vehicle_id=?", new String[]{String.valueOf(vehicleId)});

            db.setTransactionSuccessful();
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error approving booking: " + e.getMessage(), e);
            return false;
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    // ===================== Return Inspection =====================

    public boolean createReturnInspection(int rentalId, int vehicleId, int inspectorId,
                                          int mileage, String fuelLevel, String condition,
                                          String damageReport, String photos) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues v = new ContentValues();
        v.put("rental_id", rentalId);
        v.put("vehicle_id", vehicleId);
        v.put("inspection_type", "Return");
        v.put("mileage", mileage);
        v.put("fuel_level", fuelLevel);
        v.put("condition_notes", condition);
        v.put("damage_report", damageReport);
        v.put("inspector_id", inspectorId);
        v.put("photos", photos);

        long res = db.insert("Inspection", null, v);
        db.close();

        return res != -1;
    }

    // ===================== Location Methods =====================

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

    // ===================== Customer Methods =====================

    public Customer getCustomer(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM Customer WHERE customer_id=?",
                new String[]{String.valueOf(id)});
        Customer cust = null;
        if(c.moveToFirst()) {
            cust = new Customer(c.getInt(0), c.getString(1), c.getString(2),
                    c.getString(3), c.getString(5));
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

    // ===================== Vehicle Management =====================

    public boolean addNewCarComplete(String make, String model, String type, int year,
                                     double price, String plate, String imageRes,
                                     String transmission, int seats) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long makeId = -1, typeId = -1, modelId = -1;

        db.beginTransaction();
        try {
            Cursor cMake = db.rawQuery("SELECT make_id FROM Make WHERE make_name=?",
                    new String[]{make});
            if (cMake.moveToFirst()) makeId = cMake.getInt(0);
            else {
                ContentValues v = new ContentValues();
                v.put("make_name", make);
                makeId = db.insert("Make", null, v);
            }
            cMake.close();

            Cursor cType = db.rawQuery("SELECT type_id FROM Type WHERE type_name=?",
                    new String[]{type});
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
            vCar.put("transmission", transmission);      // NEW
            vCar.put("seating_capacity", seats);         // NEW
            db.insert("Vehicle", null, vCar);

            db.setTransactionSuccessful();
            Log.d(TAG, "✓ Vehicle added successfully");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error adding vehicle: " + e.getMessage(), e);
            return false;
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public boolean updateVehicle(int vehicleId, String make, String model, String type,
                                 double price, String imageRes, String transmission, int seats) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            // gets current model_id
            Cursor cVehicle = db.rawQuery("SELECT model_id FROM Vehicle WHERE vehicle_id=?",
                    new String[]{String.valueOf(vehicleId)});

            if (!cVehicle.moveToFirst()) {
                cVehicle.close();
                return false;
            }

            long modelId = cVehicle.getLong(0);
            cVehicle.close();

            // update or create Make
            long makeId = -1;
            Cursor cMake = db.rawQuery("SELECT make_id FROM Make WHERE make_name=?",
                    new String[]{make});
            if (cMake.moveToFirst()) {
                makeId = cMake.getInt(0);
            } else {
                ContentValues vMake = new ContentValues();
                vMake.put("make_name", make);
                makeId = db.insert("Make", null, vMake);
            }
            cMake.close();

            // updates or create Type
            long typeId = -1;
            Cursor cType = db.rawQuery("SELECT type_id FROM Type WHERE type_name=?",
                    new String[]{type});
            if (cType.moveToFirst()) {
                typeId = cType.getInt(0);
            } else {
                ContentValues vType = new ContentValues();
                vType.put("type_name", type);
                typeId = db.insert("Type", null, vType);
            }
            cType.close();

            // updates VehicleModel
            ContentValues vModel = new ContentValues();
            vModel.put("make_id", makeId);
            vModel.put("type_id", typeId);
            vModel.put("model_name", model);
            vModel.put("daily_rate", price);

            db.update("VehicleModel", vModel, "model_id=?",
                    new String[]{String.valueOf(modelId)});

            // updates Vehicle
            ContentValues vVehicle = new ContentValues();
            vVehicle.put("image_res_name", imageRes);
            vVehicle.put("transmission", transmission);
            vVehicle.put("seating_capacity", seats);

            int rows = db.update("Vehicle", vVehicle, "vehicle_id=?",
                    new String[]{String.valueOf(vehicleId)});

            db.setTransactionSuccessful();
            Log.d(TAG, "✓ Vehicle updated successfully");
            return rows > 0;

        } catch (Exception e) {
            Log.e(TAG, "Error updating vehicle: " + e.getMessage(), e);
            return false;
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public void deleteVehicle(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("Vehicle", "vehicle_id=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public List<VehicleItem> getAllVehicles() {
        List<VehicleItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT v.vehicle_id, mk.make_name, vm.model_name, t.type_name, " +
                "vm.daily_rate, v.status, v.image_res_name, v.transmission, v.seating_capacity " +
                "FROM Vehicle v " +
                "JOIN VehicleModel vm ON v.model_id = vm.model_id " +
                "JOIN Make mk ON vm.make_id = mk.make_id " +
                "JOIN Type t ON vm.type_id = t.type_id";
        try {
            Cursor c = db.rawQuery(query, null);
            if (c.moveToFirst()) {
                do {
                    String title = c.getString(1) + " " + c.getString(2);
                    String transmission = c.getString(7);
                    int seats = c.getInt(8);

                    list.add(new VehicleItem(
                            c.getInt(0),      // id
                            title,            // title
                            c.getString(3),   // type
                            c.getDouble(4),   // price
                            c.getString(5),   // status
                            c.getString(6),   // imageRes
                            transmission,     // transmission
                            seats             // seats
                    ));
                } while (c.moveToNext());
            }
            c.close();
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage(), e);
        }
        db.close();
        return list;
    }

    // ===================== Admin Booking View =====================

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
                            c.getInt(0), c.getString(1), c.getString(2), c.getString(3),
                            c.getString(4), c.getString(5), c.getString(6), c.getString(7),
                            c.getString(8), c.getString(9), c.getString(10), c.getString(11),
                            c.getString(12), c.getDouble(13)
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
                    list.add(new BookingItem(c.getInt(0), c.getString(1), car, dates,
                            c.getString(6), c.getDouble(7)));
                } while(c.moveToNext());
            }
            c.close();
        } catch(Exception e) {
            Log.e(TAG, "Error fetching bookings: " + e.getMessage(), e);
        }
        db.close();
        return list;
    }

    // ===================== Booking Details Methods =====================

    public BookingDetailItem getBookingDetails(int bookingId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        BookingDetailItem booking = null;

        String query = "SELECT r.booking_id, r.booking_reference, r.status, " +
                "c.first_name || ' ' || c.last_name as customer_name, c.email, c.phone, " +
                "mk.make_name || ' ' || vm.model_name as car_name, v.image_res_name, " +
                "r.pickup_date, r.pickup_time, r.return_date, r.return_time, " +
                "r.pickup_address, r.return_address, " +
                "r.total_cost, r.payment_method, r.payment_id " +
                "FROM Reservation r " +
                "JOIN Customer c ON r.customer_num = c.customer_id " +
                "JOIN Vehicle v ON r.vehicle_id = v.vehicle_id " +
                "JOIN VehicleModel vm ON v.model_id = vm.model_id " +
                "JOIN Make mk ON vm.make_id = mk.make_id " +
                "WHERE r.booking_id = ?";

        try {
            Cursor c = db.rawQuery(query, new String[]{String.valueOf(bookingId)});
            if (c.moveToFirst()) {
                booking = new BookingDetailItem(
                        c.getInt(0),      // booking_id
                        c.getString(1),   // booking_reference
                        c.getString(2),   // status
                        c.getString(3),   // customer_name
                        c.getString(4),   // customer_email
                        c.getString(5),   // customer_phone
                        c.getString(6),   // car_name
                        c.getString(7),   // car_image
                        c.getString(8),   // pickup_date
                        c.getString(9),   // pickup_time
                        c.getString(10),  // return_date
                        c.getString(11),  // return_time
                        c.getString(12),  // pickup_address
                        c.getString(13),  // return_address
                        c.getDouble(14),  // total_cost
                        c.getString(15),  // payment_method
                        c.getString(16)   // payment_receipt (payment_id)
                );
            }
            c.close();
        } catch (Exception e) {
            Log.e(TAG, "Error fetching booking details: " + e.getMessage(), e);
        }
        db.close();
        return booking;
    }

    // ===================== Inner Classes =====================

    public static class VehicleItem {
        public int id;
        public String title, type, status, imageRes, transmission;
        public double price;
        public int seats;

        public VehicleItem(int id, String t, String ty, double p, String s,
                           String img, String trans, int seats) {
            this.id = id;
            this.title = t;
            this.type = ty;
            this.price = p;
            this.status = s;
            this.imageRes = img;
            this.transmission = trans != null ? trans : "Manual";
            this.seats = seats > 0 ? seats : 5;
        }
    }

    public static class LocationItem {
        public int id;
        public String name, address;
        public LocationItem(int id, String name, String address) {
            this.id = id; this.name = name; this.address = address;
        }
    }

    public static class Customer {
        public int id;
        public String firstName, lastName, email, phone;
        public Customer(int id, String f, String l, String e, String p) {
            this.id = id; firstName = f; lastName = l; email = e; phone = p;
        }
    }

    public static class AdminBookingItem {
        public int id;
        public String bookingReference, customerName, customerEmail, carName;
        public String pickupDate, pickupTime, pickupAddress;
        public String returnDate, returnTime, returnAddress;
        public String status, paymentMethod;
        public double totalCost;

        public AdminBookingItem(int id, String ref, String custName, String custEmail,
                                String car, String pDate, String pTime, String pAddr,
                                String rDate, String rTime, String rAddr,
                                String stat, String payment, double cost) {
            this.id = id; this.bookingReference = ref; this.customerName = custName;
            this.customerEmail = custEmail; this.carName = car; this.pickupDate = pDate;
            this.pickupTime = pTime; this.pickupAddress = pAddr; this.returnDate = rDate;
            this.returnTime = rTime; this.returnAddress = rAddr; this.status = stat;
            this.paymentMethod = payment; this.totalCost = cost;
        }
    }

    public static class BookingItem {
        public int id;
        public String customerName, carName, dates, status;
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

    public static class BookingDetailItem {
        public int id;
        public String bookingReference, status;
        public String customerName, customerEmail, customerPhone;
        public String carName, carImage;
        public String pickupDate, pickupTime, returnDate, returnTime;
        public String pickupAddress, returnAddress;
        public double totalCost;
        public String paymentMethod, paymentReceipt;

        public BookingDetailItem(int id, String ref, String status,
                                 String custName, String custEmail, String custPhone,
                                 String car, String carImg,
                                 String pDate, String pTime, String rDate, String rTime,
                                 String pAddr, String rAddr,
                                 double cost, String payment, String receipt) {
            this.id = id;
            this.bookingReference = ref;
            this.status = status;
            this.customerName = custName;
            this.customerEmail = custEmail;
            this.customerPhone = custPhone;
            this.carName = car;
            this.carImage = carImg;
            this.pickupDate = pDate;
            this.pickupTime = pTime;
            this.returnDate = rDate;
            this.returnTime = rTime;
            this.pickupAddress = pAddr;
            this.returnAddress = rAddr;
            this.totalCost = cost;
            this.paymentMethod = payment;
            this.paymentReceipt = receipt;
        }
    }
}