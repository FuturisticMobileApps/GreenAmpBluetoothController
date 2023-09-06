package com.example.greenampbluetoothcontroller.data

import com.example.greenampbluetoothcontroller.data.core.BatteryPack
import com.example.greenampbluetoothcontroller.data.core.BluetoothDevices
import com.example.greenampbluetoothcontroller.util.Status

val deviceList = listOf(
    BluetoothDevices(
        name = "BAT-0001",
        address = "DE:dflD10202"
    ),
    BluetoothDevices(
        name = "BAT-0004",
        address = "DF:dtlD10202"
    ),
    BluetoothDevices(
        name = "BAT-0007",
        address = "CE:dflD10202"
    ),
)

val packList = listOf(
    BatteryPack(
        "BAT-0001",
        45.0,
        45.0,
        status = Status.OK_CHG
    ),
    BatteryPack(
        "BAT-0002",
        45.0,
        45.0,
        status = Status.OK_DSG
    ),
    BatteryPack(
        "BAT-0003",
        45.0,
        45.0,
        status = Status.P_FAIL
    ),
    BatteryPack(
        "BAT-0004",
        45.0,
        45.0,
        status = Status.FAIL
    ),
    BatteryPack(
        "BAT-0004",
        45.0,
        45.0,
        status = Status.NC
    ),
    BatteryPack(
        "BAT-0004",
        45.0,
        45.0,
        status = Status.OK_OFF
    )
)