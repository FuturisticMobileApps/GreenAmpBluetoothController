package com.example.greenampbluetoothcontroller.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.DialogInterface
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.greenampbluetoothcontroller.R
import com.example.greenampbluetoothcontroller.ble_library.BLE
import com.example.greenampbluetoothcontroller.ble_library.exceptions.PermissionsDeniedException
import com.example.greenampbluetoothcontroller.ble_library.models.BLEDevice
import com.example.greenampbluetoothcontroller.databinding.FragmentPairDeviceBinding
import com.example.greenampbluetoothcontroller.util.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("MissingPermission")
class FragmentPairDevice : BottomSheetDialogFragment(R.layout.fragment_pair_device) {

    private lateinit var binding: FragmentPairDeviceBinding

    private var ble: BLE? = null

    @Inject
    lateinit var bluetoothAdapter: BluetoothAdapter

    private val bleDeviceAdapter by lazy {
        PairDeviceAdapter {
            ble?.stopScan()
            navFromPairNewToBatteryDetails(it.macAddress, it.name)
            dismiss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setupBluetooth()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        binding = FragmentPairDeviceBinding.bind(view)

        binding.root.minimumHeight = Resources.getSystem().displayMetrics.heightPixels

        binding.recView.apply {
            adapter = bleDeviceAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.btnDismiss.setOnClickListeners {
            ble?.stopScan()
            dismiss()
        }

        lifecycleScope.launch { checkPermission() }

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

                ble?.stopScan()

                scanDevices()

            } catch (e: PermissionsDeniedException) {
                makeToast("Permissions were denied!")
            } catch (e: Exception) {
                e.printStackTrace()
                makeToast("Something went wrong, Try again later!")
            }

        }
    }

    private suspend fun scanDevices() {

        binding.pbScanning.visible()

        ble?.scanAsync(
            onUpdate = ::updateList,
            onFinish = {
                binding.pbScanning.gone()
            },
            onError = { code ->
                makeToast("Error code ${code}!")
            },
            duration = 10000,
        )

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

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        ble?.stopScan()
        ble = null
    }


    override fun onDestroy() {
        super.onDestroy()
        ble?.stopScan()
        ble = null
    }

}

