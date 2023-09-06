package com.example.greenampbluetoothcontroller.presentation

import androidx.lifecycle.ViewModel
import com.example.greenampbluetoothcontroller.data.classic.AndroidBluetoothController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BluetoothViewModel @Inject constructor(
    private val bluetoothController: AndroidBluetoothController
) : ViewModel() {


}