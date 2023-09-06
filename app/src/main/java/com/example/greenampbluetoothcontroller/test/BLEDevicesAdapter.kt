package com.example.greenampbluetoothcontroller.test

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.greenampbluetoothcontroller.ble_library.models.BLEDevice
import com.example.greenampbluetoothcontroller.databinding.ItemPairNewDevicesBinding
import com.example.greenampbluetoothcontroller.util.setOnClickListeners

@SuppressLint("MissingPermission")
class BLEDevicesAdapter(val onClicked: (bleDevice: BLEDevice) -> Unit) :
    ListAdapter<BLEDevice, BLEDevicesAdapter.BatteryPackViewHolder>(DiffCallback()) {

    inner class BatteryPackViewHolder(private val binding: ItemPairNewDevicesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(bleDevice: BLEDevice) {

            with(bleDevice) {

                with(binding) {

                    tvDeviceName.text = name

                    tvDeviceAddress.text = macAddress

                    root.setOnClickListeners {
                        onClicked(bleDevice)
                    }
                }

            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BatteryPackViewHolder {

        val binding =
            ItemPairNewDevicesBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return BatteryPackViewHolder(binding)

    }

    override fun onBindViewHolder(holder: BatteryPackViewHolder, position: Int) {

        val currentItem = getItem(position)

        holder.bind(currentItem)

    }

    class DiffCallback : DiffUtil.ItemCallback<BLEDevice>() {

        override fun areItemsTheSame(
            oldItem: BLEDevice,
            newItem: BLEDevice,
        ) = oldItem.macAddress == newItem.macAddress

        override fun areContentsTheSame(
            oldItem: BLEDevice,
            newItem: BLEDevice,
        ) = oldItem.macAddress == newItem.macAddress &&
                oldItem.device == newItem.device &&
                oldItem.rsii == newItem.rsii

    }

}