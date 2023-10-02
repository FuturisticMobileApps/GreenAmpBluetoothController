package com.example.greenampbluetoothcontroller.ui

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.greenampbluetoothcontroller.R
import com.example.greenampbluetoothcontroller.ble_library.models.BLEDevice
import com.example.greenampbluetoothcontroller.databinding.ActivityBaseContainerBinding
import com.example.greenampbluetoothcontroller.util.AppConstants.BluetoothConnect
import com.example.greenampbluetoothcontroller.util.AppConstants.BluetoothScan
import com.example.greenampbluetoothcontroller.util.hasPermission
import com.example.greenampbluetoothcontroller.util.navToBatteryDetails
import com.example.greenampbluetoothcontroller.util.navToPairDevice
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
@SuppressLint("MissingPermission")
class BaseContainer : AppCompatActivity() {

    private lateinit var binding: ActivityBaseContainerBinding

    @Inject
    lateinit var bluetoothAdapter: BluetoothAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBaseContainerBinding.inflate(layoutInflater)

        setContentView(binding.root)

    }

    override fun onStart() {
        super.onStart()
        checkBluetoothEnabled()
    }

    private fun checkBluetoothEnabled() {

        if (!bluetoothAdapter.isEnabled) {

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                val bluetoothEnableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                requestBluetoothEnableLauncher.launch(bluetoothEnableIntent)
                return
            }

            if (hasPermission(BluetoothConnect) && hasPermission(BluetoothScan)) {
                bluetoothAdapter.enable()
                navToPairDevice()
                return
            } else {

                requestBluetoothPermissionLauncher.launch(arrayOf(BluetoothConnect, BluetoothScan))
            }

        } else {

            navToPairDevice()

        }
    }


    private fun handlePermissionDenied() {

        val alert = android.app.AlertDialog.Builder(this)

        alert.create()

        alert.setTitle("Permission Required")

        alert.setIcon(R.drawable.ic_bluetooth_search_24)

        alert.setMessage("Bluetooth permission is necessary to access this app")

        alert.setPositiveButton("Go to Settings") { _, _ ->
            val settingsIntent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:$packageName")
            )
            settingsBluetoothLauncher.launch(settingsIntent)
        }

        alert.setCancelable(false)

        alert.show()

    }

    private val requestBluetoothEnableLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            checkBluetoothEnabled()
        }

    private val settingsBluetoothLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            checkBluetoothEnabled()
        }

    private val requestBluetoothPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

            if (permissions.values.contains(true)) {
                bluetoothAdapter.enable()
                checkBluetoothEnabled()
                return@registerForActivityResult
            }

            handlePermissionDenied()

        }
}