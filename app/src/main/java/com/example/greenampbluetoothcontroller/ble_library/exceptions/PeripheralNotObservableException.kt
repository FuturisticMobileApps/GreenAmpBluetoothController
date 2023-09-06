package com.example.greenampbluetoothcontroller.ble_library.exceptions

class PeripheralNotObservableException(device: String, characteristic: String) : Exception("Device's ($device) characteristic ($characteristic) does not support property NOTIFY")