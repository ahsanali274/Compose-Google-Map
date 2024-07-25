package com.map.sampleapp

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority


class LocationService : Service() {

    private val CHANNEL_ID = "fixalert-location-notification"
    private val CHANNEL_NAME = "Location Tracking Channel"

    private val locationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(applicationContext)
    }

    private val gpsReceiver = GPSReceiver {
        if (!isLocationEnabled) {
            stopSelf()
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                Log.d(
                    "Location service",
                    "Current Location = [lat : ${location.latitude}," +
                            " lng : ${location.longitude}," +
                            " accuracy : ${location.accuracy}]",
                )
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        getLocation()
        registerReceiver(gpsReceiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(gpsReceiver)
        locationClient.removeLocationUpdates(locationCallback)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return START_STICKY
        }
        createNotificationChannel(CHANNEL_ID, CHANNEL_NAME)
        startForeground(1, getNotification())
        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? = null

    private fun getNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Service")
            .setContentText("Your App is receiving location updates")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
        }
        return builder.build()
    }

    private fun getLocation() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            stopSelf()
            return
        }
        if (!isLocationEnabled) {
            stopSelf()
            return
        }

        // We'll get location update after 30 seconds if user has moved at least 10m
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY, 30 * 1000
        ).setMinUpdateDistanceMeters(10f)

        locationClient.requestLocationUpdates(
            locationRequest.build(),
            locationCallback,
            Looper.getMainLooper()
        )
    }
}

val Context.isLocationEnabled: Boolean
    get() = (getSystemService(Context.LOCATION_SERVICE) as LocationManager?)?.isProviderEnabled(
        LocationManager.NETWORK_PROVIDER
    ) ?: false

fun Context.createNotificationChannel(
    channelId: String,
    channelName: String,
    description: String = ""
) {
    val channel = NotificationChannel(
        channelId,
        channelName,
        NotificationManager.IMPORTANCE_HIGH
    )
    channel.description = description
    val manager = getSystemService(NotificationManager::class.java)
    manager.createNotificationChannel(channel)
}