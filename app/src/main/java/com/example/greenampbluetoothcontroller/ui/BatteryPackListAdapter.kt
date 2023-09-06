package com.example.greenampbluetoothcontroller.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.greenampbluetoothcontroller.data.core.BatteryPack
import com.example.greenampbluetoothcontroller.databinding.ItemBatteryPackBinding
import com.example.greenampbluetoothcontroller.util.setOnClickListeners

class BatteryPackListAdapter(val onClicked: (batteryPack: BatteryPack) -> Unit) :
    ListAdapter<BatteryPack, BatteryPackListAdapter.BatteryPackViewHolder>(DiffCallback()) {

    inner class BatteryPackViewHolder(private val binding: ItemBatteryPackBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(batteryPack: BatteryPack) {

            with(batteryPack) {

                with(binding) {

                    tvPackName.text = packName

                    root.setOnClickListeners {
                        onClicked(batteryPack)
                    }
                }

            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BatteryPackViewHolder {

        val binding =
            ItemBatteryPackBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return BatteryPackViewHolder(binding)

    }

    override fun onBindViewHolder(holder: BatteryPackViewHolder, position: Int) {

        val currentItem = getItem(position)

        holder.bind(currentItem)

    }

    class DiffCallback : DiffUtil.ItemCallback<BatteryPack>() {

        override fun areItemsTheSame(
            oldItem: BatteryPack,
            newItem: BatteryPack,
        ) = oldItem.packName == newItem.packName

        override fun areContentsTheSame(
            oldItem: BatteryPack,
            newItem: BatteryPack,
        ) = oldItem == newItem

    }

}