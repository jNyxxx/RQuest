package com.example.ridequest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "RideQuest.db";
    private static final int DATABASE_VERSION = 10; // Updated version

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Location Table
        db.execSQL("CREATE TABLE Location (" +
                "location_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "location_name TEXT, " +
                "address TEXT, " +
                "contact_num TEXT)");

        // Make Table
        db.execSQL("CREATE TABLE Make (" +
                "make_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "make_name TEXT)");

        // Type Table
        db.execSQL("CREATE TABLE Type (" +
                "type_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "type_name TEXT)");

        // Customer Table
        db.execSQL("CREATE TABLE Customer (" +
                "customer_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "first_name TEXT, " +
                "last_name TEXT, " +
                "email TEXT UNIQUE, " +
                "password TEXT, " +
                "phone TEXT UNIQUE, " +
                "date_of_birth TEXT, " +
                "drivers_license TEXT, " +
                "license_verified INTEGER DEFAULT 0, " +
                "address TEXT, " +
                "created_date TEXT DEFAULT CURRENT_TIMESTAMP)");

        // Employee Table
        db.execSQL("CREATE TABLE Employee (" +
                "employee_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "first_name TEXT, " +
                "last_name TEXT, " +
                "role TEXT, " +
                "location_id INTEGER, " +
                "FOREIGN KEY(location_id) REFERENCES Location(location_id))");

        // VehicleModel Table
        db.execSQL("CREATE TABLE VehicleModel (" +
                "model_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "make_id INTEGER, " +
                "type_id INTEGER, " +
                "model_name TEXT, " +
                "year INTEGER, " +
                "daily_rate REAL, " +
                "FOREIGN KEY(make_id) REFERENCES Make(make_id), " +
                "FOREIGN KEY(type_id) REFERENCES Type(type_id))");

        // Vehicle Table
        db.execSQL("CREATE TABLE Vehicle (" +
                "vehicle_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "model_id INTEGER, " +
                "location_id INTEGER, " +
                "vin TEXT, " +
                "plt_number TEXT UNIQUE, " +
                "status TEXT DEFAULT 'Available', " +
                "curr_mileage INTEGER, " +
                "fuel_level TEXT, " +
                "transmission TEXT, " +
                "seating_capacity INTEGER, " +
                "image_res_name TEXT, " +
                "last_inspection_date TEXT, " +
                "car_mileage TEXT, " +
                "FOREIGN KEY(model_id) REFERENCES VehicleModel(model_id), " +
                "FOREIGN KEY(location_id) REFERENCES Location(location_id))");

        // MaintenanceRecord Table
        db.execSQL("CREATE TABLE MaintenanceRecord (" +
                "mntnc_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "vehicle_id INTEGER, " +
                "employee_id INTEGER, " +
                "mntnc_date TEXT, " +
                "description TEXT, " +
                "cost REAL, " +
                "FOREIGN KEY(vehicle_id) REFERENCES Vehicle(vehicle_id), " +
                "FOREIGN KEY(employee_id) REFERENCES Employee(employee_id))");

        // Reservation Table
        db.execSQL("CREATE TABLE Reservation (" +
                "booking_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "customer_num INTEGER, " +
                "vehicle_id INTEGER, " +
                "reservation_date TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "pickup_date TEXT, " +
                "return_date TEXT, " +
                "pickup_time TEXT, " +
                "return_time TEXT, " +
                "pickup_loc_id INTEGER, " +
                "return_loc_id INTEGER, " +
                "pickup_address TEXT, " +
                "return_address TEXT, " +
                "status TEXT DEFAULT 'Pending', " +
                "total_cost REAL, " +
                "booking_reference TEXT UNIQUE, " +
                "payment_method TEXT, " +
                "payment_id TEXT, " +
                "payment_status TEXT DEFAULT 'Pending', " +
                "cancellation_date TEXT, " +
                "cancellation_fee REAL DEFAULT 0, " +
                "created_at TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY(customer_num) REFERENCES Customer(customer_id), " +
                "FOREIGN KEY(vehicle_id) REFERENCES Vehicle(vehicle_id), " +
                "FOREIGN KEY(pickup_loc_id) REFERENCES Location(location_id), " +
                "FOREIGN KEY(return_loc_id) REFERENCES Location(location_id))");

        // Rental Table
        db.execSQL("CREATE TABLE Rental (" +
                "rental_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "reservation_id INTEGER UNIQUE, " +
                "pickup_odo INTEGER, " +
                "return_odo INTEGER, " +
                "pickup_loc_id INTEGER, " +
                "return_loc_id INTEGER, " +
                "actual_pickup_dt TEXT, " +
                "actual_return_dt TEXT, " +
                "pickup_fuel_level TEXT, " +
                "return_fuel_level TEXT, " +
                "total_amount REAL, " +
                "late_return_fee REAL DEFAULT 0, " +
                "refueling_fee REAL DEFAULT 0, " +
                "FOREIGN KEY(reservation_id) REFERENCES Reservation(booking_id))");

        // Payment Table
        db.execSQL("CREATE TABLE Payment (" +
                "payment_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "rental_id INTEGER, " +
                "payment_date TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "amount REAL, " +
                "payment_mthd TEXT, " +
                "payment_status TEXT DEFAULT 'Pending', " +
                "FOREIGN KEY(rental_id) REFERENCES Rental(rental_id))");

        // Inspection Table
        db.execSQL("CREATE TABLE Inspection (" +
                "inspection_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "rental_id INTEGER, " +
                "vehicle_id INTEGER, " +
                "inspection_type TEXT, " +
                "inspection_date TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "mileage INTEGER, " +
                "fuel_level TEXT, " +
                "condition_notes TEXT, " +
                "damage_report TEXT, " +
                "inspector_id INTEGER, " +
                "photos TEXT, " +
                "FOREIGN KEY(rental_id) REFERENCES Rental(rental_id), " +
                "FOREIGN KEY(vehicle_id) REFERENCES Vehicle(vehicle_id), " +
                "FOREIGN KEY(inspector_id) REFERENCES Employee(employee_id))");

        // AccidentReport Table
        db.execSQL("CREATE TABLE AccidentReport (" +
                "report_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "rental_id INTEGER, " +
                "vehicle_id INTEGER, " +
                "customer_id INTEGER, " +
                "accident_date TEXT, " +
                "description TEXT, " +
                "police_report_num TEXT, " +
                "photos TEXT, " +
                "damage_cost REAL, " +
                "status TEXT DEFAULT 'Under Review', " +
                "created_at TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY(rental_id) REFERENCES Rental(rental_id), " +
                "FOREIGN KEY(vehicle_id) REFERENCES Vehicle(vehicle_id), " +
                "FOREIGN KEY(customer_id) REFERENCES Customer(customer_id))");

        // Insurance Table
        db.execSQL("CREATE TABLE Insurance (" +
                "insurance_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "customer_id INTEGER, " +
                "insurance_provider TEXT, " +
                "policy_number TEXT, " +
                "expiry_date TEXT, " +
                "verified INTEGER DEFAULT 0, " +
                "FOREIGN KEY(customer_id) REFERENCES Customer(customer_id))");

        // EmployeeRental Table
        db.execSQL("CREATE TABLE EmployeeRental (" +
                "er_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "employee_id INTEGER, " +
                "rental_id INTEGER, " +
                "role TEXT, " +
                "FOREIGN KEY(employee_id) REFERENCES Employee(employee_id), " +
                "FOREIGN KEY(rental_id) REFERENCES Rental(rental_id))");

        // EmployeeReservation Table
        db.execSQL("CREATE TABLE EmployeeReservation (" +
                "resv_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "employee_id INTEGER, " +
                "booking_id INTEGER, " +
                "asgn_date TEXT, " +
                "FOREIGN KEY(employee_id) REFERENCES Employee(employee_id), " +
                "FOREIGN KEY(booking_id) REFERENCES Reservation(booking_id))");

        // Seed Data
        db.execSQL("INSERT INTO Location (location_name, address, contact_num) VALUES " +
                "('Cebu HQ', 'Cebu City, Philippines', '+63-123-4567')");

        db.execSQL("INSERT INTO Customer (first_name, last_name, email, password, phone) VALUES " +
                "('Admin', 'User', 'admin', 'admin123', '000')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String[] tables = {"AccidentReport", "Insurance", "Inspection",
                "EmployeeReservation", "EmployeeRental", "Payment",
                "Rental", "Reservation", "MaintenanceRecord",
                "Vehicle", "VehicleModel", "Employee", "Customer",
                "Type", "Make", "Location"};
        for (String table : tables) {
            db.execSQL("DROP TABLE IF EXISTS " + table);
        }
        onCreate(db);
    }
}