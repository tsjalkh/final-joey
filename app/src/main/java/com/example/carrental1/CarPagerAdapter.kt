package com.example.carrental1

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class CarPagerAdapter(
    private val context: Context,
    private var cars: List<MainActivity.Car>,
    private val onPackageSelected: (MainActivity.Car, String, Int, Long) -> Unit
) : RecyclerView.Adapter<CarPagerAdapter.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val viewPagerCarImages: ViewPager2 = view.findViewById(R.id.viewPagerCarImages)
        val tabLayoutIndicator: TabLayout = view.findViewById(R.id.tabLayoutIndicator)
        val textCarName: TextView = view.findViewById(R.id.textCarName)
        val textCarInfo: TextView = view.findViewById(R.id.textCarInfo)
        val btnRentDay: MaterialButton = view.findViewById(R.id.btnRentDay)
        val btnRentWeek: MaterialButton = view.findViewById(R.id.btnRentWeek)
        val btnRentMonth: MaterialButton = view.findViewById(R.id.btnRentMonth)
        var mediator: TabLayoutMediator? = null
    }

    override fun getItemId(position: Int): Long =
        if (position in cars.indices) cars[position].id.hashCode().toLong() else position.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_car_page, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position !in cars.indices) return
        val car = cars[position]

        holder.textCarName.text = car.name
        holder.textCarInfo.text = buildString {
            append("${car.engine}  •  ${car.power}\n")
            append("${car.drivetrain}  •  ${car.seats} seats\n\n")
            append(car.description)
        }

        // Show price prominently on each button
        holder.btnRentDay.text = "1 Day  —  \$${car.dailyPrice}"
        holder.btnRentWeek.text = "1 Week  —  \$${car.weeklyPrice}"
        holder.btnRentMonth.text = "1 Month  —  \$${car.monthlyPrice}"

        // Detach stale inner mediator before swapping the image adapter
        holder.mediator?.detach()
        holder.mediator = null

        val imageAdapter = CarImageAdapter(car.images)
        holder.viewPagerCarImages.adapter = imageAdapter

        if (car.images.isNotEmpty()) {
            holder.mediator = TabLayoutMediator(holder.tabLayoutIndicator, holder.viewPagerCarImages) { _, _ -> }
            holder.mediator!!.attach()
        }

        holder.btnRentDay.setOnClickListener {
            onPackageSelected(car, "1 day", car.dailyPrice, 24L * 60 * 60 * 1000)
        }
        holder.btnRentWeek.setOnClickListener {
            onPackageSelected(car, "1 week", car.weeklyPrice, 7L * 24 * 60 * 60 * 1000)
        }
        holder.btnRentMonth.setOnClickListener {
            onPackageSelected(car, "1 month", car.monthlyPrice, 30L * 24 * 60 * 60 * 1000)
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        holder.mediator?.detach()
        holder.mediator = null
    }

    override fun getItemCount() = cars.size

    fun updateCars(newCars: List<MainActivity.Car>) {
        cars = newCars
        notifyDataSetChanged()
    }
}
