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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.content.Context;
import android.content.SharedPreferences;

public class CarRentalData {
    private static final String TAG = "CarRentalData";
    private DatabaseHelper dbHelper;

    public CarRentalData(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // ===================== Security and Account Rules =====================

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
    }

    // ===================== VEHICLE PICKUP =====================

    public boolean recordVehiclePickup(int rentalId, int vehicleId, int inspectorId,
                                       String pickupFuelLevel, String pickupAddress,
                                       String conditionNotes, String photos) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            ContentValues vRental = new ContentValues();
            vRental.put("actual_pickup_address", pickupAddress);
            vRental.put("pickup_fuel_level", pickupFuelLevel);

            int rows = db.update("Rental", vRental, "rental_id=?",
                    new String[]{String.valueOf(rentalId)});

            if (rows == 0) {
                throw new Exception("Rental not found");
            }

            // PICKUP INSPECTION RECORD
            ContentValues vInspection = new ContentValues();
            vInspection.put("rental_id", rentalId);
            vInspection.put("vehicle_id", vehicleId);
            vInspection.put("inspection_type", "Pickup");
            vInspection.put("fuel_level", pickupFuelLevel);
            vInspection.put("condition_notes", conditionNotes != null ? conditionNotes : "Good condition");
            vInspection.put("damage_report", "None");
            vInspection.put("inspector_id", inspectorId);
            vInspection.put("photos", photos);

            long inspectionId = db.insert("Inspection", null, vInspection);

            if (inspectionId == -1) {
                throw new Exception("Failed to create inspection record");
            }

            // UPDATE VEHICLE
            ContentValues vVehicle = new ContentValues();
            vVehicle.put("fuel_level", pickupFuelLevel);
            vVehicle.put("last_inspection_date", new SimpleDateFormat("yyyy-MM-dd",
                    Locale.US).format(new Date()));

            db.update("Vehicle", vVehicle, "vehicle_id=?",
                    new String[]{String.valueOf(vehicleId)});

            db.setTransactionSuccessful();

