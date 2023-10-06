package com.example.greenampbluetoothcontroller.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.greenampbluetoothcontroller.R
import com.example.greenampbluetoothcontroller.ble_library.BLE
import com.example.greenampbluetoothcontroller.ble_library.exceptions.PermissionsDeniedException
import com.example.greenampbluetoothcontroller.ble_library.models.BLEDevice
import com.example.greenampbluetoothcontroller.databinding.FragmentPairNewBinding
import com.example.greenampbluetoothcontroller.test.BLEDevicesAdapter
import com.example.greenampbluetoothcontroller.util.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("MissingPermission")
class FragmentPairNew : Fragment(R.layout.fragment_pair_new) {

    private lateinit var binding: FragmentPairNewBinding

    private var ble: BLE? = null

    private var animationJob: Job? = null

    @Inject
    lateinit var bluetoothAdapter: BluetoothAdapter

    private val bleDeviceAdapter by lazy {
        BLEDevicesAdapter {
            ble?.stopScan()
//            navToBatteryDetails(it)
            navFromPairNewToBatteryDetails(it.macAddress,it.name)
        }
    }

    private fun startAnimation() {
        animationJob = lifecycleScope.launch {
            while (true) {
                val dots = ".".repeat((binding.toolBar.tvScanTitle.text.length % 4) + 1)
                binding.toolBar.tvScanTitle.text = "Scanning$dots"
                delay(500) // Delay between dot changes (500ms)
            }
        }
    }

    private fun stopScanningAnimation() {
        animationJob?.cancel()
        animationJob = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.setupBluetooth()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        binding = FragmentPairNewBinding.bind(view)

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
                makeToast("Something went wrong, Try again later!")
            }
        }
    }

    private fun scanDevices() {

        binding.toolBar.tvScanTitle.text = "Scanning..."
        startAnimation()

        lifecycleScope.launch {

            ble?.scanAsync(
                onUpdate = ::updateList,
                onFinish = {
                    stopScanningAnimation()
                    binding.toolBar.tvScanTitle.text = "Founded devices"
                },
                onError = { code ->
                    makeToast("Error code ${code}!")
                },
                duration = 10000
            )
        }

    }

    private fun setupBluetooth() {
        try {
            this.ble = BLE(this).apply {
                verbose = true
            }
        } catch (e: Exception) {
            e.printStackTrace()

            AlertDialog.Builder(this.context)
                .setTitle("Error!")
                .setMessage(e.message)
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    private fun updateList(list: List<BLEDevice>) {

        val filteredList = list.filter { it.device.name.validateString().isNotEmpty() }

        bleDeviceAdapter.submitList(filteredList)

    }

    override fun onDestroy() {
        super.onDestroy()
        ble?.stopScan()
        ble = null
    }

}