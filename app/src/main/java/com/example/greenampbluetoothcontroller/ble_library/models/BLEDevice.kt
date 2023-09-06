package com.example.greenampbluetoothcontroller.ble_library.models


import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice

@SuppressLint("MissingPermission")
class BLEDevice(var device: BluetoothDevice, var rsii: Int = 0, val advertisingId: Int = -1) {

	val name: String
	get () = device.name ?: ""

	val macAddress: String get () = device.address

	override fun toString(): String = "$name - $macAddress - ${rsii}dBm"

}