            Log.d(TAG, "Vehicle pickup recorded successfully");
            Log.d(TAG, "Inspection ID: " + inspectionId);
            Log.d(TAG, "Pickup Address: " + pickupAddress);
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error recording pickup: " + e.getMessage(), e);
            return false;
        } finally {
            db.endTransaction();
        }
    }


    // ===================== VEHICLE RETURN =====================

    public boolean recordVehicleReturn(int rentalId, int vehicleId, int inspectorId,
                                       String returnFuelLevel, String returnAddress,
                                       String conditionNotes, String damageReport,
                                       String photos, double additionalCharges) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            // Get rental details
            Cursor c = db.rawQuery("SELECT pickup_fuel_level, total_amount " +
                            "FROM Rental WHERE rental_id=?",
                    new String[]{String.valueOf(rentalId)});

            if (!c.moveToFirst()) {
                c.close();
                throw new Exception("Rental not found");
            }

            String pickupFuel = c.getString(0);
            double baseAmount = c.getDouble(1);
            c.close();

            // UPDATE RENTAL
            ContentValues vRental = new ContentValues();
            vRental.put("actual_return_address", returnAddress);
            vRental.put("return_fuel_level", returnFuelLevel);

            db.update("Rental", vRental, "rental_id=?",
                    new String[]{String.valueOf(rentalId)});

            // RETURN INSPECTION RECORD
            ContentValues vInspection = new ContentValues();
            vInspection.put("rental_id", rentalId);
            vInspection.put("vehicle_id", vehicleId);
            vInspection.put("inspection_type", "Return");
            vInspection.put("fuel_level", returnFuelLevel);
            vInspection.put("condition_notes", conditionNotes != null ? conditionNotes : "Good condition");
            vInspection.put("damage_report", damageReport != null ? damageReport : "None");
            vInspection.put("inspector_id", inspectorId);
            vInspection.put("photos", photos);

            long inspectionId = db.insert("Inspection", null, vInspection);

            if (inspectionId == -1) {
                throw new Exception("Failed to create return inspection");
            }

            //  VEHICLE STATUS
            ContentValues vVehicle = new ContentValues();
            vVehicle.put("status", "Available");
            vVehicle.put("fuel_level", returnFuelLevel);
            vVehicle.put("last_inspection_date", new SimpleDateFormat("yyyy-MM-dd",
                    Locale.US).format(new Date()));

            db.update("Vehicle", vVehicle, "vehicle_id=?",
                    new String[]{String.valueOf(vehicleId)});

            // PAYMENT RECORD
            double balanceDue = baseAmount * 0.70;
            double finalPayment = balanceDue + additionalCharges;

            ContentValues vPayment = new ContentValues();
            vPayment.put("rental_id", rentalId);
            vPayment.put("amount", finalPayment);
            vPayment.put("payment_mthd", "Cash");
            vPayment.put("payment_status", "Completed");

            db.insert("Payment", null, vPayment);

            db.setTransactionSuccessful();

            Log.d(TAG, "Vehicle return recorded successfully");
            Log.d(TAG, "Return Inspection ID: " + inspectionId);
            Log.d(TAG, "Return Address: " + returnAddress);
            Log.d(TAG, "Final payment: $" + finalPayment);

            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error recording return: " + e.getMessage(), e);
            return false;
        } finally {
            db.endTransaction();
        }
    }



    // ===================== Car Availability and Management =====================

    public boolean isCarAvailable(int vehicleId, String pickupDate, String returnDate) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cStatus = db.rawQuery("SELECT status FROM Vehicle WHERE vehicle_id=?",
                new String[]{String.valueOf(vehicleId)});
        if (!cStatus.moveToFirst() || !cStatus.getString(0).equals("Available")) {
            cStatus.close();
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
        return available;
    }

    // ===================== Booking and Reservation =====================

    public List<EmployeeRentalItem> getAllEmployeeRentals() {
        List<EmployeeRentalItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT er.er_id, er.employee_id, " +
                "e.first_name || ' ' || e.last_name as employee_name, " +
                "er.rental_id, r.reservation_id, er.role " +
                "FROM EmployeeRental er " +
                "JOIN Employee e ON er.employee_id = e.employee_id " +
                "JOIN Rental r ON er.rental_id = r.rental_id " +
                "ORDER BY er.er_id DESC";

        try {
            Cursor c = db.rawQuery(query, null);
            if (c.moveToFirst()) {
                do {
                    list.add(new EmployeeRentalItem(
                            c.getInt(0),      // er_id
                            c.getInt(1),      // employee_id
                            c.getString(2),   // employee_name
                            c.getInt(3),      // rental_id
                            c.getInt(4),      // reservation_id
                            c.getString(5)    // role
                    ));
                } while (c.moveToNext());
            }
            c.close();
            Log.d(TAG, "Loaded " + list.size() + " employee-rental records");
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage(), e);
        }
        return list;
    }

    public EmployeeReservationItem getEmployeeForBooking(int bookingId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        EmployeeReservationItem item = null;

        String query = "SELECT er.resv_id, er.employee_id, " +
                "e.first_name || ' ' || e.last_name as employee_name, e.role, " +
                "er.booking_id, r.booking_reference, er.asgn_date " +
                "FROM EmployeeReservation er " +
                "JOIN Employee e ON er.employee_id = e.employee_id " +
                "JOIN Reservation r ON er.booking_id = r.booking_id " +
                "WHERE er.booking_id = ?";

        try {
            Cursor c = db.rawQuery(query, new String[]{String.valueOf(bookingId)});
            if (c.moveToFirst()) {
                item = new EmployeeReservationItem(
                        c.getInt(0), c.getInt(1), c.getString(2),
                        c.getString(3), c.getInt(4), c.getString(5), c.getString(6)
                );
            }
            c.close();
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage(), e);
        }
        return item;
    }

    public void debugListEmployeeTracking() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Log.d(TAG, "=================================");
        Log.d(TAG, "=== EMPLOYEE TRACKING RECORDS ===");
        Log.d(TAG, "=================================");

        // EmployeeReservation
        Cursor cRes = db.rawQuery(
                "SELECT er.resv_id, e.first_name || ' ' || e.last_name, " +
                        "r.booking_reference, er.asgn_date " +
                        "FROM EmployeeReservation er " +
                        "JOIN Employee e ON er.employee_id = e.employee_id " +
                        "JOIN Reservation r ON er.booking_id = r.booking_id", null);

        Log.d(TAG, "--- EmployeeReservation ---");
        if (cRes.moveToFirst()) {
            do {
                Log.d(TAG, "ID: " + cRes.getInt(0) + " | Employee: " + cRes.getString(1) +
                        " | Booking: " + cRes.getString(2) + " | Date: " + cRes.getString(3));
            } while (cRes.moveToNext());
            Log.d(TAG, "Total: " + cRes.getCount() + " records");
        } else {
            Log.d(TAG, "No records");
        }
        cRes.close();

        // EmployeeRental
        Cursor cRent = db.rawQuery(
                "SELECT er.er_id, e.first_name || ' ' || e.last_name, " +
                        "er.rental_id, er.role " +
                        "FROM EmployeeRental er " +
                        "JOIN Employee e ON er.employee_id = e.employee_id", null);

        Log.d(TAG, "--- EmployeeRental ---");
        if (cRent.moveToFirst()) {
            do {
                Log.d(TAG, "ID: " + cRent.getInt(0) + " | Employee: " + cRent.getString(1) +
                        " | Rental: " + cRent.getInt(2) + " | Role: " + cRent.getString(3));
            } while (cRent.moveToNext());
            Log.d(TAG, "Total: " + cRent.getCount() + " records");
        } else {
            Log.d(TAG, "No records");
        }
        cRent.close();

        Log.d(TAG, "=================================");
    }

    private String generateBookingId() {
        return "RQ" + System.currentTimeMillis();
    }

    public boolean createPendingBooking(int customerId, int vehicleId, String carName,
                                        String pickupDate, String returnDate,
                                        String pickupTime, String returnTime,
                                        String pickupAddress, String returnAddress,
                                        int rentalDays, double baseCost,
                                        String insuranceType, double insuranceFee,
                                        int lateHours, double lateFee,
                                        double totalCost,
                                        String paymentMethod, String paymentId,
                                        String receiptImage) { // <--- NEW PARAMETER

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            // 1. CHECK IF COLUMN EXISTS, IF NOT ADD IT (Auto-Migration)
            try {
                Cursor c = db.rawQuery("SELECT payment_receipt FROM Reservation LIMIT 1", null);
                c.close();
            } catch (Exception e) {
                // Column doesn't exist, so we add it dynamically
                db.execSQL("ALTER TABLE Reservation ADD COLUMN payment_receipt TEXT");
                Log.d(TAG, "Added payment_receipt column to Reservation table");
            }

            String bookingRef = "RQ" + System.currentTimeMillis();

            ContentValues r = new ContentValues();
            r.put("customer_num", customerId);
            r.put("vehicle_id", vehicleId);
            r.put("booking_reference", bookingRef);
            r.put("status", "Pending");

            // Schedule
            r.put("pickup_date", pickupDate);
            r.put("return_date", returnDate);
            r.put("pickup_time", pickupTime);
            r.put("return_time", returnTime);
            r.put("pickup_address", pickupAddress);
            r.put("return_address", returnAddress);

            // Costs
            r.put("rental_days", rentalDays);
            r.put("base_cost", baseCost);
            r.put("insurance_type", insuranceType != null ? insuranceType : "None");
            r.put("insurance_fee", insuranceFee);
            r.put("late_hours", lateHours);
            r.put("late_fee", lateFee);
            r.put("total_cost", totalCost);

            // Payment Details
            r.put("payment_method", paymentMethod);
            r.put("payment_id", paymentId);         // Stores "PAY-123..."
            r.put("payment_status", "Pending");
            r.put("payment_receipt", receiptImage); // Stores the Image

            long bookingId = db.insert("Reservation", null, r);

            // Update Vehicle Status
            if (bookingId != -1) {
                ContentValues vCar = new ContentValues();
                vCar.put("status", "Pending");
                db.update("Vehicle", vCar, "vehicle_id=?", new String[]{String.valueOf(vehicleId)});
            }

            db.setTransactionSuccessful();
            return bookingId != -1;

        } catch (Exception ex) {
            Log.e(TAG, "Booking creation failed: " + ex.getMessage(), ex);
            return false;
        } finally {
            db.endTransaction();
        }
    }

    // Get insurance description based on type
    private String getInsuranceDescription(String insuranceType) {
        if (insuranceType == null) return "No coverage";

        switch (insuranceType) {
            case "Personal Insurance":
                return "Personal insurance coverage provided by customer";

            case "Basic Insurance":
                return "Basic Insurance Coverage:\n" +
                        "• +20% of daily rate\n" +
                        "• Full coverage\n" +
                        "• Roadside assistance\n" +
                        "• Personal injury protection";

            default:
                return "Insurance type: " + insuranceType;
        }
    }

    public List<InsuranceItem> getCustomerInsurance(int customerId) {
        List<InsuranceItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT insurance_id, insurance_type, insurance_description, " +
                "booking_reference, created_at " +
                "FROM Insurance " +
                "WHERE customer_id = ? " +
                "ORDER BY created_at DESC";

        try {
            Cursor c = db.rawQuery(query, new String[]{String.valueOf(customerId)});
            if (c.moveToFirst()) {
                do {
                    list.add(new InsuranceItem(
                            c.getInt(0),      // insurance_id
                            customerId,         //CUSTOMER ID
                            c.getString(1),   // insurance_type
                            c.getString(2),   // insurance_description
                            c.getString(3),   // booking_reference
                            c.getString(4)    // created_at
                    ));
                } while (c.moveToNext());
            }
            c.close();
            Log.d(TAG, "Loaded " + list.size() + " insurance records for customer " + customerId);
        } catch (Exception e) {
            Log.e(TAG, "Error fetching insurance records: " + e.getMessage(), e);
        }
        return list;
    }

    public InsuranceItem getInsuranceByBooking(String bookingReference) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        InsuranceItem insurance = null;

        String query = "SELECT insurance_id, customer_id, insurance_type, " +
                "insurance_description, booking_reference, created_at " +
                "FROM Insurance " +
                "WHERE booking_reference = ?";

        try {
            Cursor c = db.rawQuery(query, new String[]{bookingReference});
            if (c.moveToFirst()) {
                insurance = new InsuranceItem(
                        c.getInt(0),      // insurance_id
                        c.getInt(1),      // customer_id
                        c.getString(2),   // insurance_type
                        c.getString(3),   // insurance_description
                        c.getString(4),   // booking_reference
                        c.getString(5)    // created_at
                );
            }
            c.close();
        } catch (Exception e) {
            Log.e(TAG, "Error fetching insurance: " + e.getMessage(), e);
        }
        return insurance;
    }

    public List<InsuranceItem> getAllInsurance() {
        List<InsuranceItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT i.insurance_id, i.customer_id, " +
                "c.first_name || ' ' || c.last_name as customer_name, " +
                "i.insurance_type, i.insurance_description, " +
                "i.booking_reference, i.created_at " +
                "FROM Insurance i " +
                "JOIN Customer c ON i.customer_id = c.customer_id " +
                "ORDER BY i.created_at DESC";

        try {
            Cursor c = db.rawQuery(query, null);
            if (c.moveToFirst()) {
                do {
                    InsuranceItem item = new InsuranceItem(
                            c.getInt(0),      // insurance_id
                            c.getInt(1),      // customer_id
                            c.getString(3),   // insurance_type
                            c.getString(4),   // insurance_description
                            c.getString(5),   // booking_reference
                            c.getString(6)    // created_at
                    );
                    // customer name display
                    item.customerName = c.getString(2);
                    list.add(item);
                } while (c.moveToNext());
            }
            c.close();
            Log.d(TAG, "Loaded " + list.size() + " total insurance records");
        } catch (Exception e) {
            Log.e(TAG, "Error fetching all insurance: " + e.getMessage(), e);
        }
        return list;
    }

    public void debugListAllInsurance() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT i.insurance_id, " +
                "c.first_name || ' ' || c.last_name as customer, " +
                "i.insurance_type, i.booking_reference, i.created_at " +
                "FROM Insurance i " +
                "JOIN Customer c ON i.customer_id = c.customer_id " +
                "ORDER BY i.insurance_id DESC";

        Cursor c = db.rawQuery(query, null);

        Log.d(TAG, "=================================");
        Log.d(TAG, "=== ALL INSURANCE RECORDS ===");
        Log.d(TAG, "=================================");

        if (c.moveToFirst()) {
            do {
                Log.d(TAG, "Insurance #" + c.getInt(0));
                Log.d(TAG, "  Customer: " + c.getString(1));
                Log.d(TAG, "  Type: " + c.getString(2));
                Log.d(TAG, "  Booking Ref: " + c.getString(3));
                Log.d(TAG, "  Created: " + c.getString(4));
                Log.d(TAG, "---------------------------------");
            } while (c.moveToNext());
            Log.d(TAG, "Total: " + c.getCount() + " insurance records");
        } else {
            Log.d(TAG, "No insurance records found");
        }

        c.close();
        Log.d(TAG, "=================================");
    }

    // ===================== EMPLOYEE AUTHENTICATION =====================

    // employee login
    public Employee loginEmployee(String email, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT employee_id, first_name, last_name, role FROM Employee WHERE email=? AND password=?",
                new String[]{email, password});

        Employee emp = null;
        if (c.moveToFirst()) {
            emp = new Employee(c.getInt(0), c.getString(1), c.getString(2), c.getString(3));
        }
        c.close();
        return emp;
    }

    // ===================== INSPECTION AGENT LOGIC =====================

    public boolean updateBookingStatus(int bookingId, String newStatus) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", newStatus);

        // Update timestamp if possible
        try {
            values.put("updated_at", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(new java.util.Date()));
        } catch (Exception e) {} // Ignore if column missing

        int rows = db.update("Reservation", values, "booking_id = ?", new String[]{String.valueOf(bookingId)});
        return rows > 0;
    }

    public boolean addInspection(int bookingId, String type, String fuel, String damage, String notes) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // 1. Get Vehicle ID from Reservation
        int vehicleId = -1;
        Cursor c = db.rawQuery("SELECT vehicle_id FROM Reservation WHERE booking_id=?", new String[]{String.valueOf(bookingId)});
        if (c.moveToFirst()) vehicleId = c.getInt(0);
        c.close();

        // 2. Get Rental ID if exists (otherwise use bookingId)
        int rentalId = -1;
        Cursor cRent = db.rawQuery("SELECT rental_id FROM Rental WHERE reservation_id=?", new String[]{String.valueOf(bookingId)});
        if (cRent.moveToFirst()) rentalId = cRent.getInt(0);
        cRent.close();

        // 3. Insert Inspection
        ContentValues values = new ContentValues();
        values.put("rental_id", rentalId != -1 ? rentalId : bookingId);
        values.put("vehicle_id", vehicleId);
        values.put("inspection_type", type);
        values.put("fuel_level", fuel);
        values.put("damage_report", damage);
        values.put("condition_notes", notes);
        values.put("inspection_date", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US).format(new java.util.Date()));

        long result = db.insert("Inspection", null, values);
        return result != -1;
    }

    // Get bookings that need Return Inspection
    public List<AdminBookingItem> getBookingsForInspection() {
        List<AdminBookingItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // UPDATED QUERY: Fetches both 'Confirmed' (for Pickup) and 'Rented' (for Return)
        String query = "SELECT r.booking_id, r.booking_reference, " +
                "c.first_name || ' ' || c.last_name, c.email, c.phone, " +
                "mk.make_name || ' ' || vm.model_name, " +
                "r.pickup_date, r.pickup_time, r.pickup_address, " +
                "r.return_date, r.return_time, r.return_address, " +
                "r.status, r.payment_method, r.total_cost " +
                "FROM Reservation r " +
                "JOIN Customer c ON r.customer_num = c.customer_id " +
                "JOIN Vehicle v ON r.vehicle_id = v.vehicle_id " +
                "JOIN VehicleModel vm ON v.model_id = vm.model_id " +
                "JOIN Make mk ON vm.make_id = mk.make_id " +
                "WHERE r.status IN ('Confirmed', 'Rented') " +  // <--- KEY CHANGE
                "ORDER BY r.pickup_date ASC";

        try {
            Cursor c = db.rawQuery(query, null);
            if(c.moveToFirst()) {
                do {
                    list.add(new AdminBookingItem(c.getInt(0), c.getString(1), c.getString(2),
                            c.getString(3), c.getString(4), c.getString(5), c.getString(6),
                            c.getString(7), c.getString(8), c.getString(9), c.getString(10),
                            c.getString(11), c.getString(12), c.getString(13), c.getDouble(14)));
                } while(c.moveToNext());
            }
            c.close();
        } catch(Exception e) { Log.e(TAG, "Error fetching inspection list", e); }
        return list;
    }

    public boolean submitInspection(int bookingId, int employeeId, String inspectionType, String fuel, String notes, String damage, String photoPath) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            Cursor c = db.rawQuery("SELECT vehicle_id, return_date FROM Reservation WHERE booking_id=?", new String[]{String.valueOf(bookingId)});
            if(!c.moveToFirst()) { c.close(); return false; }
            int vehicleId = c.getInt(0);
            String returnDate = c.getString(1);
            c.close();

            // Insert into Inspection Table
            ContentValues vInsp = new ContentValues();
            vInsp.put("rental_id", bookingId);
            vInsp.put("vehicle_id", vehicleId);
            vInsp.put("inspection_type", inspectionType);
            vInsp.put("inspection_date", new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date())); // Added Date
            vInsp.put("fuel_level", fuel);
            vInsp.put("condition_notes", notes);
            vInsp.put("damage_report", damage);
            vInsp.put("inspector_id", employeeId);
            vInsp.put("photos", photoPath); // <--- SAVING PHOTO
            db.insert("Inspection", null, vInsp);

            // Log Employee Activity
            ContentValues vEmpRes = new ContentValues();
            vEmpRes.put("employee_id", employeeId);
            vEmpRes.put("booking_id", bookingId);
            vEmpRes.put("asgn_date", returnDate);
            db.insert("EmployeeReservation", null, vEmpRes);

            // UPDATE RENTAL TABLE
            ContentValues vRentalUpdate = new ContentValues();
            if (inspectionType.equalsIgnoreCase("Pickup")) {
                vRentalUpdate.put("pickup_fuel_level", fuel);
            } else {
                vRentalUpdate.put("return_fuel_level", fuel);
            }
            // Ensure your Rental table has a column that links to booking_id (e.g. reservation_id)
            db.update("Rental", vRentalUpdate, "reservation_id=?", new String[]{String.valueOf(bookingId)});

            // UPDATE RESERVATION STATUS
            ContentValues vRes = new ContentValues();
            if (inspectionType.equalsIgnoreCase("Pickup")) {
                vRes.put("status", "Rented");
            } else {
                vRes.put("status", "Inspected");
            }
            db.update("Reservation", vRes, "booking_id=?", new String[]{String.valueOf(bookingId)});

            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error submitting inspection: " + e.getMessage());
            return false;
        } finally {
            db.endTransaction();
        }
    }

    // ================== INSPECTION HISTORY ==================

    public List<InspectionLogItem> getInspectionHistory() {
        List<InspectionLogItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Fetches all details to the database
        String query = "SELECT " +
                "i.inspection_id, " +       // 0
                "i.inspection_type, " +     // 1
                "i.inspection_date, " +     // 2
                "i.fuel_level, " +          // 3
                "i.condition_notes, " +     // 4
                "i.damage_report, " +       // 5
                "mk.make_name || ' ' || vm.model_name AS car_name, " + // 6
                "c.first_name || ' ' || c.last_name AS customer_name, " + // 7
                "e.first_name || ' ' || e.last_name AS inspector_name, " + // 8
                "i.photos " +               // 9
                "FROM Inspection i " +
                "JOIN Vehicle v ON i.vehicle_id = v.vehicle_id " +
                "JOIN VehicleModel vm ON v.model_id = vm.model_id " +
                "JOIN Make mk ON vm.make_id = mk.make_id " +
                "JOIN Reservation r ON i.rental_id = r.booking_id " +
                "JOIN Customer c ON r.customer_num = c.customer_id " +
                "LEFT JOIN Employee e ON i.inspector_id = e.employee_id " +
                "ORDER BY i.inspection_id DESC";

        try {
            Cursor c = db.rawQuery(query, null);
            if (c.moveToFirst()) {
                do {
                    list.add(new InspectionLogItem(
                            c.getInt(0),      // id
                            c.getString(1),   // type
                            c.getString(2),   // date
                            c.getString(3),   // fuel
                            c.getString(4),   // notes
                            c.getString(5),   // damage
                            c.getString(6),   // carName
                            c.getString(7),   // customerName
                            c.getString(8),   // inspectorName
                            c.getString(9)    // photos
                    ));
                } while (c.moveToNext());
            }
            c.close();
        } catch (Exception e) {
            Log.e(TAG, "Error loading history: " + e.getMessage());
        }
        return list;
    }

    // Inspection Logs
    public static class InspectionLogItem {
        public int inspectionId;
        public String type, date, fuel, notes, damage, photos;
        public String carName, customerName, inspectorName;

        public InspectionLogItem(int id, String type, String date, String fuel,
                                 String notes, String damage, String car,
                                 String cust, String insp, String photoPath) {
            this.inspectionId = id;
            this.type = type;
            this.date = date;
            this.fuel = fuel;
            this.notes = notes;
            this.damage = damage;
            this.carName = car;
            this.customerName = cust;
            this.inspectorName = insp;
            this.photos = photoPath;
        }
    }

    // ===================== MECHANIC  =====================

    public List<MaintenanceLogItem> getAllMaintenanceLogs() {
        List<MaintenanceLogItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT m.mntnc_id, m.mntnc_date, m.description, m.cost, " +
                "e.first_name || ' ' || e.last_name as mechanic_name, " +  // ← FULL NAME
                "mk.make_name || ' ' || vm.model_name as car_name " +
                "FROM MaintenanceRecord m " +
                "JOIN Employee e ON m.employee_id = e.employee_id " +      // ← JOINS Employee table
                "JOIN Vehicle v ON m.vehicle_id = v.vehicle_id " +
                "JOIN VehicleModel vm ON v.model_id = vm.model_id " +
                "JOIN Make mk ON vm.make_id = mk.make_id " +
                "ORDER BY m.mntnc_date DESC";

        try {
            Cursor c = db.rawQuery(query, null);
            if (c.moveToFirst()) {
                do {
                    String mechanicName = c.getString(4);  // Get mechanic full name
                    Log.d(TAG, "Maintenance Record - Mechanic: " + mechanicName); // Debug log

                    list.add(new MaintenanceLogItem(
                            c.getInt(0),      // id
                            c.getString(1),   // date
                            c.getString(2),   // description
                            c.getDouble(3),   // cost
                            mechanicName,     // mechanic FULL NAME from database
                            c.getString(5)    // car name
                    ));
                } while (c.moveToNext());
            }
            c.close();
            Log.d(TAG, "Loaded " + list.size() + " maintenance logs with mechanic names");
        } catch (Exception e) {
            Log.e(TAG, "Error fetching maintenance logs", e);
        }
        return list;
    }

    // maintenanceLogItem
    public static class MaintenanceLogItem {
        public int id;
        public String date, description, mechanic, carName;
        public double cost;

        public MaintenanceLogItem(int id, String date, String desc,
                                  double cost, String mechanic, String car) {
            this.id = id;
            this.date = date;
            this.description = desc;
            this.cost = cost;
            this.mechanic = mechanic;
            this.carName = car;
        }
    }

    public List<VehicleMaintenanceItem> getAllVehiclesForMaintenance() {
        List<VehicleMaintenanceItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT v.vehicle_id, " +
                "mk.make_name || ' ' || vm.model_name as car_name, " +
                "v.plt_number, v.status, v.image_res_name, " +
                "COALESCE(" +
                "  (SELECT COUNT(*) FROM MaintenanceRecord WHERE vehicle_id = v.vehicle_id), " +
                "  0) as maintenance_count, " +
                "COALESCE(" +
                "  (SELECT MAX(mntnc_date) FROM MaintenanceRecord WHERE vehicle_id = v.vehicle_id), " +
                "  'Never') as last_service " +
                "FROM Vehicle v " +
                "JOIN VehicleModel vm ON v.model_id = vm.model_id " +
                "JOIN Make mk ON vm.make_id = mk.make_id " +
                "ORDER BY v.vehicle_id DESC";

        try {
            Cursor c = db.rawQuery(query, null);
            if (c.moveToFirst()) {
                do {
                    list.add(new VehicleMaintenanceItem(
                            c.getInt(0),        // vehicle_id
                            c.getString(1),     // car_name
                            c.getString(2),     // plate
                            c.getString(3),     // status
                            c.getString(4),     // image
                            c.getInt(5),        // maintenance_count
                            c.getString(6)      // last_service
                    ));
                } while (c.moveToNext());
            }
            c.close();
            Log.d(TAG, "Loaded " + list.size() + " vehicles for maintenance");
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage(), e);
        }
        return list;
    }

    // class for Vehicle Maintenance
    public static class VehicleMaintenanceItem {
        public int vehicleId;
        public String carName, plate, status, imageRes, lastService;
        public int maintenanceCount;

        public VehicleMaintenanceItem(int id, String name, String plate,
                                      String status, String img, int count, String lastSvc) {
            this.vehicleId = id;
            this.carName = name;
            this.plate = plate;
            this.status = status;
            this.imageRes = img;
            this.maintenanceCount = count;
            this.lastService = lastSvc;
        }
    }

    public boolean submitMaintenance(int vehicleId, int employeeId, String description, double cost) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();

        v.put("vehicle_id", vehicleId);
        v.put("employee_id", employeeId);  // ← This stores the mechanic's employee_id
        v.put("mntnc_date", new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date()));
        v.put("description", description);
        v.put("cost", cost);

        long res = db.insert("MaintenanceRecord", null, v);

        if (res != -1) {
            Log.d(TAG, "✓ Maintenance record created: ID=" + res +
                    ", Employee=" + employeeId +
                    ", Vehicle=" + vehicleId);
        } else {
            Log.e(TAG, "✗ Failed to create maintenance record");
        }

        return res != -1;
    }

    // simple employee class
    public static class Employee {
        public int id;
        public String firstName, lastName, role;
        public Employee(int id, String f, String l, String r) {
            this.id = id; firstName = f; lastName = l; role = r;
        }
    }



    // ===================== INNER CLASS InsuranceItem =====================
    public static class InsuranceItem {
        public int id;
        public int customerId;
        public String customerName; // display
        public String insuranceType;
        public String insuranceDescription;
        public String bookingReference;
        public String createdAt;

        public InsuranceItem(int id, int customerId, String type,
                             String description, String bookingRef, String created) {
            this.id = id;
            this.customerId = customerId;
            this.insuranceType = type;
            this.insuranceDescription = description;
            this.bookingReference = bookingRef;
            this.createdAt = created;
        }
    }

    // ===================== 24h Cancellation Policy =====================

    public boolean cancelBooking(int bookingId, boolean isAdminCancellation) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            Cursor c = db.rawQuery("SELECT pickup_date, pickup_time, total_cost, status, vehicle_id " +
                            "FROM Reservation WHERE booking_id=?",
                    new String[]{String.valueOf(bookingId)});

            if (!c.moveToFirst()) {
                c.close();
                Log.e(TAG, "Booking #" + bookingId + " not found");
                return false;
            }

            String pickupDate = c.getString(0);
            String pickupTime = c.getString(1);
            double totalCost = c.getDouble(2);
            String currentStatus = c.getString(3);
            int vehicleId = c.getInt(4);
            c.close();

            if (currentStatus.equals("Cancelled")) {
                Log.e(TAG, "Booking already cancelled");
                return false;
            }

            // CALCULATE CANCELLATION FEE
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
                        Log.d(TAG, "⚠Cancellation within 24 hours - Fee: $" + cancellationFee);
                    } else {
                        Log.d(TAG, "Cancellation more than 24 hours before pickup - No fee");
                    }
                }
            } else {
                Log.d(TAG, "ℹ️ Admin cancellation - No fee charged");
            }

            // UPDATES RESERVATION
            ContentValues v = new ContentValues();
            v.put("status", "Cancelled");
            v.put("cancellation_fee", cancellationFee);

            int rows = db.update("Reservation", v, "booking_id=?",
                    new String[]{String.valueOf(bookingId)});

            // RESTORE VEHICLE STATUS
            if (rows > 0) {
                ContentValues vCar = new ContentValues();
                vCar.put("status", "Available");
                db.update("Vehicle", vCar, "vehicle_id=?", new String[]{String.valueOf(vehicleId)});

                Log.d(TAG, "Booking #" + bookingId + " cancelled successfully");
                Log.d(TAG, "Vehicle #" + vehicleId + " is now Available again.");
                return true;
            }

            return false;

        } catch (Exception e) {
            Log.e(TAG, "Error cancelling booking: " + e.getMessage(), e);
            return false;
        }
    }

    // ===================== Admin Approval =====================
    public boolean approveBooking(int bookingId, int employeeId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            // 1. Fetch details (including late_fee and payment_method)
            String query = "SELECT vehicle_id, total_cost, pickup_address, return_address, late_fee " +
                    "FROM Reservation WHERE booking_id=?";

            Cursor c = db.rawQuery(query, new String[]{String.valueOf(bookingId)});
            if (!c.moveToFirst()) { c.close(); return false; }

            int vehicleId = c.getInt(0);
            double totalCost = c.getDouble(1);
            String pickupAddress = c.getString(2);
            String returnAddress = c.getString(3);
            double reservationLateFee = c.getDouble(4);
            c.close();

            // 2. UPDATE RESERVATION -> Marks the booking as confirmed and paid
            ContentValues vReservation = new ContentValues();
            vReservation.put("status", "Confirmed");
            vReservation.put("payment_status", "Paid"); // <--- Updates Reservation Status
            db.update("Reservation", vReservation, "booking_id=?", new String[]{String.valueOf(bookingId)});

            // 3. UPDATE VEHICLE -> Rented
            ContentValues vCar = new ContentValues();
            vCar.put("status", "Rented");
            db.update("Vehicle", vCar, "vehicle_id=?", new String[]{String.valueOf(vehicleId)});

            // 4. CREATE RENTAL -> Generates the Rental ID
            ContentValues vRental = new ContentValues();
            vRental.put("reservation_id", bookingId);
            vRental.put("actual_pickup_address", pickupAddress);
            vRental.put("actual_return_address", returnAddress);
            vRental.put("total_amount", totalCost);
            vRental.put("late_return_fee", reservationLateFee);

            long rentalId = db.insert("Rental", null, vRental);
            if (rentalId == -1) throw new Exception("Failed to create rental");

            // [Employee links omitted...]

            // 5. CREATE PAYMENT RECORD -> Marks the official payment as Completed
            // This is where the record finally appears in your Payment Table
            ContentValues vPayment = new ContentValues();
            vPayment.put("rental_id", rentalId); // Now we have the ID!
            vPayment.put("amount", totalCost * 0.30);
            vPayment.put("payment_date", new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()));
            vPayment.put("payment_status", "Completed"); // <--- Final "Completed" Status

            db.insert("Payment", null, vPayment);

            db.setTransactionSuccessful();
            return true;

        } catch (Exception e) {
            Log.e("CarRentalData", "Error: " + e.getMessage());
            return false;
        } finally {
            db.endTransaction();
        }
    }

    public int getCurrentEmployeeId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("EmployeeSession", Context.MODE_PRIVATE);
        return prefs.getInt("EMPLOYEE_ID", 1);
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

        return res != -1;
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
        return rows > 0;
    }

    // ===================== Vehicle Management =====================

    public boolean addNewCarComplete(String make, String model, String type, int year,
                                     double price, String plate, String imageRes,
                                     String transmission, int seats,
                                     String color, String category, String fuelType) { // NEW ARGS
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long makeId = -1, typeId = -1, modelId = -1;

        db.beginTransaction();
        try {
            // Make Logic
            Cursor cMake = db.rawQuery("SELECT make_id FROM Make WHERE make_name=?", new String[]{make});
            if (cMake.moveToFirst()) makeId = cMake.getInt(0);
            else {
                ContentValues v = new ContentValues();
                v.put("make_name", make);
                makeId = db.insert("Make", null, v);
            }
            cMake.close();

            // Type
            Cursor cType = db.rawQuery("SELECT type_id FROM Type WHERE type_name=?", new String[]{type});
            if (cType.moveToFirst()) typeId = cType.getInt(0);
            else {
                ContentValues v = new ContentValues();
                v.put("type_name", type);
                typeId = db.insert("Type", null, v);
            }
            cType.close();

            // Model
            ContentValues vModel = new ContentValues();
            vModel.put("make_id", makeId);
            vModel.put("type_id", typeId);
            vModel.put("model_name", model);
            vModel.put("year", year);
            vModel.put("daily_rate", price);
            modelId = db.insert("VehicleModel", null, vModel);

            // Vehicle
            ContentValues vCar = new ContentValues();
            vCar.put("model_id", modelId);
            vCar.put("plt_number", plate);
            vCar.put("status", "Available");
            vCar.put("image_res_name", imageRes);
            vCar.put("transmission", transmission);
            vCar.put("seating_capacity", seats);
            vCar.put("color", color);
            vCar.put("category", category);
            vCar.put("fuel_type", fuelType);

            db.insert("Vehicle", null, vCar);

            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error adding vehicle", e);
            return false;
        } finally {
            db.endTransaction();
        }
    }

    // updateVehicle
    public boolean updateVehicle(int vehicleId, String make, String model, String type,
                                 double price, String imageRes, String transmission, int seats,
                                 String color, String category, String fuelType) { // NEW PARAMETERS
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            Cursor cVehicle = db.rawQuery("SELECT model_id FROM Vehicle WHERE vehicle_id=?",
                    new String[]{String.valueOf(vehicleId)});

            if (!cVehicle.moveToFirst()) {
                cVehicle.close();
                return false;
            }

            long modelId = cVehicle.getLong(0);
            cVehicle.close();

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

            ContentValues vModel = new ContentValues();
            vModel.put("make_id", makeId);
            vModel.put("type_id", typeId);
            vModel.put("model_name", model);
            vModel.put("daily_rate", price);

            db.update("VehicleModel", vModel, "model_id=?",
                    new String[]{String.valueOf(modelId)});


            ContentValues vVehicle = new ContentValues();
            vVehicle.put("image_res_name", imageRes);
            vVehicle.put("transmission", transmission);
            vVehicle.put("seating_capacity", seats);
            vVehicle.put("color", color);
            vVehicle.put("category", category);
            vVehicle.put("fuel_type", fuelType);

            int rows = db.update("Vehicle", vVehicle, "vehicle_id=?",
                    new String[]{String.valueOf(vehicleId)});

            db.setTransactionSuccessful();
            Log.d(TAG, "✓ Vehicle updated successfully with new details");
            return rows > 0;

        } catch (Exception e) {
            Log.e(TAG, "Error updating vehicle: " + e.getMessage(), e);
            return false;
        } finally {
            db.endTransaction();
        }
    }

    public void deleteVehicle(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("Vehicle", "vehicle_id=?", new String[]{String.valueOf(id)});
    }

    public List<VehicleItem> getAllVehicles() {
        List<VehicleItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT v.vehicle_id, mk.make_name, vm.model_name, t.type_name, " +
                "vm.daily_rate, v.status, v.image_res_name, v.transmission, v.seating_capacity, " +
                "v.plt_number, v.color, v.category, v.fuel_type " + // Added cols
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
                            c.getInt(0),      // id
                            title,            // title
                            c.getString(3),   // type (Body type like Hatchback)
                            c.getDouble(4),   // price
                            c.getString(5),   // status
                            c.getString(6),   // imageRes
                            c.getString(7),   // transmission
                            c.getInt(8),      // seats
                            c.getString(9),   // plate
                            c.getString(10),  // color
                            c.getString(11),  // category
                            c.getString(12)   // fuelType
                    ));
                } while (c.moveToNext());
            }
            c.close();
        } catch (Exception e) { Log.e(TAG, "Error: " + e.getMessage()); }
        return list;
    }

    // ===================== Admin Booking View =====================

    public List<AdminBookingItem> getAllBookingsForAdmin() {
        List<AdminBookingItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT r.booking_id, r.booking_reference, " +
                "c.first_name || ' ' || c.last_name as customer_name, " +
                "c.email, " +
                "c.phone, " +
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
                            c.getString(4),   // phone
                            c.getString(5),   // car_name
                            c.getString(6),   // pickup_date
                            c.getString(7),   // pickup_time
                            c.getString(8),   // pickup_address CUSTOM ADDRESS
                            c.getString(9),   // return_date
                            c.getString(10),  // return_time
                            c.getString(11),  // return_address CUSTOM ADDRESS
                            c.getString(12),  // status
                            c.getString(13),  // payment_method
                            c.getDouble(14)   // total_cost
                    ));
                } while(c.moveToNext());
            }
            c.close();
            Log.d(TAG, "Loaded " + list.size() + " bookings for admin");
        } catch(Exception e) {
            Log.e(TAG, "Error fetching admin bookings: " + e.getMessage(), e);
        }
        return list;
    }

    // ===================== ADMIN RETURN LOGIC =====================

    public boolean markBookingAsReturned(int bookingId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            // 1. Get vehicle_id from the booking
            Cursor c = db.rawQuery("SELECT vehicle_id FROM Reservation WHERE booking_id=?",
                    new String[]{String.valueOf(bookingId)});

            if (!c.moveToFirst()) {
                c.close();
                return false;
            }
            int vehicleId = c.getInt(0);
            c.close();

            // Updates Reservation Status to Completed
            ContentValues vRes = new ContentValues();
            vRes.put("status", "Completed");
            vRes.put("updated_at", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()));

            db.update("Reservation", vRes, "booking_id=?", new String[]{String.valueOf(bookingId)});

            // Updates Vehicle Status back to Available after returned
            ContentValues vVeh = new ContentValues();
            vVeh.put("status", "Available");
            db.update("Vehicle", vVeh, "vehicle_id=?", new String[]{String.valueOf(vehicleId)});

            db.setTransactionSuccessful();
            Log.d(TAG, "Booking #" + bookingId + " returned. Vehicle #" + vehicleId + " is now Available.");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error returning vehicle: " + e.getMessage());
            return false;
        } finally {
            db.endTransaction();
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
                    list.add(new BookingItem(c.getInt(0), c.getString(1), car, dates,
                            c.getString(6), c.getDouble(7)));
                } while(c.moveToNext());
            }
            c.close();
        } catch(Exception e) {
            Log.e(TAG, "Error fetching bookings: " + e.getMessage(), e);
        }
        return list;
    }

    // ===================== Booking Details Methods =====================

    public BookingDetailItem getBookingDetails(int bookingId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        BookingDetailItem booking = null;

        String query = "SELECT r.booking_id, r.booking_reference, r.status, " +
                "c.first_name || ' ' || c.last_name as customer_name, " +
                "c.email, " +
                "c.phone, " +
                "mk.make_name || ' ' || vm.model_name as car_name, " +
                "v.image_res_name, " +
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
                        c.getString(5),   // customer_phone  NOW INCLUDED
                        c.getString(6),   // car_name
                        c.getString(7),   // car_image
                        c.getString(8),   // pickup_date
                        c.getString(9),   // pickup_time
                        c.getString(10),  // return_date
                        c.getString(11),  // return_time
                        c.getString(12),  // pickup_address  CUSTOM ADDRESS
                        c.getString(13),  // return_address CUSTOM ADDRESS
                        c.getDouble(14),  // total_cost
                        c.getString(15),  // payment_method
                        c.getString(16)   // payment_receipt
                );
                Log.d(TAG, "Loaded booking details for ID: " + bookingId);
            } else {
                Log.e(TAG, "No booking found with ID: " + bookingId);
            }
            c.close();
        } catch (Exception e) {
            Log.e(TAG, "Error fetching booking details: " + e.getMessage(), e);
        }
        return booking;
    }

    // ===================== @getCustomerBookings =====================
    public List<CustomerBookingItem> getCustomerBookings(int customerId) {
        List<CustomerBookingItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT r.booking_id, r.booking_reference, " +
                "mk.make_name || ' ' || vm.model_name as car_name, " +
                "v.image_res_name, " +
                "r.pickup_date, r.return_date, " +
                "r.status, r.total_cost, r.cancellation_fee " +
                "FROM Reservation r " +
                "JOIN Vehicle v ON r.vehicle_id = v.vehicle_id " +
                "JOIN VehicleModel vm ON v.model_id = vm.model_id " +
                "JOIN Make mk ON vm.make_id = mk.make_id " +
                "WHERE r.customer_num = ? " +
                "ORDER BY r.booking_id DESC";

        try {
            Cursor c = db.rawQuery(query, new String[]{String.valueOf(customerId)});
            if (c.moveToFirst()) {
                do {
                    list.add(new CustomerBookingItem(
                            c.getInt(0),      // booking_id
                            c.getString(1),   // booking_reference
                            c.getString(2),   // car_name
                            c.getString(3),   // car_image
                            c.getString(4),   // pickup_date
                            c.getString(5),   // return_date
                            c.getString(6),   // status
                            c.getDouble(7),   // total_cost
                            c.isNull(8) ? 0 : c.getDouble(8)  // cancellation_fee
                    ));
                } while (c.moveToNext());
            }
            c.close();
            Log.d(TAG, "Loaded " + list.size() + " bookings for customer ID: " + customerId);
        } catch (Exception e) {
            Log.e(TAG, "Error fetching customer bookings: " + e.getMessage(), e);
        }
        return list;
    }

    // ===================== DEBUG METHODS =====================

    public void debugMaintenanceRecords() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT m.mntnc_id, " +
                "mk.make_name || ' ' || vm.model_name as car, " +
                "e.first_name || ' ' || e.last_name as mechanic, " +
                "m.mntnc_date, m.cost, m.description " +
                "FROM MaintenanceRecord m " +
                "JOIN Employee e ON m.employee_id = e.employee_id " +
                "JOIN Vehicle v ON m.vehicle_id = v.vehicle_id " +
                "JOIN VehicleModel vm ON v.model_id = vm.model_id " +
                "JOIN Make mk ON vm.make_id = mk.make_id " +
                "ORDER BY m.mntnc_id DESC";

        Cursor c = db.rawQuery(query, null);

        Log.d(TAG, "=================================");
        Log.d(TAG, "=== MAINTENANCE RECORDS DEBUG ===");
        Log.d(TAG, "=================================");

        if (c.moveToFirst()) {
            do {
                Log.d(TAG, "Record #" + c.getInt(0));
                Log.d(TAG, "  Car: " + c.getString(1));
                Log.d(TAG, "  Mechanic: " + c.getString(2));  // ← Should show full name
                Log.d(TAG, "  Date: " + c.getString(3));
                Log.d(TAG, "  Cost: ₱" + c.getDouble(4));
                Log.d(TAG, "  Description: " + c.getString(5));
                Log.d(TAG, "---------------------------------");
            } while (c.moveToNext());
            Log.d(TAG, "Total: " + c.getCount() + " maintenance records");
        } else {
            Log.d(TAG, "No maintenance records found");
        }

        c.close();
        Log.d(TAG, "=================================");
    }

    public void debugListAllReservations() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT r.booking_id, r.booking_reference, " +
                "c.first_name || ' ' || c.last_name as customer, c.phone, " +
                "mk.make_name || ' ' || vm.model_name as car, " +
                "r.pickup_date, r.pickup_time, r.pickup_address, " +
                "r.return_date, r.return_time, r.return_address, " +
                "r.status, r.total_cost, r.payment_method " +
                "FROM Reservation r " +
                "JOIN Customer c ON r.customer_num = c.customer_id " +
                "JOIN Vehicle v ON r.vehicle_id = v.vehicle_id " +
                "JOIN VehicleModel vm ON v.model_id = vm.model_id " +
                "JOIN Make mk ON vm.make_id = mk.make_id " +
                "ORDER BY r.booking_id DESC";

        Cursor c = db.rawQuery(query, null);

        Log.d(TAG, "=================================");
        Log.d(TAG, "=== ALL RESERVATIONS IN DATABASE ===");
        Log.d(TAG, "=================================");

        if (c.moveToFirst()) {
            do {
                Log.d(TAG, "Booking #" + c.getInt(0) + " | Ref: " + c.getString(1));
                Log.d(TAG, "  Customer: " + c.getString(2) + " | Phone: " + c.getString(3));
                Log.d(TAG, "  Car: " + c.getString(4));
                Log.d(TAG, "  Pickup: " + c.getString(5) + " " + c.getString(6) + " at " + c.getString(7));
                Log.d(TAG, "  Return: " + c.getString(8) + " " + c.getString(9) + " at " + c.getString(10));
                Log.d(TAG, "  Status: " + c.getString(11) + " | Cost: $" + c.getDouble(12));
                Log.d(TAG, "  Payment: " + c.getString(13));
                Log.d(TAG, "---------------------------------");
            } while (c.moveToNext());
            Log.d(TAG, "Total: " + c.getCount() + " reservations");
        } else {
            Log.d(TAG, "No reservations found in database");
        }

        c.close();
        Log.d(TAG, "=================================");
    }

    public void debugListAllVehicles() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT v.vehicle_id, mk.make_name, vm.model_name, t.type_name, " +
                "vm.daily_rate, v.status, v.transmission, v.seating_capacity, v.plt_number " +
                "FROM Vehicle v " +
                "JOIN VehicleModel vm ON v.model_id = vm.model_id " +
                "JOIN Make mk ON vm.make_id = mk.make_id " +
                "JOIN Type t ON vm.type_id = t.type_id";

        Cursor c = db.rawQuery(query, null);

        Log.d(TAG, "=================================");
        Log.d(TAG, "=== ALL VEHICLES IN DATABASE ===");
        Log.d(TAG, "=================================");

        if (c.moveToFirst()) {
            do {
                Log.d(TAG, "Vehicle #" + c.getInt(0) + " | " + c.getString(1) + " " + c.getString(2));
                Log.d(TAG, "  Type: " + c.getString(3) + " | Rate: $" + c.getDouble(4));
                Log.d(TAG, "  Status: " + c.getString(5) + " | " + c.getString(6) + " | " + c.getInt(7) + " seats");
                Log.d(TAG, "  Plate: " + c.getString(8));
                Log.d(TAG, "---------------------------------");
            } while (c.moveToNext());
            Log.d(TAG, "Total: " + c.getCount() + " vehicles");
        } else {
            Log.d(TAG, "No vehicles found in database");
        }

        c.close();
        Log.d(TAG, "=================================");
    }

    public void debugPrintBooking(int bookingId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM Reservation WHERE booking_id=?",
                new String[]{String.valueOf(bookingId)});

        if (c.moveToFirst()) {
            Log.d(TAG, "=== BOOKING DETAILS ===");
            for (int i = 0; i < c.getColumnCount(); i++) {
                String colName = c.getColumnName(i);
                String value = c.getString(i);
                Log.d(TAG, colName + ": " + value);
            }
        } else {
            Log.e(TAG, "Booking #" + bookingId + " not found!");
        }

        c.close();
    }

    public void debugTableCounts() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] tables = {"Customer", "Vehicle", "VehicleModel", "Make", "Type",
                "Reservation", "Location", "Rental", "Payment"};

        Log.d(TAG, "=== DATABASE TABLE COUNTS ===");
        for (String table : tables) {
            Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + table, null);
            if (c.moveToFirst()) {
                Log.d(TAG, table + ": " + c.getInt(0) + " rows");
            }
            c.close();
        }
        Log.d(TAG, "============================");
    }

    public void debugListAllEmployees() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT employee_id, first_name, last_name, email, role FROM Employee", null);

        Log.d(TAG, "=================================");
        Log.d(TAG, "=== ALL EMPLOYEES IN DATABASE ===");
        Log.d(TAG, "=================================");

        if (c.moveToFirst()) {
            do {
                Log.d(TAG, "ID: " + c.getInt(0) +
                        " | Name: " + c.getString(1) + " " + c.getString(2) +
                        " | Email: " + c.getString(3) +
                        " | Role: " + c.getString(4));
            } while (c.moveToNext());
            Log.d(TAG, "Total: " + c.getCount() + " employees");
        } else {
            Log.d(TAG, "No employees found - DATABASE MAY NEED TO BE RECREATED");
        }
        c.close();
        Log.d(TAG, "=================================");
    }


    // ===================== Inner Classes =====================

    public static class EmployeeReservationItem {
        public int resvId, employeeId, bookingId;
        public String employeeName, role, bookingReference, asgnDate;

        public EmployeeReservationItem(int resvId, int empId, String empName,
                                       String role, int bookingId,
                                       String bookingRef, String date) {
            this.resvId = resvId;
            this.employeeId = empId;
            this.employeeName = empName;
            this.role = role;
            this.bookingId = bookingId;
            this.bookingReference = bookingRef;
            this.asgnDate = date;
        }
    }

    public static class EmployeeRentalItem {
        public int erId, employeeId, rentalId, reservationId;
        public String employeeName, role;

        public EmployeeRentalItem(int erId, int empId, String empName,
                                  int rentalId, int reservationId, String role) {
            this.erId = erId;
            this.employeeId = empId;
            this.employeeName = empName;
            this.rentalId = rentalId;
            this.reservationId = reservationId;
            this.role = role;
        }
    }

    public static class VehicleItem {
        public int id;
        public String title, type, status, imageRes, transmission;
        public double price;
        public int seats;
        public String plate, color, category, fuelType;

        public VehicleItem(int id, String t, String ty, double p, String s,
                           String img, String trans, int seats,
                           String plt, String col, String cat, String fuel) {
            this.id = id; this.title = t; this.type = ty; this.price = p;
            this.status = s; this.imageRes = img; this.transmission = trans; this.seats = seats;
            this.plate = plt; this.color = col; this.category = cat; this.fuelType = fuel;
        }
    }


    public static class CustomerBookingItem {
        public int id;
        public String bookingReference, carName, carImage;
        public String pickupDate, returnDate, status;
        public double totalCost, cancellationFee;

        public CustomerBookingItem(int id, String ref, String car, String img,
                                   String pDate, String rDate, String status,
                                   double cost, double cancelFee) {
            this.id = id;
            this.bookingReference = ref;
            this.carName = car;
            this.carImage = img;
            this.pickupDate = pDate;
            this.returnDate = rDate;
            this.status = status;
            this.totalCost = cost;
            this.cancellationFee = cancelFee;
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
        public String bookingReference, customerName, customerEmail, customerPhone;
        public String carName;
        public String pickupDate, pickupTime, pickupAddress;
        public String returnDate, returnTime, returnAddress;
        public String status, paymentMethod;
        public double totalCost;

        public AdminBookingItem(int id, String ref, String custName, String custEmail,
                                String custPhone,
                                String car, String pDate, String pTime, String pAddr,
                                String rDate, String rTime, String rAddr,
                                String stat, String payment, double cost) {
            this.id = id;
            this.bookingReference = ref;
            this.customerName = custName;
            this.customerEmail = custEmail;
            this.customerPhone = custPhone;
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