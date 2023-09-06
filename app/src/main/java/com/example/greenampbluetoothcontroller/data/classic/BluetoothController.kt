package com.example.greenampbluetoothcontroller.data.classic

import android.bluetooth.BluetoothDevice
import com.example.greenampbluetoothcontroller.data.core.BatteryPack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface BluetoothController {
    val isConnected: StateFlow<Boolean>
    val scannedDevices: StateFlow<List<BluetoothDevice>>
    val pairedDevices: StateFlow<List<BluetoothDevice>>
    val errors: SharedFlow<String>

    fun startDiscovery()
    fun stopDiscovery()

    fun startBluetoothServer(): Flow<ConnectionResult>
    fun connectToDevice(device: BluetoothDevice): Flow<ConnectionResult>

    suspend fun trySendMessage(message: String): BatteryPack?

    fun closeConnection()
    fun release()
}