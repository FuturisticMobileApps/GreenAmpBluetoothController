package com.example.greenampbluetoothcontroller.ui

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.greenampbluetoothcontroller.R
import com.example.greenampbluetoothcontroller.databinding.FragmentPermissionBinding
import com.example.greenampbluetoothcontroller.util.AppConstants.BluetoothConnect
import com.example.greenampbluetoothcontroller.util.AppConstants.BluetoothScan
import com.example.greenampbluetoothcontroller.util.AppConstants.coarseLocation
import com.example.greenampbluetoothcontroller.util.AppConstants.fineLocation
import com.example.greenampbluetoothcontroller.util.gone
import com.example.greenampbluetoothcontroller.util.hasPermission
import com.example.greenampbluetoothcontroller.util.navFromPermissionToPairNew
import com.example.greenampbluetoothcontroller.util.visible
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
@SuppressLint("MissingPermission")
class PermissionFragment : Fragment(R.layout.fragment_permission) {

    private lateinit var binding: FragmentPermissionBinding

    @Inject
    lateinit var bluetoothAdapter: BluetoothAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPermissionBinding.bind(view)
        checkPermission()
    }


    private fun checkPermission() {

        if (!bluetoothAdapter.isEnabled || !requireContext().hasPermission(coarseLocation) || !requireContext().hasPermission(
                fineLocation
            )) {

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                val bluetoothEnableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                requestBluetoothEnableLauncher.launch(bluetoothEnableIntent)
                return
            }

            if (requireContext().hasPermission(BluetoothConnect) && requireContext().hasPermission(
                    BluetoothScan
                ) && requireContext().hasPermission(coarseLocation) && requireContext().hasPermission(
                    fineLocation
                )
            ) {
                binding.tvPermission.gone()
                bluetoothAdapter.enable()
                navToPairNew()
            } else {

                requestBluetoothPermissionLauncher.launch(
                    arrayOf(
                        BluetoothConnect, BluetoothScan,
                        coarseLocation, fineLocation
                    )
                )
            }

        } else {

            binding.tvPermission.gone()
            bluetoothAdapter.enable()
            navToPairNew()

        }

        return
    }


    private fun handlePermissionDenied(message: String = "Bluetooth and GPS permissions have been permanently denied. Please enable them from the app settings") {

        val alert = android.app.AlertDialog.Builder(requireContext())

        alert.create()

        alert.setTitle("Permission Required")

        alert.setIcon(R.drawable.ic_bluetooth_search_24)

        alert.setMessage(message)

        alert.setPositiveButton("Go to Settings") { _, _ ->
            val settingsIntent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:${requireActivity().packageName}")
            )
            settingsBluetoothLauncher.launch(settingsIntent)
        }

        alert.setCancelable(false)

        alert.show()

    }

    private val requestBluetoothEnableLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            checkPermission()
        }

    private val settingsBluetoothLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            checkPermission()
        }

    private val requestBluetoothPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

            if (permissions.values.contains(false)) {
                handlePermissionDenied()
                binding.tvPermission.visible()
            } else {
                binding.tvPermission.gone()
                bluetoothAdapter.enable()
                checkPermission()
                return@registerForActivityResult
            }


        }


    private fun navToPairNew() {
        navFromPermissionToPairNew()
    }

}