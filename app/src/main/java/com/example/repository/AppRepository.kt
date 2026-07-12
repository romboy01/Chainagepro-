package com.example.repository

import android.util.Log
import com.example.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

object AppRepository {
    private const val TAG = "AppRepository"

    // Firebase references (null-safe in case of failure to load/no services json)
    private var auth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null
    private var isFirebaseAvailable = false

    // Cache streams for full-app responsiveness (combines remote/local sync)
    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _chainages = MutableStateFlow<List<Chainage>>(emptyList())
    val chainages: StateFlow<List<Chainage>> = _chainages.asStateFlow()

    private val _requirements = MutableStateFlow<List<Requirement>>(emptyList())
    val requirements: StateFlow<List<Requirement>> = _requirements.asStateFlow()

    private val _shipments = MutableStateFlow<List<Shipment>>(emptyList())
    val shipments: StateFlow<List<Shipment>> = _shipments.asStateFlow()

    private val _drivers = MutableStateFlow<List<Driver>>(emptyList())
    val drivers: StateFlow<List<Driver>> = _drivers.asStateFlow()

    private val _groupMessages = MutableStateFlow<List<GroupMessage>>(emptyList())
    val groupMessages: StateFlow<List<GroupMessage>> = _groupMessages.asStateFlow()

    private val _loadingRequests = MutableStateFlow<List<LoadingRequest>>(emptyList())
    val loadingRequests: StateFlow<List<LoadingRequest>> = _loadingRequests.asStateFlow()

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val _activityLogs = MutableStateFlow<List<ActivityLog>>(emptyList())
    val activityLogs: StateFlow<List<ActivityLog>> = _activityLogs.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _currentDriver = MutableStateFlow<Driver?>(null)
    val currentDriver: StateFlow<Driver?> = _currentDriver.asStateFlow()

    private val listeners = mutableListOf<ListenerRegistration>()

    init {
        try {
            auth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()
            isFirebaseAvailable = true
            Log.d(TAG, "Firebase initialized successfully.")
        } catch (e: Exception) {
            isFirebaseAvailable = false
            Log.e(TAG, "Firebase is not available (probably missing google-services.json): ${e.message}")
        }
        
        // Always pre-populate local states first so the app has high-quality demo data instantly
        prepopulateData()
    }

