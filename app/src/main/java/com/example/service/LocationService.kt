package com.example.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.repository.AppRepository
import com.google.android.gms.location.*
import kotlinx.coroutines.*

class LocationService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null

    companion object {
        const val CHANNEL_ID = "LocationServiceChannel"
        const val NOTIFICATION_ID = 1001
        var isRunning = false
        
        // Simulating driving path coordinates
        val routeWaypoints = listOf(
            Pair(23.0225, 72.5714), // Crusher Plant
            Pair(23.0240, 72.5725),
            Pair(23.0262, 72.5738),
            Pair(23.0285, 72.5746),
            Pair(23.0305, 72.5760),
            Pair(23.0322, 72.5790),
            Pair(23.0338, 72.5850)  // Active Chainage
        )
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, getNotification("Live GPS Tracking Active", "Updating fleet telemetry in real-time"))

        // Attempt to request fused location
        try {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 8000L).apply {
                setMinUpdateIntervalMillis(5000L)
                setMaxUpdateAgeMillis(2000L)
            }.build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(p0: LocationResult) {
                    val location = p0.lastLocation ?: return
                    updateLocationData(location.latitude, location.longitude)
                }
            }

            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient?.requestLocationUpdates(locationRequest, locationCallback!!, Looper.getMainLooper())
            } else {
                startSimulationRoute()
            }
        } catch (e: Exception) {
            Log.e("LocationService", "Failed to init dynamic GPS Client: ${e.message}. Launching premium path simulator.")
            startSimulationRoute()
        }
    }

    private var simJob: Job? = null
    private fun startSimulationRoute() {
        simJob = serviceScope.launch {
            var index = 0
            while (isActive) {
                val coords = routeWaypoints[index]
                updateLocationData(coords.first, coords.second)
                index = (index + 1) % routeWaypoints.size
                delay(6000L) // step every 6 seconds
            }
        }
    }

    private fun updateLocationData(lat: Double, lng: Double) {
        val activeDriver = AppRepository.currentDriver.value
        if (activeDriver != null) {
            AppRepository.updateDriverLiveLocation(activeDriver.id, lat, lng)
            val updatedShipmentId = activeDriver.activeShipmentId
            val infoStr = if (updatedShipmentId != null) {
                "Active Shipment Ref: $updatedShipmentId"
            } else "Awaiting weighbridge assignment"
            
            // Post notification update
            val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mgr.notify(NOTIFICATION_ID, getNotification("Fleet Logged: ${activeDriver.vehicleNumber}", "Position: ${String.format("%.4f", lat)}, ${String.format("%.4f", lng)} | $infoStr"))
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        simJob?.cancel()
        locationCallback?.let { fusedLocationClient?.removeLocationUpdates(it) }
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun getNotification(title: String, text: String): Notification {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Chainage Navigator Transit Tracker",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Runs background location and material fleet updates."
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
