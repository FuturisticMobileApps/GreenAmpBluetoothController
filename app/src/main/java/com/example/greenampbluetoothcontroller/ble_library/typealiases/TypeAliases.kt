package com.example.greenampbluetoothcontroller.ble_library.typealiases

internal typealias EmptyCallback = () -> Unit

internal typealias Callback <T> = (T) -> Unit

internal typealias PermissionRequestCallback = (granted: Boolean) -> Unit
