package com.example.greenampbluetoothcontroller.battery

import androidx.annotation.ColorInt

data class BatteryColorBoundary(
    var topBound: Int,
    @ColorInt var color: Int
)