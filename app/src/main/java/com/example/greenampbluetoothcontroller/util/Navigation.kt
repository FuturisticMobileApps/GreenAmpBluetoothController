package com.example.greenampbluetoothcontroller.util

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import com.example.greenampbluetoothcontroller.R
import com.example.greenampbluetoothcontroller.ble_library.models.BLEDevice
import com.example.greenampbluetoothcontroller.ui.BatteryDetailsFragment
import com.example.greenampbluetoothcontroller.ui.FragmentPairNew
import com.example.greenampbluetoothcontroller.ui.FragmentPairNewDirections
import com.example.greenampbluetoothcontroller.ui.PermissionFragmentDirections
import com.example.greenampbluetoothcontroller.ui.TestFragment

/** from navGraph */

fun Fragment.navFromPermissionToPairNew(){
    findNavController().navigate(PermissionFragmentDirections.actionPermissionFragmentToFragmentPairNew())
}

fun Fragment.navFromPairNewToBatteryDetails(macAddress : String,deviceName : String){
    findNavController().navigate(FragmentPairNewDirections.actionFragmentPairNewToBatteryDetailsFragment(macAddress,deviceName))
}

fun AppCompatActivity.navToTestFragment() {
    FragmentPairNew().beginFragmentTransaction(supportFragmentManager)
}

fun Fragment.navToPairDevice() {
    FragmentPairNew().beginFragmentTransaction(requireActivity().supportFragmentManager)
}


fun AppCompatActivity.navToPairDevice() {
    FragmentPairNew().beginFragmentTransaction(supportFragmentManager)
}


fun Fragment?.beginFragmentTransaction(fragmentManager: FragmentManager?, id: Int? = null) {

    try {

        if (this == null || fragmentManager == null || id == null ) return

//        val containerViewId = id ?: R.id.baseContainer
//
//        if (containerViewId == 0) return
//
//        fragmentManager.beginTransaction().replace(
//            containerViewId, this, this.javaClass.simpleName
//        ).addToBackStack(null).commit()

    } catch (e: Exception) {

        e.printStackTrace()

    }

}
