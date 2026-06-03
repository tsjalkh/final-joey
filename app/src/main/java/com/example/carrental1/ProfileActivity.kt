package com.example.carrental1

import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class ProfileActivity : AppCompatActivity() {

    private lateinit var imageProfile: ImageView
    private lateinit var textProfileName: TextView
    private lateinit var textProfileEmail: TextView
    private lateinit var textProfilePhone: TextView
    private lateinit var textProfilePayment: TextView
    private lateinit var textRentalDetails: TextView
    private lateinit var buttonChangePhoto: MaterialButton

    private val pickImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        uri?.let { imageProfile.setImageURI(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbarProfile)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        imageProfile = findViewById(R.id.imageProfile)
        textProfileName = findViewById(R.id.textProfileName)
        textProfileEmail = findViewById(R.id.textProfileEmail)
        textProfilePhone = findViewById(R.id.textProfilePhone)
        textProfilePayment = findViewById(R.id.textProfilePayment)
        textRentalDetails = findViewById(R.id.textRentalDetails)
        buttonChangePhoto = findViewById(R.id.buttonChangePhoto)

        loadUserData()
        loadRentalHistory()
        animateCardsIn()

        buttonChangePhoto.setOnClickListener {
            pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun animateCardsIn() {
        val cards = listOf<View>(
            findViewById(R.id.cardRentalHistory)
        )
        cards.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 60f
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(450)
                .setStartDelay(200L + index * 100L)
                .start()
        }
    }

    private fun loadUserData() {
        val prefs = getSharedPreferences("UserData", MODE_PRIVATE)
        val userId = prefs.getInt("userId", -1)

        if (userId != -1) {
            val user = DatabaseHelper.getInstance(this).getUserById(userId)
            if (user != null) {
                textProfileName.text = user.name
                textProfileEmail.text = user.email
                textProfilePhone.text = if (user.phone == "N/A") "Not set" else user.phone
                return
            }
        }

        textProfileName.text = prefs.getString("userName", "User") ?: "User"
        textProfileEmail.text = prefs.getString("userEmail", "N/A") ?: "N/A"
        textProfilePhone.text = "Not set"
    }

    private fun loadRentalHistory() {
        val userId = getSharedPreferences("UserData", MODE_PRIVATE).getInt("userId", -1)

        if (userId != -1) {
            val booking = DatabaseHelper.getInstance(this).getLatestBooking(userId)
            if (booking != null) {
                textRentalDetails.text = getString(
                    R.string.latest_booking_format,
                    booking.id,
                    booking.carName,
                    booking.carCode,
                    booking.start,
                    booking.end,
                    booking.duration,
                    booking.cost.toInt()
                )
                textProfilePayment.text = booking.paymentMethod
                return
            }
        }

        textRentalDetails.text = getString(R.string.no_rentals_yet)
        textProfilePayment.text = "N/A"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        @Suppress("DEPRECATION")
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
