package com.example.ridequest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "RideQuest.db";
    private static final int DATABASE_VERSION = 9; // Forces a clean rebuild

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create 13 Tables matching ERD
        db.execSQL("CREATE TABLE Location (location_id INTEGER PRIMARY KEY AUTOINCREMENT, location_name TEXT, address TEXT, contact_num TEXT)");
        db.execSQL("CREATE TABLE Make (make_id INTEGER PRIMARY KEY AUTOINCREMENT, make_name TEXT)");
        db.execSQL("CREATE TABLE Type (type_id INTEGER PRIMARY KEY AUTOINCREMENT, type_name TEXT)");
        db.execSQL("CREATE TABLE Customer (customer_id INTEGER PRIMARY KEY AUTOINCREMENT, first_name TEXT, last_name TEXT, email TEXT UNIQUE, password TEXT, phone TEXT)");
        db.execSQL("CREATE TABLE Employee (employee_id INTEGER PRIMARY KEY AUTOINCREMENT, first_name TEXT, last_name TEXT, role TEXT, location_id INTEGER, FOREIGN KEY(location_id) REFERENCES Location(location_id))");

        db.execSQL("CREATE TABLE VehicleModel (model_id INTEGER PRIMARY KEY AUTOINCREMENT, make_id INTEGER, type_id INTEGER, model_name TEXT, year INTEGER, daily_rate REAL, FOREIGN KEY(make_id) REFERENCES Make(make_id), FOREIGN KEY(type_id) REFERENCES Type(type_id))");

        db.execSQL("CREATE TABLE Vehicle (vehicle_id INTEGER PRIMARY KEY AUTOINCREMENT, model_id INTEGER, location_id INTEGER, vin TEXT, plt_number TEXT, status TEXT, curr_mileage INTEGER, image_res_name TEXT, FOREIGN KEY(model_id) REFERENCES VehicleModel(model_id), FOREIGN KEY(location_id) REFERENCES Location(location_id))");

        db.execSQL("CREATE TABLE MaintenanceRecord (mntnc_id INTEGER PRIMARY KEY AUTOINCREMENT, vehicle_id INTEGER, employee_id INTEGER, mntnc_date TEXT, description TEXT, cost REAL, FOREIGN KEY(vehicle_id) REFERENCES Vehicle(vehicle_id), FOREIGN KEY(employee_id) REFERENCES Employee(employee_id))");

        // UPDATED RESERVATION TABLE WITH ALL REQUIRED FIELDS
        db.execSQL("CREATE TABLE Reservation (booking_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "customer_num INTEGER, " +
                "vehicle_id INTEGER, " +
                "reservation_date TEXT, " +
                "pickup_date TEXT, " +
                "return_date TEXT, " +
                "pickup_time TEXT, " +
                "return_time TEXT, " +
                "pickup_loc_id INTEGER, " +
                "return_loc_id INTEGER, " +
                "pickup_address TEXT, " +
                "return_address TEXT, " +
                "status TEXT, " +
                "total_cost REAL, " +
                "booking_reference TEXT, " +
                "payment_method TEXT, " +
                "FOREIGN KEY(customer_num) REFERENCES Customer(customer_id), " +
                "FOREIGN KEY(vehicle_id) REFERENCES Vehicle(vehicle_id))");

        db.execSQL("CREATE TABLE Rental (rental_id INTEGER PRIMARY KEY AUTOINCREMENT, reservation_id INTEGER UNIQUE, pickup_odo INTEGER, return_odo INTEGER, pickup_loc_id INTEGER, return_loc_id INTEGER, actual_pickup_dt TEXT, actual_return_dt TEXT, total_amount REAL, FOREIGN KEY(reservation_id) REFERENCES Reservation(booking_id))");
        db.execSQL("CREATE TABLE Payment (payment_id INTEGER PRIMARY KEY AUTOINCREMENT, rental_id INTEGER, payment_date TEXT, amount REAL, payment_mthd TEXT, FOREIGN KEY(rental_id) REFERENCES Rental(rental_id))");
        db.execSQL("CREATE TABLE EmployeeRental (er_id INTEGER PRIMARY KEY AUTOINCREMENT, employee_id INTEGER, rental_id INTEGER, role TEXT, FOREIGN KEY(employee_id) REFERENCES Employee(employee_id), FOREIGN KEY(rental_id) REFERENCES Rental(rental_id))");
        db.execSQL("CREATE TABLE EmployeeReservation (resv_id INTEGER PRIMARY KEY AUTOINCREMENT, employee_id INTEGER, booking_id INTEGER, asgn_date TEXT, FOREIGN KEY(employee_id) REFERENCES Employee(employee_id), FOREIGN KEY(booking_id) REFERENCES Reservation(booking_id))");

        // Seed Admin & Location
        db.execSQL("INSERT INTO Location (location_name, address) VALUES ('Cebu HQ', 'Cebu City, Philippines')");
        db.execSQL("INSERT INTO Customer (first_name, last_name, email, password, phone) VALUES ('Admin', 'User', 'admin', 'admin123', '000')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String[] tables = {"EmployeeReservation", "EmployeeRental", "Payment", "Rental", "Reservation", "MaintenanceRecord", "Vehicle", "VehicleModel", "Employee", "Customer", "Type", "Make", "Location"};
        for (String table : tables) {
            db.execSQL("DROP TABLE IF EXISTS " + table);
        }
        onCreate(db);
    }
}