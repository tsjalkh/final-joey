package com.example.carrental1

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class PaymentActivity : AppCompatActivity() {

    private lateinit var textCarName: TextView
    private lateinit var textPackage: TextView
    private lateinit var textSelectedDates: TextView
    private lateinit var buttonPickDate: MaterialButton
    private lateinit var buttonCustomEndDate: MaterialButton
    private lateinit var buttonConfirm: MaterialButton
    private lateinit var radioGroupPayment: RadioGroup
    private lateinit var layoutCardInfo: View
    private lateinit var editPaymentPhone: TextInputEditText
    
    private lateinit var editCardNumber: TextInputEditText
    private lateinit var editExpiryDate: TextInputEditText
    private lateinit var editCvv: TextInputEditText

    private var carId: String = ""
    private var carName: String = ""
    private var basePrice: Int = 0
    private var packageType: String = ""
    private var originalPackageType: String = ""
    private var dailyRate: Int = 0
    
    private var startDate: Calendar = Calendar.getInstance()
    private var endDate: Calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbarPayment)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Get data from intent
        carId = intent.getStringExtra("carId") ?: ""
        carName = intent.getStringExtra("carName") ?: ""
        basePrice = intent.getIntExtra("price", 0)
        packageType = intent.getStringExtra("duration") ?: ""
        originalPackageType = packageType
        dailyRate = intent.getIntExtra("dailyPrice", basePrice)

        textCarName = findViewById(R.id.textPaymentCarName)
        textPackage = findViewById(R.id.textPaymentPackage)
        textSelectedDates = findViewById(R.id.textSelectedDates)
        buttonPickDate = findViewById(R.id.buttonPickDate)
        buttonCustomEndDate = findViewById(R.id.buttonCustomEndDate)
        buttonConfirm = findViewById(R.id.buttonConfirmPayment)
        radioGroupPayment = findViewById(R.id.radioGroupPayment)
        layoutCardInfo = findViewById(R.id.layoutCardInfo)
        editPaymentPhone = findViewById(R.id.editPaymentPhone)
        editCardNumber = findViewById(R.id.editCardNumber)
        editExpiryDate = findViewById(R.id.editExpiryDate)
        editCvv = findViewById(R.id.editCvv)

        textCarName.text = "$carName ($carId)"
        textPackage.text = getString(R.string.selected_package_format, packageType, basePrice)

        setupDatePickers()
        setupPaymentLogic()

        buttonConfirm.setOnClickListener { confirmRental() }
    }

    private fun setupDatePickers() {
        buttonPickDate.setOnClickListener {
            val dpd = DatePickerDialog(
                this,
                { _, year, month, day ->
                    startDate.set(year, month, day)
                    updateEndBasedOnPackage()
                    updateDateDisplay()
                },
                startDate.get(Calendar.YEAR),
                startDate.get(Calendar.MONTH),
                startDate.get(Calendar.DAY_OF_MONTH)
            )
            dpd.datePicker.minDate = System.currentTimeMillis()
            dpd.show()
        }

        buttonCustomEndDate.setOnClickListener {
            val dpd = DatePickerDialog(
                this,
                { _, year, month, day ->
                    val newEnd = Calendar.getInstance()
                    newEnd.set(year, month, day)
                    if (newEnd.after(startDate)) {
                        endDate = newEnd
                        calculateCustomPrice()
                        updateDateDisplay()
                    } else {
                        Toast.makeText(this, "End date must be after start date", Toast.LENGTH_SHORT).show()
                    }
                },
                endDate.get(Calendar.YEAR),
                endDate.get(Calendar.MONTH),
                endDate.get(Calendar.DAY_OF_MONTH)
            )
            dpd.datePicker.minDate = startDate.timeInMillis + (24 * 60 * 60 * 1000)
            dpd.show()
        }
    }

    private fun updateEndBasedOnPackage() {
        endDate = startDate.clone() as Calendar
        packageType = originalPackageType
        basePrice = intent.getIntExtra("price", 0)
        when (packageType) {
            "1 day" -> endDate.add(Calendar.DAY_OF_YEAR, 1)
            "1 week" -> endDate.add(Calendar.DAY_OF_YEAR, 7)
            "1 month" -> endDate.add(Calendar.MONTH, 1)
        }
        textPackage.text = getString(R.string.selected_package_format, packageType, basePrice)
    }

    private fun calculateCustomPrice() {
        val diff = endDate.timeInMillis - startDate.timeInMillis
        val days = (diff / (1000 * 60 * 60 * 24)).toInt()
        val customPrice = if (days > 0) days * dailyRate else dailyRate
        packageType = "$days days (Custom)"
        basePrice = customPrice
        textPackage.text = getString(R.string.custom_duration_format, packageType, basePrice)
    }

    private fun updateDateDisplay() {
        val startStr = dateFormat.format(startDate.time)
        val endStr = dateFormat.format(endDate.time)
        textSelectedDates.text = "$startStr  →  $endStr"
    }

    private fun setupPaymentLogic() {
        radioGroupPayment.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.radioCard) {
                layoutCardInfo.visibility = View.VISIBLE
            } else {
                layoutCardInfo.visibility = View.GONE
                if (checkedId == R.id.radioCash) {
                    showCashPopup()
                }
            }
        }
    }

    private fun showCashPopup() {
        AlertDialog.Builder(this)
            .setTitle("Cash Payment")
            .setMessage("Please pay cash when you come to collect the car at ${dateFormat.format(startDate.time)}.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun confirmRental() {
        val phone = editPaymentPhone.text.toString().trim()
        if (phone.isEmpty()) {
            Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show()
            return
        }

        if (textSelectedDates.text.toString() == getString(R.string.no_date_selected)) {
            Toast.makeText(this, "Please select rental dates", Toast.LENGTH_SHORT).show()
            return
        }

        val paymentMethod = if (findViewById<RadioButton>(R.id.radioCash).isChecked) "Cash" else if (findViewById<RadioButton>(R.id.radioCard).isChecked) "Card" else ""
        if (paymentMethod.isEmpty()) {
            Toast.makeText(this, "Select payment method", Toast.LENGTH_SHORT).show()
            return
        }

        if (paymentMethod == "Card" && (editCardNumber.text.isNullOrEmpty() || editExpiryDate.text.isNullOrEmpty() || editCvv.text.isNullOrEmpty())) {
            Toast.makeText(this, "Please enter card details", Toast.LENGTH_SHORT).show()
            return
        }

        val userPrefs = getSharedPreferences("UserData", MODE_PRIVATE)
        val userId = userPrefs.getInt("userId", -1)
        
        var generatedBookingId: Long = -1
        
        if (userId != -1) {
            val dbHelper = DatabaseHelper.getInstance(this)
            dbHelper.updatePhone(userId, phone)
            generatedBookingId = dbHelper.addBooking(
                userId = userId,
                carName = carName,
                carCode = carId,
                start = dateFormat.format(startDate.time),
                end = dateFormat.format(endDate.time),
                duration = packageType,
                cost = basePrice.toDouble(),
                payment = paymentMethod
            )
        }

        // Save to SharedPrefs
        getSharedPreferences("RentalData", MODE_PRIVATE).edit {
            putString("carName", carName)
            putString("carId", carId)
            putString("duration", packageType)
            putInt("price", basePrice)
            putString("startDate", dateFormat.format(startDate.time))
            putString("endDate", dateFormat.format(endDate.time))
            putString("paymentMethod", paymentMethod)
            putString("phone", phone) // Save phone
            putLong("bookingId", generatedBookingId)
        }

        // Go to Summary
        val intent = Intent(this, RentalSummaryActivity::class.java).apply {
            putExtra("bookingId", generatedBookingId)
            putExtra("carId", carId)
            putExtra("carName", carName)
            putExtra("duration", packageType)
            putExtra("price", basePrice)
            putExtra("paymentMethod", paymentMethod)
            putExtra("fullName", userPrefs.getString("userName", ""))
            putExtra("email", userPrefs.getString("userEmail", ""))
        }
        startActivity(intent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}