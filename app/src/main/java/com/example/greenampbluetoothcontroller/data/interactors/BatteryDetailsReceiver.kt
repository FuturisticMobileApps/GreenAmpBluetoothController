package com.example.greenampbluetoothcontroller.data.interactors

import com.example.greenampbluetoothcontroller.data.core.BatteryDetails
import com.example.greenampbluetoothcontroller.util.Resource
import kotlinx.coroutines.flow.MutableSharedFlow

interface BatteryDetailsReceiver {

    val data: MutableSharedFlow<Resource<BatteryDetails>>

    fun reconnect()

    fun disconnect()

    fun startReceiving()

    fun closeConnection()

}