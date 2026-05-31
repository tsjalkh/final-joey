package com.example.carrental1

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
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

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            imageProfile.setImageURI(imageUri)
        }
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

        buttonChangePhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImage.launch(intent)
        }
    }

    private fun loadUserData() {
        val userPrefs = getSharedPreferences("UserData", MODE_PRIVATE)
        val userId = userPrefs.getInt("userId", -1)
        
        if (userId != -1) {
            val dbHelper = DatabaseHelper(this)
            val user = dbHelper.getUserById(userId)
            dbHelper.close()
            if (user != null) {
                textProfileName.text = user.name
                textProfileEmail.text = user.email
                textProfilePhone.text = user.phone
                return
            }
        }

        // Fallback
        textProfileName.text = userPrefs.getString("userName", "User")
        textProfileEmail.text = userPrefs.getString("userEmail", "N/A")
        textProfilePhone.text = "N/A"
    }

    private fun loadRentalHistory() {
        val userPrefs = getSharedPreferences("UserData", MODE_PRIVATE)
        val userId = userPrefs.getInt("userId", -1)

        if (userId != -1) {
            val dbHelper = DatabaseHelper(this)
            val booking = dbHelper.getLatestBooking(userId)
            dbHelper.close()

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
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}