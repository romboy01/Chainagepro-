package com.example.model

import java.io.Serializable

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "", // Admin, Project Manager, Site Engineer, Supervisor, WMM Operator, DBM Operator, BC Operator, GSB Operator, Weighbridge Operator, Crusher Incharge, WMM Incharge, DBM Incharge, BC Incharge, GSB Incharge, Plant Incharge
    val phone: String = "",
    val onlineStatus: Boolean = false,
    val currentProject: String = ""
) : Serializable

data class Driver(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val vehicleNumber: String = "",
    val vehicleVerify: Boolean = false,
    val activeShipmentId: String? = null,
    val currentLat: Double = 0.0,
    val currentLng: Double = 0.0,
    val lastUpdated: Long = 0L
) : Serializable

data class Chainage(
    val id: String = "",
    val projectId: String = "",
    val name: String = "",
    val startKm: Double = 0.0,
    val endKm: Double = 0.0,
    val status: String = "Active" // Active, Completed
) : Serializable

data class Requirement(
    val id: String = "",
    val chainageId: String = "",
    val projectId: String = "",
    val materialType: String = "", // GSB, WMM, DBM, BC
    val targetQuantity: Double = 0.0,
    val completedQuantity: Double = 0.0,
    val status: String = "Pending", // Pending, Active, Completed
    val requestedBy: String = "",
    val timestamp: Long = 0L
) : Serializable

data class Shipment(
    val id: String = "",
    val requirementId: String = "",
    val driverId: String = "",
    val driverName: String = "",
    val driverPhone: String = "",
    val vehicleNumber: String = "",
    val materialType: String = "",
    val grossWeight: Double = 0.0,
    val tareWeight: Double = 0.0,
    val netWeight: Double = 0.0,
    val status: String = "Assigned", // Assigned, Loading, Weighbridge, Dispatched, Delivered, Cancelled
    val dispatchTime: Long = 0L,
    val deliveryTime: Long = 0L,
    val sourceName: String = "Crusher Crusher Plant & Weighbridge Base",
    val destName: String = "Chainage Road Segment",
    val sourceLat: Double = 23.0225,
    val sourceLng: Double = 72.5714,
    val destLat: Double = 23.0338,
    val destLng: Double = 72.5850,
    val notes: String = ""
) : Serializable

data class DriverLocation(
    val driverId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val bearing: Float = 0.0f,
    val accuracy: Float = 0.0f,
    val timestamp: Long = 0L
) : Serializable

data class ActivityLog(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userRole: String = "",
    val actionType: String = "",
    val description: String = "",
    val timestamp: Long = 0L
) : Serializable

data class AppNotification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "General", // Chat, Dispatch, Loading, Requirement
    val timestamp: Long = 0L,
    val isRead: Boolean = false
) : Serializable

data class GroupMessage(
    val id: String = "",
    val messageText: String = "",
    val replyToId: String? = null,
    val replyToText: String? = null,
    val senderName: String = "",
    val senderNumber: String = "",
    val senderRole: String = "",
    val timestamp: Long = 0L
) : Serializable

data class LoadingRequest(
    val id: String = "",
    val requirementId: String = "",
    val chainageId: String = "",
    val chainageName: String = "",
    val materialType: String = "",
    val status: String = "Pending", // Pending, Approved, Rejected
    val requestedBy: String = "",
    val approvedBy: String? = null,
    val timestamp: Long = 0L
) : Serializable

data class Project(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val lengthKm: Double = 0.0
) : Serializable

data class AppSettings(
    val id: String = "default_settings",
    val autoWeighbridgeTrigger: Boolean = true,
    val fcmEnabled: Boolean = true,
    val gpsIntervalSeconds: Int = 10
) : Serializable
