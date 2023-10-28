package com.example.greenampbluetoothcontroller.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.greenampbluetoothcontroller.ble_library.models.BLEDevice
import com.example.greenampbluetoothcontroller.databinding.ItemPairDeviceBinding
import com.example.greenampbluetoothcontroller.util.setOnClickListeners

@SuppressLint("MissingPermission")
class PairDeviceAdapter(val onClicked: (bleDevice: BLEDevice) -> Unit) :
    ListAdapter<BLEDevice, PairDeviceAdapter.BatteryPackViewHolder>(DiffCallback()) {

    inner class BatteryPackViewHolder(private val binding: ItemPairDeviceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(bleDevice: BLEDevice) {

            with(bleDevice) {

                with(binding) {

                    tvDeviceName.text = name

                    root.setOnClickListeners {
                        onClicked(bleDevice)
                    }
                }

            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BatteryPackViewHolder {

        val binding =
            ItemPairDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)

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
                oldItem.device == newItem.device

    }

}