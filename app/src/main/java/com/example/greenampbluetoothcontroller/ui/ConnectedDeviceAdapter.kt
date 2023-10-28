package com.example.greenampbluetoothcontroller.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.greenampbluetoothcontroller.ble_library.models.BLEDevice
import com.example.greenampbluetoothcontroller.dataStore.BLEDeviceLocal
import com.example.greenampbluetoothcontroller.databinding.ItemConnectedDeviceBinding
import com.example.greenampbluetoothcontroller.util.setOnClickListeners

@SuppressLint("MissingPermission")
class ConnectedDeviceAdapter(val onClicked: (bleDevice: BLEDeviceLocal) -> Unit) :
    ListAdapter<BLEDeviceLocal, ConnectedDeviceAdapter.BatteryPackViewHolder>(DiffCallback()) {

    inner class BatteryPackViewHolder(private val binding: ItemConnectedDeviceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(bleDevice: BLEDeviceLocal) {

            with(bleDevice) {

                with(binding) {

                    tvDeviceName.text = deviceName

                    root.setOnClickListeners {
                        onClicked(bleDevice)
                    }
                }

            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BatteryPackViewHolder {

        val binding =
            ItemConnectedDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return BatteryPackViewHolder(binding)

    }

    override fun onBindViewHolder(holder: BatteryPackViewHolder, position: Int) {

        val currentItem = getItem(position)

        holder.bind(currentItem)

    }

    class DiffCallback : DiffUtil.ItemCallback<BLEDeviceLocal>() {

        override fun areItemsTheSame(
            oldItem: BLEDeviceLocal,
            newItem: BLEDeviceLocal,
        ) = oldItem.macAddress == newItem.macAddress

        override fun areContentsTheSame(
            oldItem: BLEDeviceLocal,
            newItem: BLEDeviceLocal,
        ) = oldItem.macAddress == newItem.macAddress &&
                oldItem.deviceName == newItem.deviceName

    }

}