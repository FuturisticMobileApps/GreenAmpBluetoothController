package com.example.greenampbluetoothcontroller.ble_library.models

import android.bluetooth.BluetoothGattService

data class BluetoothService(
	private val service: BluetoothGattService,
) {
	val isPrimary get() = this.service.type == BluetoothGattService.SERVICE_TYPE_PRIMARY
	val isSecondary get() = this.service.type == BluetoothGattService.SERVICE_TYPE_SECONDARY
	val characteristics get() = this.service.characteristics.map { BluetoothCharacteristic(it) }
}