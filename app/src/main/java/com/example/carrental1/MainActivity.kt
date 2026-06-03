package com.example.carrental1

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var autoCompleteCategory: AutoCompleteTextView
    private lateinit var autoCompleteCar: AutoCompleteTextView
    private lateinit var recyclerViewCars: RecyclerView

    private lateinit var carPagerAdapter: CarPagerAdapter
    private lateinit var databaseHelper: DatabaseHelper

    private var selectedCategory = "Sedans"
    private var allCarsList: List<Car> = emptyList()

    data class Car(
        val id: String,
        val name: String,
        val category: String,
        val engine: String,
        val power: String,
        val drivetrain: String,
        val seats: Int,
        val description: String,
        val dailyPrice: Int,
        val weeklyPrice: Int,
        val monthlyPrice: Int,
        val images: List<String>
    )

    private val carSeedList by lazy {
        listOf(
            Car("CAM24", "Toyota Camry", "Sedans", "2.5L 4-Cylinder", "203 hp", "Front-Wheel Drive", 5, "Comfortable, reliable sedan.", 45, 280, 950, listOf(
                "file:///android_asset/images/camry_front.jpeg",
                "file:///android_asset/images/camry_back.jpeg",
                "file:///android_asset/images/toyota_interior2.jpeg"
            )),
            Car("CIV24", "Honda Civic", "Sedans", "2.0L 4-Cylinder", "158 hp", "Front-Wheel Drive", 5, "Fuel-efficient compact sedan.", 40, 250, 850, listOf(
                "file:///android_asset/images/civic_front.jpeg",
                "file:///android_asset/images/civic_side.jpeg",
                "file:///android_asset/images/civic_inside.jpeg"
            )),
            Car("ALT24", "Nissan Altima", "Sedans", "2.5L 4-Cylinder", "188 hp", "Front-Wheel Drive", 5, "Spacious sedan with good comfort.", 42, 265, 900, listOf(
                "file:///android_asset/images/altima_front.jpeg",
                "file:///android_asset/images/altima_back.jpeg",
                "file:///android_asset/images/altima_back_side.jpeg",
                "file:///android_asset/images/altima_interior.jpeg"
            )),
            Car("RAV24", "Toyota RAV4", "SUVs", "2.5L 4-Cylinder", "203 hp", "All-Wheel Drive", 5, "Practical SUV with stability.", 60, 390, 1350, listOf(
                "file:///android_asset/images/rav4_front.jpeg",
                "file:///android_asset/images/rav4_side.jpeg",
                "file:///android_asset/images/rav4_back.jpeg",
                "file:///android_asset/images/toyota_interior.jpeg"
            )),
            Car("TUC24", "Hyundai Tucson", "SUVs", "2.5L 4-Cylinder", "187 hp", "All-Wheel Drive", 5, "Modern SUV with good tech.", 58, 370, 1280, listOf(
                "file:///android_asset/images/tuscon_front.jpeg",
                "file:///android_asset/images/tuscon_back.jpeg",
                "file:///android_asset/images/tuscon_interior.jpeg"
            )),
            Car("SOR24", "Kia Sorento", "SUVs", "2.5L 4-Cylinder", "191 hp", "All-Wheel Drive", 7, "Large SUV for families.", 75, 480, 1650, listOf(
                "file:///android_asset/images/kia_front.jpeg",
                "file:///android_asset/images/kia_back.jpeg",
                "file:///android_asset/images/kia_interior.jpeg"
            )),
            Car("MUS24", "Ford Mustang", "Sports Cars", "5.0L V8", "480 hp", "Rear-Wheel Drive", 4, "Powerful sports car.", 120, 780, 2800, listOf(
                "file:///android_asset/images/mustang_front.jpeg",
                "file:///android_asset/images/mustang_back.jpeg",
                "file:///android_asset/images/mustang_interior.jpeg"
            )),
            Car("CAMR24", "Chevrolet Camaro", "Sports Cars", "6.2L V8", "455 hp", "Rear-Wheel Drive", 4, "Sporty coupe with bold styling.", 115, 740, 2650, listOf(
                "file:///android_asset/images/camaro_front.jpeg",
                "file:///android_asset/images/camaro_back.jpeg",
                "file:///android_asset/images/camaro_interior.jpeg"
            )),
            Car("Z424", "BMW Z4", "Sports Cars", "3.0L Turbo I6", "382 hp", "Rear-Wheel Drive", 2, "Luxury roadster.", 140, 900, 3200, listOf(
                "file:///android_asset/images/z4_front_side.jpeg",
                "file:///android_asset/images/z4_side.jpeg",
                "file:///android_asset/images/z4_back.jpeg",
                "file:///android_asset/images/z4_back_side.jpeg",
                "file:///android_asset/images/z4_interior1.jpeg",
                "file:///android_asset/images/z4_interior2.jpeg"
            ))
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        autoCompleteCategory = findViewById(R.id.autoCompleteCategory)
        autoCompleteCar = findViewById(R.id.autoCompleteCar)
        recyclerViewCars = findViewById(R.id.recyclerViewCars)

        databaseHelper = DatabaseHelper.getInstance(this)
        allCarsList = try {
            databaseHelper.seedCars(carSeedList)
            databaseHelper.getAllCars()
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "DB load error", e)
            Toast.makeText(this, "Error loading car data. Please restart the app.", Toast.LENGTH_LONG).show()
            emptyList()
        }

        setupCarList()
        setupCategoryDropdown()

        recyclerViewCars.post {
            loadCarsByCategory(selectedCategory)
        }
    }

    private fun setupCarList() {
        carPagerAdapter = CarPagerAdapter(this, emptyList()) { car, duration, price, _ ->
            openPaymentPage(car, duration, price)
        }
        recyclerViewCars.layoutManager = LinearLayoutManager(this)
        recyclerViewCars.adapter = carPagerAdapter
    }

    private fun setupCategoryDropdown() {
        val categories = listOf("Sedans", "SUVs", "Sports Cars")
        autoCompleteCategory.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        )
        autoCompleteCategory.setText(categories[0], false)
        autoCompleteCategory.setOnItemClickListener { _, _, position, _ ->
            loadCarsByCategory(categories[position])
        }
    }

    private fun loadCarsByCategory(category: String) {
        selectedCategory = category
        val filtered = allCarsList.filter { it.category == category }

        autoCompleteCar.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, filtered.map { it.name })
        )
        if (filtered.isNotEmpty()) {
            autoCompleteCar.setText(filtered[0].name, false)
        }

        autoCompleteCar.setOnItemClickListener { _, _, position, _ ->
            recyclerViewCars.smoothScrollToPosition(position)
        }

        carPagerAdapter.updateCars(filtered)
        recyclerViewCars.scrollToPosition(0)
    }

    private fun openPaymentPage(car: Car, duration: String, price: Int) {
        startActivity(Intent(this, PaymentActivity::class.java).apply {
            putExtra("carId", car.id)
            putExtra("carName", car.name)
            putExtra("duration", duration)
            putExtra("price", price)
            putExtra("dailyPrice", car.dailyPrice)
        })
        @Suppress("DEPRECATION")
        overridePendingTransition(R.anim.slide_up_in, R.anim.fade_out)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.carmenu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menuSedans -> { loadCarsByCategory("Sedans"); true }
            R.id.menuSUVs -> { loadCarsByCategory("SUVs"); true }
            R.id.menuSports -> { loadCarsByCategory("Sports Cars"); true }
            R.id.menuProfile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                @Suppress("DEPRECATION")
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                true
            }
            R.id.menuSupport -> {
                Toast.makeText(this, "Support: Call +961 70 000 000", Toast.LENGTH_LONG).show(); true
            }
            R.id.menuLogout -> {
                getSharedPreferences("UserData", MODE_PRIVATE).edit { clear() }
                startActivity(Intent(this, LoginActivity::class.java))
                @Suppress("DEPRECATION")
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
