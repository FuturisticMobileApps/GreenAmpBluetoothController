package com.example.greenampbluetoothcontroller.ble_library.exceptions

class ScanFailureException(val code: Int) : Exception("Scan failed to execute!\nError code: $code")