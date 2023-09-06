package com.example.greenampbluetoothcontroller.test

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.greenampbluetoothcontroller.R
import com.example.greenampbluetoothcontroller.ble_library.BLE
import com.example.greenampbluetoothcontroller.ble_library.exceptions.PermissionsDeniedException
import com.example.greenampbluetoothcontroller.ble_library.models.BLEDevice
import com.example.greenampbluetoothcontroller.databinding.FragmentPairNewBinding
import com.example.greenampbluetoothcontroller.util.*
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
class TestFragment : Fragment(R.layout.fragment_pair_new) {

    private lateinit var binding: FragmentPairNewBinding

    private var ble: BLE? = null

    private val bleDeviceAdapter by lazy {
        BLEDevicesAdapter {
            ble?.stopScan()
            navToDetailsScreen(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        binding = FragmentPairNewBinding.bind(view)

        startBluetooth()

        checkPermission()

        binding.recView.adapter = bleDeviceAdapter

        binding.toolBar.cardRefresh.setOnClickListeners {
            ble?.stopScan()
            scanDevices()
        }

    }


    private fun checkPermission() {

        lifecycleScope.launch {

            try {

                val isPermissionsGranted =
                    ble?.verifyPermissions(rationaleRequestCallback = { next ->
                        makeToast("We need the bluetooth permissions!")
                        next()
                    }) ?: false

                if (!isPermissionsGranted) {
                    makeToast("Permissions denied!")
                    return@launch
                }

                val isBluetoothActive = ble?.verifyBluetoothAdapterState() ?: false

                if (!isBluetoothActive) {
                    makeToast("Bluetooth adapter off!")
                    return@launch
                }


                val isLocationActive = ble?.verifyLocationState() ?: false

                if (!isLocationActive) {
                    makeToast("Location services off!")
                    return@launch
                }

                scanDevices()

            } catch (e: PermissionsDeniedException) {
                makeToast("Permissions were denied!")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun scanDevices() {

        binding.progressBar.mainProgressLayout.visible()

        lifecycleScope.launch {

            ble?.scanAsync(
                onUpdate = ::updateList,
                onFinish = {
                    binding.progressBar.mainProgressLayout.gone()
                },
                onError = { code ->
                    makeToast("Error code ${code}!")
                },
                duration = 10000
            )
        }

    }

    private fun startBluetooth() {
        ble = BLE(fragment = this)
        ble?.verbose = true
    }

    private fun updateList(list: List<BLEDevice>) {

        val filteredList = list.filter { it.device.name.validateString().isNotEmpty() }

        bleDeviceAdapter.submitList(filteredList)

    }

    override fun onDestroyView() {
        ble?.stopScan()
        ble = null
        super.onDestroyView()
    }
}