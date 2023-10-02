package com.example.greenampbluetoothcontroller.util

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.greenampbluetoothcontroller.ui.BatteryPackListFragment
import com.example.greenampbluetoothcontroller.R
import com.example.greenampbluetoothcontroller.ble_library.models.BLEDevice
import com.example.greenampbluetoothcontroller.test.DetailsFragment
import com.example.greenampbluetoothcontroller.ui.FragmentPairNew
import com.example.greenampbluetoothcontroller.ui.BatteryDetailsFragment
import com.example.greenampbluetoothcontroller.ui.FragmentPinVerification

fun AppCompatActivity.navToTestFragment(){
    FragmentPairNew().beginFragmentTransaction(supportFragmentManager)
}

fun Fragment.navToPairDevice(){
    FragmentPairNew().beginFragmentTransaction(requireActivity().supportFragmentManager)
}


fun AppCompatActivity.navToPairDevice(){
    FragmentPairNew().beginFragmentTransaction(supportFragmentManager)
}
fun AppCompatActivity.navToBatteryDetails(bleDevice: BLEDevice) {
    BatteryDetailsFragment(bleDevice).beginFragmentTransaction(supportFragmentManager)
}


fun Fragment.navToBatteryDetails(bleDevice: BLEDevice) {
    BatteryDetailsFragment(bleDevice).beginFragmentTransaction(requireActivity().supportFragmentManager)
}


fun Fragment.navToDetailsScreen(bleDevice : BLEDevice){
    DetailsFragment(bleDevice).beginFragmentTransaction(requireActivity().supportFragmentManager)
}


fun AppCompatActivity.navToBatteryPackList(){
    BatteryPackListFragment().beginFragmentTransaction(supportFragmentManager)
}

fun Fragment.navToBatteryPackList(){
    BatteryPackListFragment().beginFragmentTransaction(requireActivity().supportFragmentManager)
}


fun Fragment.navToFragmentPinVerification(){
    FragmentPinVerification().beginFragmentTransaction(requireActivity().supportFragmentManager)
}

fun Fragment?.beginFragmentTransaction(fragmentManager: FragmentManager?, id: Int? = null) {

    try {

        if (this == null || fragmentManager == null) return

        val containerViewId = id ?: R.id.baseContainer

        if (containerViewId == 0) return

        fragmentManager.beginTransaction().replace(
            containerViewId, this, this.javaClass.simpleName
        ).commitAllowingStateLoss()

    } catch (e: Exception) {

        e.printStackTrace()

    }

}
