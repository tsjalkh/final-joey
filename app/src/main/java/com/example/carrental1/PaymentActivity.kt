package com.example.carrental1

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
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

    private var carId = ""
    private var carName = ""
    private var basePrice = 0
    private var packageType = ""
    private var originalPackageType = ""
    private var dailyRate = 0

    private var startDate: Calendar = Calendar.getInstance()
    private var endDate: Calendar = Calendar.getInstance()
    private var hasSelectedDates = false
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbarPayment)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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
        setupPaymentToggle()
        animateContentIn()

        buttonConfirm.setOnClickListener { confirmRental() }
    }

    private fun animateContentIn() {
        val scrollView = findViewById<View>(android.R.id.content)
        scrollView.alpha = 0f
        scrollView.translationY = 30f
        scrollView.animate().alpha(1f).translationY(0f).setDuration(400).setStartDelay(100).start()
    }

    private fun setupDatePickers() {
        buttonPickDate.setOnClickListener {
            val today = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    startDate.set(year, month, day)
                    updateEndBasedOnPackage()
                    hasSelectedDates = true
                    updateDateDisplay()
                },
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.minDate = System.currentTimeMillis()
                show()
            }
        }

        buttonCustomEndDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    val newEnd = Calendar.getInstance().apply { set(year, month, day) }
                    if (newEnd.after(startDate)) {
                        endDate = newEnd
                        calculateCustomPrice()
                        hasSelectedDates = true
                        updateDateDisplay()
                    } else {
                        Toast.makeText(this, "End date must be after start date", Toast.LENGTH_SHORT).show()
                    }
                },
                endDate.get(Calendar.YEAR),
                endDate.get(Calendar.MONTH),
                endDate.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.minDate = startDate.timeInMillis + (24L * 60 * 60 * 1000)
                show()
            }
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
        val days = ((endDate.timeInMillis - startDate.timeInMillis) / (1000L * 60 * 60 * 24)).toInt()
        basePrice = if (days > 0) days * dailyRate else dailyRate
        packageType = "$days days (Custom)"
        textPackage.text = getString(R.string.custom_duration_format, packageType, basePrice)
    }

    private fun updateDateDisplay() {
        textSelectedDates.text = "${dateFormat.format(startDate.time)}  →  ${dateFormat.format(endDate.time)}"
    }

    private fun setupPaymentToggle() {
        radioGroupPayment.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.radioCard) {
                layoutCardInfo.visibility = View.VISIBLE
            } else {
                layoutCardInfo.visibility = View.GONE
                if (checkedId == R.id.radioCash) showCashDialog()
            }
        }
    }

    private fun showCashDialog() {
        val dateStr = if (hasSelectedDates) dateFormat.format(startDate.time) else "your chosen pick-up date"
        AlertDialog.Builder(this)
            .setTitle("Cash Payment")
            .setMessage("Please pay cash when you come to collect the car on $dateStr.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun confirmRental() {
        val phone = editPaymentPhone.text.toString().trim()
        if (phone.isEmpty()) {
            Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show()
            return
        }

        if (!hasSelectedDates) {
            Toast.makeText(this, "Please select rental dates", Toast.LENGTH_SHORT).show()
            return
        }

        val cashChecked = findViewById<RadioButton>(R.id.radioCash).isChecked
        val cardChecked = findViewById<RadioButton>(R.id.radioCard).isChecked
        if (!cashChecked && !cardChecked) {
            Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show()
            return
        }

        val paymentMethod = if (cashChecked) "Cash" else "Card"

        if (paymentMethod == "Card") {
            if (editCardNumber.text.isNullOrBlank() ||
                editExpiryDate.text.isNullOrBlank() ||
                editCvv.text.isNullOrBlank()
            ) {
                Toast.makeText(this, "Please fill in all card details", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val userPrefs = getSharedPreferences("UserData", MODE_PRIVATE)
        val userId = userPrefs.getInt("userId", -1)
        var bookingId = -1L

        if (userId != -1) {
            val db = DatabaseHelper.getInstance(this)
            db.updatePhone(userId, phone)
            bookingId = db.addBooking(
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

        scheduleRentalEndNotification(endDate, carName, bookingId)

        getSharedPreferences("RentalData", MODE_PRIVATE).edit {
            putString("carName", carName)
            putString("carId", carId)
            putString("duration", packageType)
            putInt("price", basePrice)
            putString("startDate", dateFormat.format(startDate.time))
            putString("endDate", dateFormat.format(endDate.time))
            putString("paymentMethod", paymentMethod)
            putString("phone", phone)
            putLong("bookingId", bookingId)
        }

        startActivity(Intent(this, RentalSummaryActivity::class.java).apply {
            putExtra("bookingId", bookingId)
            putExtra("carId", carId)
            putExtra("carName", carName)
            putExtra("duration", packageType)
            putExtra("price", basePrice)
            putExtra("paymentMethod", paymentMethod)
            putExtra("fullName", userPrefs.getString("userName", ""))
            putExtra("email", userPrefs.getString("userEmail", ""))
        })
        @Suppress("DEPRECATION")
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }

    private fun scheduleRentalEndNotification(endCal: Calendar, car: String, bookingId: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                RentalEndReceiver.CHANNEL_ID, "Rental Reminders", NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Notifies you when your rental period ends" }
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }

        // TEMP: fire in 10 seconds for testing
        val notifyAt = System.currentTimeMillis() + 10_000

        val intent = Intent(this, RentalEndReceiver::class.java).apply {
            putExtra("carName", car)
            putExtra("bookingId", bookingId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this, bookingId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, notifyAt, pendingIntent)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, notifyAt, pendingIntent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_down_out)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
