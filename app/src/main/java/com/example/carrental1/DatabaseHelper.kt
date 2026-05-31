package com.example.carrental1

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "CarRental.db"
        private const val DATABASE_VERSION = 4 // Updated version for phone number and car seeding

        // Table Names
        const val TABLE_CARS = "cars"
        const val TABLE_USERS = "users"
        const val TABLE_BOOKINGS = "bookings"

        // CARS Columns
        const val CAR_ID = "car_id"
        const val CAR_BRAND = "brand"
        const val CAR_MODEL = "model"
        const val CAR_CODE = "car_code"
        const val CAR_CATEGORY = "category"
        const val CAR_ENGINE = "engine"
        const val CAR_POWER = "power"
        const val CAR_DRIVETRAIN = "drivetrain"
        const val CAR_SEATS = "seats"
        const val CAR_DESC = "description"
        const val CAR_PRICE_DAY = "price_per_day"
        const val CAR_PRICE_WEEK = "price_per_week"
        const val CAR_PRICE_MONTH = "price_per_month"
        const val CAR_IMAGES = "images" // Stored as comma-separated URLs
        const val CAR_AVAILABLE = "is_available"

        // USERS Columns
        const val USER_ID = "user_id"
        const val USER_NAME = "full_name"
        const val USER_EMAIL = "email"
        const val USER_PHONE = "phone"
        const val USER_PASSWORD = "password"

        // BOOKINGS Columns
        const val BOOKING_ID = "booking_id"
        const val BOOKING_USER_ID = "user_id_fk"
        const val BOOKING_CAR_NAME = "car_name"
        const val BOOKING_CAR_CODE = "car_code_fk"
        const val BOOKING_START = "start_date"
        const val BOOKING_END = "end_date"
        const val BOOKING_DURATION = "duration"
        const val BOOKING_COST = "total_cost"
        const val BOOKING_PAYMENT_METHOD = "payment_method"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createCarsTable = ("CREATE TABLE " + TABLE_CARS + " ("
                + CAR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + CAR_BRAND + " TEXT, "
                + CAR_MODEL + " TEXT NOT NULL, "
                + CAR_CODE + " TEXT, "
                + CAR_CATEGORY + " TEXT, "
                + CAR_ENGINE + " TEXT, "
                + CAR_POWER + " TEXT, "
                + CAR_DRIVETRAIN + " TEXT, "
                + CAR_SEATS + " INTEGER, "
                + CAR_DESC + " TEXT, "
                + CAR_PRICE_DAY + " REAL, "
                + CAR_PRICE_WEEK + " REAL, "
                + CAR_PRICE_MONTH + " REAL, "
                + CAR_IMAGES + " TEXT, "
                + CAR_AVAILABLE + " INTEGER DEFAULT 1);")

        val createUsersTable = ("CREATE TABLE " + TABLE_USERS + " ("
                + USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + USER_NAME + " TEXT NOT NULL, "
                + USER_EMAIL + " TEXT NOT NULL UNIQUE, "
                + USER_PHONE + " TEXT, "
                + USER_PASSWORD + " TEXT NOT NULL);")

        val createBookingsTable = ("CREATE TABLE " + TABLE_BOOKINGS + " ("
                + BOOKING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + BOOKING_USER_ID + " INTEGER, "
                + BOOKING_CAR_NAME + " TEXT, "
                + BOOKING_CAR_CODE + " TEXT, "
                + BOOKING_START + " TEXT, "
                + BOOKING_END + " TEXT, "
                + BOOKING_DURATION + " TEXT, "
                + BOOKING_COST + " REAL, "
                + BOOKING_PAYMENT_METHOD + " TEXT);")

        db.execSQL(createCarsTable)
        db.execSQL(createUsersTable)
        db.execSQL(createBookingsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Cars are always re-seeded from code, safe to recreate
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CARS")
        db.execSQL(
            "CREATE TABLE $TABLE_CARS (" +
            "$CAR_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$CAR_BRAND TEXT, $CAR_MODEL TEXT NOT NULL, $CAR_CODE TEXT, " +
            "$CAR_CATEGORY TEXT, $CAR_ENGINE TEXT, $CAR_POWER TEXT, $CAR_DRIVETRAIN TEXT, " +
            "$CAR_SEATS INTEGER, $CAR_DESC TEXT, " +
            "$CAR_PRICE_DAY REAL, $CAR_PRICE_WEEK REAL, $CAR_PRICE_MONTH REAL, " +
            "$CAR_IMAGES TEXT, $CAR_AVAILABLE INTEGER DEFAULT 1);"
        )

        // Version 4 added the phone column — add it without touching existing user data
        if (oldVersion < 4) {
            try {
                db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $USER_PHONE TEXT")
            } catch (_: Exception) {
                // Column already exists, nothing to do
            }
        }
    }

    // --- User Methods ---

    fun addUser(name: String, email: String, password: String): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(USER_NAME, name)
        values.put(USER_EMAIL, email)
        values.put(USER_PASSWORD, password)
        val id = db.insert(TABLE_USERS, null, values)
        db.close()
        return id
    }

    fun updatePhone(userId: Int, phone: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(USER_PHONE, phone)
        db.update(TABLE_USERS, values, "$USER_ID = ?", arrayOf(userId.toString()))
        db.close()
    }

    fun checkUser(email: String, password: String): UserInfo? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            null,
            "$USER_EMAIL = ? AND $USER_PASSWORD = ?",
            arrayOf(email, password),
            null, null, null
        )

        var userInfo: UserInfo? = null
        if (cursor.moveToFirst()) {
            userInfo = UserInfo(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(USER_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(USER_NAME)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(USER_EMAIL)),
                phone = cursor.getString(cursor.getColumnIndexOrThrow(USER_PHONE)) ?: "N/A"
            )
        }
        cursor.close()
        db.close()
        return userInfo
    }

    fun getUserById(userId: Int): UserInfo? {
        val db = this.readableDatabase
        val cursor = db.query(TABLE_USERS, null, "$USER_ID = ?", arrayOf(userId.toString()), null, null, null)
        var userInfo: UserInfo? = null
        if (cursor.moveToFirst()) {
            userInfo = UserInfo(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(USER_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(USER_NAME)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(USER_EMAIL)),
                phone = cursor.getString(cursor.getColumnIndexOrThrow(USER_PHONE)) ?: "N/A"
            )
        }
        cursor.close()
        db.close()
        return userInfo
    }

    // --- Booking Methods ---

    fun addBooking(userId: Int, carName: String, carCode: String, start: String, end: String, duration: String, cost: Double, payment: String): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(BOOKING_USER_ID, userId)
        values.put(BOOKING_CAR_NAME, carName)
        values.put(BOOKING_CAR_CODE, carCode)
        values.put(BOOKING_START, start)
        values.put(BOOKING_END, end)
        values.put(BOOKING_DURATION, duration)
        values.put(BOOKING_COST, cost)
        values.put(BOOKING_PAYMENT_METHOD, payment)
        val id = db.insert(TABLE_BOOKINGS, null, values)
        db.close()
        return id
    }

    fun getLatestBooking(userId: Int): BookingInfo? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_BOOKINGS,
            null,
            "$BOOKING_USER_ID = ?",
            arrayOf(userId.toString()),
            null, null, "$BOOKING_ID DESC", "1"
        )

        var booking: BookingInfo? = null
        if (cursor.moveToFirst()) {
            booking = BookingInfo(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(BOOKING_ID)),
                carName = cursor.getString(cursor.getColumnIndexOrThrow(BOOKING_CAR_NAME)) ?: "",
                carCode = cursor.getString(cursor.getColumnIndexOrThrow(BOOKING_CAR_CODE)) ?: "",
                start = cursor.getString(cursor.getColumnIndexOrThrow(BOOKING_START)) ?: "",
                end = cursor.getString(cursor.getColumnIndexOrThrow(BOOKING_END)) ?: "",
                duration = cursor.getString(cursor.getColumnIndexOrThrow(BOOKING_DURATION)) ?: "",
                cost = cursor.getDouble(cursor.getColumnIndexOrThrow(BOOKING_COST)),
                paymentMethod = cursor.getString(cursor.getColumnIndexOrThrow(BOOKING_PAYMENT_METHOD)) ?: ""
            )
        }
        cursor.close()
        db.close()
        return booking
    }

    // --- Car Methods ---

    fun getAllCars(): List<MainActivity.Car> {
        val db = this.readableDatabase
        val carList = mutableListOf<MainActivity.Car>()
        val cursor = db.rawQuery("SELECT * FROM $TABLE_CARS", null)
        
        if (cursor.moveToFirst()) {
            do {
                val imagesStr = cursor.getString(cursor.getColumnIndexOrThrow(CAR_IMAGES))
                val images = imagesStr?.split(",") ?: emptyList()
                
                carList.add(MainActivity.Car(
                    id = cursor.getString(cursor.getColumnIndexOrThrow(CAR_CODE)) ?: "",
                    name = cursor.getString(cursor.getColumnIndexOrThrow(CAR_MODEL)) ?: "",
                    category = cursor.getString(cursor.getColumnIndexOrThrow(CAR_CATEGORY)) ?: "",
                    engine = cursor.getString(cursor.getColumnIndexOrThrow(CAR_ENGINE)) ?: "",
                    power = cursor.getString(cursor.getColumnIndexOrThrow(CAR_POWER)) ?: "",
                    drivetrain = cursor.getString(cursor.getColumnIndexOrThrow(CAR_DRIVETRAIN)) ?: "",
                    seats = cursor.getInt(cursor.getColumnIndexOrThrow(CAR_SEATS)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(CAR_DESC)) ?: "",
                    dailyPrice = cursor.getInt(cursor.getColumnIndexOrThrow(CAR_PRICE_DAY)),
                    weeklyPrice = cursor.getInt(cursor.getColumnIndexOrThrow(CAR_PRICE_WEEK)),
                    monthlyPrice = cursor.getInt(cursor.getColumnIndexOrThrow(CAR_PRICE_MONTH)),
                    images = images
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return carList
    }

    fun seedCars(cars: List<MainActivity.Car>) {
        val db = this.writableDatabase
        // Check if already seeded
        val countCursor = db.rawQuery("SELECT count(*) FROM $TABLE_CARS", null)
        countCursor.moveToFirst()
        val count = countCursor.getInt(0)
        countCursor.close()
        
        if (count == 0) {
            for (car in cars) {
                val values = ContentValues()
                values.put(CAR_CODE, car.id)
                values.put(CAR_MODEL, car.name)
                values.put(CAR_CATEGORY, car.category)
                values.put(CAR_ENGINE, car.engine)
                values.put(CAR_POWER, car.power)
                values.put(CAR_DRIVETRAIN, car.drivetrain)
                values.put(CAR_SEATS, car.seats)
                values.put(CAR_DESC, car.description)
                values.put(CAR_PRICE_DAY, car.dailyPrice)
                values.put(CAR_PRICE_WEEK, car.weeklyPrice)
                values.put(CAR_PRICE_MONTH, car.monthlyPrice)
                values.put(CAR_IMAGES, car.images.joinToString(","))
                db.insert(TABLE_CARS, null, values)
            }
        }
        db.close()
    }

    data class UserInfo(val id: Int, val name: String, val email: String, val phone: String)
    data class BookingInfo(
        val id: Long,
        val carName: String,
        val carCode: String,
        val start: String,
        val end: String,
        val duration: String,
        val cost: Double,
        val paymentMethod: String
    )
}
