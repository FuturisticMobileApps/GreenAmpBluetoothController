package com.example.greenampbluetoothcontroller.data.classic

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import com.example.greenampbluetoothcontroller.data.core.BatteryPack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidBluetoothController @Inject constructor(
    private val bluetoothManager: BluetoothManager,
    private val bluetoothAdapter: BluetoothAdapter
) : BluetoothController {

    override val isConnected: StateFlow<Boolean>
        get() = TODO("Not yet implemented")
    override val scannedDevices: StateFlow<List<BluetoothDevice>>
        get() = TODO("Not yet implemented")
    override val pairedDevices: StateFlow<List<BluetoothDevice>>
        get() = TODO("Not yet implemented")
    override val errors: SharedFlow<String>
        get() = TODO("Not yet implemented")

    override fun startDiscovery() {
        TODO("Not yet implemented")
    }

    override fun stopDiscovery() {
        TODO("Not yet implemented")
    }

    override fun startBluetoothServer(): Flow<ConnectionResult> {
        TODO("Not yet implemented")
    }

    override fun connectToDevice(device: BluetoothDevice): Flow<ConnectionResult> {
        TODO("Not yet implemented")
    }

    override suspend fun trySendMessage(message: String): BatteryPack? {
        TODO("Not yet implemented")
    }

    override fun closeConnection() {
        TODO("Not yet implemented")
    }

    override fun release() {
        TODO("Not yet implemented")
    }

}