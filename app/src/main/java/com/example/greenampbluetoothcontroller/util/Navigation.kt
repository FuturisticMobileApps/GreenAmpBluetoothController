package com.example.greenampbluetoothcontroller.util

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.greenampbluetoothcontroller.ui.BatteryDetailsFragmentDirections
import com.example.greenampbluetoothcontroller.ui.FragmentConnectedDeviceDirections
import com.example.greenampbluetoothcontroller.ui.FragmentPairDevice
import com.example.greenampbluetoothcontroller.ui.PermissionFragmentDirections

/** from navGraph */

fun Fragment.navToConnectedDevice() {
    findNavController().navigate(PermissionFragmentDirections.actionPermissionFragmentToFragmentConnectedDevice())
}

fun Fragment.openPairDeviceDialog() {
    FragmentPairDevice().show(requireActivity().supportFragmentManager, "PairNewDialog")
}

fun Fragment.batteryDetailsToConnectedDevice() {
    findNavController().navigate(BatteryDetailsFragmentDirections.actionBatteryDetailsFragmentToFragmentConnectedDevice())
}

fun Fragment.navFromPairNewToBatteryDetails(macAddress: String, deviceName: String) {
    findNavController().navigate(
        FragmentConnectedDeviceDirections.actionFragmentConnectedDeviceToBatteryDetailsFragment(
            macAddress,
            deviceName
        )
    )
}