    private fun prepopulateData() {
        // Pre-populating robust sample data
        val initialProjects = listOf(
            Project("p1", "National Highway NH-48 Ext", "Ahmedabad - Vadodara Section", 48.0),
            Project("p2", "Expressway Corridor Sec-4", "Delhi - Mumbai Corridor", 12.5),
            Project("p3", "Eastern Bypass Phase-II", "Ring Road Concourse", 18.2)
        )
        _projects.value = initialProjects

        val initialChainages = listOf(
            Chainage("c1", "p1", "Elevated Flyover Bypass", 12.400, 14.800, "Active"),
            Chainage("c2", "p1", "Widen Lane Segment A", 20.100, 25.500, "Active"),
            Chainage("c3", "p1", "Main Toll Apron Area", 0.000, 2.100, "Active"),
            Chainage("c4", "p2", "Interchange Crossing Sec-4B", 42.000, 45.200, "Active")
        )
        _chainages.value = initialChainages

        val initialRequirements = listOf(
            Requirement("r1", "c1", "p1", "GSB Subbase", 15000.0, 9850.2, "Active", "Site Eng. Mehta", System.currentTimeMillis() - 86400000),
            Requirement("r2", "c1", "p1", "WMM Mix Base", 12000.0, 4320.0, "Active", "Supervisor Rajan", System.currentTimeMillis() - 43200000),
            Requirement("r3", "c2", "p1", "DBM Asphalt Binder", 8000.0, 1200.5, "Active", "Site Eng. Mehta", System.currentTimeMillis() - 21600000),
            Requirement("r4", "c3", "p1", "BC Wearing Course", 5000.0, 0.0, "Pending", "PM Sharma", System.currentTimeMillis())
        )
        _requirements.value = initialRequirements

        val initialDrivers = listOf(
            Driver("d1", "Vipul Patel", "+919876543210", "GJ-01-ZZ-1234", true, "s1", 23.0225, 72.5714, System.currentTimeMillis()),
            Driver("d2", "Sukhdev Singh", "+919876543211", "HR-55-AA-5678", true, null, 23.0512, 72.5930, System.currentTimeMillis() - 300000),
            Driver("d3", "Mahendra Yadav", "+919876543212", "MH-12-QQ-9012", false, null, 23.0118, 72.5622, System.currentTimeMillis() - 600000)
        )
        _drivers.value = initialDrivers

        val initialShipments = listOf(
            Shipment("s1", "r1", "d1", "Vipul Patel", "+919876543210", "GJ-01-ZZ-1234", "GSB Subbase", 28.45, 12.15, 16.30, "Dispatched", System.currentTimeMillis() - 3600000, 0L, notes = "Direct dispatch to Ch:12+800 LHS"),
            Shipment("s2", "r2", "d2", "Sukhdev Singh", "+919876543211", "HR-55-AA-5678", "WMM Mix Base", 32.10, 12.20, 19.90, "Delivered", System.currentTimeMillis() - 7200000, System.currentTimeMillis() - 5400000, notes = "Unloaded successfully at Sec B"),
            Shipment("s3", "r3", "d3", "Mahendra Yadav", "+919876543212", "MH-12-QQ-9012", "DBM Asphalt Binder", 0.0, 0.0, 0.0, "Assigned", System.currentTimeMillis(), 0L)
        )
        _shipments.value = initialShipments

        val initialMessages = listOf(
            GroupMessage("m1", "Dispatching 5 trucks of WMM to elevated flyover section.", null, null, "Rakesh Joshi", "+919988776655", "Plant Incharge", System.currentTimeMillis() - 7200000),
            GroupMessage("m2", "Received 3 trucks. Grade quality checked.", "m1", "Dispatching 5 trucks of WMM...", "Nilesh Mehta", "+919988776611", "Site Engineer", System.currentTimeMillis() - 3600000),
            GroupMessage("m3", "Sukhdev has completed delivery of truck HR-55-AA-5678.", null, null, "Vipul Patel", "+919876543210", "Driver", System.currentTimeMillis() - 1800000)
        )
        _groupMessages.value = initialMessages

        val initialRequests = listOf(
            LoadingRequest("lr1", "r1", "c1", "Elevated Flyover Bypass", "GSB Subbase", "Approved", "Supervisor Rajan", "Rakesh Joshi", System.currentTimeMillis() - 10800000),
            LoadingRequest("lr2", "r3", "c2", "Widen Lane Segment A", "DBM Asphalt Binder", "Pending", "Site Eng. Mehta", null, System.currentTimeMillis() - 1800000)
        )
        _loadingRequests.value = initialRequests

        val initialNotifications = listOf(
            AppNotification("n1", "admin1", "New Loading Request", "Site Eng. Mehta requested active DBM Asphalt Binder start loading for c2.", "Loading", System.currentTimeMillis() - 1800000, false),
            AppNotification("n2", "admin1", "Shipment Dispatched", "Sukhdev Singh (HR-55-AA-5678) has cleared the Weighbridge.", "Dispatch", System.currentTimeMillis() - 3600000, true)
        )
        _notifications.value = initialNotifications

        val initialLogs = listOf(
            ActivityLog("l1", "admin1", "Rakesh Joshi", "Plant Incharge", "APPROVE_LOADING", "Approved loading request lr1 for Elevated Flyover Bypass", System.currentTimeMillis() - 10800000),
            ActivityLog("l2", "op1", "Anuj Shah", "Weighbridge Operator", "RECORD_WEIGHT", "Gross weight recorded for GJ-01-ZZ-1234: 28.45 Tonnes", System.currentTimeMillis() - 3600000)
        )
        _activityLogs.value = initialLogs
    }

