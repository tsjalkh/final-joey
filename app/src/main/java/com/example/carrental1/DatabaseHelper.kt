package com.example.carrental1

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "CarRental.db"
        private const val DATABASE_VERSION = 6

        @Volatile
        private var instance: DatabaseHelper? = null

        fun getInstance(context: Context): DatabaseHelper =
            instance ?: synchronized(this) {
                instance ?: DatabaseHelper(context.applicationContext).also { instance = it }
            }

        const val TABLE_CARS = "cars"
        const val TABLE_USERS = "users"
        const val TABLE_BOOKINGS = "bookings"

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
        const val CAR_IMAGES = "images"
        const val CAR_AVAILABLE = "is_available"

        const val USER_ID = "user_id"
        const val USER_NAME = "full_name"
        const val USER_EMAIL = "email"
        const val USER_PHONE = "phone"
        const val USER_PASSWORD = "password"

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
        db.execSQL(
            "CREATE TABLE $TABLE_CARS (" +
            "$CAR_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$CAR_BRAND TEXT, $CAR_MODEL TEXT NOT NULL, $CAR_CODE TEXT, " +
            "$CAR_CATEGORY TEXT, $CAR_ENGINE TEXT, $CAR_POWER TEXT, $CAR_DRIVETRAIN TEXT, " +
            "$CAR_SEATS INTEGER, $CAR_DESC TEXT, " +
            "$CAR_PRICE_DAY REAL, $CAR_PRICE_WEEK REAL, $CAR_PRICE_MONTH REAL, " +
            "$CAR_IMAGES TEXT, $CAR_AVAILABLE INTEGER DEFAULT 1);"
        )
        db.execSQL(
            "CREATE TABLE $TABLE_USERS (" +
            "$USER_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$USER_NAME TEXT NOT NULL, $USER_EMAIL TEXT NOT NULL UNIQUE, " +
            "$USER_PHONE TEXT, $USER_PASSWORD TEXT NOT NULL);"
        )
        db.execSQL(
            "CREATE TABLE $TABLE_BOOKINGS (" +
            "$BOOKING_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$BOOKING_USER_ID INTEGER, $BOOKING_CAR_NAME TEXT, $BOOKING_CAR_CODE TEXT, " +
            "$BOOKING_START TEXT, $BOOKING_END TEXT, $BOOKING_DURATION TEXT, " +
            "$BOOKING_COST REAL, $BOOKING_PAYMENT_METHOD TEXT);"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
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
        if (oldVersion < 5) {
            try {
                db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $USER_PHONE TEXT")
            } catch (_: Exception) {}
        }
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS $TABLE_BOOKINGS (" +
            "$BOOKING_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$BOOKING_USER_ID INTEGER, $BOOKING_CAR_NAME TEXT, $BOOKING_CAR_CODE TEXT, " +
            "$BOOKING_START TEXT, $BOOKING_END TEXT, $BOOKING_DURATION TEXT, " +
            "$BOOKING_COST REAL, $BOOKING_PAYMENT_METHOD TEXT);"
        )
    }

    fun addUser(name: String, email: String, password: String): Long {
        val values = ContentValues().apply {
            put(USER_NAME, name)
            put(USER_EMAIL, email)
            put(USER_PASSWORD, password)
        }
        return writableDatabase.insert(TABLE_USERS, null, values)
    }

    fun updatePhone(userId: Int, phone: String) {
        val values = ContentValues().apply { put(USER_PHONE, phone) }
        writableDatabase.update(TABLE_USERS, values, "$USER_ID = ?", arrayOf(userId.toString()))
    }

    fun checkUser(email: String, password: String): UserInfo? {
        return readableDatabase.query(
            TABLE_USERS, null,
            "$USER_EMAIL = ? AND $USER_PASSWORD = ?",
            arrayOf(email, password), null, null, null
        ).use { cursor ->
            if (cursor.moveToFirst()) buildUserInfo(cursor) else null
        }
    }

    fun getUserById(userId: Int): UserInfo? {
        return readableDatabase.query(
            TABLE_USERS, null, "$USER_ID = ?", arrayOf(userId.toString()), null, null, null
        ).use { cursor ->
            if (cursor.moveToFirst()) buildUserInfo(cursor) else null
        }
    }

    private fun buildUserInfo(cursor: android.database.Cursor): UserInfo {
        val phoneIdx = cursor.getColumnIndex(USER_PHONE)
        return UserInfo(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(USER_ID)),
            name = cursor.getString(cursor.getColumnIndexOrThrow(USER_NAME)) ?: "",
            email = cursor.getString(cursor.getColumnIndexOrThrow(USER_EMAIL)) ?: "",
            phone = if (phoneIdx >= 0) cursor.getString(phoneIdx) ?: "N/A" else "N/A"
        )
    }

    fun addBooking(
        userId: Int, carName: String, carCode: String,
        start: String, end: String, duration: String,
        cost: Double, payment: String
    ): Long {
        val values = ContentValues().apply {
            put(BOOKING_USER_ID, userId)
            put(BOOKING_CAR_NAME, carName)
            put(BOOKING_CAR_CODE, carCode)
            put(BOOKING_START, start)
            put(BOOKING_END, end)
            put(BOOKING_DURATION, duration)
            put(BOOKING_COST, cost)
            put(BOOKING_PAYMENT_METHOD, payment)
        }
        return writableDatabase.insert(TABLE_BOOKINGS, null, values)
    }

    fun getLatestBooking(userId: Int): BookingInfo? {
        return readableDatabase.query(
            TABLE_BOOKINGS, null,
            "$BOOKING_USER_ID = ?", arrayOf(userId.toString()),
            null, null, "$BOOKING_ID DESC", "1"
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                BookingInfo(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(BOOKING_ID)),
                    carName = cursor.getString(cursor.getColumnIndexOrThrow(BOOKING_CAR_NAME)) ?: "",
                    carCode = cursor.getString(cursor.getColumnIndexOrThrow(BOOKING_CAR_CODE)) ?: "",
                    start = cursor.getString(cursor.getColumnIndexOrThrow(BOOKING_START)) ?: "",
                    end = cursor.getString(cursor.getColumnIndexOrThrow(BOOKING_END)) ?: "",
                    duration = cursor.getString(cursor.getColumnIndexOrThrow(BOOKING_DURATION)) ?: "",
                    cost = cursor.getDouble(cursor.getColumnIndexOrThrow(BOOKING_COST)),
                    paymentMethod = cursor.getString(cursor.getColumnIndexOrThrow(BOOKING_PAYMENT_METHOD)) ?: ""
                )
            } else null
        }
    }

    fun getAllCars(): List<MainActivity.Car> {
        return readableDatabase.rawQuery("SELECT * FROM $TABLE_CARS", null).use { cursor ->
            val list = mutableListOf<MainActivity.Car>()
            while (cursor.moveToNext()) {
                val images = cursor.getString(cursor.getColumnIndexOrThrow(CAR_IMAGES))
                    ?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
                list.add(
                    MainActivity.Car(
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
                    )
                )
            }
            list
        }
    }

    fun seedCars(cars: List<MainActivity.Car>) {
        val db = writableDatabase
        val count = db.rawQuery("SELECT count(*) FROM $TABLE_CARS", null).use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
        if (count > 0) return
        for (car in cars) {
            val values = ContentValues().apply {
                put(CAR_CODE, car.id)
                put(CAR_MODEL, car.name)
                put(CAR_CATEGORY, car.category)
                put(CAR_ENGINE, car.engine)
                put(CAR_POWER, car.power)
                put(CAR_DRIVETRAIN, car.drivetrain)
                put(CAR_SEATS, car.seats)
                put(CAR_DESC, car.description)
                put(CAR_PRICE_DAY, car.dailyPrice)
                put(CAR_PRICE_WEEK, car.weeklyPrice)
                put(CAR_PRICE_MONTH, car.monthlyPrice)
                put(CAR_IMAGES, car.images.joinToString(","))
            }
            db.insert(TABLE_CARS, null, values)
        }
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
