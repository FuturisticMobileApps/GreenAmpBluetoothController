package com.example.greenampbluetoothcontroller.test

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.greenampbluetoothcontroller.R
import com.example.greenampbluetoothcontroller.ble_library.BLE
import com.example.greenampbluetoothcontroller.ble_library.BluetoothConnection
import com.example.greenampbluetoothcontroller.ble_library.exceptions.ScanTimeoutException
import com.example.greenampbluetoothcontroller.ble_library.models.BLEDevice
import com.example.greenampbluetoothcontroller.databinding.FragmentDetailsBinding
import com.example.greenampbluetoothcontroller.util.loadAdapter
import com.example.greenampbluetoothcontroller.util.setOnClickListeners
import kotlinx.coroutines.launch
import java.util.*

@SuppressLint("MissingPermission")
class DetailsFragment(private val bleDevice: BLEDevice) : Fragment(R.layout.fragment_details) {

    private lateinit var binding: FragmentDetailsBinding

    private var ble: BLE? = null

    private var connection: BluetoothConnection? = null

    private var observing = false

    private val characteristicsList = listOf(
        TX_CHAR_UUID,
        CCCD,
        "RX_SERVICE_UUID",
        RX_CHAR_UUID
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        binding = FragmentDetailsBinding.bind(view)

        binding.acsCharacteristics.loadAdapter(characteristicsList)

        binding.acsCharacteristics.setText(characteristicsList.first(), false)

        startBluetooth()

        binding.btnConnect.setOnClickListeners {

            observeData(binding.acsCharacteristics.text.toString())
        }

        binding.btnForceConnect.setOnClickListeners {
            forceConnect()
        }

    }


    private fun observeData(characteristics: String = TX_CHAR_UUID) {

        Log.i("characteristics", characteristics)

        lifecycleScope.launch {

            updateStatus("Connecting...")

            ble?.connect(bleDevice)?.let { connection ->

                connection.observe(characteristic = characteristics) { value: ByteArray ->
                    updateStatus("Connected...")
                    Log.i("byteArray", "$value")
                    binding.byteArray.text = value.toString()
                }

                connection.observeString(
                    characteristic = characteristics,
                    charset = Charsets.UTF_8
                ) { value: String ->
                    updateStatus("Connected...")
                    Log.i("string", value)
                    binding.byteArray.text = value
                }
            }
        }
    }

    private fun forceConnect() {

        lifecycleScope.launch {
            try {
                // Update variables
                updateStatus("Connecting...")

                // Tries to connect with the provided mac address
                ble?.scanFor(macAddress = bleDevice.macAddress, timeout = 20000)?.let {
                    onDeviceConnected(it)
                }
            } catch (e: ScanTimeoutException) {

                updateStatus("No device found!")

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun onDeviceConnected(bluetoothConnection: BluetoothConnection) {

        connection?.apply {

            this@DetailsFragment.connection = bluetoothConnection

            // Define the on re-connect handler
            onConnect = {
                // Update variables
                updateStatus("Connected!")

                observeData()
            }

            // Define the on disconnect handler
            onDisconnect = {
                // Update variables
                updateStatus("Disconnected!")
            }

            // Update variables
            updateStatus("Connected!")

            observeData()
        }
    }

    private fun observeData() {

        this.connection?.let {

            val deviceCharacteristic = binding.acsCharacteristics.text.toString()

            // If the desired characteristic is not available to be observed, pick the first available one
            // This is not really needed, it is just that this would be nice to have for when using generic BLE devices with this sample app
            var candidates = it.notifiableCharacteristics
            if (candidates.isEmpty()) candidates = it.readableCharacteristics
            if (candidates.contains(deviceCharacteristic)) candidates =
                arrayListOf(deviceCharacteristic, CCCD, "RX_SERVICE_UUID", RX_CHAR_UUID)
            val characteristic = candidates.first()


            if (observing) {
                it.stopObserving(characteristic)
            } else {

                it.observeString(
                    characteristic,
                    owner = this.viewLifecycleOwner,
                    interval = 5000L
                ) { new ->
                    binding.string.text = new
                }

            }

            observing = !observing

        }
    }

    private fun startBluetooth() {
        ble = BLE(fragment = this)
        ble?.verbose = true

    }

    private fun updateStatus(text: String) {
        binding.tvStatus.text = text
    }

    override fun onDestroyView() {

        lifecycleScope.launch {
            // Closes the connection with the device
            connection?.close()
            connection = null

            // Destroys the ble instance
            ble?.stopScan()
            ble = null
        }

        super.onDestroyView()
    }

    companion object {

        val TX_POWER_UUID = "00001804-0000-1000-8000-00805f9b34fb"
        val TX_POWER_LEVEL_UUID = "00002a07-0000-1000-8000-00805f9b34fb"
        val CCCD = "00002902-0000-1000-8000-00805f9b34fb"
        val FIRMWARE_REVISON_UUID = "00002a26-0000-1000-8000-00805f9b34fb"
        val DIS_UUID = "0000180a-0000-1000-8000-00805f9b34fb"


        //0000fff1 = Android --> BLE UUID Write
        val RX_CHAR_UUID = "0000fff1-0000-1000-8000-00805f9b34fb"

        //0000fff4 = BLE --> Android Notification
        val TX_CHAR_UUID = "0000fff4-0000-1000-8000-00805f9b34fb"
    }
}