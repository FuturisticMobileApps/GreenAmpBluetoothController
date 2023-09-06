package com.example.greenampbluetoothcontroller.data.core

import com.example.greenampbluetoothcontroller.util.Status

data class BatteryPack(
    val packName: String,
    val soc: Double,
    val temperature: Double,
    val status : Status
)
