package com.example.greenampbluetoothcontroller.ui

import android.annotation.SuppressLint
import android.bluetooth.le.ScanSettings
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.greenampbluetoothcontroller.R
import com.example.greenampbluetoothcontroller.ble_library.BLE
import com.example.greenampbluetoothcontroller.ble_library.BluetoothConnection
import com.example.greenampbluetoothcontroller.ble_library.enums.Priority
import com.example.greenampbluetoothcontroller.ble_library.exceptions.ScanTimeoutException
import com.example.greenampbluetoothcontroller.ble_library.models.BLEDevice
import com.example.greenampbluetoothcontroller.databinding.FragmentBatteryDetailsBinding
import com.example.greenampbluetoothcontroller.util.makeToast
import com.example.greenampbluetoothcontroller.util.navToPairDevice
import com.example.greenampbluetoothcontroller.util.setOnClickListeners
import com.example.greenampbluetoothcontroller.util.visibleAndSetText
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
class BatteryDetailsFragment : Fragment(R.layout.fragment_battery_details) {

    private val args : BatteryDetailsFragmentArgs by navArgs()

    private lateinit var binding: FragmentBatteryDetailsBinding

    private var ble: BLE? = null
    private var connection: BluetoothConnection? = null

    private var isObserving: Boolean = false
    private val rxServiceUUID = "0000fff4-0000-1000-8000-00805f9b34fb"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBluetooth()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBatteryDetailsBinding.bind(view)
        requestPermissions()
    }


    private fun requestPermissions() = lifecycleScope.launch {
        Log.d("MainActivity", "Setting bluetooth manager up...")

        val permissionsGranted = ble?.verifyPermissions(rationaleRequestCallback = { next ->
            makeToast("We need the bluetooth permissions!")
            next()
        })
        if (permissionsGranted == false) {
            // Shows UI feedback if the permissions were denied
            makeToast("Permissions denied!")
            return@launch
        }

        if (ble?.verifyBluetoothAdapterState() == false) {
            makeToast("Bluetooth adapter off!")
            return@launch
        }

        if (ble?.verifyLocationState() == false) {
            makeToast("Location services off!")
            return@launch
        }

        initialization()

    }


    private fun initialization() {

        with(binding) {

            toolBar.imgBack.setOnClickListeners {
                findNavController().popBackStack()
            }

            onButtonConnectClick()

            toolBar.cardConnect.setOnClickListeners {


                onButtonObserveClick()

                /* if (binding.tvConnectionStatus.text != "Not Updated" || binding.tvConnectionStatus.text != "Disconnected!"){
                     onButtonDisconnectClick()
                 } else{
                     onButtonConnectClick()
                 }
 */
            }
        }
    }


    private fun setupBluetooth() {
        ble = BLE(this).apply {
            verbose = true
        }
    }

    private fun updateStatus(loading: Boolean, text: String) {
        with(binding) {
            binding.progressBar.root.isVisible = loading
            tvConnectionStatus.text = text
//            toolBar.imgConnect.setImageResource(
//                if (text == "Disconnected!") R.drawable.ic_bluetooth_search_24 else R.drawable.baseline_bluetooth_disabled_24
//            )
        }
    }

    private fun onButtonObserveClick() {

        this.connection?.let {

            // Observe
            if (isObserving) {
                it.stopObserving(rxServiceUUID)
            } else {

                it.observe(
                    rxServiceUUID,
                    owner = this.viewLifecycleOwner,
                    interval = 5000L,
                ) { byteArray ->


                    with(binding) {


                        tvVoltage.text = "${DataFormatter.getVoltage(byteArray)} mV"

                        tvTemperature.text = "${DataFormatter.getTemperature(byteArray)}°C"

                        tvCurrent.text = "${DataFormatter.getCurrent(byteArray)} mA"

                        tvSoc.text = "${DataFormatter.getSOC(byteArray)} %"

                        val cells = DataFormatter.getCellCount(byteArray)

                        tvCellVoltage.text = "${cells.first}S Voltages"

                        enableAllCells(byteArray, cells.second)
                    }
                }
            }

            this.isObserving = !isObserving

            val observeTxt =
                if (isObserving) "Updating..." else "Not Updated"

            updateStatus(false, observeTxt)

        }

    }


    private fun enableAllCells(byteArray: ByteArray, booleanArray: BooleanArray) {

        if (booleanArray.size > 16) return

        with(binding) {

            var cellCount = 0

            if (booleanArray[0]) {
                cellCount++
                tvCell1.visibleAndSetText(
                    "Cell $cellCount : ${
                        DataFormatter.getCellVoltage(
                            byteArray,
                            14,
                            15
                        )
                    } mV"
                )
            }
            if (booleanArray[1]) {
                cellCount++
                tvCell2.visibleAndSetText(
                    "Cell $cellCount : ${
                        DataFormatter.getCellVoltage(
                            byteArray,
                            16,
                            17
                        )
                    } mV"
                )
            }
            if (booleanArray[2]) {
                cellCount++
                tvCell3.visibleAndSetText(
                    "Cell $cellCount : ${
                        DataFormatter.getCellVoltage(
                            byteArray,
                            18,
                            19
                        )
                    } mV"
                )
            }
            if (booleanArray[3]) {
                cellCount++
                tvCell4.visibleAndSetText(
                    "Cell $cellCount : ${
                        DataFormatter.getCellVoltage(
                            byteArray,
                            20,
                            21
                        )
                    } mV"
                )
            }
            if (booleanArray[4]) {
                cellCount++
                tvCell5.visibleAndSetText(
                    "Cell $cellCount : ${
                        DataFormatter.getCellVoltage(
                            byteArray,
                            22,
                            23
                        )
                    } mV"
                )
            }
            if (booleanArray[5]) {
                cellCount++
                tvCell6.visibleAndSetText(
                    "Cell $cellCount : ${
                        DataFormatter.getCellVoltage(
                            byteArray,
                            24,
                            25
                        )
                    } mV"
                )
            }
            if (booleanArray[6]) {
                cellCount++
                tvCell7.visibleAndSetText(
                    "Cell $cellCount : ${
                        DataFormatter.getCellVoltage(
                            byteArray,
                            26,
                            27
                        )
                    } mV"
                )
            }
            if (booleanArray[7]) {
                cellCount++
                tvCell8.visibleAndSetText(
                    "Cell $cellCount : ${
                        DataFormatter.getCellVoltage(
                            byteArray,
                            28,
                            29
                        )
                    } mV"
                )
            }
            if (booleanArray[8]) {
                cellCount++
                tvCell9.visibleAndSetText(
                    "Cell $cellCount : ${
                        DataFormatter.getCellVoltage(
                            byteArray,
                            30,
                            31
                        )
                    } mV"
                )
            }
            if (booleanArray[9]) {
                cellCount++
                tvCell10.visibleAndSetText(
                    "Cell $cellCount : ${
                        DataFormatter.getCellVoltage(
                            byteArray,
                            32,
                            33
                        )
                    } mV"
                )
            }
            if (booleanArray[10]) {
                cellCount++
                tvCell11.visibleAndSetText(
                    "Cell $cellCount : ${
                        DataFormatter.getCellVoltage(
                            byteArray,
                            34,
                            35
                        )
                    } mV"
                )
            }
            if (booleanArray[11]) {
                cellCount++
                tvCell12.visibleAndSetText(
                    "Cell $cellCount : ${
                        DataFormatter.getCellVoltage(
                            byteArray,
                            36,
                            37
                        )
                    } mV"
                )
            }
            if (booleanArray[12]) {
                cellCount++
                tvCell13.visibleAndSetText(
                    "Cell $cellCount : ${
                        DataFormatter.getCellVoltage(
                            byteArray,
                            38,
                            39
                        )
                    } mV"
                )
            }
            if (booleanArray[13]) {
                cellCount++
                tvCell14.visibleAndSetText(
                    "Cell $cellCount : ${
                        DataFormatter.getCellVoltage(
                            byteArray,
                            40,
                            41
                        )
                    } mV"
                )
            }
            if (booleanArray[14]) {
                cellCount++
                tvCell15.visibleAndSetText(
                    "Cell $cellCount : ${
                        DataFormatter.getCellVoltage(
                            byteArray,
                            42,
                            43
                        )
                    } mV"
                )
            }
            if (booleanArray[15]) {
                cellCount++
                tvCell16.visibleAndSetText(
                    "Cell $cellCount : ${
                        DataFormatter.getCellVoltage(
                            byteArray,
                            44,
                            45
                        )
                    } mV"
                )
            }
        }
    }


    private fun onButtonConnectClick() {
        lifecycleScope.launch {
            try {
                // Update variables
                updateStatus(true, "Connecting...")
                //
                val scanSettings = ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .setReportDelay(0)
                    .build()
                val priority = Priority.High
                val timeout = 10000L // 10 seconds
                val servicecharacteristics = rxServiceUUID

                ble?.scanFor(
                    macAddress = args.macAddress,
                    timeout = timeout,
                    priority = priority,
                    settings = scanSettings
                )?.let {
                    onDeviceConnected(it)
                }


            } catch (e: ScanTimeoutException) {
                // Update variables
                updateStatus(false, "No device found!")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun onDeviceConnected(connection: BluetoothConnection) {
        connection.apply {
            this@BatteryDetailsFragment.connection = connection
            // For larger messages, you can use this method to request a larger MTU
            // val success = connection?.requestMTU(512)
            // Define the on re-connect handler
            onConnect = {
                // Update variables
                updateStatus(false, "Connected")
//                onButtonObserveClick()
            }

            // Define the on disconnect handler
            onDisconnect = {
                updateStatus(false, "Disconnected!")
            }

            updateStatus(false, "Connected")

        }
    }

    private fun onButtonDisconnectClick() {
        lifecycleScope.launch {
            // Update variables
            updateStatus(true, "Disconnecting...")

            // Closes the connection
            connection?.close()
            connection = null

            // Update variables
            updateStatus(false, "Disconnected!")
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        lifecycleScope.launch {
            // Closes the connection with the device
            connection?.close()
            connection = null
            // Destroys the ble instance
            ble?.stopScan()
            ble = null
        }
    }
}