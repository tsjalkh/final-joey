package com.example.carrental1

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private var autoCompleteCategory: AutoCompleteTextView? = null
    private var autoCompleteCar: AutoCompleteTextView? = null
    private var viewPagerCars: ViewPager2? = null
    private var tabLayoutCarIndicator: TabLayout? = null
    
    private var carPagerAdapter: CarPagerAdapter? = null
    private var mainMediator: TabLayoutMediator? = null
    private var databaseHelper: DatabaseHelper? = null

    private var selectedCategory = "Sedans"
    private var selectedCar: Car? = null
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
        val images: List<String>,
    )

    private val carSeedList by lazy {
        listOf(
            Car("CAM24", "Toyota Camry", "Sedans", "2.5L 4-Cylinder", "203 hp", "Front-Wheel Drive", 5, "Comfortable, reliable sedan.", 45, 280, 950, listOf(
                "https://images.unsplash.com/photo-1621007947382-bb3c3994e3fb?w=800",
                "https://images.unsplash.com/photo-1619767886558-efdc259cde1a?w=800",
                "https://images.unsplash.com/photo-1631835320214-e053a4799986?w=800",
                "https://images.unsplash.com/photo-1617469767053-d3b508a0d822?w=800"
            )),
            Car("CIV24", "Honda Civic", "Sedans", "2.0L 4-Cylinder", "158 hp", "Front-Wheel Drive", 5, "Fuel-efficient compact sedan.", 40, 250, 850, listOf(
                "https://images.unsplash.com/photo-1599912027806-cfec9f5944b6?w=800",
                "https://images.unsplash.com/photo-1605810230434-7631ac76ec81?w=800",
                "https://images.unsplash.com/photo-1603811440715-618451877d9f?w=800",
                "https://images.unsplash.com/photo-1594070319944-7c0c63105cb2?w=800"
            )),
            Car("ALT24", "Nissan Altima", "Sedans", "2.5L 4-Cylinder", "188 hp", "Front-Wheel Drive", 5, "Spacious sedan with good comfort.", 42, 265, 900, listOf(
                "https://images.unsplash.com/photo-1534093607318-f025413f49cb?w=800",
                "https://images.unsplash.com/photo-1606148644562-0e248b945d8b?w=800",
                "https://images.unsplash.com/photo-1617469767053-d3b508a0d822?w=800",
                "https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?w=800"
            )),
            Car("RAV24", "Toyota RAV4", "SUVs", "2.5L 4-Cylinder", "203 hp", "All-Wheel Drive", 5, "Practical SUV with stability.", 60, 390, 1350, listOf(
                "https://images.unsplash.com/photo-1582236940823-3058bc9a813e?w=800",
                "https://images.unsplash.com/photo-1630043818197-2961e685f400?w=800",
                "https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?w=800",
                "https://images.unsplash.com/photo-1550355291-bbee04a92027?w=800"
            )),
            Car("TUC24", "Hyundai Tucson", "SUVs", "2.5L 4-Cylinder", "187 hp", "All-Wheel Drive", 5, "Modern SUV with good tech.", 58, 370, 1280, listOf(
                "https://images.unsplash.com/photo-1622321590747-d5867a6d884a?w=800",
                "https://images.unsplash.com/photo-1631481541416-56c39a2d3c90?w=800",
                "https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?w=800",
                "https://images.unsplash.com/photo-1609521263047-f8f205293f24?w=800"
            )),
            Car("SOR24", "Kia Sorento", "SUVs", "2.5L 4-Cylinder", "191 hp", "All-Wheel Drive", 7, "Large SUV for families.", 75, 480, 1650, listOf(
                "https://images.unsplash.com/photo-1619682817481-e994891cd1f5?w=800",
                "https://images.unsplash.com/photo-1617814076367-b759c7d6274a?w=800",
                "https://images.unsplash.com/photo-1542281286-9e0a16bb7366?w=800",
                "https://images.unsplash.com/photo-1621007947382-bb3c3994e3fb?w=800"
            )),
            Car("MUS24", "Ford Mustang", "Sports Cars", "5.0L V8", "480 hp", "Rear-Wheel Drive", 4, "Powerful sports car.", 120, 780, 2800, listOf(
                "https://images.unsplash.com/photo-1584345604482-81317384c400?w=800",
                "https://images.unsplash.com/photo-1525609004556-c46c7d6cf048?w=800",
                "https://images.unsplash.com/photo-1547744152-14d985cb937f?w=800",
                "https://images.unsplash.com/photo-1612802084222-3ecd7b2713f0?w=800"
            )),
            Car("CAMR24", "Chevrolet Camaro", "Sports Cars", "6.2L V8", "455 hp", "Rear-Wheel Drive", 4, "Sporty coupe with bold styling.", 115, 740, 2650, listOf(
                "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=800",
                "https://images.unsplash.com/photo-1549434764-da2006764428?w=800",
                "https://images.unsplash.com/photo-1615762867265-78189c746bc5?w=800",
                "https://images.unsplash.com/photo-1502161739775-80dc84a2d4f2?w=800"
            )),
            Car("Z424", "BMW Z4", "Sports Cars", "3.0L Turbo I6", "382 hp", "Rear-Wheel Drive", 2, "Luxury roadster.", 140, 900, 3200, listOf(
                "https://images.unsplash.com/photo-1556448851-9359658cb446?w=800",
                "https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800",
                "https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800",
                "https://images.unsplash.com/photo-1580273916550-e323be2ae537?w=800"
            ))
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: androidx.appcompat.widget.Toolbar? = findViewById(R.id.toolbar)
        toolbar?.let { setSupportActionBar(it) }

        autoCompleteCategory = findViewById(R.id.autoCompleteCategory)
        autoCompleteCar = findViewById(R.id.autoCompleteCar)
        viewPagerCars = findViewById(R.id.viewPagerCars)
        tabLayoutCarIndicator = findViewById(R.id.tabLayoutCarIndicator)

        databaseHelper = DatabaseHelper(this)
        try {
            databaseHelper?.seedCars(carSeedList)
            allCarsList = databaseHelper?.getAllCars() ?: emptyList()
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "DB load error", e)
            allCarsList = emptyList()
            Toast.makeText(this, "Error loading car data. Please try restarting the app.", Toast.LENGTH_LONG).show()
        }

        setupCategoryDropdown()
        setupCarPager()

        viewPagerCars?.post {
            loadCarsByCategory(selectedCategory)
        }
    }

    private fun setupCategoryDropdown() {
        val categories = listOf("Sedans", "SUVs", "Sports Cars")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        autoCompleteCategory?.setAdapter(adapter)
        autoCompleteCategory?.setText(categories[0], false)
        
        autoCompleteCategory?.setOnItemClickListener { _, _, position, _ ->
            loadCarsByCategory(categories[position])
        }
    }

    private fun setupCarPager() {
        carPagerAdapter = CarPagerAdapter(this, emptyList()) { car, duration, price, _ ->
            openPaymentPage(car, duration, price)
        }
        
        viewPagerCars?.adapter = carPagerAdapter
        
        viewPagerCars?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val filteredCars = allCarsList.filter { it.category == selectedCategory }
                if (position >= 0 && position < filteredCars.size) {
                    selectedCar = filteredCars[position]
                    autoCompleteCar?.setText(selectedCar?.name, false)
                }
            }
        })
    }

    private fun loadCarsByCategory(category: String) {
        selectedCategory = category
        val filteredCars = allCarsList.filter { it.category == category }
        val carNames = filteredCars.map { it.name }
        
        val carAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, carNames)
        autoCompleteCar?.setAdapter(carAdapter)
        
        carPagerAdapter?.updateCars(filteredCars)
        
        try {
            mainMediator?.detach()
            val indicator = tabLayoutCarIndicator
            val pager = viewPagerCars
            if (indicator != null && pager != null && filteredCars.isNotEmpty()) {
                mainMediator = TabLayoutMediator(indicator, pager) { _, _ -> }
                mainMediator?.attach()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        viewPagerCars?.setCurrentItem(0, false)
        
        if (filteredCars.isNotEmpty()) {
            selectedCar = filteredCars[0]
            autoCompleteCar?.setText(selectedCar?.name, false)
        }
        
        autoCompleteCar?.setOnItemClickListener { _, _, position, _ ->
            viewPagerCars?.setCurrentItem(position, true)
        }
    }

    private fun openPaymentPage(car: Car, duration: String, price: Int) {
        val intent = Intent(this, PaymentActivity::class.java).apply {
            putExtra("carId", car.id)
            putExtra("carName", car.name)
            putExtra("duration", duration)
            putExtra("price", price)
            putExtra("dailyPrice", car.dailyPrice)
        }
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.carmenu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuSedans -> { loadCarsByCategory("Sedans"); return true }
            R.id.menuSUVs -> { loadCarsByCategory("SUVs"); return true }
            R.id.menuSports -> { loadCarsByCategory("Sports Cars"); return true }
            R.id.menuProfile -> { startActivity(Intent(this, ProfileActivity::class.java)); return true }
            R.id.menuSupport -> { Toast.makeText(this, "Support: Call +961 70 000 000", Toast.LENGTH_LONG).show(); return true }
            R.id.menuLogout -> {
                getSharedPreferences("UserData", MODE_PRIVATE).edit { clear() }
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        databaseHelper?.close()
    }
}
