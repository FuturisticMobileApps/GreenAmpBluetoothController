package com.example.greenampbluetoothcontroller.dataStore

data class BLEDeviceLocal(
    val deviceName : String,
    val macAddress : String,
    val time : Long = System.currentTimeMillis()
)
