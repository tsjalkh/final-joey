package com.example.carrental1

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

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

        // Animate the success icon with a bounce/pop effect
        val successIconFrame = findViewById<View>(R.id.successIconFrame)
        val popInAnim = AnimationUtils.loadAnimation(this, R.anim.scale_pop_in)
        successIconFrame.startAnimation(popInAnim)

        // Fade in the summary card with a delay
        val summaryCard = textSummary.parent.parent as? View
        summaryCard?.let {
            it.alpha = 0f
            it.translationY = 40f
            it.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(400).start()
        }

        val buttonBackHome = findViewById<MaterialButton>(R.id.buttonBackHome)
        buttonBackHome.visibility = View.VISIBLE
        buttonBackHome.alpha = 0f
        buttonBackHome.animate().alpha(1f).setDuration(400).setStartDelay(700).start()

        buttonBackHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }
    }
}
