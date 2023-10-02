package com.example.greenampbluetoothcontroller.ui

import android.annotation.SuppressLint
import android.bluetooth.le.ScanSettings
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
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
import com.example.greenampbluetoothcontroller.util.testData
import com.example.greenampbluetoothcontroller.util.validateString
import com.example.greenampbluetoothcontroller.util.visibleAndSetText
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
class BatteryDetailsFragment(private val bleDevice: BLEDevice) : Fragment(R.layout.fragment_battery_details) {

    private lateinit var binding: FragmentBatteryDetailsBinding

    private var ble: BLE? = null
    private var connection: BluetoothConnection? = null

    private var isObserving: Boolean = false
    private val rxServiceUUID = "0000fff0-0000-1000-8000-00805f9b34fb"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBluetooth()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBatteryDetailsBinding.bind(view)

        initialization()

    }


    private fun initialization() {

        with(binding) {

            toolBar.imgBack.setOnClickListeners {
                navToPairDevice()
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
                it.observeString(
                    rxServiceUUID,
                    owner = this.viewLifecycleOwner,
                    interval = 5000L,
                    charset = Charsets.UTF_8
                ) { new ->

                    makeToast(new)

                    with(binding) {

                        tvVoltage.text = "${getVoltage(new, 0, 8)} mV"

                        tvTemperature.text = "${getFloatValue(new, 8, 16)}°C"

                        tvCurrent.text = "${getFloatValue(new, 16, 24)} mA"

                        tvSoc.text = "${getSOC(new, 92, 94)} %"

                        val cells = getCellCount(testData, 24, 28)

                        tvCellVoltage.text = "${cells.first} cells"

                        enableCell(testData, cells.second)
                    }
                }
            }

            val observeTxt =
                if (isObserving) "Observing..." else "Not Updated"

            updateStatus(false,observeTxt)

            this.isObserving = !isObserving

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
                    macAddress = bleDevice.macAddress,
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

    private fun enableCell(inputString: String, booleanArray: BooleanArray) {
        if (booleanArray.size > 16) return

        with(binding) {

            if (booleanArray[0]) tvCell1.visibleAndSetText(
                "Cell 1: ${
                    getCellVoltage(
                        inputString,
                        28,
                        32
                    )
                } mV"
            )
            if (booleanArray[1]) tvCell2.visibleAndSetText(
                "Cell 2: ${
                    getCellVoltage(
                        inputString,
                        32,
                        36
                    )
                } mV"
            )
            if (booleanArray[2]) tvCell3.visibleAndSetText(
                "Cell 3: ${
                    getCellVoltage(
                        inputString,
                        36,
                        40
                    )
                } mV"
            )
            if (booleanArray[3]) tvCell4.visibleAndSetText(
                "Cell 4: ${
                    getCellVoltage(
                        inputString,
                        40,
                        44
                    )
                } mV"
            )
            if (booleanArray[4]) tvCell5.visibleAndSetText(
                "Cell 5: ${
                    getCellVoltage(
                        inputString,
                        44,
                        48
                    )
                } mV"
            )
            if (booleanArray[5]) tvCell6.visibleAndSetText(
                "Cell 6: ${
                    getCellVoltage(
                        inputString,
                        48,
                        52
                    )
                } mV"
            )
            if (booleanArray[6]) tvCell7.visibleAndSetText(
                "Cell 7: ${
                    getCellVoltage(
                        inputString,
                        52,
                        56
                    )
                } mV"
            )
            if (booleanArray[7]) tvCell8.visibleAndSetText(
                "Cell 8: ${
                    getCellVoltage(
                        inputString,
                        56,
                        60
                    )
                } mV"
            )
            if (booleanArray[8]) tvCell9.visibleAndSetText(
                "Cell 9: ${
                    getCellVoltage(
                        inputString,
                        60,
                        64
                    )
                } mV"
            )
            if (booleanArray[9]) tvCell10.visibleAndSetText(
                "Cell 10 ${
                    getCellVoltage(
                        inputString,
                        64,
                        68
                    )
                } mV"
            )
            if (booleanArray[10]) tvCell11.visibleAndSetText(
                "Cell 11 ${
                    getCellVoltage(
                        inputString,
                        68,
                        72
                    )
                } mV"
            )
            if (booleanArray[11]) tvCell12.visibleAndSetText(
                "Cell 12 ${
                    getCellVoltage(
                        inputString,
                        72,
                        76
                    )
                } mV"
            )
            if (booleanArray[12]) tvCell13.visibleAndSetText(
                "Cell 13 ${
                    getCellVoltage(
                        inputString,
                        76,
                        80
                    )
                } mV"
            )
            if (booleanArray[13]) tvCell14.visibleAndSetText(
                "Cell 14 ${
                    getCellVoltage(
                        inputString,
                        80,
                        84
                    )
                } mV"
            )
            if (booleanArray[14]) tvCell15.visibleAndSetText(
                "Cell 15 ${
                    getCellVoltage(
                        inputString,
                        84,
                        88
                    )
                } mV"
            )
            if (booleanArray[15]) tvCell16.visibleAndSetText(
                "Cell 16 ${
                    getCellVoltage(
                        inputString,
                        88,
                        92
                    )
                } mV"
            )
        }
    }

    private fun getVoltage(inputString: String, start: Int, end: Int): String {

        return try {

            val targetString = inputString.validateString().substring(start, end)

            if (targetString.length != 8) {
                return "-"
            }

            val hexStrings = targetString.validateString().chunked(2).map { it.toInt(16) }

            val bytes = byteArrayOf(
                hexStrings[0].toByte(), hexStrings[1].toByte(), hexStrings[2].toByte(),
                hexStrings[3].toByte()
            )
            val int = ((bytes[3].toInt() and 0xFF) shl 24) or
                    ((bytes[2].toInt() and 0xFF) shl 16) or
                    ((bytes[1].toInt() and 0xFF) shl 8) or
                    (bytes[0].toInt() and 0xFF)

            Log.i("output", "getVoltage: $int")

            return int.toString()

        } catch (e: Exception) {
            "-"
        }
    }

    private fun getFloatValue(inputString: String, start: Int, end: Int): String {

        return try {

            val targetString = inputString.validateString().substring(start, end)

            if (targetString.length != 8) {
                return "-"
            }

            val hexStrings = targetString.validateString().chunked(2).map { it.toInt(16) }


            val bytes = byteArrayOf(
                hexStrings[0].toByte(), hexStrings[1].toByte(), hexStrings[2].toByte(),
                hexStrings[3].toByte()
            )

            val intBits = ((bytes[3].toInt() and 0xFF) shl 24) or
                    ((bytes[2].toInt() and 0xFF) shl 16) or
                    ((bytes[1].toInt() and 0xFF) shl 8) or
                    (bytes[0].toInt() and 0xFF)
            val float = Float.fromBits(intBits)

            println("The float value is: $float")

            return float.toString()

        } catch (e: Exception) {
            "-"
        }
    }

    private fun getSOC(inputString: String, start: Int, end: Int): String {

        return try {

            val targetString = inputString.validateString().substring(start, end)

            if (targetString.length != 2) {
                return "-"
            }

            val hexInt = targetString.toInt(16)

            val byte: Byte = hexInt.toByte()
            val int: Int = byte.toInt() and 0xFF
            println("The integer value is: $int")

            int.toString()

        } catch (e: Exception) {
            "-"
        }
    }

    private fun getCellCount(
        inputString: String,
        start: Int,
        end: Int
    ): Pair<String, BooleanArray> {

        return try {

            val targetString = inputString.validateString().substring(start, end)

            if (targetString.length != 4) {
                Pair("-", BooleanArray(16))
            }

            val hexStrings = targetString.validateString().chunked(2).map { it.toInt(16) }

            val bytes = byteArrayOf(hexStrings[0].toByte(), hexStrings[1].toByte())
            val boolArray = BooleanArray(16)
            for (i in bytes.indices) {
                for (j in 0 until 8) {
                    boolArray[i * 8 + j] = bytes[i].toInt() and (1 shl j) != 0
                }
            }
            println("The Boolean array is: ${boolArray.contentToString()}")

            val trueCount = boolArray.count { it }
            println("The number of true values is: $trueCount")

            Pair(trueCount.toString(), boolArray)

        } catch (e: Exception) {
            Pair("-", BooleanArray(16))
        }
    }

    private fun getCellVoltage(inputString: String, start: Int, end: Int): String {

        return try {

            val targetString = inputString.validateString().substring(start, end)

            if (targetString.length != 4) {
                return "-"
            }

            val hexStrings = targetString.validateString().chunked(2).map { it.toInt(16) }

            val bytes = byteArrayOf(
                hexStrings[0].toByte(), hexStrings[1].toByte()
            )

            val uint16 = ((bytes[1].toInt() and 0xFF) shl 8) or
                    (bytes[0].toInt() and 0xFF)
            println("The uint16_t value is: $uint16")

            return uint16.toString()

        } catch (e: Exception) {
            "-"
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
                updateStatus(false, "Connected!")

                onButtonObserveClick()
            }

            // Define the on disconnect handler
            onDisconnect = {
                updateStatus(false, "Disconnected!")
            }

            updateStatus(false, "Connected!")

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