    // 1. Role-Based Login System
    fun loginAsUser(email: String, role: String, onResult: (User?, String?) -> Unit) {
        val cleanEmail = email.trim()
        val name = cleanEmail.split("@").firstOrNull()?.capitalize() ?: "Staff User"
        val mockUser = User(
            id = "usr_${UUID.randomUUID().toString().take(6)}",
            name = name,
            email = cleanEmail,
            role = role,
            phone = "+919000012345",
            onlineStatus = true,
            currentProject = "p1"
        )
        _currentUser.value = mockUser
        addActivityLog(mockUser.id, mockUser.name, mockUser.role, "LOGIN", "Logged into Chainage Navigator Pro")

        if (isFirebaseAvailable) {
            auth?.signInWithEmailAndPassword(cleanEmail, "123456")?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth?.currentUser?.uid ?: mockUser.id
                    val remoteUser = mockUser.copy(id = uid)
                    _currentUser.value = remoteUser
                    
                    // Sync user profile to Firestore
                    firestore?.collection("users")?.document(uid)?.set(remoteUser)
                    onResult(remoteUser, null)
                } else {
                    // Fail gracefully and use mock login for rapid preview
                    Log.w(TAG, "Auth sign-in failed, utilizing robust offline mode.", task.exception)
                    onResult(mockUser, null)
                }
            }
        } else {
            onResult(mockUser, null)
        }
    }

    fun loginAsDriver(phone: String, vehicleNumber: String, onResult: (Driver?, String?) -> Unit) {
        val cleanPhone = phone.trim()
        val cleanVehicle = vehicleNumber.trim().uppercase()
        
        // Find if driver exists or create a new one
        val matched = _drivers.value.find { it.phone == cleanPhone || it.vehicleNumber == cleanVehicle }
        val finalDriver = matched?.copy(phone = cleanPhone, vehicleNumber = cleanVehicle, lastUpdated = System.currentTimeMillis())
            ?: Driver(
                id = "drv_${UUID.randomUUID().toString().take(6)}",
                name = "Driver " + cleanVehicle.takeLast(4),
                phone = cleanPhone,
                vehicleNumber = cleanVehicle,
                vehicleVerify = true,
                lastUpdated = System.currentTimeMillis()
            )

        // Make sure it is in local drivers roster
        if (matched == null) {
            _drivers.value = _drivers.value + finalDriver
        } else {
            _drivers.value = _drivers.value.map { if (it.id == finalDriver.id) finalDriver else it }
        }
        
        _currentDriver.value = finalDriver
        addActivityLog(finalDriver.id, finalDriver.name, "Driver", "LOGIN_DRIVER", "Driver checked in with vehicle $cleanVehicle")

        if (isFirebaseAvailable) {
            firestore?.collection("drivers")?.document(finalDriver.id)?.set(finalDriver)
                ?.addOnSuccessListener {
                    onResult(finalDriver, null)
                }?.addOnFailureListener {
                    // Fallback
                    onResult(finalDriver, null)
                }
        } else {
            onResult(finalDriver, null)
        }
    }

    fun logout() {
        val user = _currentUser.value
        if (user != null) {
            addActivityLog(user.id, user.name, user.role, "LOGOUT", "Logged out of application")
        }
        _currentUser.value = null
        _currentDriver.value = null
        try {
            auth?.signOut()
        } catch (e: Exception) {}
    }

    // 2. Chainage & Requirements Management
    fun addProject(name: String, location: String, lengthKm: Double) {
        val newProject = Project("p_${UUID.randomUUID().toString().take(6)}", name, location, lengthKm)
        _projects.value = _projects.value + newProject
        
        if (isFirebaseAvailable) {
            firestore?.collection("projects")?.document(newProject.id)?.set(newProject)
        }
    }

    fun addChainage(projectId: String, segmentName: String, startKm: Double, endKm: Double) {
        val newChainage = Chainage(
            id = "c_${UUID.randomUUID().toString().take(6)}",
            projectId = projectId,
            name = segmentName,
            startKm = startKm,
            endKm = endKm,
            status = "Active"
        )
        _chainages.value = _chainages.value + newChainage
        
        if (isFirebaseAvailable) {
            firestore?.collection("chainages")?.document(newChainage.id)?.set(newChainage)
        }
        
        val user = _currentUser.value
        if (user != null) {
            addActivityLog(user.id, user.name, user.role, "ADD_CHAINAGE", "Created new chainage stretch: $segmentName (${startKm}km to ${endKm}km)")
        }
    }

    fun addRequirement(chainageId: String, projectId: String, material: String, quantity: Double) {
        val newRequirement = Requirement(
            id = "req_${UUID.randomUUID().toString().take(6)}",
            chainageId = chainageId,
            projectId = projectId,
            materialType = material,
            targetQuantity = quantity,
            completedQuantity = 0.0,
            status = "Pending",
            requestedBy = _currentUser.value?.name ?: "System",
            timestamp = System.currentTimeMillis()
        )
        _requirements.value = _requirements.value + newRequirement
        
        if (isFirebaseAvailable) {
            firestore?.collection("requirements")?.document(newRequirement.id)?.set(newRequirement)
        }

        // Trigger loading notification
        sendLocalNotification(
            "New Requirement Posted",
            "Req: $material - $quantity Tonnes requested for chainage ${_chainages.value.find { it.id == chainageId }?.name ?: ""}",
            "Requirement"
        )
        
        val user = _currentUser.value
        if (user != null) {
            addActivityLog(user.id, user.name, user.role, "CREATE_REQUIREMENT", "Posted requirement for $quantity Tons of $material")
        }
    }

    // 3. Loading Request & Approval Workflow
    fun requestLoading(requirementId: String) {
        val req = _requirements.value.find { it.id == requirementId } ?: return
        val chainage = _chainages.value.find { it.id == req.chainageId }
        val newRequest = LoadingRequest(
            id = "lr_${UUID.randomUUID().toString().take(6)}",
            requirementId = requirementId,
            chainageId = req.chainageId,
            chainageName = chainage?.name ?: "Main Stretch",
            materialType = req.materialType,
            status = "Pending",
            requestedBy = _currentUser.value?.name ?: "Site Representative",
            timestamp = System.currentTimeMillis()
        )
        _loadingRequests.value = _loadingRequests.value + newRequest
        
        if (isFirebaseAvailable) {
            firestore?.collection("loading_requests")?.document(newRequest.id)?.set(newRequest)
        }

        sendLocalNotification(
            "Loading Permission Request",
            "${newRequest.requestedBy} requested permission to load ${newRequest.materialType}",
            "Loading"
        )
        
        val user = _currentUser.value
        if (user != null) {
            addActivityLog(user.id, user.name, user.role, "REQUEST_LOADING", "Requested loading start approval for ${req.materialType}")
        }
    }

    fun approveOrRejectLoadingRequest(requestId: String, approve: Boolean) {
        val status = if (approve) "Approved" else "Rejected"
        _loadingRequests.value = _loadingRequests.value.map {
            if (it.id == requestId) {
                it.copy(status = status, approvedBy = _currentUser.value?.name ?: "Plant Manager")
            } else it
        }

        val request = _loadingRequests.value.find { it.id == requestId } ?: return
        
        // If approved, update requirement status to denote active processing
        if (approve) {
            _requirements.value = _requirements.value.map {
                if (it.id == request.requirementId) {
                    it.copy(status = "Active")
                } else it
            }
        }

        if (isFirebaseAvailable) {
            firestore?.collection("loading_requests")?.document(requestId)?.update(
                "status", status,
                "approvedBy", _currentUser.value?.name ?: "Authorized Manager"
            )
            if (approve) {
                firestore?.collection("requirements")?.document(request.requirementId)?.update("status", "Active")
            }
        }

        sendLocalNotification(
            "Loading Request $status",
            "Load request for ${request.materialType} at ${request.chainageName} was $status",
            "Loading"
        )
        
        val user = _currentUser.value
        if (user != null) {
            addActivityLog(user.id, user.name, user.role, "ACTION_LOADING_REQUEST", "$status loading request $requestId")
        }
    }

    // 4. Dispatch System & Weighbridge Automation (Gross, Tare, Net)
    fun processWeighbridge(shipmentId: String, grossWeight: Double, tareWeight: Double) {
        val net = if (grossWeight > tareWeight) grossWeight - tareWeight else 0.0
        
        var completedShipment: Shipment? = null
        
        _shipments.value = _shipments.value.map {
            if (it.id == shipmentId) {
                val updated = it.copy(
                    grossWeight = grossWeight,
                    tareWeight = tareWeight,
                    netWeight = net,
                    status = "Dispatched",
                    dispatchTime = System.currentTimeMillis()
                )
                completedShipment = updated
                updated
            } else it
        }

        val shipment = completedShipment ?: return

        // Auto update completed quantity in the relevant requirement
        _requirements.value = _requirements.value.map {
            if (it.id == shipment.requirementId) {
                val newQty = it.completedQuantity + net
                val newStatus = if (newQty >= it.targetQuantity) "Completed" else it.status
                it.copy(completedQuantity = newQty, status = newStatus)
            } else it
        }

        if (isFirebaseAvailable) {
            firestore?.collection("shipments")?.document(shipmentId)?.set(shipment)
            
            // Sync requirement completed quantity
            val req = _requirements.value.find { it.id == shipment.requirementId }
            if (req != null) {
                firestore?.collection("requirements")?.document(req.id)?.set(req)
            }
        }

        sendLocalNotification(
            "Vehicle Dispatched ($net T)",
            "Truck ${shipment.vehicleNumber} dispatched with Net load of $net Tonnes.",
            "Dispatch"
        )
        
        val user = _currentUser.value
        if (user != null) {
            addActivityLog(
                user.id, user.name, user.role, "WEIGHBRIDGE_CHECKOUT",
                "Processed Weighbridge for ${shipment.vehicleNumber}. Net Weight: $net Tonnes"
            )
        }
    }

    fun assignDriverToShipment(requirementId: String, driverId: String, vehicleNum: String, driverPhone: String) {
        val req = _requirements.value.find { it.id == requirementId } ?: return
        val matchedDriver = _drivers.value.find { it.id == driverId }
        val id = "ship_${UUID.randomUUID().toString().take(6)}"
        
        val newShipment = Shipment(
            id = id,
            requirementId = requirementId,
            driverId = driverId,
            driverName = matchedDriver?.name ?: "Driver Assignee",
            driverPhone = driverPhone,
            vehicleNumber = vehicleNum,
            materialType = req.materialType,
            status = "Assigned",
            dispatchTime = 0L
        )

        _shipments.value = _shipments.value + newShipment

        // Update driver's active shipment
        _drivers.value = _drivers.value.map {
            if (it.id == driverId) {
                it.copy(activeShipmentId = id, vehicleNumber = vehicleNum, vehicleVerify = true)
            } else it
        }

        if (isFirebaseAvailable) {
            firestore?.collection("shipments")?.document(id)?.set(newShipment)
            firestore?.collection("drivers")?.document(driverId)?.update(
                "activeShipmentId", id,
                "vehicleNumber", vehicleNum,
                "vehicleVerify", true
            )
        }

        sendLocalNotification(
            "Driver Shipment Assigned",
            "Shipment for ${req.materialType} assigned to Vehicle $vehicleNum",
            "Dispatch"
        )
    }

    // 5. Driver Confirmation, Delivery and Live Tracking
    fun updateDriverLiveLocation(driverId: String, lat: Double, lng: Double) {
        _drivers.value = _drivers.value.map {
            if (it.id == driverId) {
                it.copy(currentLat = lat, currentLng = lng, lastUpdated = System.currentTimeMillis())
            } else it
        }

        val updatedDriver = _drivers.value.find { it.id == driverId } ?: return
        if (_currentDriver.value?.id == driverId) {
            _currentDriver.value = updatedDriver
        }

        if (isFirebaseAvailable) {
            firestore?.collection("drivers")?.document(driverId)?.update(
                "currentLat", lat,
                "currentLng", lng,
                "lastUpdated", System.currentTimeMillis()
            )
            // Save registry to logs
            val gpl = DriverLocation(driverId, lat, lng, 0.0f, 0.0f, System.currentTimeMillis())
            firestore?.collection("driver_locations")?.add(gpl)
        }
    }

    fun confirmDelivery(shipmentId: String) {
        var completeDrvId: String? = null
        _shipments.value = _shipments.value.map {
            if (it.id == shipmentId) {
                completeDrvId = it.driverId
                it.copy(status = "Delivered", deliveryTime = System.currentTimeMillis())
            } else it
        }

        val shipmentObj = _shipments.value.find { it.id == shipmentId } ?: return

        // Clear active shipment on driver
        completeDrvId?.let { drvId ->
            _drivers.value = _drivers.value.map {
                if (it.id == drvId) {
                    it.copy(activeShipmentId = null)
                } else it
            }
            if (_currentDriver.value?.id == drvId) {
                _currentDriver.value = _currentDriver.value?.copy(activeShipmentId = null)
            }
            if (isFirebaseAvailable) {
                firestore?.collection("drivers")?.document(drvId)?.update("activeShipmentId", null)
            }
        }

        if (isFirebaseAvailable) {
            firestore?.collection("shipments")?.document(shipmentId)?.update(
                "status", "Delivered",
                "deliveryTime", System.currentTimeMillis()
            )
        }

        sendLocalNotification(
            "Shipment Delivered 🎉",
            "Trip complete! ${shipmentObj.vehicleNumber} successfully unloaded ${shipmentObj.netWeight} Tons of ${shipmentObj.materialType}.",
            "Dispatch"
        )
        
        addActivityLog(
            shipmentObj.driverId, shipmentObj.driverName, "Driver", "DELIVERY_CONFIRM",
            "Confirmed unloading at Chainage site. Net: ${shipmentObj.netWeight} T"
        )
    }

    // 6. WhatsApp-Style Real-time Group Chat System
    fun sendGroupChatMessage(text: String, replyToId: String? = null, replyToText: String? = null) {
        val user = _currentUser.value
        val driver = _currentDriver.value
        
        val senderName = user?.name ?: driver?.name ?: "Anonymous"
        val senderNumber = user?.phone ?: driver?.phone ?: "+919999999999"
        val senderRole = user?.role ?: "Driver"

        val newMessage = GroupMessage(
            id = "msg_${UUID.randomUUID().toString().take(6)}",
            messageText = text,
            replyToId = replyToId,
            replyToText = replyToText,
            senderName = senderName,
            senderNumber = senderNumber,
            senderRole = senderRole,
            timestamp = System.currentTimeMillis()
        )

        _groupMessages.value = _groupMessages.value + newMessage

        if (isFirebaseAvailable) {
            firestore?.collection("group_messages")?.document(newMessage.id)?.set(newMessage)
        }

        sendLocalNotification(
            "New Message from $senderName",
            text,
            "Chat"
        )
    }

    // 7. General local push simulator + FCM trigger config
    fun sendLocalNotification(title: String, message: String, type: String) {
        val note = AppNotification(
            id = "notif_${UUID.randomUUID().toString().take(6)}",
            userId = _currentUser.value?.id ?: "broadcast",
            title = title,
            message = message,
            type = type,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        _notifications.value = listOf(note) + _notifications.value

        if (isFirebaseAvailable) {
            firestore?.collection("notifications")?.document(note.id)?.set(note)
        }
    }

    fun markNotificationsAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
        
        if (isFirebaseAvailable) {
            _notifications.value.forEach { notif ->
                firestore?.collection("notifications")?.document(notif.id)?.update("isRead", true)
            }
        }
    }

    private fun addActivityLog(userId: String, userName: String, role: String, action: String, desc: String) {
        val log = ActivityLog(
            id = "log_${UUID.randomUUID().toString().take(6)}",
            userId = userId,
            userName = userName,
            userRole = role,
            actionType = action,
            description = desc,
            timestamp = System.currentTimeMillis()
        )
        _activityLogs.value = listOf(log) + _activityLogs.value
        
        if (isFirebaseAvailable) {
            firestore?.collection("activity_logs")?.document(log.id)?.set(log)
        }
    }

    // Real-Time Firebase sync engine
    fun startRealtimeListeners() {
        if (!isFirebaseAvailable) return
        
        // Clear old ones first
        listeners.forEach { it.remove() }
        listeners.clear()

        try {
            // Group Chat Messages
            val mListener = firestore?.collection("group_messages")
                ?.orderBy("timestamp", Query.Direction.ASCENDING)
                ?.addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e(TAG, "Group message listen failed.", e)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val messages = snapshot.toObjects(GroupMessage::class.java)
                        if (messages.isNotEmpty()) {
                            _groupMessages.value = messages
                        }
                    }
                }
            if (mListener != null) listeners.add(mListener)

            // Active Shipments
            val sListener = firestore?.collection("shipments")
                ?.addSnapshotListener { snapshot, e ->
                    if (e == null && snapshot != null) {
                        val shs = snapshot.toObjects(Shipment::class.java)
                        if (shs.isNotEmpty()) {
                            _shipments.value = shs
                        }
                    }
                }
            if (sListener != null) listeners.add(sListener)

            // Requirements
            val rListener = firestore?.collection("requirements")
                ?.addSnapshotListener { snapshot, e ->
                    if (e == null && snapshot != null) {
                        val reqs = snapshot.toObjects(Requirement::class.java)
                        if (reqs.isNotEmpty()) {
                            _requirements.value = reqs
                        }
                    }
                }
            if (rListener != null) listeners.add(rListener)

            // Loading Requests
            val lrListener = firestore?.collection("loading_requests")
                ?.addSnapshotListener { snapshot, e ->
                    if (e == null && snapshot != null) {
                        val reqs = snapshot.toObjects(LoadingRequest::class.java)
                        if (reqs.isNotEmpty()) {
                            _loadingRequests.value = reqs
                        }
                    }
                }
            if (lrListener != null) listeners.add(lrListener)

            // Drivers
            val dListener = firestore?.collection("drivers")
                ?.addSnapshotListener { snapshot, e ->
                    if (e == null && snapshot != null) {
                        val drvs = snapshot.toObjects(Driver::class.java)
                        if (drvs.isNotEmpty()) {
                            _drivers.value = drvs
                        }
                    }
                }
            if (dListener != null) listeners.add(dListener)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start real-time sync listeners.", e)
        }
    }

    fun stopRealtimeListeners() {
        listeners.forEach { it.remove() }
        listeners.clear()
    }
}
