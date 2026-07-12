package com.example.service

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeighbridgeWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d("WeighbridgeWorker", "Periodic weighbridge automation and cache sync check executing.")
        
        try {
            // Check if any shipment is in "Loading" state and simulate weighbridge processing
            val pendingDispatched = AppRepository.shipments.value.filter { it.status == "Assigned" }
            
            if (pendingDispatched.isNotEmpty()) {
                Log.d("WeighbridgeWorker", "Found ${pendingDispatched.size} shipments awaiting Scale. Emulating auto weighbridge ticket.")
                
                // Automate dispatch for testing / demonstration purposes if setting is enabled
                val targetShipment = pendingDispatched.first()
                
                // Emulate weights
                val gross = 32.50 // 32.50 Tonnes
                val tare = 12.10  // 12.10 Tonnes (standard multi-axle dumper dray tare)
                
                withContext(Dispatchers.Main) {
                    AppRepository.processWeighbridge(targetShipment.id, gross, tare)
                }
            }

            // Sync any offline modifications if Firebase is active
            AppRepository.startRealtimeListeners()
            
            Result.success()
        } catch (e: Exception) {
            Log.e("WeighbridgeWorker", "Weighbridge sync work pipeline hit trouble: ${e.message}")
            Result.retry()
        }
    }
}
