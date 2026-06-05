package com.example.carrental1

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class RentalEndReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val carName = intent.getStringExtra("carName") ?: "your car"
        val bookingId = intent.getLongExtra("bookingId", -1L)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Rental Reminders", NotificationManager.IMPORTANCE_HIGH)
                    .apply { description = "Notifies you when your rental period ends" }
            )
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Rental Ended — $carName")
            .setContentText("Your rental period for $carName has ended today. Thank you for choosing CedarDrive!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Your rental period for $carName has ended today. Thank you for choosing CedarDrive!")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(if (bookingId != -1L) bookingId.toInt() else 0, notification)
    }

    companion object {
        const val CHANNEL_ID = "rental_end_channel"
    }
}
