package com.example.greenampbluetoothcontroller.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.greenampbluetoothcontroller.R
import com.example.greenampbluetoothcontroller.dataStore.DevicePreferencesManager
import com.example.greenampbluetoothcontroller.databinding.FragmentConnectedDeviceBinding
import com.example.greenampbluetoothcontroller.util.navFromPairNewToBatteryDetails
import com.example.greenampbluetoothcontroller.util.openPairDeviceDialog
import com.example.greenampbluetoothcontroller.util.setOnClickListeners
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
@SuppressLint("MissingPermission")
class FragmentConnectedDevice : Fragment(R.layout.fragment_connected_device) {

    private lateinit var binding: FragmentConnectedDeviceBinding

    @Inject
    lateinit var devicePreferencesManager: DevicePreferencesManager

    private val bleConnectDeviceAdapter by lazy {
        ConnectedDeviceAdapter {
            navFromPairNewToBatteryDetails(it.macAddress, it.deviceName)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentConnectedDeviceBinding.bind(view)

        binding.recConnectedDevice.apply {
            adapter = bleConnectDeviceAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.imgAdd.setOnClickListeners {
            openPairDeviceDialog()
        }

        lifecycleScope.launch {
            prefillList()
        }

    }

    private suspend fun prefillList() {

        val deviceList = devicePreferencesManager.getDeviceList()

        if (deviceList.isEmpty())
//            openPairDeviceDialog()
        else
            bleConnectDeviceAdapter.submitList(deviceList)


    }


}