package com.example.greenampbluetoothcontroller.ui

import android.annotation.SuppressLint
import android.bluetooth.le.ScanSettings
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.greenampbluetoothcontroller.R
import com.example.greenampbluetoothcontroller.ble_library.BLE
import com.example.greenampbluetoothcontroller.ble_library.BluetoothConnection
import com.example.greenampbluetoothcontroller.ble_library.enums.Priority
import com.example.greenampbluetoothcontroller.ble_library.exceptions.ScanTimeoutException
import com.example.greenampbluetoothcontroller.dataStore.BLEDeviceLocal
import com.example.greenampbluetoothcontroller.dataStore.DevicePreferencesManager
import com.example.greenampbluetoothcontroller.databinding.FragmentBatteryDetailsNewBinding
import com.example.greenampbluetoothcontroller.util.batteryDetailsToConnectedDevice
import com.example.greenampbluetoothcontroller.util.gone
import com.example.greenampbluetoothcontroller.util.makeToast
import com.example.greenampbluetoothcontroller.util.setOnClickListeners
import com.example.greenampbluetoothcontroller.util.validateString
import com.example.greenampbluetoothcontroller.util.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
@SuppressLint("MissingPermission")
class BatteryDetailsFragment : Fragment(R.layout.fragment_battery_details_new) {

    private val args: BatteryDetailsFragmentArgs by navArgs()

    @Inject
    lateinit var devicePreferencesManager: DevicePreferencesManager

    private lateinit var binding: FragmentBatteryDetailsNewBinding

    private var loadingJob: Job? = null

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
        binding = FragmentBatteryDetailsNewBinding.bind(view)
        requestPermissions()

        binding.icBack.setOnClickListeners {
            batteryDetailsToConnectedDevice()
        }
        binding.icBackLoading.setOnClickListeners {
            batteryDetailsToConnectedDevice()
        }

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

            tvName.text = args.deviceName

            /* toolBar.imgBack.setOnClickListeners {
                 findNavController().popBackStack()
             }*/

            onButtonConnectClick()

