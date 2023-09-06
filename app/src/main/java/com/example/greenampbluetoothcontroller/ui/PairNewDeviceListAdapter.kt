package com.example.greenampbluetoothcontroller.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.greenampbluetoothcontroller.data.core.BluetoothDevices
import com.example.greenampbluetoothcontroller.databinding.ItemPairNewDevicesBinding

class PairNewDeviceListAdapter(val clickedDevice: (device: BluetoothDevices) -> Unit) :
    ListAdapter<BluetoothDevices, PairNewDeviceListAdapter.PairedDeviceListViewHolder>(DiffCallback()) {

    private lateinit var context: Context


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PairedDeviceListViewHolder {

        context = parent.context

        val binding =
            ItemPairNewDevicesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PairedDeviceListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PairedDeviceListViewHolder, position: Int) {

        val currentItem = getItem(position)

        holder.bind(currentItem)
    }


    inner class PairedDeviceListViewHolder(private val binding: ItemPairNewDevicesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(bluetoothDevice: BluetoothDevices) {

            binding.tvDeviceName.text = bluetoothDevice.name

            binding.tvDeviceAddress.text = bluetoothDevice.address

            binding.root.setOnClickListener {
                clickedDevice(bluetoothDevice)
            }
        }
    }


    class DiffCallback : DiffUtil.ItemCallback<BluetoothDevices>() {

        override fun areItemsTheSame(
            oldItem: BluetoothDevices,
            newItem: BluetoothDevices,
        ) = oldItem.name == newItem.name

        override fun areContentsTheSame(
            oldItem: BluetoothDevices,
            newItem: BluetoothDevices,
        ) = oldItem == newItem

    }


}

