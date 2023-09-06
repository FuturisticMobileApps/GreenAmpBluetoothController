package com.example.greenampbluetoothcontroller.data.core

import com.example.greenampbluetoothcontroller.data.interactors.ConnectionState

data class BatteryDetails(
    val temperature:Float,
    val voltage : Float,
    val connectionState: ConnectionState
)