            /* toolBar.cardConnect.setOnClickListeners {

                 onButtonObserveClick()

                  if (binding.tvConnectionStatus.text != "Not Updated" || binding.tvConnectionStatus.text != "Disconnected!"){
                      onButtonDisconnectClick()
                  } else{
                      onButtonConnectClick()
                 }
             }*/

        }
    }


    private fun setupBluetooth() {
        ble = BLE(this).apply {
            verbose = true
        }
    }

    private fun updateStatus(loading: Boolean, text: String) {
        with(binding) {
            if (loading) {
                llConnecting.visible()
                startLoadingAnimation()
                clConnected.gone()
            } else {
                llConnecting.gone()
                stopLoadingAnimation()
                clConnected.visible()
            }
            tvChargingStatus.text = text.validateString()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onButtonObserveClick() {

        var chargingStatus = "Idle  "

        saveDevice()

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

                        val voltage = DataFormatter.getVoltage(byteArray)

                        val current = DataFormatter.getCurrent(byteArray)

                        val temperature = DataFormatter.getTemperature(byteArray)

                        val battery = DataFormatter.getSOC(byteArray).toIntOrNull() ?: 0

                        when (battery) {

                            in 0..20 -> {
                                binding.root.setBackgroundResource(R.drawable.red_background)
                                binding.tvSOC.setGradientColors(Color.BLACK, Color.RED)
                            }

                            in 20..50 -> {
                                binding.root.setBackgroundResource(R.drawable.orange_background)
                                binding.tvSOC.setGradientColors(
                                    Color.BLACK,
                                    Color.parseColor("#FFFF00")
                                )
                            }

                            in 50..80 -> {
                                binding.root.setBackgroundResource(R.drawable.yellow_background)
                                binding.tvSOC.setGradientColors(
                                    Color.BLACK,
                                    Color.parseColor("#90EE90")
                                )
                            }

                            in 80..100 -> {
                                binding.root.setBackgroundResource(R.drawable.green_background)
                                binding.tvSOC.setGradientColors(Color.BLACK, Color.GREEN)
                            }
                        }

                        tvPackVoltage.text = "$voltage mV"

                        tvTemperature.text = "${temperature}°C"

                        tvCurrent.text = "$current mA"

                        tvSOC.text = "$battery%"

                        val cells = DataFormatter.getCellCount(byteArray)

                        tvSeriesPackName.text = "${cells.first} Series Pack"

                        enableAllCells(byteArray, cells.second)


                        val power = ((voltage.toDoubleOrNull() ?: 1.0) * (current.toDoubleOrNull()
                            ?: 1.0)) / 1000000

                        tvPackPower.text = "${String.format("%.2f", power)} W"

                        chargingStatus = with(current.toDoubleOrNull() ?: 0.0) {
                            if (this > 0)
                                "Discharging"
                            else if (this < 0)
                                "Charging"
                            else
                                "Idle  "

                        }

                    }
                }
            }

            this.isObserving = !isObserving

            val observeTxt =
                if (isObserving) "Updating..." else "Reconnect"

            lifecycleScope.launch {
                updateStatus(false, observeTxt)
                binding.tvChargingStatus.text = chargingStatus
            }

        }

    }

    private fun saveDevice() {

        lifecycleScope.launch {

            val bleDeviceLocal =
                BLEDeviceLocal(deviceName = args.deviceName, macAddress = args.macAddress)

            val deviceList = devicePreferencesManager.getDeviceList().toMutableList()

            if (deviceList.isEmpty()) {

                val addressList = listOf(bleDeviceLocal)

                devicePreferencesManager.updateDeviceList(addressList)

            } else {

                if (!deviceList.map { it.deviceName }.contains(bleDeviceLocal.deviceName)) {
                    deviceList.add(bleDeviceLocal)
                    devicePreferencesManager.updateDeviceList(deviceList)
                }

            }

        }
    }


    private fun enableAllCells(byteArray: ByteArray, booleanArray: BooleanArray) {

        val packsArray = arrayListOf<String>()

        if (booleanArray.size > 16) return

        if (booleanArray[0]) {
            val data = "${
                DataFormatter.getCellVoltage(
                    byteArray,
                    14,
                    15
                )
            } mV"
            packsArray.add(data)
        }
        if (booleanArray[1]) {

            val data = "${
                DataFormatter.getCellVoltage(
                    byteArray,
                    16,
                    17
                )
            } mV"

            packsArray.add(data)
        }
        if (booleanArray[2]) {

            val data = "${
                DataFormatter.getCellVoltage(
                    byteArray,
                    18,
                    19
                )
            } mV"

            packsArray.add(data)
        }
        if (booleanArray[3]) {

            val data = "${
                DataFormatter.getCellVoltage(
                    byteArray,
                    20,
                    21
                )
            } mV"

            packsArray.add(data)

        }
        if (booleanArray[4]) {
            val data = "${
                DataFormatter.getCellVoltage(
                    byteArray,
                    22,
                    23
                )
            } mV"

            packsArray.add(data)
        }
        if (booleanArray[5]) {
            val data = "${
                DataFormatter.getCellVoltage(
                    byteArray,
                    24,
                    25
                )
            } mV"

            packsArray.add(data)
        }
        if (booleanArray[6]) {
            val data = "${
                DataFormatter.getCellVoltage(
                    byteArray,
                    26,
                    27
                )
            } mV"

            packsArray.add(data)
        }
        if (booleanArray[7]) {
            val data = "${
                DataFormatter.getCellVoltage(
                    byteArray,
                    28,
                    29
                )
            } mV"

            packsArray.add(data)
        }
        if (booleanArray[8]) {
            val data = "${
                DataFormatter.getCellVoltage(
                    byteArray,
                    30,
                    31
                )
            } mV"

            packsArray.add(data)
        }
        if (booleanArray[9]) {
            val data = "${
                DataFormatter.getCellVoltage(
                    byteArray,
                    32,
                    33
                )
            } mV"

            packsArray.add(data)
        }
        if (booleanArray[10]) {
            val data = "${
                DataFormatter.getCellVoltage(
                    byteArray,
                    34,
                    35
                )
            } mV"

            packsArray.add(data)
        }
        if (booleanArray[11]) {
            val data = "${
                DataFormatter.getCellVoltage(
                    byteArray,
                    36,
                    37
                )
            } mV"

            packsArray.add(data)
        }
        if (booleanArray[12]) {
            val data = "${
                DataFormatter.getCellVoltage(
                    byteArray,
                    38,
                    39
                )
            } mV"

            packsArray.add(data)
        }
        if (booleanArray[13]) {
            val data = "${
                DataFormatter.getCellVoltage(
                    byteArray,
                    40,
                    41
                )
            } mV"

            packsArray.add(data)
        }
        if (booleanArray[14]) {
            val data = "${
                DataFormatter.getCellVoltage(
                    byteArray,
                    42,
                    43
                )
            } mV"

            packsArray.add(data)
        }
        if (booleanArray[15]) {
            val data = "${
                DataFormatter.getCellVoltage(
                    byteArray,
                    44,
                    45
                )
            } mV"

            packsArray.add(data)
        }

        enableTextFields(packsArray)
    }

    private fun enableTextFields(packsArray: List<String>) {


        with(binding) {

            packsArray.forEachIndexed { index, value ->

                when (index + 1) {

                    1 -> {
                        llPack1.visible()
                        tvPack1.text = packsArray[0]
                    }

                    2 -> {
                        llPack2.visible()
                        tvPack2.text = packsArray[1]
                    }

                    3 -> {
                        llPack3.visible()
                        tvPack3.text = packsArray[2]
                    }

                    4 -> {
                        llPack4.visible()
                        tvPack4.text = packsArray[3]
                    }

                    5 -> {
                        llPack5.visible()
                        tvPack5.text = packsArray[4]
                    }

                    6 -> {
                        llPack6.visible()
                        tvPack6.text = packsArray[5]
                    }

                    7 -> {
                        llPack7.visible()
                        tvPack7.text = packsArray[6]
                    }

                    8 -> {
                        llPack8.visible()
                        tvPack8.text = packsArray[7]
                    }

                    9 -> {
                        llPack9.visible()
                        tvPack9.text = packsArray[8]
                    }

                    10 -> {
                        llPack10.visible()
                        tvPack10.text = packsArray[9]
                    }

                    11 -> {
                        llPack11.visible()
                        tvPack11.text = packsArray[10]
                    }

                    12 -> {
                        llPack12.visible()
                        tvPack12.text = packsArray[11]
                    }

                    13 -> {
                        llPack13.visible()
                        tvPack13.text = packsArray[12]
                    }

                    14 -> {
                        llPack14.visible()
                        tvPack14.text = packsArray[13]
                    }

                    15 -> {
                        llPack15.visible()
                        tvPack15.text = packsArray[14]
                    }

                    16 -> {
                        llPack16.visible()
                        tvPack16.text = packsArray[15]
                    }
                }

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
                    withContext(Dispatchers.Main) {
                        onDeviceConnected(it)
                    }
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
                onButtonObserveClick()
            }

            // Define the on disconnect handler
            onDisconnect = {
                updateStatus(false, "Disconnected!")
            }

            updateStatus(false, "Connected")

            onButtonObserveClick()

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

    private fun startLoadingAnimation() {
        loadingJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                animateDot(binding.dot1)
                animateDot(binding.dot2)
                animateDot(binding.dot3)
                animateDot(binding.dot4)
                delay(500)
                delay(500)
            }
        }
    }


    private suspend fun animateDot(dot: TextView) {
        dot.alpha = 1.0f
        delay(250)
        dot.alpha = 0.5f
    }

    private fun stopLoadingAnimation() {
        loadingJob?.cancel()
    }
}