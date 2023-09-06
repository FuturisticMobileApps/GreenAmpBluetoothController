package com.example.greenampbluetoothcontroller.data.classic

import com.example.greenampbluetoothcontroller.data.core.BatteryPack

sealed interface ConnectionResult {
    object ConnectionEstablished : ConnectionResult
    data class TransferSucceeded(val batteryDetails: BatteryPack) : ConnectionResult
    data class Error(val message: String) : ConnectionResult
}