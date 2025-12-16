package com.example.ridequest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "RideQuest.db";
    private static final int DATABASE_VERSION = 20; // Increment version to trigger onUpgrade

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
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
                "first_name TEXT NOT NULL, " +
                "last_name TEXT NOT NULL, " +
                "email TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "phone TEXT UNIQUE NOT NULL, " +
                "date_of_birth TEXT NOT NULL, " +
                "drivers_license TEXT NOT NULL, " +
                "license_verified INTEGER DEFAULT 0, " +
                "address TEXT NOT NULL, " +
                "created_date TEXT DEFAULT CURRENT_TIMESTAMP)");

        // Employee Table
        db.execSQL("CREATE TABLE Employee (" +
                "employee_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "first_name TEXT, " +
                "last_name TEXT, " +
                "email TEXT UNIQUE, " +
                "password TEXT, " +
                "role TEXT)");

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
                "plt_number TEXT UNIQUE, " +
                "status TEXT DEFAULT 'Available', " +
                "fuel_level TEXT, " +
                "transmission TEXT DEFAULT 'Manual', " +
                "seating_capacity INTEGER DEFAULT 5, " +
                "image_res_name TEXT, " +
                "last_inspection_date TEXT, " +
                "color TEXT, " +
                "category TEXT, " +
                "fuel_type TEXT, " +
                "FOREIGN KEY(model_id) REFERENCES VehicleModel(model_id))");

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
                "customer_num INTEGER NOT NULL, " +
                "vehicle_id INTEGER NOT NULL, " +
                "reservation_date TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "pickup_date TEXT NOT NULL, " +
                "return_date TEXT NOT NULL, " +
                "pickup_time TEXT NOT NULL, " +
                "return_time TEXT NOT NULL, " +
                "pickup_address TEXT NOT NULL, " +
                "return_address TEXT NOT NULL, " +
                "status TEXT DEFAULT 'Pending', " +
                "total_cost REAL NOT NULL, " +
                "booking_reference TEXT UNIQUE, " +
                "payment_method TEXT, " +
                "payment_id TEXT, " +
                "payment_status TEXT DEFAULT 'Pending', " +
                "cancellation_fee REAL DEFAULT 0, " +
                "rental_days INTEGER DEFAULT 1, " +
                "late_hours INTEGER DEFAULT 0, " +
                "late_fee REAL DEFAULT 0, " +
                "base_cost REAL, " +
                "insurance_type TEXT, " +
                "insurance_fee REAL DEFAULT 0, " +
                "created_at TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY(customer_num) REFERENCES Customer(customer_id), " +
                "FOREIGN KEY(vehicle_id) REFERENCES Vehicle(vehicle_id))");

        // Rental Table
        db.execSQL("CREATE TABLE Rental (" +
                "rental_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "reservation_id INTEGER UNIQUE, " +
                "actual_pickup_address TEXT, " +
                "actual_return_address TEXT, " +
                "pickup_fuel_level TEXT, " +
                "return_fuel_level TEXT, " +
                "total_amount REAL, " +
                "late_return_fee REAL DEFAULT 0, " +
                "FOREIGN KEY(reservation_id) REFERENCES Reservation(booking_id))");

        // PAYMENT TABLE
        db.execSQL("CREATE TABLE Payment (" +
                "payment_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "rental_id INTEGER, " +
                "payment_date TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "amount REAL, " +
                "payment_status TEXT DEFAULT 'Pending', " +
                "FOREIGN KEY(rental_id) REFERENCES Rental(rental_id))");

        // Inspection Table
        db.execSQL("CREATE TABLE Inspection (" +
                "inspection_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "rental_id INTEGER, " +
                "vehicle_id INTEGER, " +
                "inspection_type TEXT, " +
                "inspection_date TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "fuel_level TEXT, " +
                "condition_notes TEXT, " +
                "damage_report TEXT, " +
                "inspector_id INTEGER, " +
                "photos TEXT, " +
                "FOREIGN KEY(rental_id) REFERENCES Rental(rental_id), " +
                "FOREIGN KEY(vehicle_id) REFERENCES Vehicle(vehicle_id), " +
                "FOREIGN KEY(inspector_id) REFERENCES Employee(employee_id))");

        // Insurance Table
        db.execSQL("CREATE TABLE Insurance (" +
                "insurance_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "customer_id INTEGER NOT NULL, " +
                "insurance_type TEXT NOT NULL, " +
                "insurance_description TEXT, " +
                "booking_reference TEXT, " +
                "created_at TEXT DEFAULT CURRENT_TIMESTAMP, " +
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

        // Favorites Table
        db.execSQL("CREATE TABLE Favorites (" +
                "fav_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "customer_id INTEGER NOT NULL, " +
                "vehicle_id INTEGER NOT NULL, " +
                "created_at TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY(customer_id) REFERENCES Customer(customer_id), " +
                "FOREIGN KEY(vehicle_id) REFERENCES Vehicle(vehicle_id), " +
                "UNIQUE(customer_id, vehicle_id))");

        // Insert sample data
        insertSampleData(db);
    }

    private void insertSampleData(SQLiteDatabase db) {
        // Insert Employees
        db.execSQL("INSERT INTO Employee (first_name, last_name, email, password, role) VALUES " +
                "('Keith', 'Justin', 'admin', 'admin123', 'Manager')");

        db.execSQL("INSERT INTO Employee (first_name, last_name, email, password, role) VALUES " +
                "('James', 'Bond', 'agent', 'agent123', 'Inspection Agent')");

        db.execSQL("INSERT INTO Employee (first_name, last_name, email, password, role) VALUES " +
                "('Fix', 'It', 'mechanic', 'mech123', 'Mechanic Agent')");

        // Insert Sample Customer
        db.execSQL("INSERT INTO Customer (first_name, last_name, email, password, phone, date_of_birth, drivers_license, license_verified, address) VALUES " +
                "('Keith', 'Justin', 'keith', 'keith123', '09123456789', '2003-09-14', 'DL-2025-987654', 0, 'Cebu City, Philippines')");


        // Insert Makes
        db.execSQL("INSERT INTO Make (make_name) VALUES ('Toyota')");
        db.execSQL("INSERT INTO Make (make_name) VALUES ('Honda')");
        db.execSQL("INSERT INTO Make (make_name) VALUES ('Ford')");
        db.execSQL("INSERT INTO Make (make_name) VALUES ('Nissan')");
        db.execSQL("INSERT INTO Make (make_name) VALUES ('Chevrolet')");
        db.execSQL("INSERT INTO Make (make_name) VALUES ('Hyundai')");

        // Insert Types
        db.execSQL("INSERT INTO Type (type_name) VALUES ('Sedan')");
        db.execSQL("INSERT INTO Type (type_name) VALUES ('SUV')");
        db.execSQL("INSERT INTO Type (type_name) VALUES ('Hatchback')");
        db.execSQL("INSERT INTO Type (type_name) VALUES ('Van')");
        db.execSQL("INSERT INTO Type (type_name) VALUES ('Pickup Truck')");
        db.execSQL("INSERT INTO Type (type_name) VALUES ('Sports Car')");

        // Insert Vehicle Models with daily rates
        db.execSQL("INSERT INTO VehicleModel (make_id, type_id, model_name, year, daily_rate) VALUES " +
                "(1, 1, 'Camry', 2023, 2500.00)"); // Toyota Camry Sedan
        db.execSQL("INSERT INTO VehicleModel (make_id, type_id, model_name, year, daily_rate) VALUES " +
                "(1, 2, 'RAV4', 2023, 3500.00)"); // Toyota RAV4 SUV
        db.execSQL("INSERT INTO VehicleModel (make_id, type_id, model_name, year, daily_rate) VALUES " +
                "(2, 1, 'Civic', 2023, 2200.00)"); // Honda Civic Sedan
        db.execSQL("INSERT INTO VehicleModel (make_id, type_id, model_name, year, daily_rate) VALUES " +
                "(2, 2, 'CR-V', 2023, 3200.00)"); // Honda CR-V SUV
        db.execSQL("INSERT INTO VehicleModel (make_id, type_id, model_name, year, daily_rate) VALUES " +
                "(3, 5, 'F-150', 2023, 4000.00)"); // Ford F-150 Pickup
        db.execSQL("INSERT INTO VehicleModel (make_id, type_id, model_name, year, daily_rate) VALUES " +
                "(4, 1, 'Altima', 2023, 2300.00)"); // Nissan Altima Sedan
        db.execSQL("INSERT INTO VehicleModel (make_id, type_id, model_name, year, daily_rate) VALUES " +
                "(5, 4, 'Suburban', 2023, 4500.00)"); // Chevrolet Suburban Van
        db.execSQL("INSERT INTO VehicleModel (make_id, type_id, model_name, year, daily_rate) VALUES " +
                "(6, 3, 'Accent', 2023, 1800.00)"); // Hyundai Accent Hatchback

        // Insert Vehicles with images
        // Note: Replace these image names with your actual drawable file names

        // Toyota Camry - White Sedan
        db.execSQL("INSERT INTO Vehicle (model_id, plt_number, status, fuel_level, transmission, seating_capacity, image_res_name, last_inspection_date, color, category, fuel_type) VALUES " +
                "(1, 'ABC-1234', 'Available', '100', 'Automatic', 5, 'car_sedan_white', '2024-12-01', 'White', 'Sedan', 'Gasoline')");

        // Toyota RAV4 - Black SUV
        db.execSQL("INSERT INTO Vehicle (model_id, plt_number, status, fuel_level, transmission, seating_capacity, image_res_name, last_inspection_date, color, category, fuel_type) VALUES " +
                "(2, 'XYZ-5678', 'Available', '95', 'Automatic', 7, 'car_suv_black', '2024-11-28', 'Black', 'SUV', 'Gasoline')");

        // Honda Civic - Red Sedan
        db.execSQL("INSERT INTO Vehicle (model_id, plt_number, status, fuel_level, transmission, seating_capacity, image_res_name, last_inspection_date, color, category, fuel_type) VALUES " +
                "(3, 'DEF-9012', 'Available', '90', 'Manual', 5, 'car_sedan_red', '2024-12-05', 'Red', 'Sedan', 'Gasoline')");

        // Honda CR-V - Silver SUV
        db.execSQL("INSERT INTO Vehicle (model_id, plt_number, status, fuel_level, transmission, seating_capacity, image_res_name, last_inspection_date, color, category, fuel_type) VALUES " +
                "(4, 'GHI-3456', 'Available', '85', 'Automatic', 7, 'car_suv_silver', '2024-11-20', 'Silver', 'SUV', 'Diesel')");

        // Ford F-150 - Blue Pickup
        db.execSQL("INSERT INTO Vehicle (model_id, plt_number, status, fuel_level, transmission, seating_capacity, image_res_name, last_inspection_date, color, category, fuel_type) VALUES " +
                "(5, 'JKL-7890', 'Available', '100', 'Automatic', 5, 'car_pickup_blue', '2024-12-10', 'Blue', 'Pickup Truck', 'Diesel')");

        // Nissan Altima - Gray Sedan
        db.execSQL("INSERT INTO Vehicle (model_id, plt_number, status, fuel_level, transmission, seating_capacity, image_res_name, last_inspection_date, color, category, fuel_type) VALUES " +
                "(6, 'MNO-2468', 'Available', '80', 'Automatic', 5, 'car_sedan_gray', '2024-11-25', 'Gray', 'Sedan', 'Gasoline')");

        // Chevrolet Suburban - White Van
        db.execSQL("INSERT INTO Vehicle (model_id, plt_number, status, fuel_level, transmission, seating_capacity, image_res_name, last_inspection_date, color, category, fuel_type) VALUES " +
                "(7, 'PQR-1357', 'Available', '95', 'Automatic', 8, 'car_van_white', '2024-12-08', 'White', 'Van', 'Gasoline')");

        // Hyundai Accent - Blue Hatchback
        db.execSQL("INSERT INTO Vehicle (model_id, plt_number, status, fuel_level, transmission, seating_capacity, image_res_name, last_inspection_date, color, category, fuel_type) VALUES " +
                "(8, 'STU-9753', 'Available', '88', 'Manual', 5, 'car_hatchback_blue', '2024-12-03', 'Blue', 'Hatchback', 'Gasoline')");

        // Additional vehicles
        db.execSQL("INSERT INTO Vehicle (model_id, plt_number, status, fuel_level, transmission, seating_capacity, image_res_name, last_inspection_date, color, category, fuel_type) VALUES " +
                "(9, 'VWX-1111', 'Rented', '75', 'Automatic', 5, 'car_gtr_orange', '2024-11-15', 'Orange', 'Sedan', 'Gasoline')");

        db.execSQL("INSERT INTO Vehicle (model_id, plt_number, status, fuel_level, transmission, seating_capacity, image_res_name, last_inspection_date, color, category, fuel_type) VALUES " +
                "(10, 'YZA-2222', 'Maintenance', '60', 'Automatic', 7, 'car_suv_red', '2024-10-20', 'Red', 'SUV', 'Gasoline')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String[] tables = {"Favorites" ,"Insurance", "Inspection",
                "EmployeeReservation", "EmployeeRental", "Payment",
                "Rental", "Reservation", "MaintenanceRecord",
                "Vehicle", "VehicleModel", "Employee", "Customer",
                "Type", "Make"};

        for (String table : tables) {
            db.execSQL("DROP TABLE IF EXISTS " + table);
        }
        onCreate(db);
    }
}