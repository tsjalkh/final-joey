package com.example.carrental1

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RentalSummaryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rental_summary)

        val textSummary: TextView = findViewById(R.id.textSummary)

        val bookingId = intent.getLongExtra("bookingId", -1)
        val carId = intent.getStringExtra("carId") ?: "N/A"
        val carName = intent.getStringExtra("carName") ?: "N/A"
        val fullName = intent.getStringExtra("fullName") ?: "N/A"
        val email = intent.getStringExtra("email") ?: "N/A"
        val duration = intent.getStringExtra("duration") ?: "N/A"
        val paymentMethod = intent.getStringExtra("paymentMethod") ?: "N/A"
        val price = intent.getIntExtra("price", 0)

        textSummary.text = getString(
            R.string.summary_confirmation_format,
            bookingId,
            fullName,
            email,
            carName,
            carId,
            duration,
            paymentMethod,
            price
        )

        Toast.makeText(this, "Booking #$bookingId Confirmed", Toast.LENGTH_LONG).show()
    }
}
